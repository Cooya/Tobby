package main;

import java.util.Vector;

import utilities.Log;

public class FatalError {
	private static Vector<Thread[]> instancesVector = new Vector<Thread[]>();
	
	public FatalError(String str) {
		new Exception(str).printStackTrace();
		for(Thread[] instance : instancesVector)
			for(Thread thread : instance)
				if(Thread.currentThread() == thread) {
					//killInstance(instance);
					//return;
					System.exit(1);
				}
	}
	
	public FatalError(Exception e) {
		e.printStackTrace();
		for(Thread[] instance : instancesVector)
			for(Thread thread : instance)
				if(Thread.currentThread() == thread) {
					//killInstance(instance);
					//return;
					System.exit(1);
				}
	}
	
	public static void newInstance(Thread[] instance) {
		instancesVector.add(instance);
	}
	
	// ne fonctionne pas pour le moment
	public static void killInstance(Thread[] instance) {
		Log.p("Killing instance.");
		for(Thread thread : instance)
			thread.interrupt();
		instancesVector.remove(instance); // ne s'exécute pas normalement
	}
}