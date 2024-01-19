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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import auth.AuthenticationRemote;

public class ClientHandler implements Runnable  {

    private Socket clientSocket;
    private static  String RMI_SERVER_URL = "rmi://localhost/AuthenticationService";
    private static final String ADMIN_TOPIC = "evote.admin";
    private static final String VOTING_TOPIC = "evote.client";
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            AuthenticationRemote authenticationService = (AuthenticationRemote) Naming.lookup(RMI_SERVER_URL);

            boolean isAuthenticated = false;
            boolean isRegistered = false;
            
            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                JSONObject clientData = new JSONObject(clientMessage);
                
                
                
                if (clientData.has("Register") && clientData.getBoolean("Register")) {
                	
                	isRegistered = performRegistration(authenticationService, clientData.getString("Username"),clientData.getString("Password"),clientData.getBoolean("IsAdmin"));
                	
                	if (isRegistered) {
                			writer.println("{\"registeration_succeeded\":true}");
                	} else {
                		writer.println("{\"registeration_succeeded\":false}");
                	}
                	
                	
                } else if (clientData.has("Login") && clientData.getBoolean("Login")) {

                	isAuthenticated = performLogin(authenticationService, clientData.getString("Username"),clientData.getString("Password"));
                	if (isAuthenticated) {
                        if (authenticationService.isAdmin(clientData.getString("Username"))){
                			writer.println("{\"login_succeeded\":true,\"isAdmin\":true}");
                		} else {writer.println("{\"login_succeeded\":true,\"isAdmin\":false}");}
                	} else {
                		writer.println("{\"login_succeeded\":false}");
                		System.out.println("Login failed. Invalid credentials.");
                	}
                
                } else if (clientData.has("getVotes") && clientData.getBoolean("getVotes") ) {
                    // Handle request to get the list of available votes
                    sendAvailableVotes(writer);
                } else if (clientData.has("name") && clientData.has("description") ) {
                    // Handle admin message to create a vote
                	sendAdminMessage(clientData.toString());
                } else if (clientData.has("name") && clientData.has("selectedOption") ) {
                    // Handle regular client vote message
                	sendVoterMessage(clientData.toString());
                }
            }
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    
    private boolean performLogin(AuthenticationRemote authenticationService, String Username, String Password) throws RemoteException {
    	
    	return authenticationService.authenticate(Username, Password);
	}
    
    private boolean performRegistration(AuthenticationRemote authenticationService, String Username , String Password , Boolean IsAdmin ) throws RemoteException {
		
		 return  authenticationService.register( Username, Password, IsAdmin);
	}

	private void sendAvailableVotes(PrintWriter writer) {
		//  Auto-generated method stub
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
    
	private void sendAdminMessage(String adminMessage) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(ADMIN_TOPIC, adminMessage);
            producer.send(record);
        }
    }
    
	private void sendVoterMessage(String voterMessage) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(VOTING_TOPIC, voterMessage);
            producer.send(record);
        }
	}	

}

