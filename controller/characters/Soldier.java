package controller.characters;

import controller.CharacterState;
import controller.api.FightAPI;
import gamedata.character.PlayerStatusEnum;
import main.Instance;
import messages.context.GameContextReadyMessage;
import messages.fights.GameFightJoinRequestMessage;

public class Soldier extends Fighter {
	private FightAPI fight;
	private Captain captain;

	public Soldier(Instance instance, String login, String password, int serverId, int breed) {
		super(instance, login, password, serverId, breed);
		this.fight = new FightAPI(this);
	}
	
	public Captain getCaptain() {
		return this.captain;
	}
	
	protected void setCaptain(Captain captain) {
		this.captain = captain;
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
		GFJRM.fightId = this.roleplayContext.currentCaptainFightId;
		GFJRM.serialize();
		this.instance.outPush(GFJRM);
		this.instance.log.p("Request for join fight sent.");
	}
	
	public void run() {
		waitState(CharacterState.IS_LOADED);
		this.social.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AVAILABLE); // pour pouvoir être invité dans le groupe
		if(inState(CharacterState.IN_FIGHT)) { // reprise de combat
			GameContextReadyMessage GCRM = new GameContextReadyMessage(); // je ne sais pas à quoi sert ce message
			GCRM.serialize(this.infos.currentMap.id);
			this.instance.outPush(GCRM);
			this.fight.fightManager(true);
		}
		if(inState(CharacterState.IN_PARTY))
			this.social.leaveGroup();
		waitState(CharacterState.IN_PARTY);
		this.social.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
		
		while(!isInterrupted()) { // boucle déplacements + combats
			this.fight.rebirthManager(); // besoin de renaître au phénix ?
			followCaptain();
			waitState(CharacterState.CAPTAIN_ACT);
			if(this.captain.inState(CharacterState.IN_FIGHT)) {
				waitState(CharacterState.FIGHT_LAUNCHED);
				joinFight();
				if(waitState(CharacterState.IN_FIGHT)) {
					this.fight.fightManager(false);
					this.fight.levelUpManager();
				}
			}
			else if(this.captain.inState(CharacterState.NEED_TO_EMPTY_INVENTORY))
				this.social.goToExchange(this.mule);
		}
		System.out.println("Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
}