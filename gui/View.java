package gui;

import java.util.Enumeration;
import java.util.Hashtable;
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

public class View {
	private JFrame frame;
	private JDesktopPane desktopPane;
	protected Vector<CharacterFrame> charactersFrame;
	protected JMenuItem menuItem;
	protected Vector<JMenuItem> accountsListItems;
	protected JMenu accountsMenu;
	protected LoginPanel loginPanel;
	private JMenuBar menuBar;

	protected View() {
		this.charactersFrame = new Vector<CharacterFrame>();
		
		menuItem = new JMenuItem("Load account");
		accountsMenu = new JMenu("Accounts list");
		accountsListItems = new Vector<JMenuItem>();
		
		JMenu menu = new JMenu("Start");
		menu.add(menuItem);
		menu.add(accountsMenu);
		
		menuBar = new JMenuBar();
		menuBar.add(menu);
		
		desktopPane = new JDesktopPane();
		desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		
		frame = new JFrame("Tobby");
		frame.setJMenuBar(menuBar);
		frame.setContentPane(desktopPane);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	    frame.setLocationRelativeTo(null);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	protected LoginPanel createLoginPanel() {
		return new LoginPanel("Login panel");
	}
	
	protected FighterOptionsPanel createFighterOptionsPanel() {
		return new FighterOptionsPanel("Fighter options panel");
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
	
	protected CharacterFrame getInstance(JInternalFrame graphicalFrame) {
		for(CharacterFrame frame : this.charactersFrame)
			if(frame == graphicalFrame)
				return frame;
		return null;
	}
	
	protected class LoginPanel extends JFrame {
		private static final long serialVersionUID = 5372329190332625490L;
		public JTextField loginField;
		public JTextField passwordField;
		public JTextField serverField;
		public JButton connectButton;
		
		private LoginPanel(String title) {
			super(title);
			JPanel panelAccount = new JPanel();
			JLabel enterAccount = new JLabel("Login :     ");
			this.loginField = new JTextField(20);
			panelAccount.add(enterAccount);
			panelAccount.add(this.loginField);

			JPanel panelPassword = new JPanel();
			JLabel enterPassword = new JLabel("Password :");
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
	
	protected class FighterOptionsPanel extends JFrame {
		private static final long serialVersionUID = 3793718835483044716L;
		public JCheckBox isLoneWolf;
		public JButton submitButton;
		private JList<String> areaList;
		
		private FighterOptionsPanel(String title) {
			super(title);
			Enumeration<String> areaStrings = AreaTable.table.keys();
			DefaultListModel<String> model = new DefaultListModel<String>();
			while(areaStrings.hasMoreElements())
				model.addElement(areaStrings.nextElement());
			
			JPanel loneWolfPanel = new JPanel();
			loneWolfPanel.add(new JLabel("Lone wolf ?"));
			this.isLoneWolf = new JCheckBox();
			loneWolfPanel.add(this.isLoneWolf);
			
			JPanel areasPanel = new JPanel();
			areasPanel.add(new JLabel("Select an area :"));
			this.areaList = new JList<>(model);
			areasPanel.add(this.areaList);
			
			JPanel submitPanel = new JPanel();
			this.submitButton = new JButton("Go");
			submitPanel.add(this.submitButton);
			
			add(loneWolfPanel);
			add(areasPanel);
			add(submitPanel);
			
			setSize(300, 300);
			setLocationRelativeTo(null);
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
			setVisible(true);
		}
		
		protected int getSelectedAreaId() {
			if(this.areaList.getSelectedValue() == null)
				return 0;
			return AreaTable.table.get(this.areaList.getSelectedValue());
		}
	}
	
	private static class AreaTable {
		private static Hashtable<String, Integer> table = new Hashtable<String, Integer>();
		
		static {
			table.put("Contour d'Astrub", 92);
			table.put("Cité d'Astrub", 95);
			table.put("Lac d'Incarnam", 442);
			table.put("Forêt d'Incarnam", 443);
			table.put("Champs d'Incarnam", 444);
			table.put("Pâturages d'Incarnam", 445);
			table.put("Route des âmes d'Incarnam", 450);
		}
	}
}