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
	private JLabel nameLabel;
	private JLabel mapLabel;
	private JLabel cellLabel;
	private JLabel lifeLabel;
	private JLabel weightLabel;
	private JLabel experienceLabel;
	private JLabel kamasLabel;
	private JLabel fightsLabel;

	// logs
	private JTextArea textArea;

	public CharacterFrame(String name) {
		super(name, true, true, true, true);
		setSize(400, 400);	
		tabbedPane = new JTabbedPane();
		textAreaPanel = new JPanel();
		informationsPanel = new JPanel();
		mapLabel = new JLabel("Map id : unknown");
		cellLabel = new JLabel("Cell id : unknown");
		nameLabel = new JLabel("Name : unknown");
		lifeLabel = new JLabel("Life : unknown");
		weightLabel = new JLabel("Weight : unknown");
		experienceLabel = new JLabel("Experience : unknown");
		kamasLabel = new JLabel("Kamas number : unknown");
		fightsLabel = new JLabel("Fights counter : unknown");
		
		this.textArea = new JTextArea(20, 35);

		this.textArea.setEditable(false); 
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		add(new JScrollPane(textArea));
		informationsPanel.setLayout(new BoxLayout(informationsPanel, BoxLayout.PAGE_AXIS));
		informationsPanel.add(nameLabel);
		informationsPanel.add(mapLabel);
		informationsPanel.add(cellLabel);
		informationsPanel.add(lifeLabel);
		informationsPanel.add(weightLabel);
		informationsPanel.add(experienceLabel);
		informationsPanel.add(kamasLabel);
		informationsPanel.add(fightsLabel);
		textAreaPanel.add(new JScrollPane(textArea));
		tabbedPane.addTab("Logs", textAreaPanel);
		tabbedPane.addTab("Informations",informationsPanel);
		add(tabbedPane);
	}

	public void appendText(String text, Color color) {
		textArea.append(text + EOL);
	}

	public void appendRefreshInfos(CharacterInformations infos) {
		nameLabel.setText(infos.characterName + " (" + infos.level + ")");
		if(infos.currentMap != null)
			mapLabel.setText(String.valueOf(MapPosition.getMapPositionById(infos.currentMap.id)));
		cellLabel.setText("Cell id : " + String.valueOf(infos.currentCellId));
		weightLabel.setText("Weight : " + infos.weight + "/" + infos.weightMax);
		if(infos.stats != null) {
			lifeLabel.setText("Life points : " + infos.stats.lifePoints + "/" + infos.stats.maxLifePoints);
			experienceLabel.setText("Experience : " + String.valueOf((int) infos.stats.experience) + "/" + String.valueOf((int) infos.stats.experienceNextLevelFloor));
			kamasLabel.setText("Kamas number : " + infos.stats.kamas);
		}
		fightsLabel.setText("Fights counter : " + String.valueOf(infos.fightsCounter));
	}
}