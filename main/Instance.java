package main;

import java.util.LinkedList;
import java.util.Vector;

import controller.CharacterController;
import controller.FighterController;
import controller.MuleController;
import messages.Message;
import frames.ConnectionFrame;
import frames.FightFrame;
import frames.IFrame;
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
	private Vector<IFrame> workingFrames;
	private Vector<Vector<IFrame>> frameUpdates; // résolution d'accès concurrents lors de l'ajout ou de la suppression de frames
	private boolean workingFramesUpdates;
	private LinkedList<Message> output;
	private LinkedList<Message> input;
	
	public Instance(boolean type, String login, String password, int serverId, CharacterFrame graphicalFrame) {
		this.id = instancesId++;
		instances.add(this);
		this.log = new Log(login, graphicalFrame);
		//this.graphicalFrame = graphicalFrame;
		this.net = new NetworkInterface(this);
		if(type)
			this.character = new MuleController(this, login, password, serverId);
		else
			this.character = new FighterController(this, login, password, serverId);
		this.workingFrames = new Vector<IFrame>();
		this.frameUpdates = new Vector<Vector<IFrame>>();
		this.frameUpdates.add(new Vector<IFrame>()); // add
		this.frameUpdates.add(new Vector<IFrame>()); // del
		this.workingFramesUpdates = false;
		this.output = new LinkedList<Message>();
		this.input = new LinkedList<Message>();
		
		this.workingFrames.add(new ConnectionFrame(this, character));
		
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
	
	public static void fatalError(String str) {
		new Exception(str).printStackTrace();
		Instance.killCurrentInstance();
	}
	
	public static void fatalError(Exception e) {
		e.printStackTrace();
		Instance.killCurrentInstance();
	}
	
	public static void killCurrentInstance() {
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
			throw new Error("Invalid thread.");
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
				for(IFrame frame : this.workingFrames)
					if(frame.processMessage(msg))
						break;
				if(workingFramesUpdates)
					updateWorkingFrames();
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
		if(mule == null)
			((FighterController) this.character).setMule(null);
		else
			((FighterController) this.character).setMule((MuleController) mule.character);
	}
	
	public Latency getLatency() {
		return net.latency;
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
		this.frameUpdates.get(1).add(this.workingFrames.get(0)); // on retire la ConnectionFrame
		this.frameUpdates.get(0).add(new SynchronisationFrame(this));
		this.frameUpdates.get(0).add(new RoleplayFrame(this, character));
		this.workingFramesUpdates = true;
	}
	
	public void startFight() {
		this.log.p("Fight frame running.");
		this.frameUpdates.get(0).add(new FightFrame(this, (FighterController) character));
		this.workingFramesUpdates = true;
	}
	
	public void quitFight() {
		for(IFrame frame : this.workingFrames)
			if(frame instanceof FightFrame) {
				this.log.p("Fight frame stopping.");
				this.frameUpdates.get(1).add(frame);
				this.workingFramesUpdates = true;
				return;
			}
	}
	
	private void updateWorkingFrames() {
		for(IFrame frame : this.frameUpdates.get(0))
			this.workingFrames.add(frame);
		for(IFrame frame : this.frameUpdates.get(1))
			this.workingFrames.remove(frame);
		this.workingFramesUpdates = false;
	}
	
	private static Log getLog() {
		Thread currentThread = currentThread();
		for(Instance instance : instances)
			for(Thread thread : instance.threads)
				if(thread == currentThread)
					return instance.log;
		return null;
	}
}