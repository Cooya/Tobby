package main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class AccountsManager {
	private static final String EOL = System.getProperty("line.separator");
	private Map<Integer, Account> accounts;
	
	protected AccountsManager() {
		this.accounts = new HashMap<Integer, Account>();
	}

	// fonction utilis�e lors du mode graphique
	protected Account createAccount(String login, String password, int serverId) {
		int accountId = DatabaseConnection.newAccount(login, password, serverId);
		if(accountId != -1) {
			Account account = new Account(accountId, login, password, serverId);
			this.accounts.put(account.id, account);
			return account;
		}
		return null;
	}
	
	protected void updateAccountConnectionStatus(int id, boolean isConnected) {
		Account account = this.accounts.get(id);
		if(account != null)
			account.isConnected = isConnected;
	}
	
	protected Vector<Account> retrieveAccounts(int number) {
		Vector<Account> accounts = new Vector<Account>();
		for(Account account : this.accounts.values())
			if(account.serverId == 0 && !account.isConnected) {
				accounts.add(account);
				if(accounts.size() == number)
					return accounts;
			}
		ResultSet resultSet = DatabaseConnection.retrieveAccounts(number - accounts.size());
		accounts.addAll(resultSetToAccounts(resultSet));
		return accounts;
	}
	
	protected Account retrieveAccount(String login) {
		for(Account account : this.accounts.values())
			if(account.login == login) {
				if(!account.isConnected)
					return null;
				else
					return account;
			}
		ResultSet resultSet = DatabaseConnection.retrieveAccount(login);
		return resultSetToAccount(resultSet);
	}
	
	protected Account retrieveAccount(int id) {
		for(Account account : this.accounts.values())
			if(account.id == id) {
				if(!account.isConnected)
					return null;
				else
					return account;
			}
		ResultSet resultSet = DatabaseConnection.retrieveAccount(id);
		return resultSetToAccount(resultSet);
	}
	
	protected void displayAllAccounts() {
		for(Account account : this.accounts.values())
			System.out.println(account);
	}
	
	@Override 
	public String toString() {
		StringBuilder str = new StringBuilder(); 
		for(Account account : this.accounts.values()) {
			str.append(account);
			str.append(EOL);
		}
		return str.toString();
	}
	
	private Vector<Account> resultSetToAccounts(ResultSet resultSet) {
		Vector<Account> accounts = new Vector<Account>();
		Account account;
		while((account = resultSetToAccount(resultSet)) != null) {
			this.accounts.put(account.id, account);
			accounts.add(account);
		}
		return accounts;
	}
	
	private Account resultSetToAccount(ResultSet resultSet) {
		try {
			if(resultSet.next())
				return new Account(resultSet.getInt("id"), resultSet.getString("login"), resultSet.getString("password"), resultSet.getInt("serverId"));
			else
				resultSet.getStatement().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected static class Account {
		protected int id;
		protected String login;
		protected String password;
		protected int serverId;
		protected boolean isConnected;

		private Account(int id, String login, String password, int serverId) {
			this.id = id;
			this.login = login;
			this.password = password;
			this.serverId = serverId;
			this.isConnected = false;
		}
		
		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append(this.id);
			str.append(" ");
			str.append(this.login);
			str.append(" ");
			str.append(this.serverId);
			str.append(" ");
			str.append(this.isConnected);
			return str.toString();
		}
		
		/*
		private static final Comparator<Account> BEHAVIOUR = (Account a1, Account a2) -> Integer.compare(a1.serverId, a2.serverId);
	    private static final Comparator<Account> LOGIN = (Account a1, Account a2) -> a1.login.compareTo(a2.login);
	    private static final Comparator<Account> BOTH = (Account a1, Account a2) -> BEHAVIOUR.thenComparing(LOGIN).compare(a1, a2);
		*/
	}
}