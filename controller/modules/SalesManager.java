package controller.modules;

import gamedata.bid.ObjectItemToSellInBid;
import gamedata.bid.SellerBuyerDescriptor;
import gamedata.d2o.modules.Item;
import gamedata.d2o.modules.ItemType;
import gamedata.inventory.ObjectItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import main.DatabaseConnection;
import main.Log;
import messages.exchanges.ExchangeBidHousePriceMessage;
import messages.exchanges.ExchangeBidPriceForSellerMessage;
import messages.exchanges.ExchangeObjectModifyPricedMessage;
import messages.exchanges.ExchangeObjectMoveMessage;
import messages.exchanges.ExchangeObjectMovePricedMessage;
import messages.exchanges.ExchangeSellMessage;
import controller.CharacterState;
import controller.characters.Character;

public class SalesManager {
	private static HashMap<Integer, HashMap<Integer, Integer>> averagePrices = new HashMap<Integer, HashMap<Integer,Integer>>();
	
	private Character character;
	private Date lastSellingRoutine;
	private Map<Integer, ObjectItemToSellInBid> objectsInBid;
	private SellerBuyerDescriptor infos;
	private ExchangeBidPriceForSellerMessage objectToSellAskedInfos;
	private int lastObjectUIDAdded;
	
	public static void setAveragePrices(int serverId, Vector<Integer> ids, Vector<Integer> avgPrices) {
		int idsCount = ids.size();
		HashMap<Integer, Integer> prices = new HashMap<Integer, Integer>(idsCount);
		for(int i = 0; i < idsCount; ++i)
			prices.put(ids.get(i), avgPrices.get(i));
		averagePrices.put(serverId, prices);
	}
	
	public static int getAveragePrice(int serverId, int objectGID) {
		Integer price = averagePrices.get(serverId).get(objectGID);
		if(price == null)
			return 0;
		return price;
	}
	
	public static boolean averagePricesAreSet(int serverId) {
		return averagePrices.get(serverId) != null;
	}
	
	public SalesManager(Character character) {
		this.character = character;
	}
	
	public void setObjectsInBid(Vector<ObjectItemToSellInBid> objectsInBid) {
		this.objectsInBid = new HashMap<Integer, ObjectItemToSellInBid>(objectsInBid.size());
		for(ObjectItemToSellInBid object : objectsInBid)
			this.objectsInBid.put(object.objectUID, object);
	}
	
	public void setInfos(SellerBuyerDescriptor infos) {
		this.infos = infos;
	}
	
	public void setAskedInfos(ExchangeBidPriceForSellerMessage infos) {
		this.objectToSellAskedInfos = infos;
	}
	
	public void objectToSellAdded(ObjectItemToSellInBid object) {
		this.objectsInBid.put(object.objectUID, object);
		this.lastObjectUIDAdded = object.objectUID;
	}
	
	public void objectToSellRemoved(int objectUID) {
		this.objectsInBid.remove(objectUID);
	}
	
	/********** API **********/
	
	public void getObjectToSellInfos(int objectGID) {
		ExchangeBidHousePriceMessage msg = new ExchangeBidHousePriceMessage();
		msg.genId = objectGID;
		this.character.net.send(msg);
		
		while(this.objectToSellAskedInfos == null || this.objectToSellAskedInfos.genericId != objectGID)
			this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	public void putObjectToSell(int objectUID, int quantity, int price) {
		ExchangeObjectMovePricedMessage msg = new ExchangeObjectMovePricedMessage();
		msg.objectUID = objectUID;
		msg.quantity = quantity;
		msg.price = price;
		this.character.net.send(msg);
		
		this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	public void modifyObjectToSellPrice(int objectUID, int price) {
		ExchangeObjectModifyPricedMessage msg = new ExchangeObjectModifyPricedMessage();
		msg.objectUID = objectUID;
		msg.quantity = this.objectsInBid.get(objectUID).quantity;
		msg.price = price;
		this.character.net.send(msg);
		
		this.lastObjectUIDAdded = 0;
		while(this.lastObjectUIDAdded == 0)
			this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	public void removeObjectToSell(int objectUID) {
		ExchangeObjectMoveMessage msg = new ExchangeObjectMoveMessage();
		msg.objectUID = objectUID;
		msg.quantity = - this.objectsInBid.get(objectUID).quantity; // quantité négative
		this.character.net.send(msg);
		
		this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	public void sellObjectToNpc(int objectUID, int quantity) {
		ExchangeSellMessage msg = new ExchangeSellMessage();
		msg.objectToSellId = objectUID;
		msg.quantity = quantity;
		this.character.net.send(msg);
		
		this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	public void npcSelling() {
		this.character.mvt.goToInsideTavern();
		this.character.interaction.openTavernShop();
		int serverId = this.character.infos.getServerId();
		ObjectItem[] inventoryObjects = this.character.inventory.getObjects();
		for(ObjectItem object : inventoryObjects) {
			object.averagePrice = getAveragePrice(serverId, object.objectGID);
			if(object.averagePrice != 0 && object.averagePrice < 250)
				sellObjectToNpc(object.objectUID, object.quantity);
		}
		this.character.interaction.closeStorage();
	}
	
	public void bidHouseSellingRoutine() {
		if(this.lastSellingRoutine != null && new Date().getTime() - this.lastSellingRoutine.getTime() < 1000 * 3600 * 4) // 4 heures
			return;
		
		this.character.mvt.goToBidHouse();
		this.character.interaction.openBidHouse();
		reviewSales();
		int freeSlots = infos.maxItemPerAccount - this.objectsInBid.size();
		Log.info(freeSlots + " free slot(s) into the bid house.");
		this.character.interaction.closeStorage();
		this.lastSellingRoutine = new Date();
		if(freeSlots == 0)
			return;
		else
			this.character.mvt.goToInsideBank();
		this.character.interaction.openBankStorage();
		Vector<ObjectItem> objectsToSell = getBankMoreExpensiveObjectsBy100(freeSlots);
		if(objectsToSell.isEmpty()) {
			Log.info("Noting interesting in bank.");
			return;
		}
		
		while(true) {
			takeMaxObjetsAsPossible(objectsToSell);
			this.character.interaction.closeStorage();
			this.character.mvt.goToBidHouse();
			this.character.interaction.openBidHouse();
			sellObjects();
			this.character.interaction.closeStorage();
			if(objectsToSell.isEmpty())
				break;
			else {
				this.character.mvt.goToInsideBank();
				this.character.interaction.openBankStorage();
			}
		}
	}
	
	private void reviewSales() {
		int serverId = this.character.infos.getServerId();
		int currentPrice;
		int newPrice;
		Vector<Integer> objectGIDProcessed = new Vector<Integer>();
		
		// récupération des objets en vente du compte sur ce serveur stockés dans la base de données
		ResultSet resultSet = DatabaseConnection.retrieveAllSales(serverId, this.character.id);
		if(resultSet == null) // si aucun objet en vente, on a rien à faire
			return;
		Vector<ObjectItemToSellInBid> objectsFromDatabase = convertFromResultSet(resultSet);
		synchronizeBidHouseAndDatabase(objectsFromDatabase);
		
		// parcours des objets en vente stockés dans la base de données
		for(ObjectItemToSellInBid object : objectsFromDatabase) {
			
			// si ce type d'objet a déjà été traité, on passe
			if(objectGIDProcessed.contains(object.objectGID))
				continue;
			
			// récupération du prix actuel et du prix moyen
			getObjectToSellInfos(object.objectGID);
			currentPrice = this.objectToSellAskedInfos.minimalPrices.get(2);
			
			// si le prix actuel est inférieur au prix de l'objet en vente
			if(currentPrice < object.objectPrice) {
				// on interroge la base de données sur le prix de vente des collègues pour cet objet
				resultSet = DatabaseConnection.retrievePrices(object.objectGID, serverId);
				
				// récupération du prix des collègues depuis la base de données
				newPrice = setPrice(object.objectGID, currentPrice);
				
				// modification du prix pour tous les objets du même type
				modifyObjectsToSellPrice(objectsFromDatabase, object.objectGID, newPrice);
			}
			// si l'objet est en vente au même prix depuis plus de 24h
			else if(object.unsoldDelay + 24 < infos.unsoldDelay) {
				// on baisse le prix de 5%
				newPrice = (int) (currentPrice * 0.95);
				
				// modification du prix pour tous les objets du même type
				modifyObjectsToSellPrice(objectsFromDatabase, object.objectGID, newPrice);
			}
			this.objectToSellAskedInfos = null;
			objectGIDProcessed.add(object.objectGID);
		}
	}
	
	private void modifyObjectsToSellPrice(Vector<ObjectItemToSellInBid> objectsInBid, int objectGID, int price) {
		int serverId = this.character.infos.getServerId();
		String objectName = Item.getItemById(objectGID, false).getName();
		for(ObjectItemToSellInBid object : objectsInBid)
			if(object.objectGID == objectGID) {
				modifyObjectToSellPrice(object.objectUID, price);
				DatabaseConnection.updateSale(object.objectUID, this.lastObjectUIDAdded, price, serverId);
				Log.info("Price decreased at " + price + " kamas for \"" + objectName + "\".");
			}
	}
	
	private void synchronizeBidHouseAndDatabase(Vector<ObjectItemToSellInBid> objectsFromDatabase) {
		// si l'hotel de vente et la base de données ne sont pas synchronisés
		if(this.objectsInBid.size() != objectsFromDatabase.size()) {
			Log.warn("Bid house and database are not synchronized.");
			int serverId = this.character.infos.getServerId();
			
			// on les synchronise en parcourant les objets de l'hôtel de vente
			// pour trouver les objets manquants dans la base de données
			for(ObjectItemToSellInBid object : this.objectsInBid.values()) {
				if(!objectsFromDatabase.contains(object)) {
					DatabaseConnection.newSale(object.objectUID, object.objectGID, Item.getItemById(object.objectGID, false).getName(), object.objectPrice, object.quantity, serverId, this.character.id);
					objectsFromDatabase.add(object);
				}
			}
		}
	}
	
	private Vector<ObjectItem> getBankMoreExpensiveObjectsBy100(int nFirst) {
		int serverId = this.character.infos.getServerId();
		// on récupère la liste des objets en banque que l'on trie par prix moyen décroissant
		ObjectItem bankObjects[] = this.character.bank.getObjects();
		for(ObjectItem object : bankObjects)
			object.averagePrice = getAveragePrice(serverId, object.objectGID);
		Arrays.sort(bankObjects, ObjectItem.AVG_PRICE_DESC);
		
		Vector<ObjectItem> objectsToSell = new Vector<ObjectItem>(nFirst);
		int packsBy100Counter = 0;
		int nbPossiblePacks;
		
		// parcours de la liste des objets en banque triés
		for(ObjectItem object : bankObjects) {
			// si l'objet est vendable en hdv ressources
			if(this.infos.types.contains(ItemType.getItemTypeById(Item.getItemById(object.objectGID, false).typeId).id)) {
				// et qu'il y en a au moins 100
				if(object.quantity >= 100) {
					// on ajoute l'objet aux objets à vendre et on ajuste la quantité pour pouvoir faire des packs de 100
					objectsToSell.add(object);
					nbPossiblePacks = object.quantity / 100;
					object.quantity = nbPossiblePacks * 100;
					
					// on incrémente le compteur de packs avec le nombre de packs possible pour cet objet
					packsBy100Counter += nbPossiblePacks;
					
					// si le compteur tombe exactement avec le nombre de packs demandés, on a fini 
					if(packsBy100Counter == nFirst)
						return objectsToSell;
					// sinon, on limite la quantité à récupérer pour cet objet
					else if(packsBy100Counter > nFirst) {
						object.quantity -= (packsBy100Counter - nFirst) * 100;
						return objectsToSell;
					}
				}
			}
		}
		return objectsToSell;
	}
	
	private void takeMaxObjetsAsPossible(Vector<ObjectItem> bankObjects) {
		int availablePods = this.character.infos.getWeightMax() - this.character.infos.getWeight();
		int packBy100Weight;
		int maxPacksPossibleToTake;
		Iterator<ObjectItem> it = bankObjects.iterator();
		ObjectItem object;
		
		// parcours de la liste des objets à récupérer
		while(it.hasNext()) {
			object = it.next();
			
			// calcul du poids d'un pack de 100 et du nombre de packs de 100 qu'il est possible d'emporter
			packBy100Weight = Item.getItemById(object.objectGID, false).realWeight * 100;
			maxPacksPossibleToTake = availablePods / packBy100Weight;
			
			// si on ne peut emporter aucun pack de 100 de cet objet, on a fini pour cette fois
			if(maxPacksPossibleToTake == 0)
				break;
			
			// si on peut prendre la quantité maximale de packs de 100, on considère l'objet comme traité en le supprimant
			if(maxPacksPossibleToTake * 100 >= object.quantity)
				it.remove();
			// sinon, on décremente la quantité de l'objet selon le nombre de packs possible à emporter
			else
				object.quantity -= maxPacksPossibleToTake * 100;
			
			// on envoie le message pour récupérer l'objet et on décremente les pods disponibles
			this.character.exchangeManager.getObjectFromBank(object.objectUID, object.quantity);
			availablePods -= packBy100Weight * maxPacksPossibleToTake;
		}
	}
	
	private void sellObjects() {
		// besoin d'une copie de la map car elle est modifiée pendant le parcours des objets de l'inventaire et leur mise en vente
		ObjectItem[] objectsToSell = this.character.inventory.getObjects();
		
		int serverId = this.character.infos.getServerId();
		int currentPrice;
		int newPrice;
		String objectName;
		
		// parcours des objets de l'inventaire
		for(ObjectItem object : objectsToSell) {
			
			// pour ne pas traiter les objets "parasites"
			if(object.quantity < 100)
				continue;
			
			// récupération du prix actuel et du prix moyen
			getObjectToSellInfos(object.objectGID);
			currentPrice = this.objectToSellAskedInfos.minimalPrices.get(2);
			
			// récupération du prix des collègues depuis la base de données
			newPrice = setPrice(object.objectGID, currentPrice);
			
			objectName = Item.getItemById(object.objectGID, false).getName();
			
			// on met tous les packs de 100 possibles en vente
			while(object.quantity > 0) {
				putObjectToSell(object.objectUID, 100, newPrice);
				DatabaseConnection.newSale(object.objectUID, object.objectGID, objectName, newPrice, 100, serverId, this.character.id);
				object.quantity -= 100;
				
				Log.info("Object \"" + objectName + "\" put for sale at " + newPrice + " kamas.");
			}
		}
	}
	
	private int setPrice(int objectGID, int currentPrice) {
		int priceFromDB;
		int averagePrice;
		
		// on interroge la base de données sur le prix de vente des collègues pour cet objet
		ResultSet resultSet = DatabaseConnection.retrievePrices(objectGID, this.character.infos.getServerId());
		
		// on parcours les différents prix de la base de données
		try {
			while(resultSet.next()) {
				priceFromDB = resultSet.getInt("price");
				// si le prix est inférieur au prix actuel, alors les objets ont tous été vendu
				// si le prix est égal au prix actuel, on considère ce prix
				if(priceFromDB == currentPrice)
					return currentPrice;
				// si le prix est supérieur au prix actuel, alors on fixe nous même le nouveau prix
				else if(priceFromDB > currentPrice)
					break;
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return 0;
		}
		
		// on fixe le prix selon le prix moyen de l'objet
		averagePrice = this.objectToSellAskedInfos.averagePrice * 100;
		// s'il n'existe pas de pack de 100 en vente ou si le pack actuel est supérieur au prix moyen d'un pack de 100
		// on fixe le prix égal au prix moyen d'un pack de 100
		if(currentPrice == 0 || currentPrice > averagePrice)
			return averagePrice;
		// sinon, on fixe le prix égal au prix actuel moins 1%
		else
			return (int) (currentPrice * 0.99);
	}
	
	private Vector<ObjectItemToSellInBid> convertFromResultSet(ResultSet resultSet) {
		int objectUID;
		ObjectItemToSellInBid object;
		Vector<ObjectItemToSellInBid> objectsFromDatabase = new Vector<ObjectItemToSellInBid>();
		Vector<Integer> salesToRemove = new Vector<Integer>();
		
		try {
			while(resultSet.next()) {
				objectUID = resultSet.getInt("objectUID");
				object = this.objectsInBid.get(objectUID);
				if(object == null)
					salesToRemove.add(objectUID);
				else
					objectsFromDatabase.add(object);
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		if(!salesToRemove.isEmpty()) {
			int serverId = this.character.infos.getServerId();
			for(int objUID : salesToRemove)
				DatabaseConnection.removeSale(objUID, serverId);
		}
		
		return objectsFromDatabase;
	}
}