package main;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import main.AccountsManager.Account;
import controller.CharacterBehaviour;
import controller.characters.Character;

public class CharactersManager {
	private Map<Integer, Character> characters;
	private AccountsManager accounts;
	private SquadsManager squads;
	
	protected CharactersManager(AccountsManager accounts, SquadsManager squads) {
		this.characters = new HashMap<Integer, Character>();
		this.accounts = accounts;
		this.squads = squads;
	}
	
	protected void connectCharacter(Account account, int serverId, int areaId, CharacterFrame frame, int captainId) {
		Character character;
		if(captainId == -1) // loup solitaire (pas de capitaine)
			character = Character.create(account.id, CharacterBehaviour.LONE_WOLF, account.login, account.password, serverId, areaId, new Log(account.login, frame));
		else // combattants en groupe (capitaines ou soldats)
			character = this.squads.newSquadFighter(account, serverId, areaId, frame, captainId);
		this.characters.put(account.id, character); // ajout dans la table des personnages
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
		this.characters.remove(character);
		this.squads.removeSquadFighter(character); // si le perso appartient à une escouade
		this.accounts.updateAccountConnectionStatus(characterId, false);
		return character;
	}
	
	protected Character getCharacter(int accountId) {
		return this.characters.get(accountId);
	}
	
	protected Character getCharacter(String login) {
		for(Character character : this.characters.values())
			if(character.infos.getLogin() == login)
				return character;
		return null;
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
	
	protected Collection<Character> getConnectedCharacters(int serverId) {
		if(serverId == 0)
			return this.characters.values();
		else {
			Collection<Character> characters = new Vector<Character>();
			for(Character character : this.characters.values())
				if(character.infos.getServerId() == serverId)
					characters.add(character);
			return characters;
		}
	}
	
	protected Collection<Character> getInGameFighters() {
		Collection<Character> inGameCharacters = new Vector<Character>();
		for(Character character : this.characters.values())
			if(character.infos.isInGame())
				inGameCharacters.add(character);
		return inGameCharacters;
	}
}