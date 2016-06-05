package controller.characters;

import gamedata.enums.PlayerStatusEnum;
import controller.CharacterState;
import main.Log;
import messages.context.GameContextReadyMessage;

public class LoneFighter extends Character {

	public LoneFighter(int id, String login, String password, int serverId, int breed, int areaId, Log log) {
		super(id, login, password, serverId, breed, areaId, log);
	}
	
	@Override
	public void run() {
		super.run();
		waitState(CharacterState.IS_LOADED); // attendre l'entr�e en jeu
		changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
		this.fight.updateFightArea(this.infos.getLevel());
		
		if(inState(CharacterState.IN_FIGHT)) { // reprise de combat
			GameContextReadyMessage GCRM = new GameContextReadyMessage(); // je ne sais pas � quoi sert ce message
			GCRM.mapId = this.infos.getCurrentMap().id;
			this.net.send(GCRM);
			this.fight.fightManager(true);
		}
		
		while(!isInterrupted()) {
			// besoin de rena�tre au ph�nix ?
			this.fight.rebirthManager();
			
			// besoin de mettre � jour ses caract�ristiques ou/et ses sorts ?
			this.fight.levelUpManager();
			
			// besoin d'aller se d�charger � la taverne ou en banque ?
			this.fight.inventoryManager();
			
			// besoin d'aller revoir les prix � l'h�tel de vente ?
			this.salesManager.reviewBidHouseSales();
			
			// besoin d'aller dans l'aire de combat ?
			this.mvt.goToArea(this.fight.getFightAreaId());
			
			// besoin de r�cup�rer sa vie ?
			this.fight.lifeManager();
			
			// boucle recherche et combat
			while(!isInterrupted() && !inState(CharacterState.SHOULD_DECONNECT)) {
				// recherche et lancement de combat
				if(this.fight.fightSearchManager()) {
					// on v�rifie si le combat a bien �t� lanc� (avec timeout)
					if(waitState(CharacterState.IN_FIGHT)) {
						// combat
						this.fight.fightManager(false);
						break;
					}
				}
				else {
					//changement de map
					this.mvt.changeMap();
					
					// besoin de changer d'aire de combat ?
					this.fight.fightAreaReplacementManager();
				}
			}
			
			if(inState(CharacterState.SHOULD_DECONNECT)) {
				deconnectionOrder(true);
				break;
			}
		}
		Log.info("Thread controller of character with id = " + this.id + " terminated.");
		threadTerminated();
	}
}