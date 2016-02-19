package gui;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class CharacterFrame extends JInternalFrame{
	
	public int idInstance;
	

	public JPanel podPanel;
	public JLabel actualPod;
	public JLabel maxPod;
	public JProgressBar podBar;
	
	
	
	
	public CharacterFrame(String name,int id){
		super(name,true,true,true,true);
		this.setSize(300, 300);
		idInstance=id;
		
	}

	
}
