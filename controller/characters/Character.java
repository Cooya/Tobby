package controller.characters;

import gamedata.d2p.ankama.Map;

import java.util.Hashtable;

import controller.CharacterState;
import controller.api.InteractionAPI;
import controller.api.MovementAPI;
import controller.api.SocialAPI;
import controller.informations.CharacterInformations;
import controller.informations.PartyManager;
import controller.informations.RoleplayContext;
import main.FatalError;
import main.Instance;
import main.Main;
import messages.character.BasicWhoIsRequestMessage;
import messages.synchronisation.BasicPingMessage;

public abstract class Character extends Thread {
	private Hashtable<CharacterState, Boolean> states;
	public Instance instance;
	public CharacterInformations infos;
	public RoleplayContext roleplayContext;
	public PartyManager partyManager;
	public MovementAPI mvt;
	public InteractionAPI interaction;
	public SocialAPI social;

	public Character(Instance instance, String login, String password, int serverId, int breed) {
		super(login + "/controller");
		this.states = new Hashtable<CharacterState, Boolean>();
		this.instance = instance;
		this.infos = new CharacterInformations(login, password, serverId, breed);
		this.roleplayContext = new RoleplayContext(this);
		this.partyManager = new PartyManager(this);
		this.mvt = new MovementAPI(this);
		this.interaction = new InteractionAPI(this);
		this.social = new SocialAPI(this);

		for(CharacterState state : CharacterState.values())
			this.states.put(state, false);
	}

	public void updatePosition(Map map, int cellId) {
		this.mvt.updatePosition(map, cellId);
	}

	// determine si l'inventaire est plein ou pas selon le pourcentage donné
	public boolean inventoryIsSoHeavy(float percentage) { // percentage < 1
		if(this.infos.weight > this.infos.weightMax * percentage)
			return true;
		return false;
	}

	protected void sendPingRequest() {
		BasicPingMessage BPM = new BasicPingMessage();
		BPM.serialize(false);
		this.instance.outPush(BPM);
		this.instance.log.p("Sending a ping request to server for stay connected.");
	}

	// envoie une requête WHOIS pour savoir si le modérateur du serveur est en ligne
	protected void checkIfModeratorIsOnline(String moderatorName) {
		BasicWhoIsRequestMessage BWIRM = new BasicWhoIsRequestMessage();
		BWIRM.verbose = true;
		BWIRM.search = moderatorName;
		BWIRM.serialize();
		this.instance.outPush(BWIRM);
		this.instance.log.p("Checking if moderator is online.");
		waitState(CharacterState.WHOIS_RESPONSE);
	}

	// seul le thread de traitement entre ici
	public synchronized void updateState(CharacterState state, boolean newState) {
		this.instance.log.p("State updated : " + state + " = " + newState + ".");
		this.states.put(state, newState);
		notify();
	}

	public boolean inState(CharacterState state) {
		return this.states.get(state);
	}

	// seul le contrôleur entre ici
	public boolean waitState(CharacterState state, int timeout) {
		Condition condition = null;
		boolean isEvent = false;
		boolean forbiddenTimeout = false;
		switch(state) {
			case IS_FREE : // état composé
				this.instance.log.p("Waiting for character to be free.");
				condition = new Condition(CharacterState.IS_LOADED, 60000);
				condition.addSecondState(CharacterState.IN_EXCHANGE, false);
				forbiddenTimeout = true;
				break;
			case IN_GAME_TURN : // état avec contrainte
				this.instance.log.p("Waiting for my game turn.");
				condition = new Condition(state, 0); // mort du perso dans le combat
				condition.addConstraint(CharacterState.IN_FIGHT, false);
				break;
			case IS_LOADED : // état simple
				this.instance.log.p("Waiting for character to be loaded.");
				condition = new Condition(state, 60000);
				forbiddenTimeout = true;
				break;
			case PENDING_DEMAND : // état simple
				this.instance.log.p("Waiting for exchange demand.");
				condition = new Condition(state, 0);
				condition.addConstraint(CharacterState.SHOULD_DECONNECT, true); // tant qu'on ne reçoit pas d'ordre de déconnexion
				break;
			case MULE_ONLINE : // état simple
				this.instance.log.p("Waiting for mule connection.");
				condition = new Condition(state, 0);
				condition.addConstraint(CharacterState.SHOULD_DECONNECT, true); // tant qu'on ne reçoit pas d'ordre de déconnexion
				break;
			case MULE_AVAILABLE : // état simple
				this.instance.log.p("Waiting for mule to be available.");
				condition = new Condition(state, 0);
				condition.addConstraint(CharacterState.SHOULD_DECONNECT, true); // tant qu'on ne reçoit pas d'ordre de déconnexion
				break;
			case CAN_MOVE : // état simple
				this.instance.log.p("Waiting for movement acceptation by server.");
				condition = new Condition(state, 30000);
				forbiddenTimeout = true;
				break;
			case IN_PARTY : // état simple
				this.instance.log.p("Waiting for joining party.");
				condition = new Condition(state, 30000);
				forbiddenTimeout = true;
				break;
			case NOT_IN_PARTY : // état abstrait inverse (abstrait = qui n'exste pas)
				this.instance.log.p("Waiting for leaving party.");
				condition = new Condition(CharacterState.IN_PARTY, false, 30000);
				break;
			case IN_FIGHT : // attente avec timeout
				this.instance.log.p("Waiting for fight beginning.");
				condition = new Condition(state, 5000);
				break;
			case IN_EXCHANGE : // attente avec timeout
				this.instance.log.p("Waiting for exchange acceptance.");
				condition = new Condition(state, 5000);
				break;
			case DIALOG_DISPLAYED : // attente
				this.instance.log.p("Waiting for dialog to be displayed.");
				condition = new Condition(state, 30000);
				forbiddenTimeout = true;
				break;
			case IN_REGENERATION : // attente inverse avec timeout donné (on attend juste la fin du timeout)
				this.instance.log.p("Waiting for regeneration to be completed.");
				this.states.put(state, true);
				condition = new Condition(state, false, timeout);
				condition.addConstraint(CharacterState.SHOULD_DECONNECT, true); // tant qu'on ne reçoit pas d'ordre de déconnexion
				isEvent = true; // pas un event en fait
				break;
			case NEW_ACTOR_ON_MAP : // event
				this.instance.log.p("Waiting for new actor on the map.");
				condition = new Condition(state, 0);
				isEvent = true;
				break;
			case EXCHANGE_VALIDATED : // event
				this.instance.log.p("Waiting for exchange validation.");
				condition = new Condition(state, 60000);
				condition.addConstraint(CharacterState.SHOULD_DECONNECT, true); // tant qu'on ne reçoit pas d'ordre de déconnexion
				isEvent = true;
				break;
			case CAPTAIN_ACT : // event
				this.instance.log.p("Waiting for captain act.");
				condition = new Condition(state, 0);
				condition.addConstraint(CharacterState.SHOULD_DECONNECT, true); // tant qu'on ne reçoit pas d'ordre de déconnexion
				isEvent = true;
				break;
			case SOLDIER_ACT : // event
				this.instance.log.p("Waiting for soldier act.");
				condition = new Condition(state, 0);
				condition.addConstraint(CharacterState.SHOULD_DECONNECT, true); // tant qu'on ne reçoit pas d'ordre de déconnexion
				isEvent = true;
				break;
			case NEW_PARTY_MEMBER : // event
				this.instance.log.p("Waiting for party invitation acceptation.");
				condition = new Condition(state, 30000);
				isEvent = true;
				forbiddenTimeout = true;
				break;
			case FIGHT_LAUNCHED : // event
				this.instance.log.p("Waiting for fight be launched by captain.");
				condition = new Condition(state, 30000);
				isEvent = true;
				forbiddenTimeout = true;
				break;
			case WHOIS_RESPONSE : // event
				this.instance.log.p("Waiting for WHOIS response.");
				condition = new Condition(state, 30000);
				isEvent = true;
				forbiddenTimeout = true;
				break;
			case BANK_TRANSFER : // event
				this.instance.log.p("Waiting for bank transfer to be done.");
				condition = new Condition(state, 30000);
				isEvent = true;
				forbiddenTimeout = true;
				break;
			case EXCHANGE_DEMAND_OUTCOME : // event
				this.instance.log.p("Waiting for exchange demand outcome.");
				condition = new Condition(state, 5000);
				isEvent = true;
				break;
			case SPELL_CASTED : // event avec contrainte
				this.instance.log.p("Waiting for result of spell cast.");
				condition = new Condition(state, 10000);
				condition.addConstraint(CharacterState.IN_GAME_TURN, false); // tant que le tour de jeu n'est pas terminé
				isEvent = true;
				break;
			case NEW_ACTOR_IN_FIGHT : // event avec contrainte
				this.instance.log.p("Waiting for soldier join fight.");
				condition = new Condition(state, 0);
				condition.addConstraint(CharacterState.IN_GAME_TURN, true);
				isEvent = true;
				break;
			case LEVEL_UP : // état ponctuel
			case NEED_TO_EMPTY_INVENTORY : // état ponctuel
			case SHOULD_DECONNECT : // état ponctuel
				throw new FatalError("Impossible to wait the one-time state : " + state + ".");
		}
		boolean result = waitFor(condition);
		if(isEvent)
			this.states.put(condition.expectedState, false);
		if(isInterrupted())
			return false;
		if(forbiddenTimeout && !result) // si le timeout est interdit pour cet état
			throw new FatalError("Timeout reached and forbidden for the state : " + state + ".");
		return result;
	}

	public boolean waitState(CharacterState state) {
		return waitState(state, 0);
	}

	// retourne false lors d'un timeout, d'une contrainte vraie ou d'une interruption
	private synchronized boolean waitFor(Condition condition) {
		boolean infiniteWaiting = condition.timeout == 0;
		if(infiniteWaiting)
			condition.timeout = 120000; // 2 minutes
		long startTime = System.currentTimeMillis();
		long currentTime;
		while(!isInterrupted()) { // n'entre même pas dans la boucle si le thread est en cours d'interruption
			while((currentTime = System.currentTimeMillis() - startTime) < condition.timeout) {
				if(!condition.type) {
					// condition à un état
					if(this.states.get(condition.expectedState) == condition.expectedValueForExpectedState)
						return true;
					// condition à un état avec une contrainte
					if(condition.otherState != null && this.states.get(condition.otherState) == condition.expectedValueForOtherState)
						return false;
				}
				else // condition à 2 états
					if(this.states.get(condition.expectedState) == condition.expectedValueForExpectedState && this.states.get(condition.otherState) == condition.expectedValueForOtherState)
						return true;
				try {
					wait(condition.timeout - currentTime);
				} catch(Exception e) {
					interrupt();
					return false;
				}
			}
			if(infiniteWaiting) {
				//sendPingRequest();
				if(this.infos.isConnected)
					checkIfModeratorIsOnline(Main.MODERATOR_NAME); // requête effectuée toutes les 2 minutes
				startTime = System.currentTimeMillis();
			}
			else {
				this.instance.log.p("TIMEOUT");
				return false; // si on ne l'a pas reçu à temps
			}
		}
		return false;
	}

	private static class Condition {
		private CharacterState expectedState;
		private boolean expectedValueForExpectedState;
		private CharacterState otherState;
		private boolean expectedValueForOtherState;
		private int timeout;
		private boolean type; // true = second state, false = simple state or constraint state

		private Condition(CharacterState state, boolean expectedValue, int timeout) {
			this.expectedState = state;
			this.expectedValueForExpectedState = expectedValue;
			this.timeout = timeout;
			this.otherState = null;
			this.type = false;
		}

		private Condition(CharacterState state, int timeout) {
			this(state, true, timeout);
		}

		private void addSecondState(CharacterState state, boolean expectedValue) {
			this.otherState = state;
			this.expectedValueForOtherState = expectedValue;
			this.type = true;
		}

		private void addConstraint(CharacterState state, boolean expectedValue) {
			this.otherState = state;
			this.expectedValueForOtherState = expectedValue;
			this.type = false;
		}
	}
}