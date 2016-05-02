package gui;

import java.util.Collection;
import java.util.Vector;

import utilities.BiStruct;
import utilities.BiVector;
import controller.CharacterBehaviour;
import controller.characters.Character;
import controller.characters.Fighter;
import controller.characters.Mule;
import main.Log;

class Model {
	private BiStruct<Account, Character> characters;
	protected SquadsManager squads;
	private Character mule;

	protected Model() {
		this.characters = new BiVector<Account, Character>(Account.class, Character.class);
		this.squads = new SquadsManager(this);
		this.mule = null;
	}

	// crée, stocke et lance les personnages
	protected void createCharacter(Account account, int areaId, CharacterFrame frame, Account captain) {
		if(isConnected(account)) {
			Log.info("Character already connected.");
			return;
		}
		
		account.lastAreaId = areaId; // pour la reconnexion automatique
		Character newCharacter;
		if(account.behaviour == CharacterBehaviour.WAITING_MULE) {
			newCharacter = Character.create(account.id, account.behaviour, account.login, account.password, account.serverId, areaId, new Log(account.login, frame));
			for(Character character : this.characters.values())
				if(character != null)
					((Mule) newCharacter).newCustomer((Fighter) character); // un nouveau client est ajouté dans la liste des clients de la mule
			this.mule = newCharacter;
		}
		else if(account.behaviour == CharacterBehaviour.TRAINING_MULE || account.behaviour == CharacterBehaviour.LONE_WOLF) {
			newCharacter = Character.create(account.id, account.behaviour, account.login, account.password, account.serverId, areaId, new Log(account.login, frame));
			if(this.mule != null) // si la mule est connectée
				((Mule) this.mule).newCustomer((Fighter) newCharacter); // un nouveau client est ajouté dans la liste des clients de la mule
		}
		else if(account.behaviour == CharacterBehaviour.SELLER)
			newCharacter = null; // à implémenter
		else { // combattants en groupe (capitaines ou soldats)
			newCharacter = this.squads.newSquadFighter(account, areaId, frame, captain);
			if(this.mule != null) // si la mule est connectée
				((Mule) this.mule).newCustomer((Fighter) newCharacter); // un nouveau client est ajouté dans la liste des clients de la mule
		}
		this.characters.put(account, newCharacter); // ajout dans la table des personnages
	}

	protected void reconnectCharacter(Character character) {
		character.log.p("Reconnecting character.");
		Account account = null;
		for(Account acc : this.characters.keys())
			if(acc.id == character.id)
				account = acc;
		if(account == null)
			return;
		character = Character.create(account.id, account.behaviour, account.login, account.password, account.serverId, account.lastAreaId, character.log);
		if(account.behaviour != CharacterBehaviour.WAITING_MULE && this.mule != null)
			((Mule) this.mule).newCustomer((Fighter) character); // un nouveau client est ajouté dans la liste des clients de la mule
		this.characters.put(account, character); // remplacement dans la table des personnages
	}

	protected void reconnectAllCharacters() {
		for(Character character : this.characters.values())
			if(character != null)
				reconnectCharacter(character);
	}

	protected Vector<Character> getConnectedCharacters() {
		Vector<Character> characters = new Vector<Character>();
		for(Character character : this.characters.values())
			if(character != null)
				characters.add(character);
		return characters;
	}
	
	protected boolean isConnected(Account account) {
		return characters.get(account) != null;
	}
	
	protected boolean muleIsConnected() {
		return this.mule != null;
	}
	
	protected Character getCharacter(Account account) {
		return (Character) this.characters.get(account);
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

	protected Character removeCharacter(int characterId) {
		boolean isMule = false;
		Character character = null;
		for(Account account : this.characters.keys())
			if(account.id == characterId) {
				character = (Character) this.characters.get(account);
				this.characters.put(account, null);
				isMule = account.behaviour == CharacterBehaviour.WAITING_MULE;
				break;
			}
		if(isMule)
			this.mule = null;
		else {
			if(this.mule != null) // si la mule est connectée, on supprime le personnage de la liste de ses clients
				((Mule) this.mule).removeCustomer(character);
			this.squads.removeSquadFighter(character);
		}
		return character;
	}

	protected Account createAccount(String accountLine) {
		String[] splitLine = accountLine.split(" ");
		Account account = new Account(this.characters.size(), Integer.valueOf(splitLine[0]), splitLine[1], splitLine[2], Integer.valueOf(splitLine[3]));
		this.characters.put(account, null);
		return account;
	}

	protected Account createAccount(int behaviour, String login, String password, int serverId) {
		Account account = new Account(this.characters.size(), behaviour, login, password, serverId);
		this.characters.put(account, null);
		return account;
	}
	
	protected Account getAccount(String login) {
		Collection<Account> accounts = this.characters.keys();
		for(Account account : accounts)
			if(account.login.equals(login))
				return account;
		return null;
	}
	
	protected Account getMuleFromAccountsList() {
		Collection<Account> accounts = this.characters.keys();
		for(Account account : accounts)
			if(account.behaviour == CharacterBehaviour.WAITING_MULE)
				return account;
		return null;
	}

	protected Vector<Account> getAllAccounts() {
		Vector<Account> accounts = new Vector<Account>();
		Collection<Account> accountsSet = this.characters.keys();
		for(Account account : accountsSet)
			accounts.add(account);
		return accounts;
	}

	protected static class Account {
		protected int id;
		protected int behaviour;
		protected String login;
		protected String password;
		protected int serverId;
		protected int lastAreaId;

		protected Account(int id, int behaviour, String login, String password, int serverId) {
			this.id = id;
			this.behaviour = behaviour;
			this.login = login;
			this.password = password;
			this.serverId = serverId;
		}
	}
}