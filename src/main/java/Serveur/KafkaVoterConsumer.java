package Serveur;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaVoterConsumer {
    private static final String VOTING_TOPIC = "evote.client";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "voter_consumer_group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(VOTING_TOPIC));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                records.forEach(record -> {
                    JSONObject voteData = new JSONObject(record.value());
                    String voteName = voteData.getString("name");
                    String selectedOption = voteData.getString("selectedOption");
                    // Process voter message and update the database
                    processVoteAndUpdateCounter(voteName, selectedOption);
                    });
            }
        }
    }
    
    private static void processVoteAndUpdateCounter(String voteName, String selectedOption) {
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