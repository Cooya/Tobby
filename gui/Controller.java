package gui;

import frames.Processor;
import gui.Model.Account;
import gui.View.FighterOptionsPanel;
import gui.View.LoginPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import controller.CharacterBehaviour;
import main.Emulation;
import main.Instance;
import main.Log;

public class Controller {
	private static Controller self;
	
	private View view;
	private Model model;
	private FilesManager filesManager;

	private Controller() {
		this.view = new View(); // lance tous les listeners
		this.model = new Model();
		this.filesManager = new FilesManager(view, model);
		
		Emulation.runLauncher();
		
		// le thread principal se termine ici
	}
	
	public static Controller getInstance() {
		if(self == null)
			self = new Controller();
		return self;
	}
	
	public synchronized void threadTerminated() {
		notify(); // notifie l'UI thread
	}

	// d�termine si l'identifiant du personnage pass� en param�tre est un id d'instance de l'application
	public boolean isWorkmate(double characterId) {
		for(Instance instance : this.model.getConnectedInstances())
			if(instance.getCharacter().infos.characterId == characterId)
				return true;
		return false;
	}

	// retourne l'objet Log correspondant au thread courant (utile pour les m�thodes statiques)
	public Log getLog() {
		Thread currentThread = Thread.currentThread();
		for(Instance instance : this.model.getConnectedInstances())
			for(Thread thread : instance.threads)
				if(thread == currentThread)
					return instance.log;
		return null;
	}
	
	// d�connexion de l'instance du thread courant
	public void deconnectCurrentInstance(String reason, boolean forced, boolean reconnection) {
		Instance instance = this.model.getCurrentInstance();
		if(instance != null)
			deconnectInstance(instance, reason, forced, reconnection);
	}

	// d�connexion de toutes les instances avec red�marrage ou non (les frames graphiques restent pr�sentes)
	// � ne pas appeler depuis le thread UI
	public synchronized void deconnectAllInstances(String reason, boolean forced, boolean reconnection) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					// d�connexion (voire suppression) de toutes les instances connect�es
					Log.info("Deconnecting all instances.");
					Vector<Instance> instances = model.getConnectedInstances();
					for(Instance instance : instances)
						deconnectInstance(instance, reason, forced, reconnection);
					
					// attente de l'interruption de tous les threads de chaque instance
					while(Thread.activeCount() > 1) // UI thread
						try {
							wait();
						} catch(InterruptedException e) {
							e.printStackTrace();
							exit("Thread UI interrupted.");
						}
					
					// r�connexion de toutes les instances si sp�cifi�
					if(reconnection) {
						Emulation.killLauncher();
						Emulation.runLauncher();
						model.restartAllInstances(); // red�marrage de toutes les instances
					}
				}
			});
		} catch(InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			exit("Error during invocation of the deconnection task.");
		}
	}

	// fonction appel�e lors d'une erreur critique
	public void exit(String reason) {
		this.filesManager.saveAccountsInFile();
		Emulation.killLauncher();
		Log.err(reason);
		System.exit(0);
	}
	
	// fonction appel�e � la fermeture de l'application
	public void exit() {
		long sum = 0;
		for(long l : Processor.perfTest) // TODO
			sum += l;
		System.out.println("Average time : " + sum / Processor.perfTest.size());
		
		this.filesManager.saveAccountsInFile();
		Emulation.killLauncher();
		Log.info("Application closed by user.");
		System.exit(0);
	}
	
	// cr�ation d'une instance (passage au mod�le)
	private synchronized void createInstance(Account account, int areaId, Account captain) {
		// v�rification de la disponibilit� du compte (connect� ou non)
		if(this.model.isConnected(account))
			return;
			
		CharacterFrame frame = new CharacterFrame(account.id, account.login);
		this.view.addCharacterFrame(frame);
		frame.addInternalFrameListener(new CharacterFrameListener());
		frame.setVisible(true);
		this.model.createInstance(account, areaId, frame, captain);
	}
	
	// d�connexion d'une instance avec suppression de l'instance ou non (la frame graphique reste pr�sente)
	private synchronized void deconnectInstance(Instance instance, String reason, boolean forced, boolean reconnection) {
		instance.log.p(reason);
		instance.log.flushBuffer();
		instance.deconnectionOrder(forced);
		if(!reconnection)
			this.model.removeInstance(instance.id);
		// TODO -> reconnexion � impl�menter
	}
	
	// m�thode d�clench�e par la fermeture d'une frame graphique
	private synchronized void killInstance(JInternalFrame graphicalFrame) {
		int instanceId = this.view.getInstance(graphicalFrame).id;
		this.view.removeCharacterFrame(graphicalFrame);
		Instance instance = this.model.removeInstance(instanceId);
		if(instance != null)
			instance.deconnectionOrder(true);
	}

	// �coute du clic sur le bouton "load account"
	// => lancement de la bo�te de dialogue pour la cr�ation d'un nouveau compte
	protected static class LoadAccountListener implements ActionListener {
		protected LoadAccountListener(AbstractButton button) {
			button.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			new ConnectionListener(Controller.getInstance().view.createLoginPanel());
		}
	}

	// �coute du bouton "Run" dans la bo�te de dialogue de cr�ation de nouveau de compte
	// => cr�ation d'un compte, ajout dans le fichier texte et lancement de la bo�te de dialogue des options de combat
	private static class ConnectionListener implements ActionListener {
		private LoginPanel loginPanel;

		private ConnectionListener(LoginPanel loginPanel) {
			this.loginPanel = loginPanel;
			this.loginPanel.connectButton.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			Controller controller = Controller.getInstance();
			
			String login = this.loginPanel.loginField.getText();
			String password = this.loginPanel.passwordField.getText();
			int serverId = Integer.parseInt(this.loginPanel.serverField.getText());
			int characterType = this.loginPanel.getSelectedType();
			if(login.isEmpty() || password.isEmpty() || serverId == 0)
				JOptionPane.showMessageDialog(null, "Missing informations.", "Erreur", JOptionPane.ERROR_MESSAGE);
			else {
				this.loginPanel.dispose();
				Account account = controller.model.getAccount(login); // si le compte existe d�j�
				if(account == null) {
					account = controller.model.createAccount(characterType, login, password, serverId);
					controller.view.newAccountItem(login, characterType);
				}
				new FighterOptionsPanelListener(controller.view.createFighterOptionsPanel(account.behaviour, controller.model.squads.nextFighterWillBe()), account); // affichage des options de combat �ventuelles
			}
		}
	}

	// fermeture d'une CharacterFrame
	private static class CharacterFrameListener implements InternalFrameListener {

		@Override
		public void internalFrameActivated(InternalFrameEvent arg0) {}

		@Override
		public void internalFrameClosed(InternalFrameEvent arg0) {}

		@Override
		public void internalFrameClosing(InternalFrameEvent event) {
			Controller.getInstance().killInstance(event.getInternalFrame());
		}

		@Override
		public void internalFrameDeactivated(InternalFrameEvent arg0) {}

		@Override
		public void internalFrameDeiconified(InternalFrameEvent arg0) {}

		@Override
		public void internalFrameIconified(InternalFrameEvent arg0) {}

		@Override
		public void internalFrameOpened(InternalFrameEvent arg0) {}
	}

	// �coute du bouton de lancement rapide d'un compte dans la liste des comptes du menu d�roulant
	// => lancement de la bo�te de dialogue des options de combat
	protected static class AccountItemListener implements ActionListener {
		private JMenuItem accountItem;

		protected AccountItemListener(JMenuItem item) {
			this.accountItem = item;
			this.accountItem.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			Controller controller = Controller.getInstance();
			
			Account account = controller.model.getAccount(this.accountItem.getText().split(" ")[0]);
			if(account != null)
				new FighterOptionsPanelListener(controller.view.createFighterOptionsPanel(account.behaviour, controller.model.squads.nextFighterWillBe()), account);
			else
				Log.err("Unknown account.");
		}
	}
	
	// �coute du bouton de lancement rapide d'une escouade dans la liste des escouades du menu d�roulant
	// => lancement de la bo�te de dialogue des options de combat de l'escouade
	protected static class SquadItemListener implements ActionListener {
		private JMenuItem squadItem;

		protected SquadItemListener(JMenuItem item) {
			this.squadItem = item;
			this.squadItem.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			Controller controller = Controller.getInstance();
			
			Vector<Account> squad = controller.model.squads.getSquad(this.squadItem.getText().split(" ")[0]);
			if(squad != null)
				new FighterOptionsPanelListener(controller.view.createFighterOptionsPanel(CharacterBehaviour.CAPTAIN, -1), squad);
			else
				Log.err("Unknown squad.");
		}
	}

	// �coute du bouton "Go" dans la bo�te de dialogue des options de combat
	// => lancement d'une instance
	private static class FighterOptionsPanelListener implements ActionListener {
		private FighterOptionsPanel fighterOptionsPanel;
		private Account account;
		private Vector<Account> squad;

		// instance seule
		private FighterOptionsPanelListener(FighterOptionsPanel fighterOptionsPanel, Account account) {
			this.fighterOptionsPanel = fighterOptionsPanel;
			this.account = account;
			this.fighterOptionsPanel.submitButton.addActionListener(this);
		}
		
		// escouade
		private FighterOptionsPanelListener(FighterOptionsPanel fighterOptionsPanel, Vector<Account> squad) {
			this.fighterOptionsPanel = fighterOptionsPanel;
			this.squad = squad;
			this.fighterOptionsPanel.submitButton.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			Controller controller = Controller.getInstance();
			
			 // si une mule n'est pas connect�e, on en connecte une
			if((this.account == null  || this.account.behaviour != CharacterBehaviour.WAITING_MULE) && !controller.model.muleIsConnected())
				controller.createInstance(controller.model.getMuleFromAccountsList(), 0, null);
			
			if(squad == null) { // instance seule
				this.account.behaviour = this.fighterOptionsPanel.getSelectedBehaviour();
				controller.createInstance(this.account, this.fighterOptionsPanel.getSelectedAreaId(), null);
			}
			else { // escouade
				int squadSize = squad.size();
				int selectedAreaId = this.fighterOptionsPanel.getSelectedAreaId();
				Account account;
				if(this.fighterOptionsPanel.getSelectedBehaviour() == CharacterBehaviour.LONE_WOLF) {
					for(int i = 0; i < squadSize; ++i) {
						account = squad.get(i);
						account.behaviour = CharacterBehaviour.LONE_WOLF;
						controller.createInstance(account, selectedAreaId, account);
					}
				}
				else { // vaut -1 ici car comportement non d�fini
					for(int i = 0; i < squadSize; ++i) {
						account = squad.get(i);
						if(i == 0) { // capitaine
							account.behaviour = CharacterBehaviour.CAPTAIN;
							controller.createInstance(account, selectedAreaId, account);
						}
						else {
							account.behaviour = CharacterBehaviour.SOLDIER;
							controller.createInstance(account, 0, squad.firstElement());
						}
					}
				}
			}
			this.fighterOptionsPanel.dispose();
		}
	}
}