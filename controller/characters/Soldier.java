package controller.characters;

import controller.CharacterState;
import controller.api.FightAPI;
import gamedata.enums.PlayerStatusEnum;
import gui.Controller;
import main.Log;
import messages.context.GameContextReadyMessage;
import messages.fights.GameFightJoinRequestMessage;

public class Soldier extends Fighter {
	private FightAPI fight;
	private Captain captain;
	
	// booléens accessibles pour le capitaine (états du soldat)
	protected boolean waitingPartyInvitation;
	protected boolean readyForFight;

	public Soldier(int id, String login, String password, int serverId, int breed, Log log) {
		super(id, login, password, serverId, breed, log);
		this.fight = new FightAPI(this);
	}
	
	public Captain getCaptain() {
		return this.captain;
	}
	
	protected void setCaptain(Captain captain) {
		this.captain = captain;
	}
	
	// informe le capitaine d'une action effectué par le soldat
	private void informCaptain() {
		this.captain.updateState(CharacterState.SOLDIER_ACT, true);
	}
	
	private void followCaptain() {
		waitState(CharacterState.IS_LOADED); // attendre le refresh des infos
		
		while(this.infos.currentMap.id != this.captain.infos.currentMap.id) {
			this.mvt.dynamicGoTo(this.captain.infos.currentMap.id);
			waitState(CharacterState.IS_LOADED); // attendre le refresh des infos
		}
	}
	
	private void joinFight() {
		waitState(CharacterState.IS_LOADED); // peut-être encore dans le précédent combat
		
		GameFightJoinRequestMessage GFJRM = new GameFightJoinRequestMessage();
		GFJRM.fighterId = this.captain.infos.characterId;
		GFJRM.fightId = this.partyManager.getFightId();
		this.net.send(GFJRM);
		this.log.p("Request for join fight sent.");
	}
	
	public void run() {
		waitState(CharacterState.IS_LOADED);
		
		// reprise de combat à la connexion
		if(inState(CharacterState.IN_FIGHT)) {
			GameContextReadyMessage GCRM = new GameContextReadyMessage();
			GCRM.mapId = this.infos.currentMap.id;
			this.net.send(GCRM);
			this.fight.fightManager(true);
		}
		
		// si on était déjà dans un groupe, on le quitte
		if(inState(CharacterState.IN_PARTY))
			this.partyManager.leaveParty();
		
		// on passe en mode disponible pour pouvoir être invité dans un groupe
		this.social.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AVAILABLE);
		
		// on informe le capitain qu'on est prêt et on attend la demande d'invitation de groupe puis on passe en mode absent
		this.waitingPartyInvitation = true;
		informCaptain();
		waitState(CharacterState.IN_PARTY);
		this.waitingPartyInvitation = false;
		this.social.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
		
		// boucle déplacements + combats
		while(!isInterrupted() && !inState(CharacterState.SHOULD_DECONNECT)) {
			this.fight.rebirthManager(); // besoin de renaître au phénix ?
			followCaptain();
			this.readyForFight = true;
			informCaptain();
			waitState(CharacterState.CAPTAIN_ACT);
			if(this.captain.inState(CharacterState.IN_FIGHT)) {
				waitState(CharacterState.FIGHT_LAUNCHED);
				joinFight();
				if(waitState(CharacterState.IN_FIGHT)) {
					this.readyForFight = false;
					this.fight.fightManager(false);
					this.fight.levelUpManager();
				}
			}
			else if(this.captain.inState(CharacterState.NEED_TO_EMPTY_INVENTORY)) {
				if(inventoryIsSoHeavy(0.1f))
					this.social.goToExchangeWithMule();
				else // annule l'état broadcasté par le capitaine
					updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
			}
		}
		if(inState(CharacterState.SHOULD_DECONNECT))
			deconnectionOrder(true);
		Log.info("Thread controller of character with id = " + this.id + " terminated.");
		Controller.getInstance().threadTerminated();
	}
}