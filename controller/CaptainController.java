package controller;

import gamedata.character.PlayerStatusEnum;
import gamedata.d2p.ankama.Map;

import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import main.Instance;
import main.Main;
import messages.context.GameContextReadyMessage;
import messages.parties.PartyInvitationRequestMessage;

public class CaptainController extends FighterController {
	private Vector<SoldierController> squad;
	private Vector<SoldierController> recruits;
	private int teamLevel;
	private static Lock lock = new ReentrantLock();

	public CaptainController(Instance instance, String login, String password, int serverId, int areaId) {
		super(instance, login, password, serverId, areaId);
		this.squad = new Vector<SoldierController>(7);
		this.recruits = new Vector<SoldierController>();
		this.teamLevel = 0;
	}

	@Override // indique à tous les soldats de l'escouade que le capitaine a changé de map
	public void updatePosition(Map map, int cellId) {
		super.updatePosition(map, cellId);
		broadcastEventToSquad();
	}

	// "évènement" émis depuis les soldats
	protected void soldierHasLevelUp() {
		setFightArea(++this.teamLevel);
	}

	// ajoute une nouvelle recrue à la liste des recrues en attente d'être recruté par le capitaine
	public synchronized void newRecruit(SoldierController recruit) {
		lock.lock();
		this.recruits.add(recruit);
		this.instance.log.p("Recruit " + recruit.infos.login + " added to the recruits list.");
		lock.unlock();
	}

	// retire un soldat de l'escouade
	public synchronized void removeSoldierFromSquad(SoldierController soldier) {
		lock.lock();
		this.squad.remove(soldier);
		this.instance.log.p("Recruit " + soldier.infos.login + " removed from the squad.");
		lock.unlock();
	}

	// parcourt la liste des recrues et les invite à rejoindre l'escouade (avec attente d'acceptation de l'invitation)
	private synchronized void integrateRecruits() {
		lock.lock();
		if(this.recruits.size() == 0)
			return;
		for(SoldierController recruit : recruits) {
			recruit.waitState(CharacterState.IS_LOADED);
			this.squad.add(recruit);
			recruit.captain = this;
			this.teamLevel += recruit.infos.level;
			PartyInvitationRequestMessage PIRM = new PartyInvitationRequestMessage();
			PIRM.name = recruit.infos.characterName;
			PIRM.serialize();
			this.instance.outPush(PIRM);
			if(waitState(CharacterState.NEW_PARTY_MEMBER))
				this.instance.log.p("Recruit " + recruit.infos.characterName + " joined the party.");
		}
		this.recruits.clear();
		lock.unlock();
		changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK); // pour repasser en mode absent
		setFightArea(this.teamLevel);
	}

	// indique à tous les soldats de l'escouade que le capitaine a effectué une action
	private void broadcastEventToSquad() {
		for(SoldierController soldier : squad)
			soldier.updateState(CharacterState.CAPTAIN_ACT, true);
	}

	@Override
	protected void levelUpManager() {
		if(!waitState(CharacterState.LEVEL_UP))
			return;
		waitState(CharacterState.IS_LOADED);
		upgradeSpell();
		increaseStats();
		setFightArea(++this.teamLevel);
	}

	@Override
	protected void emptyInventoryIfNecessary() {
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
			goToExchangeWithMule();
		}
	}

	@Override
	protected void regenerateLife() {
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
			fight(true);
		}

		if(inState(CharacterState.IN_PARTY))
			leaveGroup();
		changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
		setFightArea(this.teamLevel);

		while(!isInterrupted()) {
			waitState(CharacterState.IS_LOADED); // important

			// recrues en attente ?
			integrateRecruits();

			// besoin de renaître au phénix ?
			riseIfNecessary();

			// besoin de mettre à jour ses caractéristiques ou/et ses sorts ?
			levelUpManager();

			// besoin d'aller voir la mule ?
			emptyInventoryIfNecessary();

			// besoin d'aller dans l'aire de combat ?
			this.mvt.goToArea(this.areaId);

			// besoin de récupérer sa vie ?
			regenerateLife();

			while(!isInterrupted()) { // boucle recherche & combat
				waitSoldiers(); // on attend que l'escouade complète soit sur la map

				// combats
				if(lookForAndLaunchFight()) {
					if(waitState(CharacterState.IN_FIGHT)) { // on vérifie si le combat a bien été lancé (avec timeout)
						broadcastEventToSquad();
						waitSoldiersInFight();
						fight(false);
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