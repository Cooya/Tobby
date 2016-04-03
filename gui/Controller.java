package gui;

import gui.Model.Account;
import gui.View.FighterOptionsPanel;
import gui.View.LoginPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import main.Emulation;
import main.Instance;
import main.Log;

public class Controller {
	private static final String accountsFilePath = "Ressources/accounts.txt";
	private static final String EOL = System.getProperty("line.separator");
	private static View view;
	private static Model model;

	// point d'entrée de l'application graphique
	public static void runApp() {
		view = new View();
		model = new Model();
		loadAccountsFile();
		new StartListener(view.menuItem);
	}

	public static boolean isWorkmate(double characterId) {
		for(Instance instance : model.getConnectedInstances())
			if(instance.getCharacterId() == characterId)
				return true;
		return false;
	}

	public static Log getLog() {
		Thread currentThread = Thread.currentThread();
		for(Instance instance : model.getConnectedInstances())
			for(Thread thread : instance.threads)
				if(thread == currentThread)
					return instance.log;
		return null;
	}
	
	// création d'une instance (passage au modèle)
	private synchronized static void createInstance(Account account, int areaId) {
		Emulation.runLauncherIfNecessary();
		CharacterFrame frame = new CharacterFrame(account.id, account.login);
		view.addCharacterFrame(frame);
		frame.addInternalFrameListener(new CharacterFrameListener());
		frame.setVisible(true);
		model.createInstance(account, areaId, frame);
	}

	// méthode déclenchée par la fermeture d'une frame graphique
	private synchronized static void killInstance(JInternalFrame graphicalFrame) {
		int instanceId = view.getInstance(graphicalFrame).id;
		view.removeCharacterFrame(graphicalFrame);
		Instance instance = model.removeInstance(instanceId);
		if(instance != null)
			instance.interruptThreads();
	}

	// méthode déclenchée lors d'une erreur critique (la frame graphique reste présente)
	public static void deconnectInstance(String reason) {
		Instance instance = model.getCurrentInstance();
		instance.log.p(reason);
		instance.interruptThreads();
		model.removeInstance(instance.id);
	}

	// méthode déclenchée lors de la connexion d'un modérateur (les frames graphiques restent présentes)
	public static void deconnectAllInstances(String reason) {
		Vector<Instance> instances = model.getConnectedInstances();
		for(Instance instance : instances) {
			instance.log.p(reason);
			instance.interruptThreads();
			model.removeInstance(instance.id);
		}
	}
	
	protected static void exit() {
		saveAccountsInFile();
		Emulation.killLauncher();
		System.exit(0);
	}

	// chargement de la liste des comptes dans le fichier texte "accounts.txt"
	private static void loadAccountsFile() {
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(accountsFilePath));
			String line;
			Account account;
			JMenuItem item;
			while((line = buffer.readLine()) != null) {
				if(!line.equals("")) {
					account = model.createAccount(line);
					if(account.behaviour < 10)
						item = new JMenuItem(account.login + " (salesman)");
					else
						item = new JMenuItem(account.login + " (fighter)");
					view.accountsListItems.add(item);
					view.accountsMenu.add(item);
					new AccountItemListener(item);
				}
			}
			buffer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// sauvegarde de la liste des comptes dans le fichier texte "accounts.txt" (effectuée à la fermeture de l'application)
	private static void saveAccountsInFile() {
		Set<Account> accounts = model.getAllAccounts();
		String str = "";
		for(Account account : accounts)
			str += account.behaviour + " " + account.login + " " + account.password + " " + account.serverId + EOL;
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(accountsFilePath, false);
			fileWriter.write(str);
			fileWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	// écoute du clic du bouton "load account" dans le menu déroulant
	// => lancement de la boîte de dialogue pour la création d'un nouveau compte
	private static class StartListener implements ActionListener {
		private StartListener(AbstractButton button) {
			button.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			new ConnectionListener(view.createLoginPanel());
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
				Account account = model.getAccount(login); // si le compte existe déjà
				if(account == null) {
					account = model.createAccount(characterType, login, password, serverId);
					JMenuItem item = new JMenuItem(login);
					view.accountsListItems.add(item);
					view.accountsMenu.add(item);
					new AccountItemListener(item);
				}
				new FighterOptionsPanelListener(view.createFighterOptionsPanel(account.behaviour, model.nextFighterWillBe()), account); // affichage des options de combat éventuelles
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
			killInstance(event.getInternalFrame());
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
	private static class AccountItemListener implements ActionListener {
		private JMenuItem accountItem;

		private AccountItemListener(JMenuItem item) {
			this.accountItem = item;
			this.accountItem.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			Account account = model.getAccount(this.accountItem.getText().split(" ")[0]);
			if(account != null)
				new FighterOptionsPanelListener(view.createFighterOptionsPanel(account.behaviour, model.nextFighterWillBe()), account);
		}
	}

	// écoute du bouton "Go" dans la boîte de dialogue des options de combat
	// => lancement d'une instance
	private static class FighterOptionsPanelListener implements ActionListener {
		private FighterOptionsPanel fighterOptionsPanel;
		private Account account;

		private FighterOptionsPanelListener(FighterOptionsPanel fighterOptionsPanel, Account account) {
			this.fighterOptionsPanel = fighterOptionsPanel;
			this.account = account;
			this.fighterOptionsPanel.submitButton.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			account.behaviour = this.fighterOptionsPanel.getSelectedBehaviour();
			createInstance(account, this.fighterOptionsPanel.getSelectedAreaId());
			this.fighterOptionsPanel.dispose();
		}
	}
}