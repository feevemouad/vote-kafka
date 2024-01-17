package VotingApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import Client.AdminClient;
import Client.Client;

public class VotingApp {

    public static void main(String[] args) {
        try {
            // Set up input stream for console
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                // Ask the user to choose between creating a vote or voting
                System.out.println("Welcome to the Voting App!");
                System.out.println("Type 1 to create a vote or 2 to vote: ");
                System.out.println("Type 'exit' to quit.");
                String userInput = consoleReader.readLine().trim();

                if ("exit".equalsIgnoreCase(userInput)) {
                    System.out.println("Exiting Voting App. Goodbye!");
                    break;
                }

                switch (userInput) {
                    case "1":
                        // Admin option selected, run AdminClient
                        AdminClient.main(new String[]{});
                        break;
                    case "2":
                        // Voter option selected, run Client
                        Client.main(new String[]{});
                        break;
                    default:
                        System.out.println("Invalid choice. Please type 1 or 2.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
