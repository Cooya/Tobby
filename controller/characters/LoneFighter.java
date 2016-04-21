package controller.characters;

import gamedata.character.PlayerStatusEnum;
import gui.Controller;
import controller.CharacterState;
import controller.api.FightAPI;
import main.Instance;
import main.Log;
import main.Main;
import messages.context.GameContextReadyMessage;

public class LoneFighter extends Fighter {
	private FightAPI fight;

	public LoneFighter(Instance instance, String login, String password, int serverId, int breed, int areaId) {
		super(instance, login, password, serverId, breed);
		this.fight = new FightAPI(this, areaId);
	}
	
	@Override
	public void run() {
		waitState(CharacterState.IS_LOADED); // attendre l'entrée en jeu
		checkIfModeratorIsOnline(Main.MODERATOR_NAME);
		this.social.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
		this.fight.updateFightArea(this.infos.level);
		
		if(inState(CharacterState.IN_FIGHT)) { // reprise de combat
			GameContextReadyMessage GCRM = new GameContextReadyMessage(); // je ne sais pas à quoi sert ce message
			GCRM.serialize(this.infos.currentMap.id);
			this.instance.outPush(GCRM);
			this.fight.fightManager(true);
		}
		
		while(!isInterrupted()) {
			waitState(CharacterState.IS_LOADED); // important
			
			// besoin de renaître au phénix ?
			this.fight.rebirthManager();
			
			// besoin de mettre à jour ses caractéristiques ou/et ses sorts ?
			this.fight.levelUpManager();
			
			// besoin d'aller voir la mule ?
			this.fight.inventoryManager();
			
			// besoin d'aller dans l'aire de combat ?
			this.mvt.goToArea(this.fight.getFightAreaId());
			
			// besoin de récupérer sa vie ?
			this.fight.lifeManager();
			
			while(!isInterrupted() && !inState(CharacterState.SHOULD_DECONNECT)) { // boucle recherche & combat
				if(this.fight.fightSearchManager()) { // lancement de combat
					if(waitState(CharacterState.IN_FIGHT)) { // on vérifie si le combat a bien été lancé (avec timeout)
						this.fight.fightManager(false);
						checkIfModeratorIsOnline(Main.MODERATOR_NAME);
						break;
					}
				}
				else
					this.mvt.changeMap();
			}
			
			if(inState(CharacterState.SHOULD_DECONNECT)) {
				this.instance.deconnectionOrder(true);
				break;
			}
		}
		Log.info("Thread controller of instance with id = " + this.instance.id + " terminated.");
		Controller.getInstance().threadTerminated();
	}
}