package main;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import main.AccountsManager.Account;
import controller.CharacterBehaviour;
import controller.characters.Character;
import controller.characters.Fighter;
import controller.characters.Mule;

public class CharactersManager {
	private Map<Integer, Character> characters;
	private Map<Integer, Mule> mules;
	private AccountsManager accounts;
	private SquadsManager squads;
	
	protected CharactersManager(AccountsManager accounts, SquadsManager squads) {
		this.characters = new HashMap<Integer, Character>();
		this.mules = new HashMap<Integer, Mule>();
		this.accounts = accounts;
		this.squads = squads;
	}
	
	protected void connectCharacter(Account account, int serverId, int areaId, CharacterFrame frame, int captainId) {
		if(account.serverId != 0) { // mule
			Mule mule = (Mule) Character.create(account.id, CharacterBehaviour.WAITING_MULE, account.login, account.password, serverId, areaId, new Log(account.login, frame));
			for(Character character : this.characters.values())
				if(character.infos.getServerId() == serverId)
					mule.newCustomer((Fighter) character); // un nouveau client est ajouté dans la liste des clients de la mule
			this.mules.put(serverId, mule);
		}
		else {
			Fighter fighter;
			if(captainId == -1) // loup solitaire (pas de capitaine)
				fighter = (Fighter) Character.create(account.id, CharacterBehaviour.LONE_WOLF, account.login, account.password, serverId, areaId, new Log(account.login, frame));
			else // combattants en groupe (capitaines ou soldats)
				fighter = this.squads.newSquadFighter(account, serverId, areaId, frame, captainId);
			if(this.mules.containsKey(serverId)) // si la mule est connectée
				this.mules.get(serverId).newCustomer(fighter); // un nouveau client est ajouté dans la liste des clients de la mule
			this.characters.put(account.id, fighter); // ajout dans la table des personnages
		}
		account.isConnected = true;
	}
	
	/*
	protected void reconnectCharacter(Character character) {
		character.log.p("Reconnecting character.");
		// TODO -> reset du Character à implémenter
	}

	protected void reconnectAllCharacters() {
		for(Character character : this.characters.values())
			if(character != null)
				reconnectCharacter(character);
	}
	*/
	
	// suppression de la table des personnages (pas de reconnexion donc)
	protected Character removeCharacter(int characterId) {
		Character character = this.characters.get(characterId);
		if(character == null)
			return null;
		if(this.mules.containsValue(character))
			this.mules.remove(character.infos.getServerId());
		else {
			int serverId = character.infos.getServerId();
			if(this.mules.containsKey(serverId)) // si la mule est connectée
				this.mules.get(serverId).removeCustomer(character); // on supprime le personnage de la liste de ses clients
			this.characters.remove(character);
			this.squads.removeSquadFighter(character); // si le perso appartient à une escouade
		}
		this.accounts.getAccount(characterId).isConnected = false;
		return character;
	}
	
	protected boolean muleIsConnected(int serverId) {
		return this.mules.containsKey(serverId);
	}
	
	protected Character getCharacter(int accountId) {
		return this.characters.get(accountId);
	}

	protected Character getCurrentCharacter() {
		Thread currentThread = Thread.currentThread();
		for(Character character : this.characters.values())
			if(character != null)
				for(Thread thread : character.threads)
					if(thread == currentThread)
						return character;
		return null;
	}
	
	protected Collection<Character> getConnectedCharacters() {
		Collection<Character> characters = new Vector<Character>();
		characters.addAll(this.characters.values());
		characters.addAll(this.mules.values());
		return characters;
	}
	
	protected Collection<Character> getInGameFighters() {
		Collection<Character> inGameCharacters = new Vector<Character>();
		for(Character character : this.characters.values())
			if(character.infos.isInGame())
				inGameCharacters.add(character);
		return inGameCharacters;
	}
}