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
	private NetworkInterface net; // gestion de la connexion r�seau
	private Character character; // contr�leur de l'instance
	private Vector<Frame> frames; // conteneur des frames de traitement des messages
	private ConcurrentLinkedQueue<Message> output; // file des messages qui doivent �tre envoy�
	private ConcurrentLinkedQueue<Message> input; // file des messages re�us qui doivent �tre trait�
	
	// constructeur lors d'une reconnexion (log d�j� instanci�)
	public Instance(int id, int behaviour, String login, String password, int serverId, int areaId, Log log) {
		super(login + "/process");
		this.id = id;
		
		// s�lection du comportement de l'instance (� laisser en premier)
		switch(behaviour) {
			case CharacterBehaviour.WAITING_MULE : this.character = new Mule(this, login, password, serverId, BreedEnum.Sadida); break;
			case CharacterBehaviour.TRAINING_MULE : throw new FatalError("Not implemented yet !");
			case CharacterBehaviour.SELLER : throw new FatalError("Not implemented yet !");
			case CharacterBehaviour.LONE_WOLF : this.character = new LoneFighter(this, login, password, serverId, BreedEnum.Cra, areaId); break;
			case CharacterBehaviour.CAPTAIN : this.character = new Captain(this, login, password, serverId, BreedEnum.Cra, areaId); break;
			case CharacterBehaviour.SOLDIER : this.character = new Soldier(this, login, password, serverId, BreedEnum.Cra); break;
			default : throw new FatalError("Unknown behaviour.");
		}
		
		// initialisation des diff�rents modules
		this.log = log;
		this.net = new NetworkInterface(this, login);
		this.frames = new Vector<Frame>();
		this.output = new ConcurrentLinkedQueue<Message>();
		this.input = new ConcurrentLinkedQueue<Message>();
		
		// cr�ation des frames de traitement des messages
		this.frames.add(new ConnectionFrame(this, character));
		this.frames.add(new SynchronisationFrame(this, character));
		this.frames.add(new RoleplayContextFrame(this, character));
		if(behaviour == CharacterBehaviour.WAITING_MULE || behaviour == CharacterBehaviour.SELLER)
			this.frames.add(null); // pour avoir le m�me nombre de frames que les combattants (pas propre)
		else
			this.frames.add(new FightContextFrame(this, (Fighter) this.character));
		this.frames.get(0).isActive = true; // activation de la ConnectionFrame
		
		// initialisation des threads
		this.threads = new Thread[4];
		this.threads[0] = this.net;
		this.threads[1] = this.net.sender;
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
	
	// TODO -> pas terrible (impl�mentation d'une instance � refaire ?)
	public Character getCharacter() {
		return this.character;
	}
	
	public Latency getLatency() {
		return net.latency;
	}
	
	public void setGameServerIP(String gameServerIP) {
		this.net.setGameServerIP(gameServerIP);
	}
	
	public void newRecruit(Instance recruit) {
		((Captain) this.character).newRecruit((Soldier) recruit.character);
	}
	
	public static void log(String msg) {
		Log log = Controller.getInstance().getLog();
		if(log != null)
			log.p(msg);
		else
			Log.err(msg);
	}
	
	public static void log(String direction, Message msg) {
		Log log = Controller.getInstance().getLog();
		if(log != null)
			log.p(direction, msg);
		else
			Log.err(msg.toString());
	}
	
	// destruction des threads de l'instance depuis la GUI (forc�e ou non)
	public void deconnectionOrder(boolean forced) {
		if(forced) {
			this.threads[1].interrupt(); // on interrompt d'abord le sender pour �viter une exception
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
		this.net.sender.wakeUp();
	}
	
	public Message inPull() {
		return this.input.poll();
	}
	
	public Message outPull() {
		return this.output.poll();
	}
	
	public synchronized void run() {
		waitForConnection(); // attente de fin de connexion de l'instance pr�c�dente
		
		// lancement des threads de l'interface r�seau et du contr�leur du personnage
		this.net.start();
		this.net.sender.start();
		
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
		if(instanceInConnection == this) { // on lib�re le launcher
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
		this.frames.get(0).isActive = false; // d�sactivation de la ConnectionFrame
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
}