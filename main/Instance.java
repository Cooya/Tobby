package main;

import java.util.LinkedList;
import java.util.Vector;

import utilities.Log;
import messages.Message;
import frames.ConnectionFrame;
import frames.FightFrame;
import frames.IFrame;
import frames.RoleplayFrame;
import frames.SynchronisationFrame;
import gui.CharacterFrame;

public class Instance extends Thread {
	private static Vector<Instance> instances = new Vector<Instance>();
	private static boolean connectionInProcess;
	public Thread[] threads;
	public int id;
	public Log log;
	//private CharacterFrame graphicalFrame;
	private NetworkInterface net;
	private CharacterController CC;
	private Vector<IFrame> workingFrames;
	private Vector<Vector<IFrame>> frameUpdates; // résolution d'accès concurrents lors de l'ajout ou de la suppression de frames
	private boolean workingFramesUpdates;
	private LinkedList<Message> output;
	private LinkedList<Message> input;
	
	public Instance(String login, String password, int serverId, CharacterFrame graphicalFrame) {
		this.id = instances.size();
		instances.add(this);
		this.log = new Log(login, graphicalFrame);
		//this.graphicalFrame = graphicalFrame;
		this.net = new NetworkInterface(this);
		this.CC = new CharacterController(this, login, password, serverId);
		this.workingFrames = new Vector<IFrame>();
		this.frameUpdates = new Vector<Vector<IFrame>>();
		this.frameUpdates.add(new Vector<IFrame>()); // add
		this.frameUpdates.add(new Vector<IFrame>()); // del
		this.workingFramesUpdates = false;
		this.output = new LinkedList<Message>();
		this.input = new LinkedList<Message>();
		
		this.workingFrames.add(new ConnectionFrame(this, CC));
		
		waitForConnection(); // file d'attention pour la connexion des persos
		
		this.threads = new Thread[4];
		this.threads[0] = this.net;
		this.threads[1] = this.net.sender;
		this.threads[2] = this.CC;
		this.threads[3] = this;
		this.start(); // gestion des frames
		this.net.start(); // réception
		this.net.sender.start(); // envoi
		this.CC.start(); // contrôleur
	}
	
	public static void log(String msg) {
		Log log = getLog();
		if(log != null)
			log.p(msg);
		else
			System.out.println(msg);
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
		Message msg;
		while(true) {
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
					e.printStackTrace();
				}
		}
	}
	
	public void setGameServerIP(String gameServerIP) {
		this.net.setGameServerIP(gameServerIP);
	}
	
	public Latency getLatency() {
		return net.latency;
	}
	
	public void waitForConnection() {
		synchronized(Main.class) {
			while(connectionInProcess)
				try {
					Main.class.wait();
				} catch (Exception e) {
					e.printStackTrace();
				}
			connectionInProcess = true;
		}
	}
	
	public void endOfConnection() {
		synchronized(Main.class) {
			connectionInProcess = false;
			Main.class.notify();
		}
		this.frameUpdates.get(1).add(this.workingFrames.get(0)); // on retire la ConnectionFrame
		this.frameUpdates.get(0).add(new SynchronisationFrame(this));
		this.frameUpdates.get(0).add(new RoleplayFrame(this, CC));
		this.workingFramesUpdates = true;
	}
	
	public void startFight() {
		this.log.p("Fight frame running.");
		this.frameUpdates.get(0).add(new FightFrame(this, CC));
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