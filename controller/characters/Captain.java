package controller.characters;

import gamedata.d2p.ankama.Map;
import gamedata.enums.PlayerStatusEnum;
import gui.Controller;

import java.util.Vector;

import controller.CharacterState;
import controller.api.FightAPI;
import main.Log;
import messages.context.GameContextReadyMessage;
import messages.parties.PartyInvitationRequestMessage;

public class Captain extends Fighter {
	private Vector<Soldier> recruits;
	private FightAPI fight;
	private Vector<Soldier> squad;

	public Captain(int id, String login, String password, int serverId, int breed, int areaId, Log log) {
		super(id, login, password, serverId, breed, log);
		this.recruits = new Vector<Soldier>();
		this.fight = new FightAPI(this, areaId);
	}

	@Override // indique à tous les soldats de l'escouade que le capitaine a changé de map
	public void updatePosition(Map map, int cellId) {
		super.updatePosition(map, cellId);
		broadcastStateUpdate();
	}
	
	public void updateFightArea(int teamLevel) {
		this.fight.updateFightArea(teamLevel);
	}
	
	private void lifeManager() {
		int currentMissingLife;
		int maxMissingLife = this.infos.missingLife();
		for(Character teamMate : this.squad) {
			currentMissingLife = teamMate.infos.missingLife();
			if(currentMissingLife > maxMissingLife)
				maxMissingLife = currentMissingLife;
		}
		if(maxMissingLife > 0) {
			this.log.p("Break for life regeneration, " + maxMissingLife + " life points missing.");
			waitState(CharacterState.IN_REGENERATION, this.infos.regenRate * 100 * maxMissingLife); // à peu près
			this.infos.stats.lifePoints = this.infos.stats.maxLifePoints;
			this.log.graphicalFrame.setLifeLabel(this.infos.stats.lifePoints, this.infos.stats.maxLifePoints);
			for(Character teamMate : this.squad) { // update de la vie pour toute l'escouade
				teamMate.infos.stats.lifePoints = teamMate.infos.stats.maxLifePoints;
				teamMate.log.graphicalFrame.setLifeLabel(teamMate.infos.stats.lifePoints, teamMate.infos.stats.maxLifePoints);
			}
		}
	}
	
	private void inventoryManager() {
		boolean need = false;
		if(this.inState(CharacterState.NEED_TO_EMPTY_INVENTORY))
			need = true;
		if(!need) {
			for(Character soldier : this.squad)
				if(soldier.inState(CharacterState.NEED_TO_EMPTY_INVENTORY))
					need = true;
		}
		if(need) {
			this.log.p("Need to empty inventory.");
			broadcastStateUpdate();
			
			// vérification du poids de l'inventaire
			if(inventoryIsSoHeavy(0.1f)) {
				this.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
				this.social.goToExchangeWithMule();
			}
		}
	}

	// ajoute une nouvelle recrue à la liste des recrues en attente d'être recruté par le capitaine
	public void newRecruit(Soldier recruit) {
		this.recruits.add(recruit);
		recruit.setCaptain(this);
		this.log.p("Recruit " + recruit.infos.login + " added to the recruits list.");
	}

	// parcourt la liste des recrues et les invite à rejoindre l'escouade (avec attente d'acceptation de l'invitation)
	private void integrateRecruits() {
		if(this.recruits.size() == 0)
			return;
		for(Soldier recruit : recruits) {
			while(!recruit.waitingPartyInvitation)
				waitState(CharacterState.SOLDIER_ACT);
			
			// envoi d'une invitation à rejoindre le groupe
			PartyInvitationRequestMessage PIRM = new PartyInvitationRequestMessage();
			PIRM.name = recruit.infos.characterName;
			this.net.send(PIRM);
			
			// attente de l'acceptation de l'invitation
			waitState(CharacterState.NEW_PARTY_MEMBER);
		}
		this.recruits.clear();
		this.social.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK); // pour repasser en mode absent
	}

	// indique à tous les soldats de l'escouade que le capitaine a effectué une action
	private void broadcastStateUpdate() {
		for(Soldier soldier : squad)
			soldier.updateState(CharacterState.CAPTAIN_ACT, true);
	}

	private void waitSoldiersOnMap() {
		this.log.p("Waiting for complete team be on the map.");
		for(Soldier soldier : this.squad) {
			while(!this.roleplayContext.actorIsOnMap(soldier.infos.characterId))
				waitState(CharacterState.NEW_ACTOR_ON_MAP);
			while(!soldier.readyForFight)
				waitState(CharacterState.SOLDIER_ACT);
		}
	}

	private void waitSoldiersInFight() {
		this.log.p("Waiting for complete team be in the fight.");
		for(Character soldier : this.squad)
			while(!this.fightContext.inFight(soldier.infos.characterId))
				if(!waitState(CharacterState.NEW_ACTOR_IN_FIGHT)) // fin de la phase de préparation ou interruption
					return;
	}

	@Override
	public void run() {
		waitState(CharacterState.IS_LOADED); // attendre l'entrée en jeu
		this.fight.updateFightArea(this.infos.level);

		if(inState(CharacterState.IN_FIGHT)) { // reprise de combat
			GameContextReadyMessage GCRM = new GameContextReadyMessage(); // je ne sais pas à quoi sert ce message
			GCRM.mapId = this.infos.currentMap.id;
			this.net.send(GCRM);
			this.fight.fightManager(true);
		}

		if(inState(CharacterState.IN_PARTY))
			this.partyManager.leaveParty();
		this.social.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);

		while(!isInterrupted()) {
			waitState(CharacterState.IS_LOADED); // important

			// recrues en attente ?
			integrateRecruits();

			// besoin de renaître au phénix ?
			this.fight.rebirthManager();

			// besoin de mettre à jour ses caractéristiques ou/et ses sorts ?
			this.fight.levelUpManager();

			// besoin d'aller voir la mule ?
			inventoryManager();

			// besoin d'aller dans l'aire de combat ?
			this.mvt.goToArea(this.fight.getFightAreaId());

			// besoin de récupérer sa vie ?
			lifeManager();

			while(!isInterrupted() && !inState(CharacterState.SHOULD_DECONNECT)) { // boucle recherche & combat
				waitSoldiersOnMap(); // on attend que l'escouade complète soit sur la map

				// combats
				if(this.fight.fightSearchManager()) {
					if(waitState(CharacterState.IN_FIGHT)) { // on vérifie si le combat a bien été lancé (avec timeout)
						broadcastStateUpdate();
						waitSoldiersInFight();
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