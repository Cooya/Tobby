package controller.characters;

import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import controller.CharacterState;
import main.Controller;
import main.Log;
import main.Main;

public class Mule extends Character {
	private static final int BANK_INSIDE_MAP_ID = 83887104;
	private static final int BANK_OUTSIDE_MAP_ID = 84674566;
	
	private int waitingMapId;
	private Vector<Character> customers;
	private Lock lock;

	public Mule(int id, String login, String password, int serverId, int breed, Log log) {
		super(id, login, password, serverId, breed, log);
		this.waitingMapId = BANK_OUTSIDE_MAP_ID; // banque d'Astrub
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

	private void goOutAstrubBank() {
		this.mvt.moveTo(396); // on sort de la banque
		updateState(CharacterState.IS_LOADED, false); // important (porte de la banque)
	}

	@Override
	public void run() {
		while(!isInterrupted() && waitState(CharacterState.IS_FREE) && !inState(CharacterState.SHOULD_DECONNECT)) { // attente d'état importante afin de laisser le temps aux pods de se mettre à jour après un échange
			checkIfModeratorIsOnline(Main.MODERATOR_NAME);
			if(this.infos.getCurrentMap().id == BANK_INSIDE_MAP_ID) // si le perso est dans la banque
				goOutAstrubBank();
			if(this.infos.inventoryIsFull(0.1f)) { // + de 10% de l'inventaire occupé
				updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
				broadcastAvailability(false);		
				this.log.p("Need to go to empty inventory at Astrub bank.");
				this.mvt.goTo(BANK_OUTSIDE_MAP_ID); // map où se situe la banque
				this.interaction.useInteractive(317, 465440, true); // porte de la banque
				this.interaction.emptyInventoryInBank();
				updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
				goOutAstrubBank(); // on sort de la banque
			}
			this.mvt.goTo(this.waitingMapId);
			broadcastAvailability(true);
			if(waitState(CharacterState.PENDING_DEMAND)) { // on attend qu'un combattant lance un échange
				this.log.p("Exchange demand received.");
				if(this.social.processExchangeDemand(this.roleplayContext.actorDemandingExchange))
					broadcastAvailability(false);
			}
		}
		
		if(inState(CharacterState.SHOULD_DECONNECT))
			deconnectionOrder(true);
		broadcastDeconnection();
		Log.info("Thread controller of character with id = " + this.id + " terminated.");
		Controller.getInstance().threadTerminated();
	}
}