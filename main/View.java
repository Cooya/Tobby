package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.CharacterBehaviour;
import controller.informations.FightOptions;

public class View {
	private JFrame frame;
	private JDesktopPane desktopPane;
	private Vector<CharacterFrame> charactersFrame;
	protected JButton loadAccountButton;
	protected JMenu accountsListMenu;
	protected JMenu squadsListMenu;

	protected View() {
		this.charactersFrame = new Vector<CharacterFrame>();
		
		this.loadAccountButton = new JButton("Load account");
		new Controller.LoadAccountListener(this.loadAccountButton);
		this.accountsListMenu = new JMenu("Accounts list");
		this.squadsListMenu = new JMenu("Squads list");
		

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(this.loadAccountButton);
		menuBar.add(this.accountsListMenu);
		menuBar.add(this.squadsListMenu);

		desktopPane = new JDesktopPane();
		desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

		frame = new JFrame("Tobby");
		frame.setJMenuBar(menuBar);
		frame.setContentPane(desktopPane);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				frame.dispose();
				Controller.getInstance().exit(null);
			}
		});
		frame.setVisible(true);
	}
	
	protected void newAccountItem(String login, int serverId) {
		JMenuItem item;
		if(serverId != 0)
			item = new JMenuItem(login + " (salesman)");
		else
			item = new JMenuItem(login + " (fighter)");
		this.accountsListMenu.add(item);
		new Controller.AccountItemListener(item);
	}
	
	protected void newSquadItem(String squadName, int size) {
		JMenuItem item = new JMenuItem(squadName + " (" + size + ")");
		this.squadsListMenu.add(item);
		new Controller.SquadItemListener(item);
	}
	
	protected CharacterFrame getCharacter(JInternalFrame graphicalFrame) {
		for(CharacterFrame frame : this.charactersFrame)
			if(frame == graphicalFrame)
				return frame;
		return null;
	}
	
	protected CharacterFrame getCharacterFrame(int characterId) {
		for(CharacterFrame frame : this.charactersFrame)
			if(frame.id == characterId)
				return frame;
		return null;
	}
	
	protected void addCharacterFrame(CharacterFrame frame) {
		boolean posTaken;

		int size = this.charactersFrame.size();
		if(size == 0 || size >= 8) {
			frame.setLocation(0, 0);
			this.desktopPane.add(frame);
			this.charactersFrame.add(frame);
			return;
		}

		for(int y = 0; y < CharacterFrame.FRAME_SIZE * 2; y += CharacterFrame.FRAME_SIZE)
			for(int x = 0; x < CharacterFrame.FRAME_SIZE * 4; x += CharacterFrame.FRAME_SIZE) {
				posTaken = false;
				for(CharacterFrame charFrame : this.charactersFrame)
					if(charFrame.getX() == x && charFrame.getY() == y) {
						posTaken = true;
						break;
					}
				if(!posTaken) {
					frame.setLocation(x, y);
					this.desktopPane.add(frame);
					this.charactersFrame.add(frame);
					return;
				}
			}
	}

	protected void removeCharacterFrame(JInternalFrame frame) {
		this.charactersFrame.remove(frame);
	}

	protected LoginPanel createLoginPanel() {
		return new LoginPanel("Login panel");
	}

	protected FighterOptionsPanel createFighterOptionsPanel(int behaviour, int nextFighterBehaviour) {
		return new FighterOptionsPanel("Fighter options panel", behaviour, nextFighterBehaviour);
	}
	
	// boîte de dialogue permettant d'ajouter un nouveau compte
	protected class LoginPanel extends JFrame implements ActionListener {
		private static final long serialVersionUID = 5372329190332625490L;
		public JTextField loginField;
		public JTextField passwordField;
		public JTextField serverField;
		public JButton connectButton;
		private int selectedType;

		private LoginPanel(String title) {
			super(title);
			this.selectedType = CharacterBehaviour.LONE_WOLF;

			// zone login
			JPanel loginPanel = new JPanel();
			this.loginField = new JTextField(20);
			loginPanel.add(new JLabel("Login :     "));
			loginPanel.add(this.loginField);

			// zone mot de passe
			JPanel passwordPanel = new JPanel();
			this.passwordField = new JTextField(20);
			passwordPanel.add(new JLabel("Password :"));
			passwordPanel.add(this.passwordField);

			// zone serveur
			JPanel serverPanel = new JPanel();
			this.serverField = new JTextField(3);
			serverPanel.add(new JLabel("Server :"));
			serverPanel.add(this.serverField);

			this.connectButton = new JButton("Run");

			setSize(300, 300);
			setLocationRelativeTo(null);
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
			add(loginPanel);
			add(passwordPanel);
			add(serverPanel);
			add(this.connectButton);
			setVisible(true);
		}

		protected int getSelectedType() {
			return this.selectedType;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			this.selectedType = Integer.valueOf(event.getActionCommand());
		}
	}

	// boîte de dialogue permettant de sélectionner les options de combat
	// elle affiche les dernières options sélectionnées pour le personnage
	protected class FighterOptionsPanel extends JFrame implements ActionListener, ItemListener {
		private static final long serialVersionUID = 3793718835483044716L;
		
		private int selectedBehaviour;
		private JCheckBox isLoneWolf;

		// éléments en communs
		private JList<String> areaList;
		private JPanel areasPanel; // élément affichable ou non
		public JButton submitButton;

		// variable utile pour l'affichage des aires de combat ou non
		private int nextFighterBehaviour; // si ce sera un capitaine ou un soldat

		private FighterOptionsPanel(String title, int behaviour, int nextFighterBehaviour) {
			super(title);
			this.nextFighterBehaviour = nextFighterBehaviour;
			if(behaviour == CharacterBehaviour.CAPTAIN || behaviour == CharacterBehaviour.SOLDIER)
				behaviour = this.nextFighterBehaviour;
			this.selectedBehaviour = behaviour;
			
			JPanel loneWolfPanel = new JPanel();
			if(nextFighterBehaviour != -1) // lancement d'un seul combattant à la fois
				loneWolfPanel.add(new JLabel("Lone wolf"));
			else // lancement d'une escouade
				loneWolfPanel.add(new JLabel("Lone wolves"));
			this.isLoneWolf = new JCheckBox();
			this.isLoneWolf.setSelected(this.selectedBehaviour == CharacterBehaviour.LONE_WOLF);
			this.isLoneWolf.addItemListener(this);
			loneWolfPanel.add(this.isLoneWolf);

			add(loneWolfPanel);

			// création de la liste des aires
			Collection<String> areaNames = FightOptions.getAreaNames();
			DefaultListModel<String> model = new DefaultListModel<String>();
			for(String name : areaNames)
				model.addElement(name);

			// création graphique de la liste des aires
			this.areasPanel = new JPanel();
			this.areasPanel.add(new JLabel("Select an area :"));
			this.areaList = new JList<>(model);
			this.areasPanel.add(this.areaList);

			displayAreasPanel(); // affichage ou non selon le comportement selectionné

			// bouton de validation
			JPanel submitPanel = new JPanel();
			this.submitButton = new JButton("Go");
			submitPanel.add(this.submitButton);

			add(submitPanel);

			// mise en place de la boîte de dialogue
			setSize(300, 350);
			setLocationRelativeTo(null);
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
			setVisible(true);
		}

		protected int getSelectedAreaId() {
			if(this.areaList.getSelectedValue() == null)
				return 0;
			return FightOptions.getAreaIdFromName(this.areaList.getSelectedValue());
		}

		protected int getSelectedBehaviour() {
			return this.selectedBehaviour;
		}

		private void displayAreasPanel() {
			if(this.selectedBehaviour == CharacterBehaviour.LONE_WOLF ||
					this.selectedBehaviour == CharacterBehaviour.CAPTAIN ||
					this.selectedBehaviour == -1) {
				if(this.areasPanel.isValid()) // déjà affiché
					return;
				add(this.areasPanel);
				if(this.submitButton != null) { // si le bouton de soumission existe, on le passe dessous
					remove(this.submitButton);
					add(this.submitButton);
				}
			}
			else
				remove(this.areasPanel); // ne fait rien s'il n'y est pas
			revalidate();
			repaint();
		}

		@Override // callback appelé lors d'un clic sur un bouton radio des comportements
		public void actionPerformed(ActionEvent event) {
			this.selectedBehaviour = Integer.valueOf(event.getActionCommand());
			displayAreasPanel();
		}

		@Override // callback appelé lors d'un clic sur la checkbox de choix de comportement solitaire ou en groupe
		public void itemStateChanged(ItemEvent event) {
			if(event.getItemSelectable() == this.isLoneWolf) {
				if(this.isLoneWolf.isSelected())
					this.selectedBehaviour = CharacterBehaviour.LONE_WOLF;
				else
					this.selectedBehaviour = this.nextFighterBehaviour;
				if(this.nextFighterBehaviour != -1)
					displayAreasPanel();
			}
		}
	}
}