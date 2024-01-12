package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONObject;

public class AdminClient {

    public static void main(String[] args) {
        try {
            // Connect to the server
            Socket socket = new Socket("localhost", 1234);

            // Set up input and output streams
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Get admin input for vote details
            System.out.print("Enter vote name: ");
            String voteName = consoleReader.readLine();

            System.out.print("Enter vote description: ");
            String voteDescription = consoleReader.readLine();

            // Create a JSON message for admin to create a vote
            JSONObject adminMessage = new JSONObject();
            adminMessage.put("isAdmin", true);
            adminMessage.put("name", voteName);
            adminMessage.put("description", voteDescription);

            // Add options
            JSONArray optionsArray = new JSONArray();
            System.out.println("Enter vote options (type 'done' when finished):");
            String option;
            while (true) {
                option = consoleReader.readLine();
                if (option.equals("done")) {
                    break;
                }
                optionsArray.put(option);
            }
            adminMessage.put("options", optionsArray);

            // Send the JSON message to the server
            writer.println(adminMessage.toString());

            // Close the socket
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
   
    