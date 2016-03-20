package gui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import main.FatalError;
import main.Instance;

public class Model {
	private static final int MAX_GROUP_SIZE = 8;
	private HashMap<Account, Instance> instances;
	private Instance mule;
	private List<Vector<Instance>> fightGroups;
	private int incompleteFightGroupIndex;
	
	protected Model() {
		this.instances = new HashMap<Account, Instance>(); 
		this.mule = null;
		this.fightGroups = new LinkedList<Vector<Instance>>();
		this.fightGroups.add(new Vector<Instance>());
		this.incompleteFightGroupIndex = 0;
	}
	
	protected void createMuleInstance(Account account, CharacterFrame frame) {
		if(account.type != 0)
			throw new FatalError("Invalid account type.");
		Instance newInstance = new Instance(account.id, account.type, account.login, account.password, account.serverId, 0, frame);
		for(Instance instance : this.instances.values())
			if(instance != null)
				instance.setMule(newInstance);
		this.mule = newInstance;
		this.instances.put(account, newInstance);
	}
	
	protected void createFighterInstance(Account account, int areaId, CharacterFrame frame) {
		if(account.type == 0)
			throw new FatalError("Invalid account type.");
		Instance newInstance;
		if(account.type == 1) // combattant solitaire
			newInstance = new Instance(account.id, account.type, account.login, account.password, account.serverId, areaId, frame);
		else // combattant en groupe
			newInstance = newFighter(account, areaId, frame);
		if(this.mule != null) // mule connectée
			newInstance.setMule(this.mule);
		this.instances.put(account, newInstance);
	}
	
	protected Vector<Instance> getConnectedInstances() {
		Vector<Instance> instances = new Vector<Instance>();
		for(Instance instance : this.instances.values())
			if(instance != null)
				instances.add(instance);
		return instances;
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
		for(Account account : this.instances.keySet())
			if(account.id == instanceId) {
				instance = this.instances.get(account);
				this.instances.put(account, null);
				isMule = account.type == 0;
				break;
			}
		if(isMule) {
			for(Instance fighter : this.instances.values())
				if(fighter != null)
					fighter.setMule(null);
			this.mule = null;
		}
		else
			removeFighter(instance);
		return instance;
	}
	
	protected Account createAccount(String accountLine) {
		String[] splitLine = accountLine.split(" ");
		Account account = new Account(this.instances.size(), Integer.valueOf(splitLine[0]), splitLine[1], splitLine[2], Integer.valueOf(splitLine[3]));
		this.instances.put(account, null);
		return account;
	}
	
	protected Account createAccount(int type, String login, String password, int serverId) {
		Account account = new Account(this.instances.size(), type, login, password, serverId);
		this.instances.put(account, null);
		return account;
	}
	
	protected Account getAccount(String login) {
		for(Account account : this.instances.keySet())
			if(account.login.equals(login))
				return account;
		return null;
	}
	
	private Instance newFighter(Account account, int areaId, CharacterFrame frame) {
		Vector<Instance> incompleteFightGroup = this.fightGroups.get(this.incompleteFightGroupIndex);
		Instance newFighter;
		if(incompleteFightGroup.size() == 0) // captain
			newFighter = new Instance(account.id, 2, account.login, account.password, account.serverId, areaId, frame);
		else { // soldier
			newFighter = new Instance(account.id, 3, account.login, account.password, account.serverId, 0, frame);
			incompleteFightGroup.firstElement().newRecruit(newFighter); // on ajoute ce nouveau soldat aux recrues du capitaine
		}
		incompleteFightGroup.add(newFighter);
		if(incompleteFightGroup.size() == MAX_GROUP_SIZE) {
			int fightGroupNumber = this.fightGroups.size();
			while(this.incompleteFightGroupIndex != fightGroupNumber)
				if(this.fightGroups.get(++this.incompleteFightGroupIndex).size() < 8)
					return newFighter;
			this.fightGroups.add(new Vector<Instance>());
		}
		return newFighter;
	}
	
	private void removeFighter(Instance fighter) {
		int fightGroupsNumber = this.fightGroups.size();
		int currentFightGroupSize;
		Vector<Instance> currentFightGroup;
		for(int i = 0; i < fightGroupsNumber; ++i) {
			currentFightGroup = this.fightGroups.get(i);
			currentFightGroupSize = currentFightGroup.size();
			for(int j = 0; j < currentFightGroupSize; ++j)
				if(currentFightGroup.get(j) == fighter) {
					currentFightGroup.remove(fighter); // suppression de l'instance du vecteur d'instances
					if(j != 0) // si c'est pas le capitaine
						currentFightGroup.get(0).removeSoldier(fighter); // suppression du soldat de l'escouade
					if(i < this.incompleteFightGroupIndex)
						this.incompleteFightGroupIndex = i; // changement du groupe de combat à compléter
					return;
				}
		}
	}
	
	protected static class Account {
		protected int id;
		protected int type;
		protected String login;
		protected String password;
		protected int serverId;
		
		protected Account(int id, int type, String login, String password, int serverId) {
			this.id = id;
			this.type = type;
			this.login = login;
			this.password = password;
			this.serverId = serverId;
		}
	}
}