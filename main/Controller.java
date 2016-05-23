package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import main.AccountsManager.Account;
import main.SquadsManager.Squad;
import main.View.FighterOptionsPanel;
import main.View.LoginPanel;
import utilities.Reflection;
import controller.CharacterBehaviour;
import controller.characters.Character;

public class Controller {
	private static Controller self;
	
	private View view;
	private AccountsManager accounts;
	private CharactersManager characters;
	private SquadsManager squads;
	private boolean globalDeconnectionInProgress;

	private Controller() {
		if(Main.GRAPHICAL_MODE)
			this.view = new View(); // lance tous les listeners
		else
			this.view = null;
		this.accounts = new AccountsManager();
		this.squads = new SquadsManager();
		this.characters = new CharactersManager(this.accounts, this.squads);
		this.squads.setCharactersManager(this.characters);
		this.globalDeconnectionInProgress = false;
	}
	
	public static Controller getInstance() {
		if(self == null)
			self = new Controller();
		return self;
	}
	
	public synchronized void threadTerminated() {
		notifyAll(); // si un ou plusieurs threads sont en attente d'�v�nement, alors ils sont r�veill�s
	}

	// d�termine si l'identifiant du personnage pass� en param�tre est un id de personnage de l'application
	public boolean isWorkmate(double characterId) {
		for(Character character : this.characters.getConnectedCharacters(0))
			if(character.infos.getCharacterId() == characterId)
				return true;
		return false;
	}

	// retourne l'objet Log correspondant au thread courant (utile pour les m�thodes statiques)
	public Log getLog() {
		Thread currentThread = Thread.currentThread();
		for(Character character : this.characters.getConnectedCharacters(0))
			for(Thread thread : character.threads)
				if(thread == currentThread)
					return character.log;
		return null;
	}

	// fonction appel�e � la fermeture de l'application (graphique ou CLI) ou lors d'une erreur critique
	public void exit(String reason) {
		Emulation.killLauncher();
		Collection<Character> characters = this.characters.getConnectedCharacters(0);
		for(Character character : characters)
			character.log.flushBuffer();
		
		DatabaseConnection.unlockAllAccounts();
		
		if(reason != null)
			Log.err(reason);
		else
			Log.info("Application closed by user.");
		System.exit(0);
	}
	
	public Account newAccount(String login, String password, int serverId) {
		// TODO -> verifier si le compte n'existe pas d�j�
		Account account = self.accounts.createAccount(login, password, serverId);
			if(account != null && this.view != null)
				this.view.newAccountItem(login, serverId);
		return account;
	}
	
	public void createSquad(String name, int[] ids) {
		Vector<Account> members = new Vector<Account>(ids.length);
		Account account;
		for(int id : ids) {
			account = this.accounts.retrieveAccount(id);
			if(account != null)
				members.add(account);
			else
				Log.err("Account with id = " + id  + " does not exist.");
		}
		this.squads.createFixedSquad(name, members);
	}
	
	// connexion d'un personnage (passage au mod�le)
	public void connectCharacter(Account account, int serverId, int areaId, int captainId) {
		if(Main.GRAPHICAL_MODE) {
			CharacterFrame frame = new CharacterFrame(account.id, account.login);
			this.view.addCharacterFrame(frame);
			frame.addInternalFrameListener(new CharacterFrameListener());
			frame.setVisible(true);
			this.characters.connectCharacter(account, serverId, areaId, frame, captainId);
		}
		else
			this.characters.connectCharacter(account, serverId, areaId, null, captainId);
	}
	
	// connexion d'un personnage depuis l'interface graphique ou en ligne de commande
	public void connectCharacter(int accountId, int serverId, int areaId) {
		Account account = this.accounts.retrieveAccount(accountId);
		if(account == null) {
			Log.err("Invalid account id or account already used.");
			return;
		}
		connectCharacter(account, serverId, areaId, -1);
	}
	
	// connexion d'un personnage en ligne de commande
	public void connectCharacter(String login, int serverId, int areaId) {
		Account account = this.accounts.retrieveAccount(login);
		if(account == null) {
			Log.err("Invalid account id or account already used.");
			return;
		}
		connectCharacter(account, serverId, areaId, -1);
	}
	
	public void connectCharacters(int number, int serverId, int areaId) {
		for(Account account : this.accounts.retrieveAccounts(number))
			connectCharacter(account, serverId, areaId, -1);
	}
	
	public void connectSquad(int squadId, int serverId, int areaId, boolean fightTogether) {
		Squad squad = this.squads.getSquad(squadId);
		if(squad == null) {
			Log.err("Invalid squad id.");
			return;
		}
		
		Vector<Account> members = squad.getMembers();
		// check le statut de connexion
		if(!fightTogether)
			for(Account member : members)
				connectCharacter(member, serverId, areaId, -1);
		else {
			Account account = members.get(0);
			int captainId = account.id;
			connectCharacter(account, serverId, areaId, captainId); // connexion du capitaine
			int squadSize = members.size();
			for(int i = 1; i < squadSize; ++i) {
				account = members.get(i);
				connectCharacter(account, serverId, 0, captainId); // connexion des soldats
			}
		}
	}
	
	// d�connexion du personnage correspondant au thread courant
	public void deconnectCurrentCharacter(String reason, boolean forced, boolean reconnection) {
		Character character = this.characters.getCurrentCharacter();
		if(character != null)
			deconnectCharacter(character, reason, forced, reconnection);
	}
	
	public void deconnectCharacter(int accountId) {
		Character character = this.characters.getCharacter(accountId);
		if(character != null)
			deconnectCharacter(character, "Deconnected by console interface.", true, false);
		else
			Log.err("Account with id = " + accountId + " is not connected or does not exist.");
	}

	// d�connexion de tous les personnages avec red�marrage ou non (les frames graphiques restent pr�sentes)
	public void globalDeconnection(String reason, boolean forced, boolean reconnection) {
		synchronized(this) {
			if(this.globalDeconnectionInProgress)
				return;
			this.globalDeconnectionInProgress = true;
		}
		if(Main.GRAPHICAL_MODE) {
			new Thread() {
				@Override
				public void run() {
					deconnectCharacters(reason, 0, forced, reconnection);
				}
			}.start();
		}
		else
			deconnectCharacters(reason, 0, forced, reconnection);
	}
	
	// d�connexion de tous les personnages connect�s sur un serveur donn�
	public void serverDeconnection(String reason, int serverId, boolean forced, boolean reconnection) {
		if(Main.GRAPHICAL_MODE) {
			new Thread() {
				@Override
				public void run() {
					deconnectCharacters(reason, serverId, forced, reconnection);
				}
			}.start();
		}
		else
			deconnectCharacters(reason, serverId, forced, reconnection);
	}
	
	private synchronized void deconnectCharacters(String reason, int serverId, boolean forced, boolean reconnection) {
		// d�connexion (voire suppression) de tous les personnages connect�s
		Log.info("Deconnecting all characters...");
		Collection<Character> characters = self.characters.getConnectedCharacters(serverId);
		for(Character character : characters) {
			character.log.p(reason);
			character.deconnectionOrder(forced);
			if(!reconnection)
				self.characters.removeCharacter(character.id);
		}
		
		int expectedThreadCount;
		if(Main.GRAPHICAL_MODE)
			expectedThreadCount = 3; // UI thread + SQL thread + thread courant
		else
			expectedThreadCount = 2; // CLI thread + SQL thread
		
		// attente de l'interruption des threads de chaque personnage
		while(Thread.activeCount() > expectedThreadCount)
			try {
				wait();
				System.out.println(Thread.activeCount()); // TODO -> d�connexion globale � corriger
			} catch(InterruptedException e) {
				e.printStackTrace();
				exit("Global deconnection thread interrupted.");
			}
		Log.info("All characters are deconnected.");
		
		// r�connexion de tous les personnages si sp�cifi�
		if(reconnection) {
			/* TODO -> reconnexion � stabiliser
			Emulation.killLauncher();
			Emulation.runLauncher();
			model.reconnectAllCharacters();
			*/
		}
		self.globalDeconnectionInProgress = false;
	}
	
	// d�connexion d'un personnage avec suppression ou non (la frame graphique reste pr�sente)
	private void deconnectCharacter(Character character, String reason, boolean forced, boolean reconnection) {
		new Thread() {
			@Override
			public synchronized void run() {
				character.log.p(reason);
				character.log.flushBuffer();
				character.deconnectionOrder(forced);
				if(!reconnection)
					self.characters.removeCharacter(character.id);
				else {
					while(character.isActive())
						try {
							wait();
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					//model.reconnectCharacter(character); // TODO -> reconnexion � stabiliser
				}
			}
		}.start();
	}
	
	public void deconnectSquad(int squadId) {
		Squad squad = this.squads.getSquad(squadId);
		if(squad != null) {
			Vector<Account> members = squad.getMembers();
			for(Account member : members)
				if(member.isConnected)
					deconnectCharacter(this.characters.getCharacter(member.id), "Deconnected by console interface.", true, false);
		}
		else
			Log.err("Squad with id = " + squadId + " does not exist.");
	}
	
	public void displayAllAccounts() {
		System.out.print(this.accounts);
	}
	
	public void displayAllSquads() {
		System.out.print(this.squads);
	}
	
	public void displayLog() {
		Log.displayGlobalLog();
	}
	
	public void displayLog(int accountId) {
		Character character = this.characters.getCharacter(accountId);
		if(character != null)	
			character.log.displayLog(20);
		else
			System.out.println("Character not connected on this computer.");
	}
	
	public void displayPersonalInfos(int accountId) {
		Character character = this.characters.getCharacter(accountId);
		if(character != null)	
			Reflection.explore(character.infos, 1);
		else
			System.out.println("Character not connected on this computer.");
	}
	
	public void displayPersonalInfos(String login) {
		Character character = this.characters.getCharacter(login);
		if(character != null)	
			Reflection.explore(character.infos, 1);
		else
			System.out.println("Character not connected on this computer.");
	}
	
	public void displayGlobalInfos() {
		Collection<Character> characters = this.characters.getInGameFighters();
		StringBuilder str = new StringBuilder();
		for(Character character : characters) {
			str.append(character.id);
			str.append(" ");
			str.append(character.infos.getLogin());
			str.append(" -> win : ");
			str.append(character.infos.getFightsWonCounter());
			str.append(", lost : ");
			str.append(character.infos.getFightsLostCounter());
			str.append(", level : ");
			str.append(character.infos.getLevel());
			str.append(", weight : ");
			str.append(character.infos.getWeight());
			str.append("/");
			str.append(character.infos.getWeightMax());
			System.out.println(str);
			str.setLength(0);
		}
	}
	
	// m�thode d�clench�e par la fermeture d'une frame graphique
	private synchronized void killCharacter(JInternalFrame graphicalFrame) {
		int characterId = this.view.getCharacter(graphicalFrame).id;
		this.view.removeCharacterFrame(graphicalFrame);
		Character character = this.characters.removeCharacter(characterId);
		if(character != null) {
			character.log.flushBuffer();
			character.deconnectionOrder(true);
		}
	}
	
	// �coute du clic sur le bouton "load account"
	// => lancement de la bo�te de dialogue pour la cr�ation d'un nouveau compte
	protected static class LoadAccountListener implements ActionListener {
		protected LoadAccountListener(AbstractButton button) {
			button.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			new ConnectionListener(self.view.createLoginPanel());
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
			String login = this.loginPanel.loginField.getText();
			String password = this.loginPanel.passwordField.getText();
			int serverId = Integer.parseInt(this.loginPanel.serverField.getText());
			if(login.isEmpty() || password.isEmpty() || serverId == 0)
				JOptionPane.showMessageDialog(null, "Missing informations.", "Erreur", JOptionPane.ERROR_MESSAGE);
			else {
				this.loginPanel.dispose();
				Account account = self.newAccount(login, password, serverId);
				new FighterOptionsPanelListener(self.view.createFighterOptionsPanel(account.serverId, self.squads.nextFighterWillBe()), account.id, true); // affichage des options de combat �ventuelles
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

	// �coute du bouton de lancement rapide d'un compte dans la liste des comptes du menu d�roulant
	// => lancement de la bo�te de dialogue des options de combat
	protected static class AccountItemListener implements ActionListener {
		private JMenuItem accountItem;

		protected AccountItemListener(JMenuItem item) {
			this.accountItem = item;
			this.accountItem.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			/*
			Account account = self.accounts.getAccount(this.accountItem.getText().split(" ")[0]);
			if(account != null)
				new FighterOptionsPanelListener(self.view.createFighterOptionsPanel(account.serverId, self.squads.nextFighterWillBe()), account.id, true);
			else
				Log.err("Unknown account.");
			*/
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
			int id = self.squads.getSquadId(this.squadItem.getText().split(" ")[0]);
			if(id >= 0)
				new FighterOptionsPanelListener(self.view.createFighterOptionsPanel(CharacterBehaviour.CAPTAIN, -1), id, false);
			else
				Log.err("Unknown squad.");
		}
	}

	// �coute du bouton "Go" dans la bo�te de dialogue des options de combat
	// => d�marrage d'un personnage
	private static class FighterOptionsPanelListener implements ActionListener {
		private FighterOptionsPanel fighterOptionsPanel;
		private int id;
		private boolean singleAccount;

		private FighterOptionsPanelListener(FighterOptionsPanel fighterOptionsPanel, int id, boolean singleAccount) {
			this.fighterOptionsPanel = fighterOptionsPanel;
			this.id = id;
			this.singleAccount = singleAccount;
			this.fighterOptionsPanel.submitButton.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			if(this.singleAccount)
				self.connectCharacter(this.id, 11, this.fighterOptionsPanel.getSelectedAreaId());
			else // escouade
				self.connectSquad(this.id, 11, this.fighterOptionsPanel.getSelectedAreaId(), this.fighterOptionsPanel.getSelectedBehaviour() != CharacterBehaviour.LONE_WOLF);
			this.fighterOptionsPanel.dispose();
		}
	}
}