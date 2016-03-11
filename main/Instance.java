package main;

import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import controller.CharacterController;
import controller.CharacterState;
import controller.FighterController;
import controller.CaptainController;
import controller.MuleController;
import controller.SoldierController;
import messages.Message;
import frames.ConnectionFrame;
import frames.DialogContextFrame;
import frames.FightContextFrame;
import frames.Frame;
import frames.RoleplayContextFrame;
import frames.SynchronisationFrame;
import gui.CharacterFrame;
import gui.Controller;

public class Instance extends Thread {
	private static Instance instanceInConnection;
	private NetworkInterface net;
	private CharacterController character;
	private Vector<Frame> frames;
	private ConcurrentLinkedQueue<Message> output;
	private ConcurrentLinkedQueue<Message> input;
	private Date lastActivity;
	public Thread[] threads;
	public int id;
	public Log log;
	
	public Instance(int id, int type, String login, String password, int serverId, CharacterFrame graphicalFrame) {
		super(login + "/process");
		this.id = id;
		
		// initialisation des différents acteurs
		this.log = new Log(login, graphicalFrame);
		this.net = new NetworkInterface(this, login);
		if(type == 0)
			this.character = new MuleController(this, login, password, serverId);
		else if(type == 1)
			this.character = new CaptainController(this, login, password, serverId);
		else
			this.character = new SoldierController(this, login, password, serverId);
		this.frames = new Vector<Frame>();
		this.output = new ConcurrentLinkedQueue<Message>();
		this.input = new ConcurrentLinkedQueue<Message>();
		
		this.frames.add(new ConnectionFrame(this, character));
		this.frames.add(new SynchronisationFrame(this));
		this.frames.add(new RoleplayContextFrame(this, character));
		if(type == 0)
			this.frames.add(null); // pour avoir le même nombre de frames que les combattants (pas propre)
		else
			this.frames.add(new FightContextFrame(this, (FighterController) this.character));
		this.frames.add(new DialogContextFrame(this, character));
		this.frames.get(0).isActive = true; // activation de la ConnectionFrame
		
		// lancement des threads
		this.threads = new Thread[4];
		this.threads[0] = this.net;
		this.threads[1] = this.net.sender;
		this.threads[2] = this.character;
		this.threads[3] = this;
		this.start(); // gestion des frames
		this.net.start(); // réception
		this.net.sender.start(); // envoi
		this.character.start(); // contrôleur
		
		Instance.log("Instance with id = " + this.id + " started.");
	}
	
	// destruction des threads de l'instance depuis la GUI
	public void interruptThreads() {
		for(Thread thread : this.threads)
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
		this.lastActivity = new Date();
		this.log.graphicalFrame.setLastActivityLabel(this.lastActivity);
		//this.log.p(this.output.size() + " message(s) in the output queue.");
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
			((FighterController) this.character).setMule(null);
			this.character.updateState(CharacterState.MULE_AVAILABLE, false);
		}
		else {
			((FighterController) this.character).setMule((MuleController) mule.character);
			this.character.updateState(CharacterState.MULE_AVAILABLE, true);
		}
	}
	
	public void setCaptain(Instance captain) {
		((SoldierController) this.character).setCaptain((CaptainController) captain.character); 
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
			throw new FatalError("Invalid thread.");
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