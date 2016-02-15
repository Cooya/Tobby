package main;

import java.util.LinkedList;
import java.util.Vector;

import messages.Message;
import frames.ConnectionFrame;
import frames.Frame;
import frames.RoleplayFrame;
import frames.SynchronisationFrame;

public class Instance extends Thread {
	public static boolean connectionInProcess;
	private NetworkInterface net;
	private CharacterController CC;
	private Vector<Frame> workingFrames;
	public LinkedList<Message> output;
	private LinkedList<Message> input;
	
	public Instance(String login, String password, int serverId) {
		this.net = new NetworkInterface(this);
		this.CC = new CharacterController(this, login, password, serverId);
		this.workingFrames = new Vector<Frame>();
		this.output = new LinkedList<Message>();
		this.input = new LinkedList<Message>();
		
		this.workingFrames.add(new ConnectionFrame(this, CC));
		
		waitForConnection(); // file d'attention pour la connexion des persos
		
		start(); // gestion des frames
		this.net.start(); // réception
		this.net.sender.start(); // envoi
		this.CC.start(); // contrôleur
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
				for(Frame frame : workingFrames)
					if(frame.processMessage(msg))
						break;
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
		this.workingFrames.remove(0); // on retire la ConnectionFrame
		this.workingFrames.add(new SynchronisationFrame(this));
		this.workingFrames.add(new RoleplayFrame(this, CC));	
	}
}