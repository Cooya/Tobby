package main;

import java.util.LinkedList;
import java.util.Vector;

import controller.CharacterController;
import controller.CharacterState;
import controller.FighterController;
import controller.MuleController;
import messages.Message;
import frames.ConnectionFrame;
import frames.FightFrame;
import frames.Frame;
import frames.RoleplayFrame;
import frames.SynchronisationFrame;
import gui.CharacterFrame;

public class Instance extends Thread {
	private static Vector<Instance> instances = new Vector<Instance>();
	private static int instancesId = 0; // important lors des déconnexions (car instances.size() devient faux)
	private static Instance instanceInConnection;
	public Thread[] threads;
	public int id;
	public Log log;
	//private CharacterFrame graphicalFrame;
	private NetworkInterface net;
	private CharacterController character;
	private Vector<Frame> frames;
	private LinkedList<Message> output;
	private LinkedList<Message> input;
	
	public Instance(boolean type, String login, String password, int serverId, CharacterFrame graphicalFrame) {
		super(login + "/process");
		this.id = instancesId++;
		instances.add(this);
		this.log = new Log(login, graphicalFrame);
		//this.graphicalFrame = graphicalFrame;
		this.net = new NetworkInterface(this, login);
		if(type)
			this.character = new MuleController(this, login, password, serverId);
		else
			this.character = new FighterController(this, login, password, serverId);
		this.frames = new Vector<Frame>();
		this.output = new LinkedList<Message>();
		this.input = new LinkedList<Message>();
		
		this.frames.add(new ConnectionFrame(this, character));
		this.frames.add(new SynchronisationFrame(this));
		this.frames.add(new RoleplayFrame(this, character));
		if(!type)
			this.frames.add(new FightFrame(this, (FighterController) this.character));
		this.frames.get(0).isActive = true; // activation de la ConnectionFrame
		
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
	
	// appelée depuis le contrôleur (thread principal)
	public synchronized static void killInstance(Instance instance) {
		instances.remove(instance);
		for(Thread thread : instance.threads)
			thread.interrupt();
	}

	// appelée depuis un thread interne à l'instance
	public synchronized static void killCurrentInstance() {
		Thread currentThread = Thread.currentThread();
		Instance currentInstance = null;
		for(Instance instance : instances)
			for(Thread thread : instance.threads)
				if(currentThread == thread)
					currentInstance = instance;
		if(currentInstance != null) {
			instances.remove(currentInstance);
			for(Thread thread : currentInstance.threads)
				thread.interrupt();
		}
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
		try {
			return this.input.pop();
		}
		catch(Exception e) {
			return null;
		}
	}
	
	public Message outPull() {
		try {
			return this.output.pop();
		}
		catch(Exception e) {
			return null;
		}
	}
	
	public synchronized void run() {
		waitForConnection(); // attente de fin de connexion de l'instance précédente
		
		Message msg;
		while(!isInterrupted()) {
			if((msg = inPull()) != null) {
				for(Frame frame : this.frames)
					if(frame.isActive)
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
		this.log.p(Log.Status.CONSOLE, "Thread process of instance with id = " + this.id + " terminated.");
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
	
	public Latency getLatency() {
		return net.latency;
	}
	
	public static void log(Log.Status status, String msg) {
		Log log = getLog();
		if(log != null)
			log.p(status, msg);
		else
			System.out.println(msg);
	}
	
	public static void log(String msg) {
		log(Log.Status.INFO, msg);
	}
	
	public static void log(String direction, Message msg) {
		Log log = getLog();
		if(log != null)
			log.p(direction, msg);
		else
			throw new FatalError("Invalid thread.");
	}
	
	
	private static Log getLog() {
		Thread currentThread = currentThread();
		for(Instance instance : instances)
			for(Thread thread : instance.threads)
				if(thread == currentThread)
					return instance.log;
		return null;
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
		this.frames.get(2).isActive = true; // activation de la RoleplayFrame
	}
	
	public void startFight() {
		this.log.p("Fight frame running.");
		this.frames.get(3).isActive = true; // activation de la FightFrame
	}
	
	public void quitFight() {
		for(Frame frame : this.frames)
			if(frame instanceof FightFrame) {
				this.log.p("Fight frame stopping.");
				this.frames.get(3).isActive = false; // désactivation de la FightFrame
				return;
			}
	}
}