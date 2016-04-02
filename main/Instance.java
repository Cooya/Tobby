package main;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import controller.CharacterBehaviour;
import controller.CharacterState;
import controller.characters.Captain;
import controller.characters.Character;
import controller.characters.Fighter;
import controller.characters.LoneFighter;
import controller.characters.Mule;
import controller.characters.Soldier;
import messages.Message;
import frames.ConnectionFrame;
import frames.DialogContextFrame;
import frames.FightContextFrame;
import frames.Frame;
import frames.RoleplayContextFrame;
import frames.SynchronisationFrame;
import gamedata.character.BreedEnum;
import gui.CharacterFrame;
import gui.Controller;

public class Instance extends Thread {
	private static Instance instanceInConnection; // instance en cours de connexion
	
	public int id; // identifiant de l'instance
	public Log log; // gestion des logs (fichier + historique graphique)
	public Thread[] threads; // tableau contenant les 4 threads de l'instance
	private NetworkInterface net; // gestion de la connexion réseau
	private Character character; // contrôleur de l'instance
	private Vector<Frame> frames; // conteneur des frames de traitement des messages
	private ConcurrentLinkedQueue<Message> output; // file des messages qui doivent être envoyé
	private ConcurrentLinkedQueue<Message> input; // file des messages reçus qui doivent être traité
	
	public Instance(int id, int behaviour, String login, String password, int serverId, int areaId, CharacterFrame graphicalFrame) {
		super(login + "/process");
		this.id = id;
		
		// sélection du comportement de l'instance (à laisser en premier)
		switch(behaviour) {
			case CharacterBehaviour.WAITING_MULE : this.character = new Mule(this, login, password, serverId, BreedEnum.Sadida); break;
			case CharacterBehaviour.TRAINING_MULE : throw new FatalError("Not implemented yet !");
			case CharacterBehaviour.SELLER : throw new FatalError("Not implemented yet !");
			case CharacterBehaviour.LONE_WOLF : this.character = new LoneFighter(this, login, password, serverId, BreedEnum.Cra, areaId); break;
			case CharacterBehaviour.CAPTAIN : this.character = new Captain(this, login, password, serverId, BreedEnum.Cra, areaId); break;
			case CharacterBehaviour.SOLDIER : this.character = new Soldier(this, login, password, serverId, BreedEnum.Cra); break;
			default : throw new FatalError("Unknown behaviour.");
		}
		
		// initialisation des différents modules
		this.log = new Log(login, graphicalFrame);
		this.net = new NetworkInterface(this, login);
		this.frames = new Vector<Frame>();
		this.output = new ConcurrentLinkedQueue<Message>();
		this.input = new ConcurrentLinkedQueue<Message>();
		
		// création des frames de traitement des messages
		this.frames.add(new ConnectionFrame(this, character));
		this.frames.add(new SynchronisationFrame(this));
		this.frames.add(new RoleplayContextFrame(this, character));
		if(behaviour == CharacterBehaviour.WAITING_MULE || behaviour == CharacterBehaviour.SELLER)
			this.frames.add(null); // pour avoir le même nombre de frames que les combattants (pas propre)
		else
			this.frames.add(new FightContextFrame(this, (Fighter) this.character));
		this.frames.add(new DialogContextFrame(this, character));
		this.frames.get(0).isActive = true; // activation de la ConnectionFrame
		
		// lancement des threads
		this.threads = new Thread[4];
		this.threads[0] = this.net;
		this.threads[1] = this.net.sender;
		this.threads[2] = this.character;
		this.threads[3] = this;
		this.start();
		this.net.start();
		this.net.sender.start();
		this.character.start();
		
		Instance.log("Instance with id = " + this.id + " started.");
	}
	
	// destruction des threads de l'instance depuis la GUI
	public void interruptThreads() {
		for(Thread thread : this.threads)
			if(thread instanceof NetworkInterface)
				((NetworkInterface) thread).closeReceiver();
			else
				thread.interrupt();
	}
	
	public synchronized void inPush(Message msg) {
		this.input.add(msg);
		notify();
	}
	
	public void outPush(Message msg) {
		this.output.add(msg);
		this.net.sender.wakeUp();
	}
	
	public Message inPull() {
		return this.input.poll();
	}
	
	public Message outPull() {
		return this.output.poll();
	}
	
	public synchronized void run() {
		waitForConnection(); // attente de fin de connexion de l'instance précédente
		
		Message msg;
		while(!isInterrupted()) {
			if((msg = inPull()) != null) {
				for(Frame frame : this.frames)
					if(frame != null && frame.isActive)
						if(frame.processMessage(msg))
							break;
			}
			else
				try {
					wait();
				} catch(Exception e) {
					Thread.currentThread().interrupt();
				}
		}
		if(instanceInConnection == this) { // on libère le launcher
			synchronized(Main.class) {
				instanceInConnection = null;
				Main.class.notify();
			}
		}
		System.out.println("Thread process of instance with id = " + this.id + " terminated.");
	}
	
	public void setGameServerIP(String gameServerIP) {
		this.net.setGameServerIP(gameServerIP);
	}
	
	public void setMule(Instance mule) {
		if(mule == null) {
			((Fighter) this.character).mule = null;
			this.character.updateState(CharacterState.MULE_AVAILABLE, false);
		}
		else {
			((Fighter) this.character).mule = (Mule) mule.character;
			this.character.updateState(CharacterState.MULE_AVAILABLE, true);
		}
	}
	
	public void newRecruit(Instance recruit) {
		((Captain) this.character).newRecruit((Soldier) recruit.character);
	}
	
	public void removeSoldier(Instance soldier) {
		((Captain) this.character).removeSoldierFromSquad((Soldier) soldier.character);
	}
	
	public Latency getLatency() {
		return net.latency;
	}
	
	public double getCharacterId() {
		return this.character.infos.characterId;
	}
	
	public static void log(String msg) {
		Log log = Controller.getLog();
		if(log != null)
			log.p(msg);
		else
			System.out.println(msg);
	}
	
	public static void log(String direction, Message msg) {
		Log log = Controller.getLog();
		if(log != null)
			log.p(direction, msg);
		else
			System.out.println(msg);
	}
	
	public void waitForConnection() {
		synchronized(Main.class) {
			while(!isInterrupted() && instanceInConnection != null) {
				this.log.p("Waiting for connection.");
				try {
					Main.class.wait();
				} catch (Exception e) {
					Thread.currentThread().interrupt();
				}
			}
			instanceInConnection = this;
		}
	}
	
	public void endOfConnection() {
		synchronized(Main.class) {
			instanceInConnection = null;
			Main.class.notify();
		}
		this.frames.get(0).isActive = false; // désactivation de la ConnectionFrame
		this.frames.get(1).isActive = true; // activation de la SynchronisationFrame
		this.frames.get(2).isActive = true; // activation de la RoleplayContextFrame
	}

	public void startFightContext() {
		this.frames.get(3).isActive = true;
		this.log.p("Fight context frame activated.");
	}
	
	public void quitFightContext() {
		this.frames.get(3).isActive = false;
		this.log.p("Fight context frame deactivated.");
	}
	
	public void startExchangeContext() {
		this.frames.get(4).isActive = true;
		this.log.p("Dialog context frame activated.");
	}
	
	public void quitExchangeContext() {
		this.frames.get(4).isActive = false;
		this.log.p("Dialog context frame deactivated.");
	}
}