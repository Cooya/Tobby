package gui;

import gui.Model.Account;
import gui.View.FighterOptionsPanel;
import gui.View.LoginPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import controller.CharacterBehaviour;
import controller.characters.Character;
import main.Emulation;
import main.Log;

public class Controller {
	private static Controller self;
	
	private View view;
	private Model model;
	private FilesManager filesManager;
	private boolean globalDeconnectionInProgress;

	private Controller() {
		this.view = new View(); // lance tous les listeners
		this.model = new Model();
		this.filesManager = new FilesManager(view, model);
	}
	
	public static Controller getInstance() {
		if(self == null)
			self = new Controller();
		return self;
	}
	
	public synchronized void threadTerminated() {
		notifyAll(); // si un ou plusieurs threads sont en attente d'évènement, alors ils sont réveillés
	}

	// détermine si l'identifiant du personnage passé en paramètre est un id de personnage de l'application
	public boolean isWorkmate(double characterId) {
		for(Character character : this.model.getConnectedCharacters())
			if(character.infos.characterId == characterId)
				return true;
		return false;
	}

	// retourne l'objet Log correspondant au thread courant (utile pour les méthodes statiques)
	public Log getLog() {
		Thread currentThread = Thread.currentThread();
		for(Character character : this.model.getConnectedCharacters())
			for(Thread thread : character.threads)
				if(thread == currentThread)
					return character.log;
		return null;
	}
	
	// déconnexion du personnage correspondant au thread courant
	public void deconnectCurrentCharacter(String reason, boolean forced, boolean reconnection) {
		Character character = this.model.getCurrentCharacter();
		if(character != null)
			deconnectCharacter(character, reason, forced, reconnection);
	}

	// déconnexion de tous les personnages avec redémarrage ou non (les frames graphiques restent présentes)
	public void deconnectAllCharacters(String reason, boolean forced, boolean reconnection) {
		synchronized(this) {
			if(this.globalDeconnectionInProgress)
				return;
			this.globalDeconnectionInProgress = true;
		}
		new Thread() {
			@Override
			public void run() {
				// déconnexion (voire suppression) de tous les personnages connectés
				Log.info("Deconnecting all characters.");
				Vector<Character> characters = model.getConnectedCharacters();
				for(Character character : characters) {
					character.log.p(reason);
					character.log.flushBuffer();
					character.deconnectionOrder(forced);
					if(!reconnection)
						Controller.getInstance().model.removeCharacter(character.id);
				}
				
				// attente de l'interruption des threads de chaque personnage
				while(Thread.activeCount() > 3) // il ne reste normalement que l'UI thread, le thread d'émulation et le thread courant
					try {
						wait();
					} catch(InterruptedException e) {
						e.printStackTrace();
						exit("Thread UI interrupted.");
					}
				
				// réconnexion de tous les personnages si spécifié
				if(reconnection) {
					Emulation.killLauncher();
					Emulation.runLauncher();
					model.reconnectAllCharacters();
				}
				Controller.getInstance().globalDeconnectionInProgress = false;
			}
		}.start();
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
		this.filesManager.saveAccountsInFile();
		Emulation.killLauncher();
		Log.info("Application closed by user.");
		System.exit(0);
	}
	
	// création d'un personnage (passage au modèle)
	private synchronized void createCharacter(Account account, int areaId, Account captain) {
		// vérification de la disponibilité du compte (connecté ou non)
		if(this.model.isConnected(account))
			return;
			
		CharacterFrame frame = new CharacterFrame(account.id, account.login);
		this.view.addCharacterFrame(frame);
		frame.addInternalFrameListener(new CharacterFrameListener());
		frame.setVisible(true);
		this.model.createCharacter(account, areaId, frame, captain);
	}
	
	// déconnexion d'un personnage avec suppression ou non (la frame graphique reste présente)
	private void deconnectCharacter(Character character, String reason, boolean forced, boolean reconnection) {
		new Thread() {
			@Override
			public synchronized void run() {
				character.log.p(reason);
				character.log.flushBuffer();
				character.deconnectionOrder(forced);
				if(!reconnection)
					Controller.getInstance().model.removeCharacter(character.id);
				else {
					while(character.isActive())
						try {
							wait();
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
				}
				//this.model.reconnectCharacter(character); // TODO
			}
		}.start();
	}
	
	// méthode déclenchée par la fermeture d'une frame graphique
	private synchronized void killCharacter(JInternalFrame graphicalFrame) {
		int characterId = this.view.getCharacter(graphicalFrame).id;
		this.view.removeCharacterFrame(graphicalFrame);
		Character character = this.model.removeCharacter(characterId);
		if(character != null)
			character.deconnectionOrder(true);
	}

	// écoute du clic sur le bouton "load account"
	// => lancement de la boîte de dialogue pour la création d'un nouveau compte
	protected static class LoadAccountListener implements ActionListener {
		protected LoadAccountListener(AbstractButton button) {
			button.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			new ConnectionListener(self.view.createLoginPanel());
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
			String login = this.loginPanel.loginField.getText();
			String password = this.loginPanel.passwordField.getText();
			int serverId = Integer.parseInt(this.loginPanel.serverField.getText());
			int characterType = this.loginPanel.getSelectedType();
			if(login.isEmpty() || password.isEmpty() || serverId == 0)
				JOptionPane.showMessageDialog(null, "Missing informations.", "Erreur", JOptionPane.ERROR_MESSAGE);
			else {
				this.loginPanel.dispose();
				Account account = self.model.getAccount(login); // si le compte existe déjà
				if(account == null) {
					account = self.model.createAccount(characterType, login, password, serverId);
					self.view.newAccountItem(login, characterType);
				}
				new FighterOptionsPanelListener(self.view.createFighterOptionsPanel(account.behaviour, self.model.squads.nextFighterWillBe()), account); // affichage des options de combat éventuelles
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
			self.killCharacter(event.getInternalFrame());
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
			Account account = self.model.getAccount(this.accountItem.getText().split(" ")[0]);
			if(account != null)
				new FighterOptionsPanelListener(self.view.createFighterOptionsPanel(account.behaviour, self.model.squads.nextFighterWillBe()), account);
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
			Vector<Account> squad = self.model.squads.getSquad(this.squadItem.getText().split(" ")[0]);
			if(squad != null)
				new FighterOptionsPanelListener(self.view.createFighterOptionsPanel(CharacterBehaviour.CAPTAIN, -1), squad);
			else
				Log.err("Unknown squad.");
		}
	}

	// écoute du bouton "Go" dans la boîte de dialogue des options de combat
	// => démarrage d'un personnage
	private static class FighterOptionsPanelListener implements ActionListener {
		private FighterOptionsPanel fighterOptionsPanel;
		private Account account;
		private Vector<Account> squad;

		// loup solitaire
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
			 // si une mule n'est pas connectée, on en connecte une
			if((this.account == null  || this.account.behaviour != CharacterBehaviour.WAITING_MULE) && !self.model.muleIsConnected())
				self.createCharacter(self.model.getMuleFromAccountsList(), 0, null);
			
			if(squad == null) { // loup solitaire
				this.account.behaviour = this.fighterOptionsPanel.getSelectedBehaviour();
				self.createCharacter(this.account, this.fighterOptionsPanel.getSelectedAreaId(), null);
			}
			else { // escouade
				int squadSize = squad.size();
				int selectedAreaId = this.fighterOptionsPanel.getSelectedAreaId();
				Account account;
				if(this.fighterOptionsPanel.getSelectedBehaviour() == CharacterBehaviour.LONE_WOLF) {
					for(int i = 0; i < squadSize; ++i) {
						account = squad.get(i);
						account.behaviour = CharacterBehaviour.LONE_WOLF;
						self.createCharacter(account, selectedAreaId, account);
					}
				}
				else { // vaut -1 ici car comportement non défini
					for(int i = 0; i < squadSize; ++i) {
						account = squad.get(i);
						if(i == 0) { // capitaine
							account.behaviour = CharacterBehaviour.CAPTAIN;
							self.createCharacter(account, selectedAreaId, account);
						}
						else {
							account.behaviour = CharacterBehaviour.SOLDIER;
							self.createCharacter(account, 0, squad.firstElement());
						}
					}
				}
			}
			this.fighterOptionsPanel.dispose();
		}
	}
}