package controller.characters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import controller.CharacterState;
import controller.modules.MovementAPI;
import controller.modules.Mule;
import main.Controller;
import main.FatalError;
import main.Log;
import main.Main;
import messages.UnhandledMessage;
import messages.exchanges.ExchangeObjectMoveKamaMessage;

public class Mule extends Character {
	private int waitingMapId;
	private Vector<Character> customers;
	private Lock lock;

	public Mule(int id, String login, String password, int serverId, int breed, Log log) {
		super(id, login, password, serverId, breed, log);
		this.waitingMapId = MovementAPI.ASTRUB_BANK_OUTSIDE_MAP_ID;
		this.customers = new Vector<Character>();
		this.lock = new ReentrantLock();
	}
	
	public int getWaitingMapId() {
		return this.waitingMapId;
	}
	
	public void newCustomer(Character customer) {
		this.lock.lock();
		this.customers.add(customer);
		this.lock.unlock();
		customer.setMule(this);
		customer.updateState(CharacterState.MULE_AVAILABLE, inState(CharacterState.MULE_AVAILABLE));
		this.log.p("Customer " + customer.infos.getLogin() + " added to the customers list.");
	}
	
	public void removeCustomer(Character customer) {
		this.lock.lock();
		if(this.customers.remove(customer))
			this.log.p("Customer " + customer.infos.getLogin() + " removed from the customers list.");
		this.lock.unlock();
	}
	
	public boolean isCustomer(double characterId) {
		for(Character customer : this.customers)
			if(customer.infos.getCharacterId() == characterId)
				return true;
		return false;
	}
	
	private void broadcastAvailability(boolean value) {
		updateState(CharacterState.MULE_AVAILABLE, value);
		this.lock.lock();
		for(Character customer : this.customers)
			customer.updateState(CharacterState.MULE_AVAILABLE, value);
		this.lock.unlock();
	}
	
	private void broadcastDeconnection() {
		this.lock.lock();
		for(Character customer : this.customers) {
			customer.updateState(CharacterState.MULE_AVAILABLE, false);
			customer.setMule(null);
		}
		this.lock.unlock();
	}

	@Override
	public void run() {
		waitState(CharacterState.IS_LOADED); // attendre l'entrée en jeu
		while(!isInterrupted()) {
			checkIfModeratorIsOnline(Main.MODERATOR_NAME);
			if(this.infos.inventoryIsFull(0.1f)) { // + de 10% de l'inventaire occupé
				updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
				broadcastAvailability(false);		
				this.log.p("Need to go to empty inventory at Astrub bank.");
				this.mvt.goToInsideBank();
				this.interaction.openBankStorage();	
				this.exchangeManager.transfertAllObjectsFromInventory();
				this.interaction.closeStorage();
				updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
			}
			this.salesManager.bidHouseSellingRoutine();
			this.mvt.goTo(this.waitingMapId, false); // sort automatiquement de la banque s'il y est
			broadcastAvailability(true);
			if(waitState(CharacterState.PENDING_DEMAND)) { // on attend qu'un combattant lance un échange
				this.log.p("Exchange demand received.");
				if(this.exchangeManager.processExchangeDemand(this.roleplayContext.actorDemandingExchange)) {
					broadcastAvailability(false);
					this.exchangeManager.waitExchangeValidatedForValidExchange();
				}
			}
			
			if(inState(CharacterState.SHOULD_DECONNECT)) {
				deconnectionOrder(true);
				break;
			}
		}
		broadcastDeconnection();
		Log.info("Thread controller of character with id = " + this.id + " terminated.");
		Controller.getInstance().threadTerminated();
	}
}

public static ResultSet retrieveMules(int[] serverIds) {
	Log.info("Retrieving mule(s) from database.");
	query.setLength(0);
	ResultSet result;
	try {
		co.setAutoCommit(false);
		
		// récupération des mules
		query.append("SELECT id, login, password, serverId FROM accounts WHERE serverId IN (");
		for(int serverId : serverIds)
			if(serverId != serverIds[serverIds.length - 1])
				query.append(serverId + ", ");
			else
				query.append(serverId + ")");
		query.append(" AND owner IS NULL AND isBanned = 0 GROUP BY serverId;");
		result = st.executeQuery(query.toString());
		
		// réservation des mules
		query.setLength(0);
		while(result.next()) {
			query.append("UPDATE accounts SET owner = \"" + Main.USERNAME + "\" WHERE id IN (");
			if(!result.isLast())
				query.append(result.getInt("id") + ", ");
			else
				query.append(result.getInt("id") + ");");
		}
		
		if(query.length() == 0)
			throw new FatalError("None mule is available.");
		
		st = co.createStatement();
		st.executeUpdate(query.toString());
		
		co.commit();
		co.setAutoCommit(true);
		result.beforeFirst();
		return result;
	} catch(SQLException e) {
		e.printStackTrace();
		return null;
	}
}

//se rend à la map d'attente de la mule et lui transmet tous les objets de l'inventaire et les kamas
public void goToExchangeWithMule() {
	// attente de la connexion de la mule si elle n'est pas connectée
	this.character.waitState(CharacterState.MULE_ONLINE);
	Mule mule = character.getMule();
	
	// aller sur la map d'attente de la mule
	this.character.mvt.goTo(mule.getWaitingMapId(), false);
	
	// tentative d'échange avec elle
	while(!Thread.currentThread().isInterrupted()) {
		if(!this.character.roleplayContext.actorIsOnMap(mule.infos.getCharacterId())) // si la mule n'est pas sur la map
			this.character.waitState(CharacterState.NEW_ACTOR_ON_MAP); // on attend qu'elle arrive
		else {
			this.character.waitState(CharacterState.MULE_AVAILABLE);
			if(sendExchangeRequest(mule.infos.getCharacterId())) // si l'échange a été accepté
				break;
			try {
				Thread.sleep(5000); // pour pas flooder de demandes d'échange
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
	
	// transfert de tous les objets de l'inventaire
	this.character.log.p("Transfering all objects from inventory.");
	this.character.net.send(new UnhandledMessage("ExchangeObjectTransfertAllFromInvMessage"));
	
	// les kamas aussi
	ExchangeObjectMoveKamaMessage EOMKM = new ExchangeObjectMoveKamaMessage();
	EOMKM.quantity = this.character.inventory.getKamas();
	this.character.net.send(EOMKM);
	
	// on attend de pouvoir valider l'échange (bouton bloqué pendant 3 secondes après chaque action)
	try {
		Thread.sleep(3000);
	} catch(InterruptedException e) {
		Thread.currentThread().interrupt();
		return;
	}
	
	// validation de l'échange côté combattant
	validExchange();
	
	// et agissement en conséquence
	if(this.character.roleplayContext.lastExchangeOutcome) {
		this.character.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
		this.character.log.p("Exchange with mule terminated successfully.");
	}
	else
		throw new FatalError("Exchange with mule has failed.");
	
	// on repasse en mode absent après l'échange
	this.character.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
}

//traite une demande d'échange (acceptation ou refus)
public boolean processExchangeDemand(double actorIdDemandingExchange) {
	// si ce n'est pas un client, on refuse l'échange avec un sleep
	if(!((Mule) this.character).isCustomer(actorIdDemandingExchange)) {
		try {
			Thread.sleep(2000); // pour faire un peu normal
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		this.character.log.p("Refusing exchange request received from an unknown.");
		this.character.net.send(new UnhandledMessage("LeaveDialogRequestMessage"));
		return false;
	}
	
	// si le caractère actuel est une mule et qu'il est occupé, on refuse
	// ou si le caractère (peu importe le type) a besoin de vider son inventaire, on refuse aussi
	if((this.character instanceof Mule && !this.character.inState(CharacterState.MULE_AVAILABLE)) ||
			this.character.inState(CharacterState.NEED_TO_EMPTY_INVENTORY)) {
		this.character.log.p("Refusing exchange request because I am busy.");
		this.character.net.send(new UnhandledMessage("LeaveDialogRequestMessage"));
		return false;
	}
		
	// sinon on accepte l'échange
	this.character.log.p("Accepting exchange request received from a customer.");
	this.character.net.send(new UnhandledMessage("ExchangeAcceptMessage"));
	this.character.waitState(CharacterState.IN_EXCHANGE);
	return true;
}