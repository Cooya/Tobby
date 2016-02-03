package main;

import java.util.Vector;

import frames.ConnectionFrame;
import frames.Frame;
import frames.RoleplayFrame;
import frames.SynchronisationFrame;

public class Instance {
	private NetworkInterface net;
	private CharacterController CC;
	private Vector<Frame> workingFrames;
	
	public Instance(String login, String password, int serverId) {
		workingFrames = new Vector<Frame>();
		net = new NetworkInterface(workingFrames);
		CC = new CharacterController(net, login, password, serverId);
		
		// pour le moment, elles sont toutes actives
		workingFrames.add(new ConnectionFrame(net, CC));
		workingFrames.add(new SynchronisationFrame(net, CC));
		workingFrames.add(new RoleplayFrame(net, CC));
		
		net.start(); // pour l'instant, l'interface réseau communique directement avec les frames
		CC.start();
	}
}
