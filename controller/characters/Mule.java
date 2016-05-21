package controller.characters;

import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import controller.CharacterState;
import controller.modules.MovementAPI;
import main.Controller;
import main.Log;
import main.Main;

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
		((Fighter) customer).setMule(this);
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
			((Fighter) customer).setMule(null);
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