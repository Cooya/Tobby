package gui;

import java.awt.FlowLayout;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class View {
	public JFrame frame;
	public JDesktopPane dp=new JDesktopPane();
	private JMenuBar menuBar;
	public JMenuItem menuItem;
	public Hashtable<Integer,CharacterFrame> frames=new Hashtable<Integer,CharacterFrame>();

	public void Init(){
		
		frame=new JFrame("Tobby");
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setLocationRelativeTo(null);
		menuBar=new JMenuBar();
		frame.setJMenuBar(menuBar);
		frame.setContentPane(dp);
		
		
		JMenu menu = new JMenu("Commencer");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Charger compte");
		menu.add(menuItem);

		 dp.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		frame.setVisible(true);
		
		
	}
	
}
