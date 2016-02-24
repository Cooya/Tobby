package gui;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import controller.informations.CharacterInformations;

public class CharacterFrame extends JInternalFrame {
	private static final long serialVersionUID = 2448473860592287858L;
	private static String EOL = System.getProperty("line.separator");

	private	JTabbedPane tabbedPane;
	private	JPanel textAreaPanel;
	private	JPanel informationsPanel;


	/*Infos*/

	public JLabel Map;
	public JLabel Name;
	public JLabel Life;
	public JLabel Pod;
	public JLabel Experience;
	public JLabel Kamas;



	/*Logs*/


	private JTextArea textArea;


	//public JProgressBar podBar;

	public CharacterFrame(String name) {
		super(name, true, true, true, true);
		setSize(400, 400);	
		tabbedPane=new JTabbedPane();
		textAreaPanel=new JPanel();
		informationsPanel= new JPanel();
		Map=new JLabel("");
		Name=new JLabel("");
		Life=new JLabel("Life : 0/0");
		Pod=new JLabel("Pod : 0/0");
		Experience=new JLabel("Experience : 0/0");
		Kamas=new JLabel("Kamas: 0");
		this.textArea = new JTextArea(20, 35);


		this.textArea.setEditable(false); 
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		add(new JScrollPane(textArea));
		informationsPanel.setLayout(new BoxLayout(informationsPanel, BoxLayout.PAGE_AXIS));
		informationsPanel.add(Map);
		informationsPanel.add(Name);
		informationsPanel.add(Life);
		informationsPanel.add(Pod);
		informationsPanel.add(Experience);
		informationsPanel.add(Kamas);
		textAreaPanel.add(new JScrollPane(textArea));
		tabbedPane.addTab("Logs", textAreaPanel);
		tabbedPane.addTab("Informations",informationsPanel);
		add(tabbedPane);


	}

	public void appendText(String text, Color color) {
		textArea.append(text + EOL);
	}


	public void appendRefreshInfos(CharacterInformations infos){
		Map.setText(String.valueOf(infos.currentMap));
		Name.setText(infos.characterName+" "+infos.level);
		Pod.setText("Pods: "+infos.weight+"/"+infos.weightMax);
		if(infos.stats!=null){
			Life.setText("Life: "+infos.stats.lifePoints+"/"+infos.stats.maxLifePoints);
			Experience.setText("Experience: "+String.valueOf((int)infos.stats.experience)+"/"+String.valueOf((int)infos.stats.experienceNextLevelFloor));
			Kamas.setText("Kamas: "+infos.stats.kamas);
		}
	}


}