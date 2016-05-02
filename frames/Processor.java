package frames;

import gui.Controller;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import utilities.Reflection;
import controller.characters.Character;
import main.Log;
import main.Main;
import messages.Message;

@SuppressWarnings("unchecked")
public class Processor extends Thread {
	private static Character characterInConnection; // personnage en cours de connexion
	private static Vector<Class<? extends Frame>> processFrames = new Vector<Class<? extends Frame>>();
	
	private Character character;
	private Map<String, Process> processTable;
	private ConcurrentLinkedQueue<Message> input; // file des messages reçus qui doivent être traité
	
	static {
		// récupération des différentes frames de traitement dans le package "frames"
		try {
			Class<?>[] classesArray = Reflection.getClasses("frames");
			for(Class<?> cl : classesArray)
				if(cl.getSuperclass() == Frame.class)
					processFrames.add((Class<? extends Frame>) cl);
		} catch(Exception e) {
			e.printStackTrace();
			Controller.getInstance().exit("Impossible to load frame classes.");
		}
		
		/*
		for(Class<? extends Frame> processFrame : processFrames) {
			for(Method method : processFrame.getDeclaredMethods())
				System.out.println(method);
			System.out.println();
		}
		*/
	}

	public Processor(Character character, String login) {
		super(login + "/processor");
		this.character = character;
		this.processTable = new HashMap<String, Process>();
		this.input = new ConcurrentLinkedQueue<Message>();
		Frame frame;
		Method[] methods;
		String msgName;
		for(Class<? extends Frame> processFrame : processFrames) {
			try {
				frame = processFrame.getConstructor(Character.class).newInstance(character);
			} catch(Exception e) {
				e.printStackTrace();
				return;
			}
			methods = processFrame.getDeclaredMethods();
			for(Method method : methods)
				if(method.getName().equals("process")) {
					msgName = method.getParameterTypes()[0].getSimpleName();
					this.processTable.put(msgName, new Process(frame, method));
				}
		}
	}
	
	// attente si une connexion d'un autre personnage est déjà en cours
	public void waitForConnection() {
		synchronized(Main.class) {
			while(!isInterrupted() && characterInConnection != null) {
				this.character.log.p("Waiting for connection.");
				try {
					Main.class.wait();
				} catch (Exception e) {
					Thread.currentThread().interrupt();
				}
			}
			characterInConnection = this.character;
		}
	}
	
	// signale à un autre personnage en attente de connexion qu'il peut se connecter
	// appelée lors de la réception du CharacterSelectedSuccessMessage
	public void endOfConnection() {
		synchronized(Main.class) {
			characterInConnection = null;
			Main.class.notify();
		}
	}
	
	// appelée depuis le "receiver" uniquement
	public synchronized void incomingMessage(Message msg) {
		this.input.add(msg);
		notify();
	}
	
	@Override
	public synchronized void run() {
		waitForConnection(); // attente de fin de connexion du personage précédent
		
		// lancement des threads de l'interface réseau
		this.character.net.start();
		this.character.net.sender.start();
		
		Message msg;
		while(!isInterrupted()) {
			if((msg = this.input.poll()) != null)
				processMessage(msg);
			else
				try {
					wait();
				} catch(Exception e) {
					Thread.currentThread().interrupt();
				}
		}
		if(characterInConnection == this.character) { // on libère le launcher
			synchronized(Main.class) {
				characterInConnection = null;
				Main.class.notify();
			}
		}
		Log.info("Thread process of character with id = " + this.character.id + " terminated.");
		Controller.getInstance().threadTerminated();
	}
	
	// ne reçoit pas de message inconnu
	public void processMessage(Message msg) {
		Process process = this.processTable.get(msg.getName());
		if(process == null) // message inconnu ou n'ayant pas de traitement associé
			return;
		process.process(msg);
	}

	private class Process {
		private Frame processFrame; // frame où se situe la méthode "process()"
		private Method processMethod; // méthode "process()"

		private Process(Frame processFrame, Method processMethod) {
			this.processFrame = processFrame;
			this.processMethod = processMethod;
		}

		private void process(Message msg) {
			msg.deserialize(); // unique appel de la fonction "deserialize()"
			try {
				this.processMethod.invoke(processFrame, msg);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}