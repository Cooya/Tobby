package gui;

import gui.AccountsManager.Account;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import main.Log;
import main.Main;

public class FilesManager {
	private View view;
	private AccountsManager accounts;
	private SquadsManager squads;
	private boolean accountsFileIsCorrupted;
	private boolean squadsFileIsCorrupted;

	protected FilesManager(View view, AccountsManager accounts, SquadsManager squads) {
		this.view = view; // peut être null si mode graphique désactivé
		this.accounts = accounts;
		this.squads = squads;
		this.accountsFileIsCorrupted = false;
		this.squadsFileIsCorrupted = false;

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
					account = this.accounts.createAccount(line);
					if(this.view != null)
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
			int squadSize;
			String[] splitLine;
			Vector<Account> squad = null;
			while((line = buffer.readLine()) != null) {
				splitLine = line.split(" ");
				if(splitLine.length != 2) {
					buffer.close();
					this.squadsFileIsCorrupted = true;
					throw new Exception("Parse error in the squads file.");
				}
				squadName = splitLine[0];
				squadSize = Integer.valueOf(splitLine[1]);
				if(squadSize < 1 || squadSize > 8) {
					buffer.close();
					this.squadsFileIsCorrupted = true;
					throw new Exception("Invalid squad size in the squads file.");
				}
				squad = new Vector<Account>(squadSize);
				for(int i = 0; i < squadSize; ++i) {
					line = buffer.readLine();
					if(line == null || line.equals("")) {
						buffer.close();
						this.squadsFileIsCorrupted = true;
						throw new Exception("Parse error in the squads file.");
					}
					account = this.accounts.getAccount(line);
					if(account != null)
						squad.add(account);
					else
						Log.warn("Unknown login : \"" + line + "\".");
				}
				createSquad(squadName, squad);
				squadName = null;
				squad = null;
			}
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
		if(this.view != null)
			this.view.newSquadItem(squadName, squad.size());
		this.squads.createFixedSquad(squadName, squad);
	}

	// sauvegarde de la liste des comptes dans le fichier texte "accounts.txt" (effectuée à la fermeture de l'application)
	protected void saveAccountsInFile() {
		if(this.accountsFileIsCorrupted)
			return;
		try {
			FileWriter fileWriter = new FileWriter(Main.ACCOUNTS_FILEPATH, false);
			fileWriter.write(this.accounts.toFile());
			fileWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	// sauvegarde de la liste des escouades dans le fichier texte "squads.txt" (effectuée à la fermeture de l'application)
	protected void saveSquadsInFile() {
		if(this.squadsFileIsCorrupted)
			return;
		try {
			FileWriter fileWriter = new FileWriter(Main.SQUADS_FILEPATH, false);
			fileWriter.write(this.squads.toFile());
			fileWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}