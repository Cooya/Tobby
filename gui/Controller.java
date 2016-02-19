package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Instance;

public class Controller extends Thread{
	public View view;
	public Model model;

	private ActionListener actionListener;
	protected ActionListener buttonListener;

	public Controller(View v,Model m){
		view=v;
		model=m;
		v.Init();

	}

	
	public void run(){
		actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {

				JFrame getInfos=new JFrame("Enter Informations");
				getInfos.setLocationRelativeTo(null);
				getInfos.setSize(300, 300);

				JPanel panelAccount=new JPanel();
				JLabel enterAccount=new JLabel("Nom de compte:");
				JTextField fieldAccount=new JTextField(20);
				panelAccount.add(enterAccount);
				panelAccount.add(fieldAccount);

				JPanel panelPassword=new JPanel();
				JLabel enterPassword=new JLabel("Mot de passe:");
				JTextField fieldPassword=new JTextField(20);
				panelPassword.add(enterPassword);
				panelPassword.add(fieldPassword);


				JPanel panelServer=new JPanel();
				JLabel enterServer=new JLabel("Server:");
				JTextField fieldServer=new JTextField(3);
				panelServer.add(enterServer);
				panelServer.add(fieldServer);



				JButton button = new JButton("Terminer");
				buttonListener = new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						if(fieldAccount.getText().isEmpty() || fieldPassword.getText().isEmpty() || fieldServer.getText().isEmpty() ){
							JOptionPane jop = new JOptionPane();
							jop.showMessageDialog(null, "Write all informations", "Erreur", JOptionPane.ERROR_MESSAGE);
						}
						else{
							Instance botInstance=new Instance(fieldAccount.getText(),fieldPassword.getText(),Integer.parseInt(fieldServer.getText()));
							model.instances.addElement(botInstance);
							botInstance.waitForName();
							CharacterFrame chf=new CharacterFrame(botInstance.getCC().characterName,botInstance.getInstanceId());
							chf.setVisible(true);
							view.dp.add(chf);
							view.frames.put(chf.idInstance, chf);
							getInfos.dispose();
						}
					}
				};
				button.addActionListener(buttonListener);

				getInfos.getContentPane().setLayout(new BoxLayout(getInfos.getContentPane(), BoxLayout.PAGE_AXIS));
				getInfos.add(panelAccount);
				getInfos.add(panelPassword);
				getInfos.add(panelServer);
				getInfos.add(button);

				getInfos.setVisible(true);
			}
		};


		view.menuItem.addActionListener(actionListener);

	}
}



















