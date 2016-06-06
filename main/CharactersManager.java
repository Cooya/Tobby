package main;

import gamedata.enums.ServerEnum;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import main.AccountsManager.Account;
import controller.CharacterBehaviour;
import controller.characters.Character;

public class CharactersManager extends Thread {
	private static CharactersManager self;
	private static Map<Integer, Boolean> serverAvailabilities = new HashMap<Integer, Boolean>();
	{
		for(int serverId : ServerEnum.getServerIdsList())
			serverAvailabilities.put(serverId, true);
	}
	
	private Map<Integer, Character> inGameCharacters;
	private TreeMap<Long, Character> connectionQueue;
	private List<Character> inProgressDeconnectionCharacters;
	private long insertionIndex;
	private Long connectionInProgress;
	
	private CharactersManager() {
		super("CharactersManager");
		this.inGameCharacters = new HashMap<Integer, Character>(50);
		this.connectionQueue = new TreeMap<Long, Character>();
		this.inProgressDeconnectionCharacters = new Vector<Character>(10);
		this.insertionIndex = 0;
		this.connectionInProgress = null;
		
		start();
	}
	
	public static CharactersManager getInstance() {
		if(self == null)
			self = new CharactersManager();
		return self;
	}
	
	// fonction principale pour la connexion d'un personnage
	protected void connectCharacter(Account account, int serverId, int areaId, int captainId) {
		if(!serverAvailabilities.get(serverId))
			return;
		if(captainId == -1) // loup solitaire (pas de capitaine)
			this.connectionQueue.put(this.insertionIndex++, Character.create(account.id, CharacterBehaviour.LONE_WOLF, account.login, account.password, serverId, areaId, new Log(account.login)));
		else // combattants en groupe (capitaines ou soldats)
			this.connectionQueue.put(this.insertionIndex++, SquadsManager.getInstance().newSquadFighter(account, serverId, areaId, captainId));
		synchronized(this) {
			if(this.connectionInProgress == null)
				notify();
		}
	}
	
	// connexion d'un personnage en ligne de commande via l'identifiant du compte
	protected void connectCharacter(int accountId, int serverId, int areaId) {
		Account account = AccountsManager.retrieveAccount(accountId);
		if(account == null) {
			Log.err("Invalid account id or account already used.");
			return;
		}
		connectCharacter(account, serverId, areaId, -1);
	}
	
	// connexion d'un personnage en ligne de commande via le login du compte
	protected void connectCharacter(String login, int serverId, int areaId) {
		Account account = AccountsManager.retrieveAccount(login);
		if(account == null) {
			Log.err("Invalid account id or account already used.");
			return;
		}
		connectCharacter(account, serverId, areaId, -1);
	}
	
	// connexion de plusieurs personnages en ligne de commande
	protected void connectCharacters(int number, int serverId, int areaId) {
		for(Account account : AccountsManager.retrieveAccounts(serverId, number))
			connectCharacter(account, serverId, areaId, -1);
	}
	
	// retourne un personnage en jeu via son identifiant
	protected Character getInGameCharacter(int accountId) {
		return this.inGameCharacters.get(accountId);
	}
	
	// retourne un personnage en jeu via son login
	protected Character getInGameCharacter(String login) {
		for(Character character : this.inGameCharacters.values())
			if(character.infos.getLogin() == login)
				return character;
		return null;
	}
	
	// retourne les personnages en jeu connectés sur un serveur spécifique ou sur tous les serveurs
	protected Character[] getInGameCharacters(int serverId) {
		if(serverId == 0)
			return this.inGameCharacters.values().toArray(new Character[this.inGameCharacters.size()]);
		else {
			Collection<Character> characters = new Vector<Character>();
			for(Character character : this.inGameCharacters.values())
				if(character.infos.getServerId() == serverId)
					characters.add(character);
			return characters.toArray(new Character[this.inGameCharacters.size()]);
		}
	}
	
	// retourne le personnage correspondant au thread courant
	public Character getCurrentCharacter() {
		Thread currentThread = Thread.currentThread();
		
		synchronized(this) {
			if(this.connectionInProgress != null) {
				Character coCharacter = this.connectionQueue.get(this.connectionInProgress);
				if(currentThread == coCharacter || currentThread == coCharacter.net)
					return coCharacter;
			}
		}
		
		for(Character character : this.inGameCharacters.values())
			if(currentThread == character || currentThread == character.net)
				return character;
		
		return null;
	}
	
	// détermine si l'identifiant du personnage passé en paramètre est un id de personnage de l'application
	public boolean isWorkmate(double characterId) {
		for(Character character : this.inGameCharacters.values())
			if(character.infos.getCharacterId() == characterId)
				return true;
		return false;
	}
	
	// fonction appelée à la connexion en jeu ou à l'échec de la connexion d'un personnage
	public void connectionCallback(ConnectionResult status, String str) {
		Character character;
		Long characterTime;
		synchronized(this) {
			character = this.connectionQueue.get(this.connectionInProgress);
			characterTime = this.connectionInProgress;
			this.connectionInProgress = null;
		}
		switch(status) {
			case SUCCESS :
				character.log.p(str);
				this.inGameCharacters.put(character.id, character);
				this.connectionQueue.remove(characterTime);
				break;
			case SERVER_SAVING :
				deconnectCharacter(character, str, true, false);
				int characterServerId = character.infos.getServerId();
				long nextCoTime = System.currentTimeMillis() + 1000 * 60 * 5;
				for(Long time : this.connectionQueue.keySet().toArray(new Long[this.connectionQueue.size()])) {
					character = this.connectionQueue.get(time);
					if(character.infos.getServerId() == characterServerId) {
						this.connectionQueue.remove(time);
						this.connectionQueue.put(nextCoTime++, character);
					}
				}
				break;
			case SERVER_OFFLINE_OR_FULL :
				deconnectCharacter(character, str, true, false);
				characterServerId = character.infos.getServerId();
				for(Long time : this.connectionQueue.keySet().toArray(new Long[this.connectionQueue.size()])) {
					character = this.connectionQueue.get(time);
					if(character.infos.getServerId() == characterServerId)
						this.connectionQueue.remove(time);
				}
				break;
			case UNKNOWN :
			case SERVER_UNSELECTABLE :
			case ACCOUNT_BANNED_DEFINITIVELY :
			case ACCOUNT_BANNED_TEMPORARILY :
			case AUTHENTIFICATION_FAILED :
				deconnectCharacter(character, str, true, false);
				this.connectionQueue.remove(characterTime);
				break;
		}
		synchronized(this) {
			notify();
		}
	}
	
	// fonction appelée à la déconnexion complète d'un personnage
	public void deconnectionCallback(Character character) {
		this.inProgressDeconnectionCharacters.remove(character);
		if(!this.connectionQueue.containsValue(character))
			DatabaseConnection.unlockAccount(character.id);
		Log.info("Character with id = " + character.id + " deconnected, " + this.inProgressDeconnectionCharacters.size() + " remaining(s).");
		synchronized(this) {
			notify();
		}
	}
	
	// déconnexion d'un personnage via son identifiant (appel de la fonction principale de déconnexion)
	protected void deconnectCharacter(int characterId, String reason, boolean forced, boolean reconnection) {
		synchronized(this) {
			if(this.connectionInProgress != null) {
				Character character = this.connectionQueue.get(this.connectionInProgress);
				if(character.id == characterId) {
					deconnectCharacter(character, reason, forced, reconnection);
					return;
				}
			}
		}
		deconnectCharacter(this.inGameCharacters.get(characterId), reason, forced, reconnection);	
	}
	
	// fonction principale de déconnexion des personnages
	// TODO prise en considération du booléen de reconnexion
	public void deconnectCharacter(Character character, String reason, boolean forced, boolean reconnection) {
		if(character == null) {
			Log.err("Character is not connected or does not exist.");
			return;
		}
		else
			Log.info("Deconnecting character with id = " + character.id + "...");
		character.log.p(reason);
		character.log.flushBuffer();
		synchronized(this) {
			if(this.connectionInProgress != null) {
				if(character.id == this.connectionQueue.get(this.connectionInProgress).id) {
					this.connectionQueue.remove(this.connectionInProgress);
					this.connectionInProgress = null;
					notify();
					return;
				}
			}
		}
		character = this.inGameCharacters.remove(character.id);
		this.inProgressDeconnectionCharacters.add(character);
		character.deconnectionOrder(forced);
	}
	
	// déconnexion de tous les personnages connectés (sur un serveur spécifié ou sur tous)
	public synchronized void deconnectCharacters(String reason, int serverId, boolean forced, boolean reconnection) {
		if(serverId == 0) {
			Log.info("Deconnecting all connected characters...");
			for(int id : serverAvailabilities.keySet())
				serverAvailabilities.put(id, false);
			if(this.connectionInProgress != null)
				deconnectCharacter(this.connectionQueue.get(this.connectionInProgress), reason, forced, reconnection);
			for(Iterator<Character> it = this.connectionQueue.values().iterator(); it.hasNext(); it.next())
				it.remove();
		}
		else {
			Log.info("Deconnecting all connected characters on server " + ServerEnum.getServerName(serverId) + ".");
			serverAvailabilities.put(serverId, false);
			if(this.connectionInProgress != null) {
				Character coCharacter = this.connectionQueue.get(this.connectionInProgress);
				if(coCharacter.infos.getServerId() == serverId)
					deconnectCharacter(coCharacter, reason, forced, reconnection);
			}
			for(Iterator<Character> it = this.connectionQueue.values().iterator(); it.hasNext();)
				if(it.next().infos.getServerId() == serverId)
					it.remove();
		}
		for(Character character : getInGameCharacters(serverId))
			deconnectCharacter(character, reason, forced, reconnection);
	}
	
	// thread connecteur de personnages
	@Override
	public synchronized void run() {
		Entry<Long, Character> firstEntry;
		Character character;
		long currentTime;
		long characterCoTime;
		long timeToWait = 0;
		
		while(true) {
			try {
				wait(timeToWait);
			} catch(InterruptedException e) {
				return;
			}
			if(Main.exitAsked() && this.inProgressDeconnectionCharacters.isEmpty()) {
				Main.exit(null);
				return;
			}
			timeToWait = 0;
			if(this.inProgressDeconnectionCharacters.isEmpty() && this.connectionInProgress == null && (firstEntry = this.connectionQueue.firstEntry()) != null) {
				currentTime = System.currentTimeMillis();
				characterCoTime = firstEntry.getKey();
				if(currentTime >= characterCoTime) {
					this.connectionInProgress = characterCoTime;
					character = firstEntry.getValue();
					if(!character.alreadyRun())
						character.connect();
					else {
						character = Character.clone(character);
						this.connectionQueue.put(characterCoTime, character);
						character.connect();
					}
				}
				else
					timeToWait = characterCoTime - currentTime;
			}
		}
	}
}