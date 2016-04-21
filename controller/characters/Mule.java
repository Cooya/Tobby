package controller.characters;

import gui.Controller;

import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import controller.CharacterState;
import main.Instance;
import main.Log;
import main.Main;

public class Mule extends Character {
	private static final int BANK_INSIDE_MAP_ID = 83887104;
	private static final int BANK_OUTSIDE_MAP_ID = 84674566;
	
	private int waitingMapId;
	private Vector<Fighter> customers;
	private Lock lock;

	public Mule(Instance instance, String login, String password, int serverId, int breed) {
		super(instance, login, password, serverId, breed);
		this.waitingMapId = BANK_OUTSIDE_MAP_ID; // banque d'Astrub
		this.customers = new Vector<Fighter>();
		this.lock = new ReentrantLock();
	}
	
	public int getWaitingMapId() {
		return this.waitingMapId;
	}
	
	public void newCustomer(Fighter customer) {
		this.lock.lock();
		this.customers.add(customer);
		this.lock.unlock();
		customer.setMule(this);
		customer.updateState(CharacterState.MULE_AVAILABLE, inState(CharacterState.MULE_AVAILABLE));
		this.instance.log.p("Customer " + customer.infos.login + " added to the customers list.");
	}
	
	public void removeCustomer(Character customer) {
		this.lock.lock();
		if(this.customers.remove(customer))
			this.instance.log.p("Customer " + customer.infos.login + " removed from the customers list.");
		this.lock.unlock();
	}
	
	private void broadcastAvailability(boolean value) {
		this.lock.lock();
		for(Fighter customer : this.customers)
			customer.updateState(CharacterState.MULE_AVAILABLE, value);
		this.lock.unlock();
	}
	
	private void broadcastDeconnection() {
		this.lock.lock();
		for(Fighter customer : this.customers) {
			customer.updateState(CharacterState.MULE_AVAILABLE, false);
			customer.setMule(null);
		}
		this.lock.unlock();
	}

	private void goOutAstrubBank() {
		this.mvt.moveTo(396, false); // on sort de la banque
		updateState(CharacterState.IS_LOADED, false); // important (porte de la banque)
	}

	@Override
	public void run() {
		while(!isInterrupted() && waitState(CharacterState.IS_FREE) && !inState(CharacterState.SHOULD_DECONNECT)) { // attente d'état importante afin de laisser le temps aux pods de se mettre à jour après un échange
			checkIfModeratorIsOnline(Main.MODERATOR_NAME);
			if(this.infos.currentMap.id == BANK_INSIDE_MAP_ID) // si le perso est dans la banque (lancement de l'instance)
				goOutAstrubBank();
			if(inventoryIsSoHeavy(0.1f)) { // + de 10% de l'inventaire occupé
				updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
				broadcastAvailability(false);		
				this.instance.log.p("Need to go to empty inventory at Astrub bank.");
				this.mvt.goTo(BANK_OUTSIDE_MAP_ID); // map où se situe la banque
				this.interaction.useInteractive(317, 465440, 140242, true); // porte de la banque
				this.interaction.emptyInventoryInBank();
				updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
				goOutAstrubBank(); // on sort de la banque
			}
			this.mvt.goTo(this.waitingMapId);
			broadcastAvailability(true);
			if(waitState(CharacterState.PENDING_DEMAND)) { // on attend qu'un combattant lance un échange
				this.instance.log.p("Exchange demand received.");
				if(this.social.processExchangeDemand(this.roleplayContext.actorDemandingExchange)) {
					broadcastAvailability(false);
					this.social.acceptExchangeAsReceiver();
				}
			}
		}
		
		if(inState(CharacterState.SHOULD_DECONNECT))
			this.instance.deconnectionOrder(true);
		broadcastDeconnection();
		Log.info("Thread controller of instance with id = " + this.instance.id + " terminated.");
		Controller.getInstance().threadTerminated();
	}
}