package network;

import java.io.*;
import java.net.*;

public class GameClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String serverAddress;
    private int port;
    private ClientListener listener;

    private NetworkMessageListener messageListener;
    
    public GameClient(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    public void connect() {
        try {
            socket = new Socket(serverAddress, port);
            System.out.println("Connected to server: " + serverAddress + ":" + port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Start a thread to listen for messages from the server
            listener = new ClientListener();
            new Thread(listener).start();

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    // Method to send a message to the server
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // Inner class to listen for messages from the server
    private class ClientListener implements Runnable {
        public void run() {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    // Instead of printing, send the message to the listener:
                    if (messageListener != null) {
                        messageListener.onNetworkMessage(message);
                    }
                }
            } catch (IOException e) {
                System.err.println("Connection closed: " + e.getMessage());
            }
        }
    }

    
    public void setMessageListener(NetworkMessageListener listener) {
        this.messageListener = listener;
    }
    
    public void disconnect() {
        sendMessage("1#Disconnect");
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    
}
