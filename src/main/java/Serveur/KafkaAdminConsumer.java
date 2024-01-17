package Serveur;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaAdminConsumer {
    private static final String ADMIN_TOPIC = "evote.admin";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "admin_consumer_group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(ADMIN_TOPIC));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                records.forEach(record -> {
                    JSONObject adminData= new JSONObject(record.value());
                    
                    String voteName = adminData.getString("name");
                    String voteDescription = adminData.getString("description");
                    JSONArray optionsArray = adminData.getJSONArray("options");

                    // Insert the vote into the database
                    insertVoteIntoDatabase(voteName, voteDescription, optionsArray);

                });
            }
        }
    }
    
    private static void insertVoteIntoDatabase(String voteName, String voteDescription, JSONArray optionsArray) {
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
}
