package controller;

import gamedata.d2p.ankama.Map;

import java.util.Vector;

import main.Instance;
import messages.context.GameContextReadyMessage;
import messages.parties.PartyInvitationRequestMessage;

public class CaptainController extends FighterController {
	private Vector<SoldierController> squad;
	private Vector<SoldierController> recruits;
	private int teamLevel;
	
	public CaptainController(Instance instance, String login, String password, int serverId) {
		super(instance, login, password, serverId);
		this.squad = new Vector<SoldierController>(7);
		this.recruits = new Vector<SoldierController>();
		this.teamLevel = 0;
	}
	
	protected synchronized void newRecruit(SoldierController recruit) {
		this.recruits.add(recruit);
		this.instance.log.p("Recruit " + recruit.infos.characterName + " added to the recruits list.");
	}
	
	private synchronized void integrateRecruits() {
		if(this.recruits.size() == 0)
			return;
		for(SoldierController recruit : recruits) {
			this.squad.add(recruit);
			recruit.captain = this;
			this.teamLevel += recruit.infos.level;
			PartyInvitationRequestMessage PIRM = new PartyInvitationRequestMessage();
			PIRM.name = recruit.infos.characterName;
			PIRM.serialize();
			this.instance.outPush(PIRM);
			waitState(CharacterState.NEW_PARTY_MEMBER);
			updateState(CharacterState.NEW_PARTY_MEMBER, false);
			this.instance.log.p("Recruit " + recruit.infos.characterName + " joined the party.");
		}
		this.recruits.clear();
	}
	
	public void updatePosition(Map map, int cellId) {
		super.updatePosition(map, cellId);
		broadcastEventToSquad();
	}
	
	private void broadcastEventToSquad() {
		for(SoldierController soldier : squad)
			soldier.updateState(CharacterState.CAPTAIN_ACT, true);
	}
	
	private void updateFightArea() {	
		if(teamLevel < 5) {
			this.areaId = 450;
			this.monsterGroupMaxSize = 2;
		}
		else if(teamLevel < 10) {
			this.areaId = 450;
			this.monsterGroupMaxSize = 3;
		}
		else if(teamLevel < 25) {
			this.areaId = 445;
			this.monsterGroupMaxSize = 3;
		}
		else if(teamLevel < 40) {
			this.areaId = 92;
			this.monsterGroupMaxSize = 2;
		}
		else if(teamLevel < 70) {
			this.areaId = 92;
			this.monsterGroupMaxSize = 3;
		}
		else if(teamLevel < 100) {
			this.areaId = 92;
			this.monsterGroupMaxSize = 5;
		}
		else if(teamLevel < 180) {
			this.areaId = 95;
			this.monsterGroupMaxSize = 4;
		}
		else if(teamLevel < 250) {
			this.areaId = 95;
			this.monsterGroupMaxSize = 5;
		}
		else {
			this.areaId = 95;
			this.monsterGroupMaxSize = 6;
		}
		this.mvt.pathfinding.updateArea(this.areaId);
		// 92 -> contour d'Astrub
		// 95 -> pious d'Astrub
		// 442 -> lac d'Incarnam
		// 445 -> pâturages d'Incarnam
		// 450 -> route des âmes d'Incarnam
	}
	
	private void emptyInventoryIfNecessary() {
		boolean need = false;
		if(inState(CharacterState.NEED_TO_EMPTY_INVENTORY))
			need = true;
		if(!need) {
			for(CharacterController soldier : this.squad)
				if(soldier.inState(CharacterState.NEED_TO_EMPTY_INVENTORY))
					need = true;
		}
		if(need) {
			this.instance.log.p("Need to empty inventory.");
			updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
			broadcastEventToSquad();
			waitState(CharacterState.MULE_AVAILABLE);
			goToExchangeWithMule(true);
		}
	}
	
	private void regenerateLife() {
		int currentMissingLife;
		int maxMissingLife = this.infos.missingLife();
		for(CharacterController teamMate : this.squad) {
			currentMissingLife = teamMate.infos.missingLife();
			if(currentMissingLife > maxMissingLife)
				maxMissingLife = currentMissingLife;
		}
		if(maxMissingLife > 0) {
			this.instance.log.p("Break for life regeneration.");
			try {
				sleep(this.infos.regenRate * 100 * maxMissingLife); // on attend de récupérer toute sa vie
			} catch(Exception e) {
				interrupt();
				return;
			}
		}		
	}
	
	private void waitSoldiers() {
		this.instance.log.p("Waiting for complete team be on the map.");
		for(CharacterController soldier : this.squad)
			while(!this.roleplayContext.actorIsOnMap(soldier.infos.characterId))
				waitState(CharacterState.NEW_ACTOR_ON_MAP);
	}

	private void waitSoldiersInFight() {
		this.instance.log.p("Waiting for complete team be in the fight.");
		for(CharacterController soldier : this.squad)
			while(!this.fightContext.inFight(soldier.infos.characterId)) {
				waitState(CharacterState.NEW_ACTOR_IN_FIGHT);
				if(inState(CharacterState.IN_GAME_TURN)) // fin de la phase de préparation
					return;
			}
	}
	
	public void run() {
		waitState(CharacterState.IS_LOADED);
		
		if(inState(CharacterState.IN_FIGHT)) { // reprise de combat
			GameContextReadyMessage GCRM = new GameContextReadyMessage(); // je ne sais pas à quoi sert ce message
			GCRM.serialize(this.infos.currentMap.id);
			this.instance.outPush(GCRM);
			fight(true);
		}
		
		changePlayerStatus();
		updateFightArea();
		
		while(!isInterrupted()) {
			waitState(CharacterState.IS_LOADED); // important
			
			// recrues en attente ?
			integrateRecruits();
			
			// besoin de mettre à jour ses caractéristiques ou/et ses sorts ?
			upgradeStatsAndSpell();
			
			// besoin d'aller voir la mule ?
			emptyInventoryIfNecessary();
			
			// besoin d'aller dans l'aire de combat ?
			this.mvt.goToArea(this.areaId);
			
			// besoin de récupérer sa vie ?
			regenerateLife();
			
			while(!isInterrupted()) { // boucle recherche & combat
				// on attend que l'escouade complète soit sur la map
				waitSoldiers();
				
				// combats
				if(lookForAndLaunchFight()) {
					if(waitState(CharacterState.IN_FIGHT)) { // on vérifie si le combat a bien été lancé (avec timeout)
						broadcastEventToSquad();
						waitSoldiersInFight();
						fight(false);
						break;
					}
				}
				else
					if(!isInterrupted())
						this.mvt.changeMap();
			}
		}
	}
}