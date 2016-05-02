package gui;

import gui.Model.Account;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import main.Log;
import main.Main;

public class FilesManager {
	private static final String EOL = System.getProperty("line.separator");

	private View view;
	private Model model;

	protected FilesManager(View view, Model model) {
		this.view = view;
		this.model = model;

		loadAccountsFile();
		loadSquadsFile();
	}

	// chargement de la liste des comptes dans le fichier texte "accounts.txt"
	private void loadAccountsFile() {
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(Main.ACCOUNTS_FILEPATH));
			String line;
			Account account;
			while((line = buffer.readLine()) != null) {
				if(!line.equals("")) {
					account = this.model.createAccount(line);
					this.view.newAccountItem(account.login, account.behaviour);
				}
			}
			buffer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadSquadsFile() {
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(Main.SQUADS_FILEPATH));
			String line;
			Account account;
			String squadName = null;
			Vector<Account> squad = null;
			while((line = buffer.readLine()) != null) {
				if(!line.equals("")) { // ligne non vide
					if(squadName == null) { // nom de l'escouade
						squadName = line;
						squad = new Vector<Account>();
					}
					else { // membre de l'escouade
						if(squad == null) {
							buffer.close();
							throw new Exception("Parse error in the squads file.");
						}
						account = this.model.getAccount(line);
						if(account != null)
							squad.add(account);
						else
							Log.warn("Unknown login : \"" + line + "\".");
					}
				}
				else { // ligne vide
					if(squad == null || squad.size() == 0) {
						buffer.close();
						throw new Exception("Parse error in the squads file.");
					}
					createSquad(squadName, squad);
					squadName = null;
					squad = null;
				}
			}
			if(squad != null && squad.size() > 0) // fin de fichier
				createSquad(squadName, squad);
			buffer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createSquad(String squadName, Vector<Account> squad) {
		if(squad.size() > 8) {
			Log.err("Invalid squad size.");
			return;
		}
		this.view.newSquadItem(squadName, squad.size());
		this.model.squads.createFixedSquad(squadName, squad);
	}

	// sauvegarde de la liste des comptes dans le fichier texte "accounts.txt" (effectuée à la fermeture de l'application)
	protected void saveAccountsInFile() {
		Vector<Account> accounts = this.model.getAllAccounts();
		sortAccounts(accounts);
		String str = "";
		for(Account account : accounts)
			str += account.behaviour + " " + account.login + " " + account.password + " " + account.serverId + EOL;
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(Main.ACCOUNTS_FILEPATH, false);
			fileWriter.write(str);
			fileWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	// tri de la liste des comptes pour un meilleur affichage
	private static void sortAccounts(Vector<Account> accounts) {
		int accountsNb = accounts.size();

		// on trie par identifiants de comportement
		for(int i = 0; i < accountsNb; ++i)
			for(int j = i + 1; j < accountsNb; ++j)
				if(accounts.get(i).behaviour > accounts.get(j).behaviour)
					Collections.swap(accounts, i, j);

		// on trie par noms de compte
		for(int i = 0; i < accountsNb; ++i)
			for(int j = i + 1; j < accountsNb; ++j)
				if(accounts.get(i).login.compareTo(accounts.get(j).login) > 0 &&
						accounts.get(i).behaviour == accounts.get(j).behaviour)
					Collections.swap(accounts, i, j);
	}
}