package controller.characters;

import gamedata.character.PlayerStatusEnum;
import gamedata.d2p.ankama.Map;

import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import controller.CharacterState;
import controller.api.FightAPI;
import main.Instance;
import main.Main;
import messages.context.GameContextReadyMessage;
import messages.parties.PartyInvitationRequestMessage;

public class Captain extends Fighter {
	private Lock lock;
	private Vector<Soldier> squad;
	private Vector<Soldier> recruits;
	private int teamLevel;
	private FightAPI fight;

	public Captain(Instance instance, String login, String password, int serverId, int breed, int areaId) {
		super(instance, login, password, serverId, breed);
		this.lock = new ReentrantLock();
		this.squad = new Vector<Soldier>(7);
		this.recruits = new Vector<Soldier>();
		this.teamLevel = 0;
		this.fight = new FightAPI(this, areaId);
	}

	@Override // indique à tous les soldats de l'escouade que le capitaine a changé de map
	public void updatePosition(Map map, int cellId) {
		super.updatePosition(map, cellId);
		broadcastEventToSquad();
	}
	
	public void updateTeamLevel() {
		this.fight.updateFightArea(++this.teamLevel);
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
			this.instance.log.p("Break for life regeneration.");
			try {
				Thread.sleep(this.infos.regenRate * 100 * maxMissingLife); // on attend de récupérer toute sa vie
			} catch(Exception e) {
				interrupt();
				return;
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
			this.instance.log.p("Need to empty inventory.");
			this.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
			broadcastEventToSquad();
			this.social.goToExchange(this.mule);
		}
	}

	// ajoute une nouvelle recrue à la liste des recrues en attente d'être recruté par le capitaine
	public void newRecruit(Soldier recruit) {
		this.lock.lock();
		this.recruits.add(recruit);
		this.lock.unlock();
		this.instance.log.p("Recruit " + recruit.infos.login + " added to the recruits list.");
	}

	// retire un soldat de l'escouade
	public void removeSoldierFromSquad(Soldier soldier) {
		this.lock.lock();
		this.squad.remove(soldier);
		this.lock.unlock();
		this.teamLevel -= soldier.infos.level;
		this.fight.updateFightArea(this.teamLevel);
		this.instance.log.p("Recruit " + soldier.infos.login + " removed from the squad.");
	}

	// parcourt la liste des recrues et les invite à rejoindre l'escouade (avec attente d'acceptation de l'invitation)
	private void integrateRecruits() {
		if(this.recruits.size() == 0)
			return;
		for(Soldier recruit : recruits) {
			recruit.waitState(CharacterState.IS_LOADED);
			this.lock.lock();
			this.squad.add(recruit);
			this.lock.unlock();
			recruit.setCaptain(this);
			this.teamLevel += recruit.infos.level;
			PartyInvitationRequestMessage PIRM = new PartyInvitationRequestMessage();
			PIRM.name = recruit.infos.characterName;
			PIRM.serialize();
			this.instance.outPush(PIRM);
			if(waitState(CharacterState.NEW_PARTY_MEMBER))
				this.instance.log.p("Recruit " + recruit.infos.characterName + " joined the party.");
		}
		this.recruits.clear();
		this.social.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK); // pour repasser en mode absent
		this.fight.updateFightArea(this.teamLevel);
	}

	// indique à tous les soldats de l'escouade que le capitaine a effectué une action
	private void broadcastEventToSquad() {
		for(Soldier soldier : squad)
			soldier.updateState(CharacterState.CAPTAIN_ACT, true);
	}

	private void waitSoldiersOnMap() {
		this.instance.log.p("Waiting for complete team be on the map.");
		for(Character soldier : this.squad)
			while(!this.roleplayContext.actorIsOnMap(soldier.infos.characterId))
				waitState(CharacterState.NEW_ACTOR_ON_MAP);
	}

	private void waitSoldiersInFight() {
		this.instance.log.p("Waiting for complete team be in the fight.");
		for(Character soldier : this.squad)
			while(!this.fightContext.inFight(soldier.infos.characterId))
				if(!waitState(CharacterState.NEW_ACTOR_IN_FIGHT)) // fin de la phase de préparation ou interruption
					return;
	}

	@Override
	public void run() {
		waitState(CharacterState.IS_LOADED); // attendre l'entrée en jeu
		checkIfModeratorIsOnline(Main.MODERATOR_NAME);
		this.teamLevel += this.infos.level;

		if(inState(CharacterState.IN_FIGHT)) { // reprise de combat
			GameContextReadyMessage GCRM = new GameContextReadyMessage(); // je ne sais pas à quoi sert ce message
			GCRM.serialize(this.infos.currentMap.id);
			this.instance.outPush(GCRM);
			this.fight.fightManager(true);
		}

		if(inState(CharacterState.IN_PARTY))
			this.social.leaveGroup();
		this.social.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
		this.fight.updateFightArea(this.teamLevel);

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

			while(!isInterrupted()) { // boucle recherche & combat
				waitSoldiersOnMap(); // on attend que l'escouade complète soit sur la map

				// combats
				if(this.fight.fightSearchManager()) {
					if(waitState(CharacterState.IN_FIGHT)) { // on vérifie si le combat a bien été lancé (avec timeout)
						broadcastEventToSquad();
						waitSoldiersInFight();
						this.fight.fightManager(false);
						checkIfModeratorIsOnline(Main.MODERATOR_NAME);
						break;
					}
				}
				else
					this.mvt.changeMap();
			}
		}
		System.out.println("Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
}