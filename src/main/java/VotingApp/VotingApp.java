package VotingApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import org.json.JSONObject;

import Client.AdminClient;
import Client.Client;



public class VotingApp {

    public static void main(String[] args) {
        try {
        	BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        	Socket socket = new Socket("localhost", 1234);

            // Set up input and output streams
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            try (Scanner scanner = new Scanner(System.in)) {
				boolean continueLoop = true;

				while (continueLoop) {
				    System.out.println("1. Login");
				    System.out.println("2. Register");
				    System.out.println("3. Exit");
				    System.out.print("Enter your choice: ");
				    int choice = scanner.nextInt();

				    scanner.nextLine();  // Consume newline

				    switch (choice) {
				        case 1:
				            // Login
				            System.out.print("Enter your username: ");
				            String loginUsername = scanner.nextLine();
				            System.out.print("Enter your password: ");
				            String loginPassword = scanner.nextLine();
				            
				            // Perform login
				            writer.println("{\"Login\": true,\"Username\":"+loginUsername+",\"Password\":"+loginPassword+"}");
				            JSONObject isAthenticatedresponse = new JSONObject(serverReader.readLine());			     
				            boolean isAuthenticated = isAthenticatedresponse.getBoolean("login_succeeded");
				            if (isAuthenticated) {
				                System.out.println("Login successful!");
				                if (isAthenticatedresponse.getBoolean("isAdmin")) {
				                	System.out.println(" you are logged as admin!");
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
				                } else {
				                	while (true) {
				                        // Ask the user to choose between creating a vote or voting
				                        System.out.println("Welcome to the Voting App!");
				                        System.out.println("Type 1 to vote: ");
				                        System.out.println("Type 'exit' to quit.");
				                        String userInput = consoleReader.readLine().trim();

				                        if ("exit".equalsIgnoreCase(userInput)) {
				                            System.out.println("Exiting Voting App. Goodbye!");
				                            break;
				                        }

				                        switch (userInput) {
				                            case "1":
				                                // vote option selected, run Client
				                                Client.main(new String[]{});
				                                break;
				                            default:
				                                System.out.println("Invalid choice. Please type 1 or 2.");
				                        }
				                    
				                	}
				                }
				            } else {
				                System.out.println("Login failed. Invalid credentials.");
				            }
				            break;

				        case 2:
				            // Registration
				            System.out.print("Enter your username: ");
				            String registerUsername = scanner.nextLine();
				            System.out.print("Enter your password: ");
				            String registerPassword = scanner.nextLine();
				            System.out.print("Do you want to register as an admin? (yes/no): ");
				            String isAdminChoice = scanner.nextLine().toLowerCase();
				            boolean registerIsAdmin = isAdminChoice.equals("yes");
				            
				            // Perform registration
				            writer.println("{\"Register\": true,\"Username\":"+registerUsername+",\"Password\":"+registerPassword+", \"IsAdmin\":"+registerIsAdmin+"}");
				            JSONObject isRegisteredresponse = new JSONObject(serverReader.readLine());			     
				            boolean isRegistered = isRegisteredresponse.getBoolean("registeration_succeeded");
				            
				            if (isRegistered) {
				                System.out.println("Registration successful!");
				            } else {
				                System.out.println("Registration failed. User already exists.");
				            }
				            break;

				        case 3:
				            continueLoop = false;
				            break;

				        default:
				            System.out.println("Invalid choice. Please choose 1, 2, or 3.");
				    }
				}
			}
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}