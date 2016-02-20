package gui;

import gui.View.LoginPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import main.Instance;

public class Controller {
	private View view;
	private Model model;

	public Controller() {
		this.view = new View();
		this.model = new Model();
		new StartListener(this.view.menuItem);
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
}