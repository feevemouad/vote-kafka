package auth;


import org.mindrot.jbcrypt.BCrypt;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager extends UnicastRemoteObject implements AuthenticationRemote {
	    
	private static final long serialVersionUID = 1L;
	private Connection connection;

    public DatabaseManager() throws RemoteException {
    	
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/users?currentSchema=public", "mouad", "mouad");

            createUsersTable();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Failed to initialize DatabaseManager", e);
        }
    }
    
    @Override
    public synchronized boolean authenticate(String username, String password) throws RemoteException {
        return verifyUser(username, password);
    }
    
    @Override
	public synchronized boolean register(String Username, String Password, boolean IsAdmin) throws RemoteException {
    	User user = new User( Username,  Password,  IsAdmin);
		return createUser(user);
	}
    
    @Override
    public synchronized boolean isAdmin(String username)  throws RemoteException {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT is_admin FROM users WHERE username = ?"
            );
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean("is_admin");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return false if the user is not found or an error occurred
        return false;
    }

    private void createUsersTable() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY," +
                    "username TEXT NOT NULL UNIQUE," +
                    "password_hash TEXT NOT NULL," +
                    "is_admin BOOLEAN DEFAULT FALSE)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean createUser(User user) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO users(username, password_hash, is_admin) VALUES(?, ?, ?)"
            );
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPasswordHash());
            preparedStatement.setBoolean(3, user.isAdmin());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyUser(String username, String password) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT password_hash, is_admin FROM users WHERE username = ?"
            );
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPasswordHash = resultSet.getString("password_hash");
                boolean isAdmin = resultSet.getBoolean("is_admin");

                if (BCrypt.checkpw(password, storedPasswordHash)) {
                    System.out.println("Login successful!");
                    System.out.println("User is " + (isAdmin ? "admin" : "regular"));
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	
}