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

	// détermine si l'identifiant du personnage passé en paramètre est un id d'instance de l'application
	public boolean isWorkmate(double characterId) {
		for(Instance instance : this.model.getConnectedInstances())
			if(instance.getCharacter().infos.characterId == characterId)
				return true;
		return false;
	}

	// retourne l'objet Log correspondant au thread courant (utile pour les méthodes statiques)
	public Log getLog() {
		Thread currentThread = Thread.currentThread();
		for(Instance instance : this.model.getConnectedInstances())
			for(Thread thread : instance.threads)
				if(thread == currentThread)
					return instance.log;
		return null;
	}
	
	// déconnexion de l'instance du thread courant
	public void deconnectCurrentInstance(String reason, boolean forced, boolean reconnection) {
		Instance instance = this.model.getCurrentInstance();
		if(instance != null)
			deconnectInstance(instance, reason, forced, reconnection);
	}

	// déconnexion de toutes les instances avec redémarrage ou non (les frames graphiques restent présentes)
	// à ne pas appeler depuis le thread UI
	public synchronized void deconnectAllInstances(String reason, boolean forced, boolean reconnection) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					// déconnexion (voire suppression) de toutes les instances connectées
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
					
					// réconnexion de toutes les instances si spécifié
					if(reconnection) {
						Emulation.killLauncher();
						Emulation.runLauncher();
						model.restartAllInstances(); // redémarrage de toutes les instances
					}
				}
			});
		} catch(InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			exit("Error during invocation of the deconnection task.");
		}
	}

	// fonction appelée lors d'une erreur critique
	public void exit(String reason) {
		this.filesManager.saveAccountsInFile();
		Emulation.killLauncher();
		Log.err(reason);
		System.exit(0);
	}
	
	// fonction appelée à la fermeture de l'application
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
	
	// création d'une instance (passage au modèle)
	private synchronized void createInstance(Account account, int areaId, Account captain) {
		// vérification de la disponibilité du compte (connecté ou non)
		if(this.model.isConnected(account))
			return;
			
		CharacterFrame frame = new CharacterFrame(account.id, account.login);
		this.view.addCharacterFrame(frame);
		frame.addInternalFrameListener(new CharacterFrameListener());
		frame.setVisible(true);
		this.model.createInstance(account, areaId, frame, captain);
	}
	
	// déconnexion d'une instance avec suppression de l'instance ou non (la frame graphique reste présente)
	private synchronized void deconnectInstance(Instance instance, String reason, boolean forced, boolean reconnection) {
		instance.log.p(reason);
		instance.log.flushBuffer();
		instance.deconnectionOrder(forced);
		if(!reconnection)
			this.model.removeInstance(instance.id);
		// TODO -> reconnexion à implémenter
	}
	
	// méthode déclenchée par la fermeture d'une frame graphique
	private synchronized void killInstance(JInternalFrame graphicalFrame) {
		int instanceId = this.view.getInstance(graphicalFrame).id;
		this.view.removeCharacterFrame(graphicalFrame);
		Instance instance = this.model.removeInstance(instanceId);
		if(instance != null)
			instance.deconnectionOrder(true);
	}

	// écoute du clic sur le bouton "load account"
	// => lancement de la boîte de dialogue pour la création d'un nouveau compte
	protected static class LoadAccountListener implements ActionListener {
		protected LoadAccountListener(AbstractButton button) {
			button.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			new ConnectionListener(Controller.getInstance().view.createLoginPanel());
		}
	}

	// écoute du bouton "Run" dans la boîte de dialogue de création de nouveau de compte
	// => création d'un compte, ajout dans le fichier texte et lancement de la boîte de dialogue des options de combat
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
				Account account = controller.model.getAccount(login); // si le compte existe déjà
				if(account == null) {
					account = controller.model.createAccount(characterType, login, password, serverId);
					controller.view.newAccountItem(login, characterType);
				}
				new FighterOptionsPanelListener(controller.view.createFighterOptionsPanel(account.behaviour, controller.model.squads.nextFighterWillBe()), account); // affichage des options de combat éventuelles
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

	// écoute du bouton de lancement rapide d'un compte dans la liste des comptes du menu déroulant
	// => lancement de la boîte de dialogue des options de combat
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
	
	// écoute du bouton de lancement rapide d'une escouade dans la liste des escouades du menu déroulant
	// => lancement de la boîte de dialogue des options de combat de l'escouade
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

	// écoute du bouton "Go" dans la boîte de dialogue des options de combat
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
			
			 // si une mule n'est pas connectée, on en connecte une
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
				else { // vaut -1 ici car comportement non défini
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