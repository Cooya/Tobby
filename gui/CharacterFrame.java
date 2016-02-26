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
import gamedata.d2o.modules.MapPosition;

public class CharacterFrame extends JInternalFrame {
	private static final long serialVersionUID = 2448473860592287858L;
	private static String EOL = System.getProperty("line.separator");
	private	JTabbedPane tabbedPane;
	private	JPanel textAreaPanel;
	private	JPanel informationsPanel;

	// infos
	public JLabel map;
	public JLabel charName;
	public JLabel life;
	public JLabel weight;
	public JLabel experience;
	public JLabel kamas;

	// logs
	private JTextArea textArea;

	public CharacterFrame(String name) {
		super(name, true, true, true, true);
		setSize(400, 400);	
		tabbedPane = new JTabbedPane();
		textAreaPanel = new JPanel();
		informationsPanel = new JPanel();
		map=new JLabel("Map id : unknown");
		charName=new JLabel("Name : unknown");
		life=new JLabel("Life : unknown");
		weight=new JLabel("Weight : unknown");
		experience=new JLabel("Experience : unknown");
		kamas=new JLabel("Kamas number : unknown");
		this.textArea = new JTextArea(20, 35);

		this.textArea.setEditable(false); 
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		add(new JScrollPane(textArea));
		informationsPanel.setLayout(new BoxLayout(informationsPanel, BoxLayout.PAGE_AXIS));
		informationsPanel.add(map);
		informationsPanel.add(charName);
		informationsPanel.add(life);
		informationsPanel.add(weight);
		informationsPanel.add(experience);
		informationsPanel.add(kamas);
		textAreaPanel.add(new JScrollPane(textArea));
		tabbedPane.addTab("Logs", textAreaPanel);
		tabbedPane.addTab("Informations",informationsPanel);
		add(tabbedPane);
	}

	public void appendText(String text, Color color) {
		textArea.append(text + EOL);
	}

	public void appendRefreshInfos(CharacterInformations infos){
		charName.setText(infos.characterName+" "+infos.level);
		weight.setText("Pods: "+infos.weight+"/"+infos.weightMax);
		if(infos.currentMap != null)
			map.setText(String.valueOf(MapPosition.getMapPositionById(infos.currentMap.id)));
		if(infos.stats != null) {
			life.setText("Life: "+infos.stats.lifePoints+"/"+infos.stats.maxLifePoints);
			experience.setText("Experience: "+String.valueOf((int)infos.stats.experience)+"/"+String.valueOf((int)infos.stats.experienceNextLevelFloor));
			kamas.setText("Kamas: "+infos.stats.kamas);
		}
	}
}