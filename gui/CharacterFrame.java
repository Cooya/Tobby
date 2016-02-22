package gui;

import java.awt.Color;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class CharacterFrame extends JInternalFrame {
	private static final long serialVersionUID = 2448473860592287858L;
	private static String EOL = System.getProperty("line.separator");

	private JTextArea textArea;
	//public JPanel podPanel;
	//public JLabel actualPod;
	//public JLabel maxPod;
	//public JProgressBar podBar;
	
	public CharacterFrame(String name) {
		super(name, true, true, true, true);
		setSize(400, 400);	
		this.textArea = new JTextArea(5, 20);
		this.textArea.setEditable(false); 
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		add(new JScrollPane(textArea));
	}
	
	public void appendText(String text, Color color) {
		textArea.append(text + EOL);
	}
}