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

public class CharacterFrame extends JInternalFrame {
	protected static final int FRAME_SIZE = 400;
	private static final long serialVersionUID = 2448473860592287858L;
	private static String EOL = System.getProperty("line.separator");
	protected int id;
	private	JTabbedPane tabbedPane;
	private	JPanel textAreaPanel;
	private	JPanel informationsPanel;
	
	// chaînes de caractères statiques
	private static final String UNKNOWN = "unknown";
	private static final String MAP_LABEL = "Map id : ";
	private static final String CELL_LABEL = "Cell id : ";
	private static final String NAME_LABEL = "Name : ";
	private static final String LIFE_LABEL = "Life : ";
	private static final String WEIGHT_LABEL = "Weight : ";
	private static final String EXPERIENCE_LABEL = "Experience : ";
	private static final String KAMAS_LABEL = "Kamas number : ";
	private static final String FIGHTS_WON_LABEL = "Fights won counter : ";
	private static final String FIGHTS_LOST_LABEL = "Fights lost counter : ";
	private static final String MAPS_TRAVELLED_LABEL = "Maps travelled counter : ";
	private static final String AREA_LABEL = "Fight area : ";

	// infos
	private JLabel nameLabel;
	private JLabel mapLabel;
	private JLabel cellLabel;
	private JLabel lifeLabel;
	private JLabel weightLabel;
	private JLabel experienceLabel;
	private JLabel kamasLabel;
	private JLabel fightsWonLabel;
	private JLabel fightsLostLabel;
	private JLabel mapsTravelledLabel;
	private JLabel areaLabel;

	// logs
	private JTextArea textArea;

	public CharacterFrame(int id, String login) {
		super(login, true, true, true, true);
		this.id = id;
		setSize(FRAME_SIZE, FRAME_SIZE);	
		tabbedPane = new JTabbedPane();
		textAreaPanel = new JPanel();
		informationsPanel = new JPanel();
		mapLabel = new JLabel(MAP_LABEL + UNKNOWN);
		cellLabel = new JLabel(CELL_LABEL + UNKNOWN);
		nameLabel = new JLabel(NAME_LABEL + UNKNOWN);
		lifeLabel = new JLabel(LIFE_LABEL + UNKNOWN);
		weightLabel = new JLabel(WEIGHT_LABEL + UNKNOWN);
		experienceLabel = new JLabel(EXPERIENCE_LABEL + UNKNOWN);
		kamasLabel = new JLabel(KAMAS_LABEL + UNKNOWN);
		fightsWonLabel = new JLabel(FIGHTS_WON_LABEL + UNKNOWN);
		fightsLostLabel = new JLabel(FIGHTS_LOST_LABEL + UNKNOWN);
		mapsTravelledLabel = new JLabel(MAPS_TRAVELLED_LABEL + UNKNOWN);
		areaLabel = new JLabel(AREA_LABEL + UNKNOWN);
		
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
		informationsPanel.add(fightsWonLabel);
		informationsPanel.add(fightsLostLabel);
		informationsPanel.add(mapsTravelledLabel);
		informationsPanel.add(areaLabel);
		textAreaPanel.add(new JScrollPane(textArea));
		tabbedPane.addTab("Logs", textAreaPanel);
		tabbedPane.addTab("Informations",informationsPanel);
		add(tabbedPane);
	}

	public void appendText(String text, Color color) {
		this.textArea.append(text + EOL);
	}
	
	public void setNameLabel(String name, int level) {
		this.nameLabel.setText(NAME_LABEL + name + " (" + level + ")");
	}
	
	public void setMapLabel(String map) {
		this.mapLabel.setText(MAP_LABEL + map);
	}
	
	public void setCellLabel(String cell) {
		this.cellLabel.setText(CELL_LABEL + cell);
	}
	
	public void setWeightLabel(int weight, int weightMax) {
		this.weightLabel.setText(WEIGHT_LABEL + weight + "/" + weightMax);
	}
	
	public void setLifeLabel(int lifePoints, int maxLifePoints) {
		this.lifeLabel.setText(LIFE_LABEL + lifePoints + "/" + maxLifePoints);
	}
	
	public void setExperienceLabel(int experience, int experienceNextLevelFloor) {
		this.experienceLabel.setText(EXPERIENCE_LABEL + experience + "/" + experienceNextLevelFloor);
	}
	
	public void setKamasLabel(int kamas) {
		this.kamasLabel.setText(KAMAS_LABEL + kamas); 
	}
	
	public void setFightsWonLabel(int fightsWonCounter) {
		this.fightsWonLabel.setText(FIGHTS_WON_LABEL + fightsWonCounter); 
	}
	
	public void setFightsLostLabel(int fightsLostCounter) {
		this.fightsLostLabel.setText(FIGHTS_LOST_LABEL + fightsLostCounter); 
	}
	
	public void setMapsTravelledCounter(int mapsTravelledCounter) {
		this.mapsTravelledLabel.setText(MAPS_TRAVELLED_LABEL + mapsTravelledCounter);
	}

	public void setAreaLabel(String areaName) {
		this.areaLabel.setText(AREA_LABEL + "\"" + areaName + "\"");
	}
}