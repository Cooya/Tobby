package gui;

import java.util.Hashtable;

import main.Instance;

public class Model {
	private Hashtable<Integer, Instance> instances;
	protected Hashtable<String, String> accounts;
	
	protected Model() {
		this.instances = new Hashtable<Integer, Instance>();
		this.accounts = new Hashtable<String, String>();
	}
	
	// type = true si c'est une mule
	protected void addInstance(int instanceId, Instance instance, boolean type) {
		if(type)
			for(Instance fighter : this.instances.values())
				fighter.setMule(instance);
		this.instances.put(instanceId, instance);
	}
	
	protected Instance getInstance(int instanceId) {
		return this.instances.get(instanceId);
	}
	
	// type = true si c'est une mule
	protected void removeInstance(int instanceId, boolean type) {
		this.instances.remove(instanceId);
		if(type)
			for(Instance fighter : this.instances.values())
				fighter.setMule(null);
	}
}