package controller;

import gamedata.character.PlayerStatusEnum;
import main.Instance;
import messages.context.GameContextReadyMessage;
import messages.fights.GameFightJoinRequestMessage;

public class SoldierController extends FighterController {
	protected CaptainController captain;

	public SoldierController(Instance instance, String login, String password, int serverId) {
		super(instance, login, password, serverId, 0);
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
	
	@Override
	protected void levelUpManager() {
		if(!waitState(CharacterState.LEVEL_UP))
			return;
		waitState(CharacterState.IS_LOADED);
		upgradeSpell();
		increaseStats();
		this.captain.soldierHasLevelUp(); // met à jour le niveau de l'escouade et donc possiblement l'aire de combat 
	}
	
	public void run() {
		waitState(CharacterState.IS_LOADED);
		changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AVAILABLE); // pour pouvoir être invité dans le groupe
		if(inState(CharacterState.IN_FIGHT)) { // reprise de combat
			GameContextReadyMessage GCRM = new GameContextReadyMessage(); // je ne sais pas à quoi sert ce message
			GCRM.serialize(this.infos.currentMap.id);
			this.instance.outPush(GCRM);
			fight(true);
		}
		if(inState(CharacterState.IN_PARTY))
			leaveGroup();
		waitState(CharacterState.IN_PARTY);
		changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
		
		while(!isInterrupted()) { // boucle déplacements + combats
			riseIfNecessary(); // besoin de renaître au phénix ?
			followCaptain();
			waitState(CharacterState.CAPTAIN_ACT);
			if(this.captain.inState(CharacterState.IN_FIGHT)) {
				waitState(CharacterState.FIGHT_LAUNCHED);
				joinFight();
				if(waitState(CharacterState.IN_FIGHT)) {
					fight(false);
					levelUpManager();
				}
			}
			else if(this.captain.inState(CharacterState.NEED_TO_EMPTY_INVENTORY))
				goToExchangeWithMule();
		}
		System.out.println("Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
}