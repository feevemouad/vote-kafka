package Serveur;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {

    private ServerSocket serverSocket;

    public Serveur(int port) {
        try {
            // Créer le serveur socket
            serverSocket = new ServerSocket(port);
            System.out.println("Serveur en attente de connexions sur le port " + port);

            // Écouter en boucle pour les connexions entrantes
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion client : " + clientSocket);

                // Créer un thread pour gérer la connexion du client
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Fermer le serveur socket si nécessaire
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}