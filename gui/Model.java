package gui;

import java.util.Collection;
import java.util.Vector;

import utilities.BiStruct;
import utilities.BiVector;
import controller.CharacterBehaviour;
import controller.characters.Fighter;
import controller.characters.Mule;
import main.Instance;
import main.Log;

class Model {
	private BiStruct<Account, Instance> instances;
	protected SquadsManager squads;
	private Instance mule;

	protected Model() {
		this.instances = new BiVector<Account, Instance>(Account.class, Instance.class);
		this.squads = new SquadsManager(this);
		this.mule = null;
	}

	// crée, stocke et lance les instances
	protected void createInstance(Account account, int areaId, CharacterFrame frame, Account captain) {
		if(isConnected(account)) {
			Log.info("Instance already connected.");
			return;
		}
		
		account.lastAreaId = areaId; // pour la reconnexion automatique
		Instance newInstance;
		if(account.behaviour == CharacterBehaviour.WAITING_MULE) {
			newInstance = new Instance(account.id, account.behaviour, account.login, account.password, account.serverId, areaId, frame);
			for(Instance instance : this.instances.values())
				if(instance != null)
					((Mule) newInstance.getCharacter()).newCustomer((Fighter) instance.getCharacter()); // un nouveau client est ajouté dans la liste des clients de la mule
			this.mule = newInstance;
		}
		else if(account.behaviour == CharacterBehaviour.TRAINING_MULE || account.behaviour == CharacterBehaviour.LONE_WOLF) {
			newInstance = new Instance(account.id, account.behaviour, account.login, account.password, account.serverId, areaId, frame);
			if(this.mule != null) // si la mule est connectée
				((Mule) this.mule.getCharacter()).newCustomer((Fighter) newInstance.getCharacter()); // un nouveau client est ajouté dans la liste des clients de la mule
		}
		else if(account.behaviour == CharacterBehaviour.SELLER)
			newInstance = null; // à implémenter
		else { // combattants en groupe (capitaines ou soldats)
			newInstance = this.squads.newSquadFighter(account, areaId, frame, captain);
			if(this.mule != null) // si la mule est connectée
				((Mule) this.mule.getCharacter()).newCustomer((Fighter) newInstance.getCharacter()); // un nouveau client est ajouté dans la liste des clients de la mule
		}
		this.instances.put(account, newInstance); // ajout dans la table des instances
	}

	private void restartInstance(Instance instance) {
		instance.log.p("Restarting instance.");
		Account account = null;
		for(Account acc : this.instances.keys())
			if(acc.id == instance.id)
				account = acc;
		if(account == null)
			return;
		instance = new Instance(account.id, account.behaviour, account.login, account.password, account.serverId, account.lastAreaId, instance.log);
		if(account.behaviour != CharacterBehaviour.WAITING_MULE && this.mule != null)
			((Mule) this.mule.getCharacter()).newCustomer((Fighter) instance.getCharacter()); // un nouveau client est ajouté dans la liste des clients de la mule
		this.instances.put(account, instance); // remplacement dans la table des instances
	}

	protected void restartAllInstances() {
		for(Instance instance : this.instances.values())
			if(instance != null)
				restartInstance(instance);
	}

	protected Vector<Instance> getConnectedInstances() {
		Vector<Instance> instances = new Vector<Instance>();
		for(Instance instance : this.instances.values())
			if(instance != null)
				instances.add(instance);
		return instances;
	}
	
	protected boolean isConnected(Account account) {
		return instances.get(account) != null;
	}
	
	protected boolean muleIsConnected() {
		return this.mule != null;
	}
	
	protected Instance getInstance(Account account) {
		return (Instance) this.instances.get(account);
	}

	protected Instance getCurrentInstance() {
		Thread currentThread = Thread.currentThread();
		for(Instance instance : this.instances.values())
			if(instance != null)
				for(Thread thread : instance.threads)
					if(thread == currentThread)
						return instance;
		return null;
	}

	protected Instance removeInstance(int instanceId) {
		boolean isMule = false;
		Instance instance = null;
		for(Account account : this.instances.keys())
			if(account.id == instanceId) {
				instance = (Instance) this.instances.get(account);
				this.instances.put(account, null);
				isMule = account.behaviour == CharacterBehaviour.WAITING_MULE;
				break;
			}
		if(isMule)
			this.mule = null;
		else {
			if(this.mule != null) // si la mule est connectée, on supprime l'instance de la liste de ses clients
				((Mule) this.mule.getCharacter()).removeCustomer(instance.getCharacter());
			this.squads.removeSquadFighter(instance);
		}
		return instance;
	}

	protected Account createAccount(String accountLine) {
		String[] splitLine = accountLine.split(" ");
		Account account = new Account(this.instances.size(), Integer.valueOf(splitLine[0]), splitLine[1], splitLine[2], Integer.valueOf(splitLine[3]));
		this.instances.put(account, null);
		return account;
	}

	protected Account createAccount(int behaviour, String login, String password, int serverId) {
		Account account = new Account(this.instances.size(), behaviour, login, password, serverId);
		this.instances.put(account, null);
		return account;
	}
	
	protected Account getAccount(String login) {
		Collection<Account> accounts = this.instances.keys();
		for(Account account : accounts)
			if(account.login.equals(login))
				return account;
		return null;
	}
	
	protected Account getMuleFromAccountsList() {
		Collection<Account> accounts = this.instances.keys();
		for(Account account : accounts)
			if(account.behaviour == CharacterBehaviour.WAITING_MULE)
				return account;
		return null;
	}

	protected Vector<Account> getAllAccounts() {
		Vector<Account> accounts = new Vector<Account>();
		Collection<Account> accountsSet = this.instances.keys();
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