package Serveur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

public class ClientHandler implements Runnable {

    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                JSONObject clientData = new JSONObject(clientMessage);

                if (clientData.has("getVotes") && clientData.getBoolean("getVotes")) {
                    // Handle request to get the list of available votes
                    sendAvailableVotes(writer);
                } else if (clientData.has("isAdmin") && clientData.getBoolean("isAdmin")) {
                    // Handle admin message to create a vote
                    handleAdminMessage(clientData);
                } else {
                    // Handle regular client vote message
                    handleVoteMessage(clientData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAvailableVotes(PrintWriter writer) {
		// TODO Auto-generated method stub
    	try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/users?currentSchema=public", "mouad", "mouad");
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM votes");
            ResultSet resultSet = statement.executeQuery();

            JSONArray availableVotes = new JSONArray();
            while (resultSet.next()) {
                JSONObject vote = new JSONObject();
                vote.put("name", resultSet.getString("name"));
                vote.put("description", resultSet.getString("description"));
                vote.put("options", getOptionsForVote(resultSet.getInt("id")));
                availableVotes.put(vote);
            }

            writer.println(availableVotes.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}

	private JSONArray getOptionsForVote(int voteId) {
        JSONArray optionsArray = new JSONArray();
        try {
        	Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/users?currentSchema=public", "mouad", "mouad");
            PreparedStatement statement = connection.prepareStatement("SELECT option_name FROM options WHERE vote_id = ?");

            statement.setInt(1, voteId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                optionsArray.put(resultSet.getString("option_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return optionsArray;
    }

	private void handleAdminMessage(JSONObject adminData) {
        // Extract relevant information from the JSON
        String voteName = adminData.getString("name");
        String voteDescription = adminData.getString("description");
        JSONArray optionsArray = adminData.getJSONArray("options");

        // Insert the vote into the database
        insertVoteIntoDatabase(voteName, voteDescription, optionsArray);
    }

    private void handleVoteMessage(JSONObject voteData) {
        // Extract relevant information from the JSON
        String voteName = voteData.getString("name");
        String selectedOption = voteData.getString("selectedOption");

        // Process the vote and increment the counter in the database
        processVoteAndUpdateCounter(voteName, selectedOption);
    }
    
    private void insertVoteIntoDatabase(String voteName, String voteDescription, JSONArray optionsArray) {
        // Implement database connection and insertion logic here
        // Use JDBC to connect to your PostgreSQL database and execute INSERT statements
        // You will need to handle exceptions and close resources properly
    	// Insert the vote information
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/users?currentSchema=public", "mouad", "mouad")) {
            // Insert vote details
            String insertVoteQuery = "INSERT INTO votes (name, description) VALUES (?, ?)";
            try (PreparedStatement voteStatement = connection.prepareStatement(insertVoteQuery)) {
                voteStatement.setString(1, voteName);
                voteStatement.setString(2, voteDescription);
                voteStatement.executeUpdate();
            }

            // Insert options and initialize counters
            String insertOptionQuery = "INSERT INTO options (vote_id, option_name, vote_count) VALUES (?, ?, 0)";
            String getVoteIdQuery = "SELECT id FROM votes WHERE name = ?";
            try (PreparedStatement optionStatement = connection.prepareStatement(insertOptionQuery);
                 PreparedStatement voteIdStatement = connection.prepareStatement(getVoteIdQuery)) {

                voteIdStatement.setString(1, voteName);

                // Execute the query and get the result set
                try (ResultSet resultSet = voteIdStatement.executeQuery()) {
                    // Move the cursor to the first row
                    if (resultSet.next()) {
                        int voteId = resultSet.getInt("id");

                        // Check if the voteId is not -1
                        if (voteId != -1) {
                            for (int i = 0; i < optionsArray.length(); i++) {
                                optionStatement.setInt(1, voteId);
                                optionStatement.setString(2, optionsArray.getString(i));
                                optionStatement.executeUpdate();
                            }
                        }
                    }
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processVoteAndUpdateCounter(String voteName, String selectedOption) {
        // Implement logic to process the vote and update the counter in the database
        // Use JDBC to connect to your PostgreSQL database and execute UPDATE statements
        // You will need to handle exceptions and close resources properly

        // Update the counter for the selected option
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/users?currentSchema=public", "mouad", "mouad")) {
            String updateCounterQuery = "UPDATE options SET vote_count = vote_count + 1 WHERE vote_id IN (SELECT id FROM votes WHERE name = ?) AND option_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateCounterQuery)) {
                statement.setString(1, voteName);
                statement.setString(2, selectedOption);
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
}
