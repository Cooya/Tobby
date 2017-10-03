package network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import main.Log;
import main.Main;

public class DatabaseConnection {
	private static final boolean DEBUG = false;
	private static final String url = "jdbc:mysql://nicodev.fr/tobby";
	private static final String login = "tobby";
	private static final String password = "tobby";
	private static Connection co;
	private static boolean connectionUnderRepair = false;
	
	private static final String NEW_ACCOUNT = "INSERT INTO accounts (login, password, owner) VALUES (?, ?, ?)";
	private static final String NEW_CHARACTER = "INSERT INTO characters (name, serverId, accountId, level, breedId) VALUES (?, ?, ?, ?, ?)";
	private static final String UPDATE_CHARACTER_LEVEL = "UPDATE characters SET level = ? WHERE name = ? AND serverId = ?";
	private static final String RETRIEVE_ACCOUNTS = "(SELECT DISTINCT id, login, password FROM accounts LEFT JOIN characters ON accounts.id = characters.accountId " +
			"WHERE owner IS NULL AND status = 0 AND (serverId = ? OR serverId IS NULL) ORDER BY level DESC, id LIMIT ?) " +
			"UNION (SELECT DISTINCT id, login, password FROM accounts LEFT JOIN characters ON accounts.id = characters.accountId WHERE owner IS NULL AND status = 0 " +
			"ORDER BY level, id LIMIT ?)";
	private static final String RETRIEVE_ACCOUNT_WITH_LOGIN = "SELECT id, login, password FROM accounts WHERE owner IS NULL AND status = 0 AND login = ?";
	private static final String RETRIEVE_ACCOUNT_WITH_ID = "SELECT id, login, password FROM accounts WHERE owner IS NULL AND status = 0 AND id = ?";
	private static final String LOCK_ACCOUNTS = "UPDATE accounts SET owner = ? WHERE id IN ";
	private static final String LOCK_ACCOUNT = "UPDATE accounts SET owner = ? WHERE id = ?";
	private static final String UNLOCK_ACCOUNT = "UPDATE accounts SET owner = NULL WHERE id = ?";
	private static final String UNLOCK_ALL_ACCOUNTS = "UPDATE accounts SET owner = NULL WHERE owner = ?";
	private static final String UPDATE_ACCOUNT_STATUS = "UPDATE accounts SET status = ?, owner = NULL WHERE id = ?";
	private static final String NEW_SALE = "INSERT INTO sales (objectUID, objectGID, objectName, price, quantity, serverId, accountId) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_SALE = "UPDATE sales SET objectUID = ?, price = ? WHERE objectUID = ? AND serverId = ?";
	private static final String REMOVE_SALE = "DELETE FROM sales WHERE objectUID = ? AND serverId = ?";
	private static final String RETRIEVE_PRICES = "SELECT DISTINCT price FROM sales WHERE objectGID = ? AND serverId = ? ORDER BY price";
	private static final String RETRIEVE_ALL_SALES = "SELECT objectUID FROM sales WHERE serverId = ? AND accountId = ?";
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			co = DriverManager.getConnection(url, login, password);
			Log.info("Connection to database established.");
		} catch(SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			Main.exit("Impossible to connect to the database.");
		}
	}
	
	public static int newAccount(String login, String password) {
		if(!connectionIsOk())
			return 0;
		
		Log.info("Adding new account into database.");
		try {
			PreparedStatement st = co.prepareStatement(NEW_ACCOUNT, Statement.RETURN_GENERATED_KEYS);
			st.setString(1, login);
			st.setString(2, password);
			st.setString(3, Main.USERNAME);
			st.executeUpdate();
			ResultSet resultSet = st.getGeneratedKeys();
			int generatedKey = 0;
			if(resultSet.next())
				generatedKey = resultSet.getInt(1);
			st.close();
			return generatedKey;
		} catch(SQLException e) {
			fixConnection();
			return newAccount(login, password);
		}
	}
	
	public static ResultSet retrieveAccounts(int serverId, int number) {
		if(!connectionIsOk())
			return null;
		
		Log.info("Retrieving " + number + " character(s) from database for server with id = " + serverId + ".");
		ResultSet result = null;
		Vector<Integer> accountIds = new Vector<Integer>();
		
		try {
			co.setAutoCommit(false);
			
			PreparedStatement st = co.prepareStatement(RETRIEVE_ACCOUNTS);
			st.setInt(1, serverId);
			st.setInt(2, number);
			st.setInt(3, number);
			result = st.executeQuery();
			
			while(result.next() && number-- > 0)
				accountIds.add(result.getInt("id"));
			result.beforeFirst();
			
			lockAccounts(accountIds, Main.USERNAME);
			
			co.commit();
			co.setAutoCommit(true);
		} catch(SQLException e) {
			fixConnection();
			return retrieveAccounts(serverId, number);
		}
		
		return result;
	}
	
	public static ResultSet retrieveAccount(String login) {
		if(!connectionIsOk())
			return null;
		
		Log.info("Retrieving one account from database.");
		ResultSet result = null;
		int id = 0;
		
		try {
			co.setAutoCommit(false);
			
			PreparedStatement st = co.prepareStatement(RETRIEVE_ACCOUNT_WITH_LOGIN);
			st.setString(1, login);
			result = st.executeQuery();
			
			if(!result.last())
				return null;
			else {
				id = result.getInt("id");
				result.beforeFirst();
			}
			
			PreparedStatement st2 = co.prepareStatement(LOCK_ACCOUNT);
			st2.setString(1, Main.USERNAME);
			st2.setInt(2, id);
			st2.executeUpdate();
			
			co.commit();
			co.setAutoCommit(true);
			st2.close();
		} catch(SQLException e) {
			fixConnection();
			return retrieveAccount(login);
		}
		
		return result;
	}
	
	public static ResultSet retrieveAccount(int id) {
		if(!connectionIsOk())
			return null;
		
		Log.info("Retrieving one account from database.");
		ResultSet result = null;
		try {
			co.setAutoCommit(false);
			
			PreparedStatement st = co.prepareStatement(RETRIEVE_ACCOUNT_WITH_ID);
			st.setInt(1, id);
			result = st.executeQuery();
			
			if(!result.last())
				return null;
			else
				result.beforeFirst();
			
			PreparedStatement st2 = co.prepareStatement(LOCK_ACCOUNT);
			st2.setString(1, Main.USERNAME);
			st2.setInt(2, id);
			st2.executeUpdate();
			
			co.commit();
			co.setAutoCommit(true);
			st2.close();
		} catch(SQLException e) {
			fixConnection();
			return retrieveAccount(id);
		}
		
		return result;
	}
	
	public static void updateAccountStatus(int id, int status) {
		if(!connectionIsOk())
			return;
		
		Log.info("Updating status for account with id = " + id + " into database.");
		try {
			PreparedStatement st = co.prepareStatement(UPDATE_ACCOUNT_STATUS);
			st.setInt(1, status);
			st.setInt(2, id);
			st.executeUpdate();
			st.close();
		} catch(SQLException e) {
			fixConnection();
			updateAccountStatus(id, status);
		}
	}
	
	public static void unlockAccount(int id) {
		if(!connectionIsOk())
			return;
		
		Log.info("Unlocking account with id = " + id + " into database.");
		try {
			PreparedStatement st = co.prepareStatement(UNLOCK_ACCOUNT);
			st.setInt(1, id);
			st.executeUpdate();
			st.close();
		} catch(SQLException e) {
			fixConnection();
			unlockAccount(id);
		}
	}
	
	public static void unlockAllAccounts() {
		if(!connectionIsOk())
			return;
		
		Log.info("Unlocking all accounts into database.");
		try {
			PreparedStatement st = co.prepareStatement(UNLOCK_ALL_ACCOUNTS);
			st.setString(1, Main.USERNAME);
			st.executeUpdate();
			st.close();
		} catch(SQLException e) {
			fixConnection();
			unlockAllAccounts();
		}
	}
	
	public static boolean newCharacter(String name, int serverId, int accountId, int level, int breedId) {
		if(!connectionIsOk())
			return false;
		
		Log.info("Adding new character into database.");
		try {
			PreparedStatement st = co.prepareStatement(NEW_CHARACTER);
			st.setString(1, name);
			st.setInt(2, serverId);
			st.setInt(3, accountId);
			st.setInt(4, level);
			st.setInt(5, breedId);
			st.executeUpdate();
			st.close();
			return true;
		} catch(SQLException e) {
			fixConnection();
			return newCharacter(name, serverId, accountId, level, breedId);
		}
	}
	
	public static void updateCharacterLevel(String name, int serverId, int level) {
		if(!connectionIsOk())
			return;
		
		if(DEBUG)
			Log.info("Updating character level into database.");
		try {
			PreparedStatement st = co.prepareStatement(UPDATE_CHARACTER_LEVEL);
			st.setInt(1, level);
			st.setString(2, name);
			st.setInt(3, serverId);
			st.executeUpdate();
			st.close();
		} catch(SQLException e) {
			fixConnection();
			updateCharacterLevel(name, serverId, level);
		}
	}
	
	public static void newSale(int objectUID, int objectGID, String objectName, int price, int quantity, int serverId, int accountId) {
		if(!connectionIsOk())
			return;
		
		Log.info("Adding new sale with UID = " + objectUID + " into database.");
		try {
			PreparedStatement st = co.prepareStatement(NEW_SALE);
			st.setInt(1, objectUID);
			st.setInt(2, objectGID);
			st.setString(3, objectName);
			st.setInt(4, price);
			st.setInt(5, quantity);
			st.setInt(6, serverId);
			st.setInt(7, accountId);
			st.executeUpdate();
			st.close();
		} catch(SQLException e) {
			fixConnection();
			newSale(objectUID, objectGID, objectName, price, quantity, serverId, accountId);
		}
	}
	
	public static void updateSale(int oldObjectUID, int newObjectUID, int newPrice, int serverId) {
		if(!connectionIsOk())
			return;
		
		Log.info("Updating sale into database.");
		try {
			PreparedStatement st = co.prepareStatement(UPDATE_SALE);
			st.setInt(1, newObjectUID);
			st.setInt(2, newPrice);
			st.setInt(3, oldObjectUID);
			st.setInt(4, serverId);
			st.executeUpdate();
			st.close();
		} catch(SQLException e) {
			fixConnection();
			updateSale(oldObjectUID, newObjectUID, newPrice, serverId);
		}
	}
	
	public static void removeSale(int objectUID, int serverId) {
		if(!connectionIsOk())
			return;
		
		Log.info("Deleting sale with UID = " + objectUID + " into database.");
		try {
			PreparedStatement st = co.prepareStatement(REMOVE_SALE);
			st.setInt(1, objectUID);
			st.setInt(2, serverId);
			st.executeUpdate();
			st.close();
		} catch(SQLException e) {
			fixConnection();
			removeSale(objectUID, serverId);
		}
	}
	
	public static ResultSet retrievePrices(int objectGID, int serverId) {
		if(!connectionIsOk())
			return null;
		
		Log.info("Retrieving prices for a sale into database.");
		ResultSet resultSet = null;
		try {
			PreparedStatement st = co.prepareStatement(RETRIEVE_PRICES);
			st.setInt(1, objectGID);
			st.setInt(2, serverId);
			resultSet = st.executeQuery();
		} catch(SQLException e) {
			fixConnection();
			return retrievePrices(objectGID, serverId);
		}
		
		return resultSet;
	}
	
	public static ResultSet retrieveAllSales(int serverId, int accountId) {
		if(!connectionIsOk())
			return null;
		
		if(DEBUG)
			Log.info("Retrieving all sales from database.");
		ResultSet resultSet = null;
		try {
			PreparedStatement st = co.prepareStatement(RETRIEVE_ALL_SALES);
			st.setInt(1, serverId);
			st.setInt(2, accountId);
			resultSet = st.executeQuery();
		} catch(SQLException e) {
			fixConnection();
			return retrieveAllSales(serverId, accountId);
		}
		
		return resultSet;
	}
	
	private static void lockAccounts(Vector<Integer> accountIds, String owner) {
		if(!connectionIsOk())
			return;
		
		Log.info("Locking account(s) into database.");
		try {
			PreparedStatement st = co.prepareStatement(LOCK_ACCOUNTS + vectorToSQLArray(accountIds));
			st.setString(1, owner);
			st.executeUpdate();
			st.close();
		} catch(SQLException e) {
			fixConnection();
			lockAccounts(accountIds, owner);
		}
	}
	
	private static String vectorToSQLArray(Vector<Integer> vector) {
		StringBuilder str = new StringBuilder("(");
		int vectorSize = vector.size();
		for(int i = 0; i < vectorSize - 1; ++i) {
			str.append(vector.get(i));
			str.append(", ");
		}
		str.append(vector.lastElement());
		str.append(")");
		return str.toString();
	}
	
	private static synchronized boolean connectionIsOk() {
		if(connectionUnderRepair) {
			try {
				DatabaseConnection.class.wait();
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return !connectionUnderRepair;
		}
		else
			return true;
	}
	
	private static synchronized boolean fixConnection() {
		connectionUnderRepair = true;
		Log.warn("Connection lost with the database.");
		for(int i = 0; i < 5; ++i) {
			Log.info("Trying to fix the connection...");
			try {
				Thread.sleep(5000);
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				DatabaseConnection.class.notifyAll();
				return false;
			}
			try {
				co = DriverManager.getConnection(url, login, password);
				Log.info("Connection to the database fixed.");
				connectionUnderRepair = false;
				DatabaseConnection.class.notifyAll();
				return true;
			} catch(SQLException e) {
				
			}
		}
		DatabaseConnection.class.notifyAll();
		Log.err("Connection lost with the database. Impossible to fix the connection.");
		return false;
	}
}