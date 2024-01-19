package Serveur;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import auth.DatabaseManager;

public class Serveur {

    private ServerSocket serverSocket;

    public Serveur(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Serveur en attente de connexions sur le port " + port);

            // Start the RMI registry and bind the DatabaseManager
            startRMIServer();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion client : " + clientSocket);

                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close resources in the finally block
            closeServerSocket();
        }
    }

    private synchronized void startRMIServer() {
        try {
            LocateRegistry.createRegistry(1099);
            DatabaseManager databaseManager = new DatabaseManager();
            Naming.rebind("AuthenticationService", databaseManager);
            System.out.println("Authentication service is running...");
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
