package gui;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class View {
	protected Hashtable<CharacterFrame, Integer> instancesId = new Hashtable<CharacterFrame, Integer>();
	protected JFrame frame;
	protected JDesktopPane desktopPane;
	protected JMenuItem menuItem;
	protected Vector<JMenuItem> accountsListItems;
	protected JMenu accountsMenu; 
	protected JButton runMuleButton;
	protected LoginPanel loginPanel;
	private JMenuBar menuBar;

	protected View() {
		menuItem = new JMenuItem("Charger compte");
		accountsMenu = new JMenu("Liste des comptes");
		accountsListItems = new Vector<JMenuItem>();
		
		JMenu menu = new JMenu("Commencer");
		menu.add(menuItem);
		menu.add(accountsMenu);
		
		runMuleButton = new JButton("Lancer la mule");
		
		menuBar = new JMenuBar();
		menuBar.add(menu);
		menuBar.add(runMuleButton);
		
		desktopPane = new JDesktopPane();
		desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		
		frame = new JFrame("Tobby");
		frame.setJMenuBar(menuBar);
		frame.setContentPane(desktopPane);
		frame.setSize(1000, 600);
	    frame.setLocationRelativeTo(null);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	protected LoginPanel createLoginPanel() {
		return new LoginPanel("Login panel");
	}
	
	protected class LoginPanel extends JFrame {
		private static final long serialVersionUID = 5372329190332625490L;
		public JTextField loginField;
		public JTextField passwordField;
		public JTextField serverField;
		public JButton connectButton;
		
		private LoginPanel(String title) {
			super(title);
			JPanel panelAccount=new JPanel();
			JLabel enterAccount=new JLabel("Login :");
			this.loginField = new JTextField(20);
			panelAccount.add(enterAccount);
			panelAccount.add(this.loginField);

			JPanel panelPassword=new JPanel();
			JLabel enterPassword=new JLabel("Password :");
			this.passwordField = new JTextField(20);
			panelPassword.add(enterPassword);
			panelPassword.add(this.passwordField);

			JPanel panelServer=new JPanel();
			JLabel enterServer=new JLabel("Server :");
			this.serverField = new JTextField(3);
			panelServer.add(enterServer);
			panelServer.add(this.serverField);

			this.connectButton = new JButton("Run");
			
			setSize(300, 300);
			setLocationRelativeTo(null);
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
			add(panelAccount);
			add(panelPassword);
			add(panelServer);
			add(this.connectButton);
			setVisible(true);
		}
	}
}