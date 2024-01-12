package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONObject;

public class Client {

    public static void main(String[] args) {
        try {
            // Connect to the server
            Socket socket = new Socket("localhost", 1234);

            // Set up input and output streams
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Input voter ID (you can customize this based on your needs)
            System.out.print("Enter your voter ID: ");
            int voterId = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());

            // Request the list of available votes from the server
            writer.println("{\"getVotes\": true}");

            // Receive and display available votes
            System.out.println("Available Votes:");
            String votesResponse = serverReader.readLine();
            JSONArray availableVotes = new JSONArray(votesResponse);
            for (int i = 0; i < availableVotes.length(); i++) {
                JSONObject vote = availableVotes.getJSONObject(i);
                System.out.println(i + 1 + ". " + vote.getString("name") + " - " + vote.getString("description"));
            }

            // User chooses a vote
            System.out.print("Choose a vote (enter the number): ");
            int voteChoice = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine()) - 1;
            JSONObject chosenVote = availableVotes.getJSONObject(voteChoice);

            // Display available options for the chosen vote
            System.out.println("Available Options for " + chosenVote.getString("name") + ":");
            JSONArray optionsArray = chosenVote.getJSONArray("options");
            for (int i = 0; i < optionsArray.length(); i++) {
                System.out.println(i + 1 + ". " + optionsArray.getString(i));
            }

            // User chooses an option
            System.out.print("Choose an option (enter the number): ");
            int optionChoice = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine()) - 1;
            String selectedOption = optionsArray.getString(optionChoice);

            // Create a JSON message for regular clients to vote
            JSONObject voteMessage = new JSONObject();
            voteMessage.put("voterId", voterId);
            voteMessage.put("name", chosenVote.getString("name"));
            voteMessage.put("selectedOption", selectedOption);

            // Send the JSON vote message to the server
            writer.println(voteMessage.toString());

            // Close the socket
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
