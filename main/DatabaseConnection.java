package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class DatabaseConnection {
	private static final String url = "jdbc:mysql://nicodev.fr/tobby";
	private static final String login = "tobby";
	private static final String password = "tobby";
	private static Connection co;
	private static Statement st;
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			co = DriverManager.getConnection(url, login, password);
			st = co.createStatement();
			Log.info("Connection to database established.");
		} catch(SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			Controller.getInstance().exit("Impossible to connect to the database.");
		}
	}
	
	public static int newAccount(String login, String password, int serverId) {
		Log.info("Appending new account into database.");
		StringBuilder query = new StringBuilder();
		try {
			query.append("INSERT INTO accounts (login, password, serverId, owner) VALUES (");
			query.append("\"" + login + "\", ");
			query.append("\"" + password + "\", ");
			query.append(serverId + ", ");
			query.append("\"" + Main.USERNAME + "\");");
			return st.executeUpdate(query.toString(), Statement.RETURN_GENERATED_KEYS);
		} catch(SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public static ResultSet retrieveFighters(int number) {
		Log.info("Retrieving " + number + " fighter(s) from database.");
		ResultSet result;
		try {
			co.setAutoCommit(false);
			result = st.executeQuery("SELECT id, login, password, serverId FROM accounts WHERE serverId IS NULL AND owner IS NULL AND isBanned = 0 LIMIT " + number + ";");
			st = co.createStatement();
			st.executeUpdate("UPDATE accounts SET owner = \"" + Main.USERNAME + "\" WHERE serverId IS NULL AND owner IS NULL AND isBanned = 0 LIMIT " + number + ";");
			co.commit();
			co.setAutoCommit(true);
			return result;
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ResultSet retrieveMules(int[] serverIds) {
		Log.info("Retrieving mule(s) from database.");
		StringBuilder query = new StringBuilder();
		ResultSet result;
		try {
			co.setAutoCommit(false);
			
			// récupération des mules
			query.append("SELECT id, login, password, serverId FROM accounts WHERE serverId IN (");
			for(int serverId : serverIds)
				if(serverId != serverIds[serverIds.length - 1])
					query.append(serverId + ", ");
				else
					query.append(serverId + ")");
			query.append(" AND owner IS NULL AND isBanned = 0 GROUP BY serverId;");
			result = st.executeQuery(query.toString());
			
			// réservation des mules
			query.setLength(0);
			while(result.next()) {
				query.append("UPDATE accounts SET owner = \"" + Main.USERNAME + "\" WHERE id IN (");
				if(!result.isLast())
					query.append(result.getInt("id") + ", ");
				else
					query.append(result.getInt("id") + ");");
			}
			st = co.createStatement();
			st.executeUpdate(query.toString());
			
			co.commit();
			co.setAutoCommit(true);
			result.beforeFirst();
			return result;
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void freeAccounts(Vector<Integer> accountIds) {
		Log.info("Unlocking account(s) into database.");
		StringBuilder query = new StringBuilder();
		query.append("UPDATE accounts SET owner = NULL WHERE id IN (");
		int lastAccountId = accountIds.lastElement();
		for(int accountId : accountIds)
			if(accountId != lastAccountId)
				query.append(accountId + ", ");
			else
				query.append(accountId + ");");
		try {
			st.execute(query.toString());
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void unlockAllAccounts() {
		try {
			st.execute("UPDATE accounts SET owner = NULL WHERE owner = \"" + Main.USERNAME + "\";");
			Log.info("All owned accounts into database have been unlocked.");
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}