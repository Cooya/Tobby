package main;

import java.util.LinkedList;
import java.util.Vector;

import messages.Message;
import frames.ConnectionFrame;
import frames.Frame;
import frames.RoleplayFrame;
import frames.SynchronisationFrame;

public class Instance extends Thread {
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
		this.workingFrames.add(new SynchronisationFrame(this));
		this.workingFrames.add(new RoleplayFrame(this, CC));
		
		start();
		this.net.start();
		this.net.sender.start();
		this.CC.start();
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
			if((msg = inPull()) != null)
				for(Frame frame : workingFrames)
					frame.processMessage(msg);
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
}
