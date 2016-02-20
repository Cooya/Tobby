package gui;

import java.util.Hashtable;

import main.Instance;

public class Model {
	protected Hashtable<Integer, Instance> instances;
	
	protected Model() {
		this.instances = new Hashtable<Integer, Instance>();
	}
}