package gui;

import java.util.Hashtable;

import main.Instance;

public class Model {
	protected Hashtable<Integer, Instance> instances;
	protected Hashtable<String, String> accounts;
	
	protected Model() {
		this.instances = new Hashtable<Integer, Instance>();
		this.accounts = new Hashtable<String, String>();
	}
	
	protected void assignMuleToEveryFighter(Instance mule) {
		for(Instance fighter : instances.values())
			if(fighter != mule)
				fighter.setMule(mule);
	}
}