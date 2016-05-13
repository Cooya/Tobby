package controller.characters;

import gamedata.enums.PlayerStatusEnum;
import controller.CharacterState;
import controller.api.FightAPI;
import main.Controller;
import main.Log;
import messages.context.GameContextReadyMessage;

public class LoneFighter extends Fighter {
	private FightAPI fight;

	public LoneFighter(int id, String login, String password, int serverId, int breed, int areaId, Log log) {
		super(id, login, password, serverId, breed, log);
		this.fight = new FightAPI(this, areaId);
	}
	
	@Override
	public void run() {
		waitState(CharacterState.IS_LOADED); // attendre l'entr�e en jeu
		this.social.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
		this.fight.updateFightArea(this.infos.getLevel());
		
		if(inState(CharacterState.IN_FIGHT)) { // reprise de combat
			GameContextReadyMessage GCRM = new GameContextReadyMessage(); // je ne sais pas � quoi sert ce message
			GCRM.mapId = this.infos.getCurrentMap().id;
			this.net.send(GCRM);
			this.fight.fightManager(true);
		}
		
		while(!isInterrupted()) {
			waitState(CharacterState.IS_LOADED); // important
			
			// besoin de rena�tre au ph�nix ?
			this.fight.rebirthManager();
			
			// besoin de mettre � jour ses caract�ristiques ou/et ses sorts ?
			this.fight.levelUpManager();
			
			// besoin d'aller voir la mule ?
			this.fight.inventoryManager();
			
			// besoin d'aller dans l'aire de combat ?
			this.mvt.goToArea(this.fight.getFightAreaId());
			
			// besoin de r�cup�rer sa vie ?
			this.fight.lifeManager();
			
			while(!isInterrupted() && !inState(CharacterState.SHOULD_DECONNECT)) { // boucle recherche & combat
				if(this.fight.fightSearchManager()) { // lancement de combat
					if(waitState(CharacterState.IN_FIGHT)) { // on v�rifie si le combat a bien �t� lanc� (avec timeout)
						this.fight.fightManager(false);
						break;
					}
				}
				else
					this.mvt.changeMap();
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