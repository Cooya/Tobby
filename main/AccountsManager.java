package main;

import java.sql.ResultSet;
import java.sql.SQLException;

import network.DatabaseConnection;

public class AccountsManager {

	protected static Account newAccount(String login, String password) {
		int accountId = DatabaseConnection.newAccount(login, password);
		if(accountId == 0) {
			Log.err("Account already exists into the database.");
			return null;
		}
		else
			return new Account(accountId, login, password);
	}
	
	protected static Account[] retrieveAccounts(int serverId, int number) {
		ResultSet resultSet = DatabaseConnection.retrieveAccounts(serverId, number);
		Account[] accounts = resultSetToAccounts(resultSet, number);
		try {
			resultSet.getStatement().close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return accounts;
	}
	
	protected static Account retrieveAccount(String login) {
		ResultSet resultSet = DatabaseConnection.retrieveAccount(login);
		Account account = resultSetToAccount(resultSet);
		try {
			resultSet.getStatement().close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return account;
	}
	
	protected static Account retrieveAccount(int id) {
		ResultSet resultSet = DatabaseConnection.retrieveAccount(id);
		Account account = resultSetToAccount(resultSet);
		try {
			resultSet.getStatement().close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return account;
	}
	
	private static Account[] resultSetToAccounts(ResultSet resultSet, int number) {
		Account[] accounts = new Account[number];
		for(int i = 0; i < number; ++i)
			accounts[i] = resultSetToAccount(resultSet);
		return accounts;
	}
	
	private static Account resultSetToAccount(ResultSet resultSet) {
		try {
			if(resultSet.next())
				return new Account(resultSet.getInt("id"), resultSet.getString("login"), resultSet.getString("password"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected static class Account {
		protected int id;
		protected String login;
		protected String password;

		private Account(int id, String login, String password) {
			this.id = id;
			this.login = login;
			this.password = password;
		}
		
		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append(this.id);
			str.append(" ");
			str.append(this.login);
			return str.toString();
		}
		
		/*
		private static final Comparator<Account> BEHAVIOUR = (Account a1, Account a2) -> Integer.compare(a1.serverId, a2.serverId);
	    private static final Comparator<Account> LOGIN = (Account a1, Account a2) -> a1.login.compareTo(a2.login);
	    private static final Comparator<Account> BOTH = (Account a1, Account a2) -> BEHAVIOUR.thenComparing(LOGIN).compare(a1, a2);
		*/
	}
}