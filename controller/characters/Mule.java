package controller.characters;

import controller.CharacterState;
import main.Instance;
import main.Main;

public class Mule extends Character {
	private static final int BANK_INSIDE_MAP_ID = 83887104;
	private static final int BANK_OUTSIDE_MAP_ID = 84674566;
	private int waitingMapId;

	public Mule(Instance instance, String login, String password, int serverId, int breed) {
		super(instance, login, password, serverId, breed);
		this.waitingMapId = BANK_OUTSIDE_MAP_ID; // banque d'Astrub
	}

	public int getWaitingMapId() {
		return this.waitingMapId;
	}

	private void goOutAstrubBank() {
		this.mvt.moveTo(396, false); // on sort de la banque
		updateState(CharacterState.IS_LOADED, false); // important (porte de la banque)
	}

	private boolean needToGoBank(float percentage) { // percentage < 1
		if(this.infos.weight > this.infos.weightMax * percentage)
			return true;
		return false;
	}

	@Override
	public void run() {
		while(!isInterrupted() && waitState(CharacterState.IS_FREE)) { // attente d'état importante afin de laisser le temps aux pods de se mettre à jour après un échange
			checkIfModeratorIsOnline(Main.MODERATOR_NAME);
			if(this.infos.currentMap.id == BANK_INSIDE_MAP_ID) // si le perso est dans la banque (lancement de l'instance)
				goOutAstrubBank();
			if(needToGoBank(0.1f)) { // + de 10% de l'inventaire occupé
				updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
				this.instance.log.p("Need to go to empty inventory at Astrub bank.");
				if(inState(CharacterState.PENDING_DEMAND)) // TODO -> il peut y avoir un problème de pods ici
					if(this.social.processExchangeDemand(this.roleplayContext.actorDemandingExchange))
						this.social.acceptExchangeAsReceiver();
				this.mvt.goTo(BANK_OUTSIDE_MAP_ID); // map où se situe la banque
				this.interaction.useInteractive(317, 465440, 140242, true); // porte de la banque
				this.interaction.emptyInventoryInBank();
				updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
				goOutAstrubBank(); // on sort de la banque
			}
			this.mvt.goTo(this.waitingMapId);
			if(waitState(CharacterState.PENDING_DEMAND)) { // on attend qu'un combattant lance un échange
				this.instance.log.p("Exchange demand received.");
				if(this.social.processExchangeDemand(this.roleplayContext.actorDemandingExchange))
					this.social.acceptExchangeAsReceiver();
			}
		}
		System.out.println("Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
}