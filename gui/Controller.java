package gui;

import gui.Model.Account;
import gui.View.LoginPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.AbstractButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import main.Instance;
import main.Log;

public class Controller {
	private static final String accountsFilePath = "Ressources/accounts.txt";
	private static View view = new View();
	private static Model model = new Model();

	public static void runApp() {
		loadAccountsList();
		new StartListener(view.menuItem);
	}
	
	public static void restartInstance() {
		Instance instance = model.getCurrentInstance();
		instance.interruptThreads();
		// TODO
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

	private static void loadAccountsList() {
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(accountsFilePath));
			String line;
			Account account;
			JMenuItem item;
			while((line = buffer.readLine()) != null) {
				if(!line.equals("")) {
					account = model.createAccount(line);
					if(account.type == 0)
						item = new JMenuItem(account.login + " (mule)");
					else
						item = new JMenuItem(account.login);
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
	
	private static void createInstance(Account account) {
		CharacterFrame frame = new CharacterFrame(account.id, account.login);
		view.addCharacterFrame(frame);
		frame.addInternalFrameListener(new CharacterFrameListener());
		frame.setVisible(true);
		
		Instance instance = new Instance(account.id, account.type, account.login, account.password, account.serverId, frame);
		model.addInstance(account, instance);
	}

	private static void killInstance(JInternalFrame graphicalFrame) {
		int instanceId = view.getInstance(graphicalFrame).id;
		view.removeCharacterFrame(graphicalFrame);
		
		Instance instance = model.getInstance(instanceId);
		model.removeInstance(instance);
		instance.interruptThreads();
	}

	private static class StartListener implements ActionListener {
		private StartListener(AbstractButton button) {
			button.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			new ConnectionListener(view.createLoginPanel());
		}
	}

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
				loginPanel.dispose();
				Account account = model.getAccount(login);
				if(account == null) {
					account = model.createAccount(1, login, password, serverId);
					try {
						BufferedWriter buffer = new BufferedWriter(new FileWriter(accountsFilePath, true));
						buffer.newLine();
						buffer.write("1 " + login + " " + password + " " + serverId);
						buffer.close();
					} catch(Exception e) {
						e.printStackTrace();
					}
					JMenuItem item = new JMenuItem(login);
					view.accountsListItems.add(item);
					view.accountsMenu.add(item);
					new AccountItemListener(item);
				}
				createInstance(account);
			}
		}
	}

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

	private static class AccountItemListener implements ActionListener {
		private JMenuItem accountItem;

		private AccountItemListener(JMenuItem item) {
			this.accountItem = item;
			this.accountItem.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			Account account = model.getAccount(this.accountItem.getText().split(" ")[0]);
			if(account != null)
				createInstance(account);
		}
	}
}