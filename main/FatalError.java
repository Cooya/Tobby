package main;

public class FatalError { // classe en travaux
	
	public FatalError(String str) {
		new Exception(str).printStackTrace();
		System.exit(1);
		/*
		for(Thread[] instance : instancesVector)
			for(Thread thread : instance)
				if(Thread.currentThread() == thread) {
					//killInstance(instance);
					//return;
					System.exit(1);
				}
		*/
	}
	
	public FatalError(Exception e) {
		e.printStackTrace();
		System.exit(1);
		/*
		for(Thread[] instance : instancesVector)
			for(Thread thread : instance)
				if(Thread.currentThread() == thread) {
					//killInstance(instance);
					//return;
					System.exit(1);
				}
		*/
	}
	
	/*
	public static void newInstance(Thread[] instance) {
		instancesVector.add(instance);
	}
	
	// ne fonctionne pas pour le moment
	public static void killInstance(Thread[] instance) {
		Instance.log("Killing instance.");
		for(Thread thread : instance)
			thread.interrupt();
		instancesVector.remove(instance); // ne s'exécute pas normalement
	}
	*/
}