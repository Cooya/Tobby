package main;

import java.util.concurrent.ConcurrentLinkedQueue;

import controller.CharacterBehaviour;
import controller.CharacterState;
import controller.characters.Captain;
import controller.characters.Character;
import controller.characters.LoneFighter;
import controller.characters.Mule;
import controller.characters.Soldier;
import messages.Message;
import frames.Processor;
import gamedata.enums.BreedEnum;
import gui.CharacterFrame;
import gui.Controller;

public class Instance extends Thread {
	private static Instance instanceInConnection; // instance en cours de connexion
	
	public int id; // identifiant de l'instance
	public Log log; // gestion des logs (fichier + historique graphique)
	public Thread[] threads; // tableau contenant les 4 threads de l'instance
	private NetworkInterface network; // gestion de la connexion réseau
	private Processor processor; // entité chargée du traitement des messages
	private Character character; // contrôleur du personnage
	private ConcurrentLinkedQueue<Message> output; // file des messages qui doivent être envoyé
	private ConcurrentLinkedQueue<Message> input; // file des messages reçus qui doivent être traité
	
	// constructeur lors d'une reconnexion (log déjà instancié)
	public Instance(int id, int behaviour, String login, String password, int serverId, int areaId, Log log) {
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
		this.log = log;
		this.network = new NetworkInterface(this, login);
		this.processor = new Processor(this, this.character);
		this.output = new ConcurrentLinkedQueue<Message>();
		this.input = new ConcurrentLinkedQueue<Message>();
		
		// initialisation des threads
		this.threads = new Thread[4];
		this.threads[0] = this.network;
		this.threads[1] = this.network.sender;
		this.threads[2] = this.character;
		this.threads[3] = this;
		
		// lancement de la thread de traitement (qui va lancer les autres threads le moment venu)
		this.start();
		
		Log.info("Instance with id = " + this.id + " started.");
	}
	
	// constructeur classique
	public Instance(int id, int behaviour, String login, String password, int serverId, int areaId, CharacterFrame graphicalFrame) {
		this(id, behaviour, login, password, serverId, areaId, new Log(login, graphicalFrame));
	}
	
	public void startCharacterController() {
		this.character.start();
	}
	
	// TODO -> pas terrible (implémentation d'une instance à refaire ?)
	public Character getCharacter() {
		return this.character;
	}
	
	public Latency getLatency() {
		return network.latency;
	}
	
	public void setGameServerIP(String gameServerIP) {
		this.network.setGameServerIP(gameServerIP);
	}
	
	public void newRecruit(Instance recruit) {
		((Captain) this.character).newRecruit((Soldier) recruit.character);
	}
	
	public static void log(String msg) {
		Log log = Controller.getInstance().getLog();
		if(log != null)
			log.p(msg);
		else
			Log.info(msg);
	}
	
	public static void log(String direction, Message msg) {
		Log log = Controller.getInstance().getLog();
		if(log != null)
			log.p(direction, msg);
		else
			Log.info(msg.toString());
	}
	
	// destruction des threads de l'instance depuis la GUI (forcée ou non)
	public void deconnectionOrder(boolean forced) {
		if(forced) {
			this.threads[1].interrupt(); // on interrompt d'abord le sender pour éviter une exception
			((NetworkInterface) this.threads[0]).closeReceiver();
			this.threads[2].interrupt();
			this.threads[3].interrupt();
		}
		else
			this.character.updateState(CharacterState.SHOULD_DECONNECT, true);
	}
	
	public synchronized void inPush(Message msg) {
		this.input.add(msg);
		notify();
	}
	
	public void outPush(Message msg) {
		this.output.add(msg);
		this.network.sender.wakeUp();
	}
	
	public Message inPull() {
		return this.input.poll();
	}
	
	public Message outPull() {
		return this.output.poll();
	}
	
	public synchronized void run() {
		waitForConnection(); // attente de fin de connexion de l'instance précédente
		
		// lancement des threads de l'interface réseau et du contrôleur du personnage
		this.network.start();
		this.network.sender.start();
		
		Message msg;
		while(!isInterrupted()) {
			if((msg = inPull()) != null)
				this.processor.processMessage(msg);
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
		Log.info("Thread process of instance with id = " + this.id + " terminated.");
		Controller.getInstance().threadTerminated();
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
	}
}