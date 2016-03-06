package gui;

import java.util.Hashtable;
import java.util.Vector;

import main.Instance;

public class Model {
	private Hashtable<Account, Instance> instances;
	private Instance mule;
	
	protected Model() {
		this.instances = new Hashtable<Account, Instance>();
		this.mule = null;
	}
	
	protected void addInstance(Account account, Instance instance) {
		if(account.type == 0) { // création d'une mule
			for(Instance fighter : this.instances.values())
				fighter.setMule(instance);
			this.mule = instance;
		}
		else // création d'un combattant
			if(this.mule != null) // mule connectée
				instance.setMule(this.mule);
		this.instances.put(account, instance);
	}
	
	protected Instance getInstance(int instanceId) {
		return this.instances.get(instanceId);
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
			for(Thread thread : instance.threads)
				if(thread == currentThread)
					return instance;
		return null;
	}
	
	protected void removeInstance(Instance instance) {
		boolean isMule = false;
		for(Account account : this.instances.keySet())
			if(account.id == instance.id) {
				this.instances.put(account, null);
				isMule = account.type == 0;
			}
		if(isMule) {
			for(Instance fighter : this.instances.values())
				if(fighter != null)
					fighter.setMule(null);
			this.mule = null;
		}
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