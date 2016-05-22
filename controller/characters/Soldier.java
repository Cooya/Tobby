package controller.characters;

import controller.CharacterState;
import gamedata.enums.PlayerStatusEnum;
import main.Controller;
import main.Log;
import messages.context.GameContextReadyMessage;
import messages.fights.GameFightJoinRequestMessage;

public class Soldier extends Character {
	private Captain captain;
	
	// booléens accessibles pour le capitaine (états du soldat)
	protected boolean waitingPartyInvitation;
	protected boolean readyForFight;

	public Soldier(int id, String login, String password, int serverId, int breed, Log log) {
		super(id, login, password, serverId, breed, 0, log);
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
		while(this.infos.getCurrentMap().id != this.captain.infos.getCurrentMap().id)
			this.mvt.goTo(this.captain.infos.getCurrentMap().id, true);
	}
	
	private void joinFight() {
		GameFightJoinRequestMessage GFJRM = new GameFightJoinRequestMessage();
		GFJRM.fighterId = this.captain.infos.getCharacterId();
		GFJRM.fightId = this.partyManager.getFightId();
		this.net.send(GFJRM);
		this.log.p("Request for join fight sent.");
	}
	
	public void run() {
		waitState(CharacterState.IS_LOADED); // attendre l'entrée en jeu
		
		// reprise de combat à la connexion
		if(inState(CharacterState.IN_FIGHT)) {
			GameContextReadyMessage GCRM = new GameContextReadyMessage();
			GCRM.mapId = this.infos.getCurrentMap().id;
			this.net.send(GCRM);
			this.fight.fightManager(true);
		}
		
		// si on était déjà dans un groupe, on le quitte
		if(inState(CharacterState.IN_PARTY))
			this.partyManager.leaveParty();
		
		// on passe en mode disponible pour pouvoir être invité dans un groupe
		changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AVAILABLE);
		
		// on informe le capitain qu'on est prêt et on attend la demande d'invitation de groupe puis on passe en mode absent
		this.waitingPartyInvitation = true;
		informCaptain();
		waitState(CharacterState.IN_PARTY);
		this.waitingPartyInvitation = false;
		changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
		
		// boucle déplacements + combats
		while(!isInterrupted()) {
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
				this.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
				this.fight.inventoryManager();
			}
			
			if(inState(CharacterState.SHOULD_DECONNECT)) {
				deconnectionOrder(true);
				break;
			}
		}
		Log.info("Thread controller of character with id = " + this.id + " terminated.");
		Controller.getInstance().threadTerminated();
	}
}