package gui;

import gui.View.LoginPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import main.Instance;

public class Controller {
	private String accountsFilePath="Ressources/accounts.txt";
	private View view;
	private Model model;

	public Controller() {
		this.view = new View();
		this.model = new Model();
		loadingAccountsList();
		new StartListener(this.view.menuItem);
	}

	private void loadingAccountsList() {
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(accountsFilePath));
			String[] splitLine;
			String line;
			line = buffer.readLine();
			while(line != null) {
				splitLine = line.split(" ");
				model.accounts.put(splitLine[0], line);
				JMenuItem account=new JMenuItem(splitLine[0]);
				view.accountsListItems.add(account);
				view.accountsMenu.add(account);
				new AccountItemListener(account);
				line = buffer.readLine();
			}
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	private void killInstance(JInternalFrame graphicalFrame) {
		int instanceId = this.view.instancesId.get(graphicalFrame);
		Instance instance = this.model.instances.get(instanceId);
		for(Thread thread : instance.threads)
			thread.interrupt();
	}

	private class StartListener implements ActionListener {
		private StartListener(AbstractButton button) {
			button.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			new ConnectionListener(view.createLoginPanel());
		}
	}

	private class ConnectionListener implements ActionListener {
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
				CharacterFrame frame = new CharacterFrame(login);
				Instance instance = new Instance(login, password, serverId, frame);
				model.instances.put(instance.id, instance);
				view.desktopPane.add(frame);
				view.instancesId.put(frame, instance.id);
				loginPanel.dispose();
				try {
					//Ajout du compte dans le fichier de sauvegarde
					if(model.accounts.get(login)==null){
						BufferedWriter buffer = new BufferedWriter(new FileWriter(accountsFilePath,true));
						buffer.write(login+" "+password+" "+serverId);
						buffer.newLine();
						buffer.close();
						model.accounts.put(login, login+" "+password+" "+serverId);
						JMenuItem account=new JMenuItem(login);
						view.accountsListItems.add(account);
						view.accountsMenu.add(account);
						new AccountItemListener(account);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				frame.addInternalFrameListener(new CharacterFrameListener());
				frame.setVisible(true);

			}
		}
	}

	private class CharacterFrameListener implements InternalFrameListener {

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

	private class AccountItemListener implements ActionListener{
		public JMenuItem accountItem;

		private AccountItemListener(JMenuItem item) {
			this.accountItem=item;
			accountItem.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			String[] connectionInfos = model.accounts.get(this.accountItem.getText()).split(" ");
			CharacterFrame frame = new CharacterFrame(connectionInfos[0]);
			Instance instance = new Instance(connectionInfos[0], connectionInfos[1], Integer.parseInt(connectionInfos[2]), frame);
			model.instances.put(instance.id, instance);
			view.desktopPane.add(frame);
			view.instancesId.put(frame, instance.id);
			frame.addInternalFrameListener(new CharacterFrameListener());
			frame.setVisible(true);
		}
	}





}