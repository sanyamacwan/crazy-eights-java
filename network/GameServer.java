package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.GameModel;
import model.Player;
import model.Card;

public class GameServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private int maxPlayers = 4;
    private GameModel gameModel;
    private int currentTurn = 0;
    private int port;
    private String hostPlayerName; // Add field to store host player's name

    public GameServer(int port, String hostName) throws IOException {
        InetAddress addr = InetAddress.getByName(hostName);
        serverSocket = new ServerSocket(port, 50, addr);
        this.port = port;
    }

    // Add setter for host player's name
    public void setHostPlayerName(String playerName) {
        this.hostPlayerName = playerName;
    }

    public void start() {
        try {
            // Add the host as the first player
           /* if (hostPlayerName != null) {
                ClientHandler hostHandler = new ClientHandler(null, this); // Null socket for host (simulated)
                hostHandler.setPlayerIndex(0);
                hostHandler.setPlayerName(hostPlayerName);
                clients.add(hostHandler);
                // No need to start a thread for the host since it's not a real client
            }*/

            // Accept remaining clients
            while (clients.size() < maxPlayers) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                handler.setPlayerIndex(clients.size() - 1);
                handler.sendMessage("5#PlayerIndex#" + (clients.size() - 1));
                new Thread(handler).start();
            }
            initializeGame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeGame() {
        gameModel = new GameModel(maxPlayers, false);
        gameModel.initializeGame();
        Card initialDiscard = gameModel.getDeck().drawCard();
        gameModel.setLastPlayedCard(initialDiscard);
        gameModel.setActiveSuit(initialDiscard.getSuit());
        for (int i = 0; i < maxPlayers; i++) {
            Player player = gameModel.getPlayers().get(i);
            player.setName(clients.get(i).getPlayerName()); // Set player name in the model
            String handStr = player.getHand().stream()
                .map(c -> c.getSuit() + "_" + c.getRank())
                .collect(Collectors.joining(","));
            clients.get(i).sendMessage("6#Hand#" + handStr);
        }
        broadcast("10#Discard#" + initialDiscard.getSuit() + "#" + initialDiscard.getRank());
        broadcast("7#Turn#0");
    }

   /* public void handleClientMessage(ClientHandler client, String message) {
        String[] parts = message.split("#");
        String protocolId = parts[0];
        int playerIndex = client.getPlayerIndex();

        if (protocolId.equals("2")) { // Player name message
            String playerName = parts[1];
            client.setPlayerName(playerName);
        } 
        
        else if (protocolId.equals("4") && parts.length >= 3) { // PlayCard
            if (playerIndex != currentTurn) {
                client.sendMessage("15#Error#Not your turn");
                return;
            }
            String suit = parts[1];
            String rank = parts[2];
            Card card = new Card(suit, rank);
            Player player = gameModel.getPlayers().get(playerIndex);
            if (gameModel.isValidMove(card)) {
                gameModel.playCard(player, card);
                String broadcastMsg = "11#CardPlayed#" + playerIndex + "#" + suit + "#" + rank;
                if (card.getRank().equals("8") && parts.length > 3) {
                    String newSuit = parts[3];
                    gameModel.chooseNewSuit(newSuit);
                    broadcastMsg += "#" + newSuit;
                } else {
                    gameModel.processSpecialCard(card, playerIndex);
                }
                gameModel.applyPenalties(playerIndex);
                broadcast(broadcastMsg);
                if (gameModel.isGameOver()) {
                    broadcast("16#GameOver#" + playerIndex);
                } else {
                    currentTurn = (currentTurn + 1) % maxPlayers;
                    broadcast("7#Turn#" + currentTurn);
                }
            } else {
                client.sendMessage("15#Error#Invalid move");
            }
        } else if (protocolId.equals("3")) { // Chat
            String chatMessage = client.getPlayerName() + ": " + parts[1];
            broadcast("3#" + chatMessage);
        }
    }*/
    
    
    public void handleClientMessage(ClientHandler client, String message) {
        String[] parts = message.split("#");
        String protocolId = parts[0];
        int playerIndex = client.getPlayerIndex();

        if (protocolId.equals("2")) { // Player name message
            String playerName = parts[1];
            client.setPlayerName(playerName);
        } else if (protocolId.equals("4") && parts.length >= 3) { // PlayCard
            if (playerIndex != currentTurn) {
                client.sendMessage("15#Error#Not your turn");
                return;
            }
            String suit = parts[1];
            String rank = parts[2];
            Card card = new Card(suit, rank);
            // Declare player so it's available in both branches
            Player player = gameModel.getPlayers().get(playerIndex);

            if (gameModel.isValidMove(card)) {
                // Process valid move
                gameModel.playCard(player, card);
                String broadcastMsg = "11#CardPlayed#" + playerIndex + "#" + suit + "#" + rank;
                if (card.getRank().equals("8") && parts.length > 3) {
                    String newSuit = parts[3];
                    gameModel.chooseNewSuit(newSuit);
                    broadcastMsg += "#" + newSuit;
                } else {
                    gameModel.processSpecialCard(card, playerIndex);
                }
                gameModel.applyPenalties(playerIndex);
                broadcast(broadcastMsg);
                if (gameModel.isGameOver()) {
                    broadcast("16#GameOver#" + playerIndex);
                } else {
                    currentTurn = (currentTurn + 1) % maxPlayers;
                    broadcast("7#Turn#" + currentTurn);
                }
            } else {
                // Automatic drawing for invalid move:
                while (!gameModel.hasValidMove(player) && player.getHandSize() < 12) {
                    gameModel.drawCard(player);
                }
                // Build updated hand string:
                String handStr = player.getHand().stream()
                    .map(c -> c.getSuit() + "_" + c.getRank())
                    .collect(Collectors.joining(","));
                // Send the updated hand to the client:
                client.sendMessage("6#Hand#" + handStr);
                // If still no valid move, notify and skip turn; otherwise, ask the client to try again.
                if (!gameModel.hasValidMove(player)) {
                    client.sendMessage("15#Error#No valid moves and hand is full. Skipping turn.");
                    currentTurn = (currentTurn + 1) % maxPlayers;
                    broadcast("7#Turn#" + currentTurn);
                } else {
                    client.sendMessage("15#Error#Invalid move. Cards drawn. Please try again.");
                }
            }
        } else if (protocolId.equals("3")) { // Chat
            String chatMessage = client.getPlayerName() + ": " + parts[1];
            broadcast("3#" + chatMessage);
        }
    }

    

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            if (client.getSocket() != null) { // Skip host (null socket)
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static class ClientHandler implements Runnable {
        private Socket socket;
        private GameServer server;
        private PrintWriter out;
        private int playerIndex;
        private String playerName; // Add field to store player name

        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
            try {
                if (socket != null) {
                    out = new PrintWriter(socket.getOutputStream(), true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setPlayerIndex(int index) {
            this.playerIndex = index;
        }

        public int getPlayerIndex() {
            return playerIndex;
        }

        public void setPlayerName(String name) {
            this.playerName = name;
        }

        public String getPlayerName() {
            return playerName != null ? playerName : "Player " + (playerIndex + 1);
        }

        public void sendMessage(String message) {
            if (out != null) {
                System.out.println("Server sending to client " + playerIndex + ": " + message);
                out.println(message);
                out.flush(); // Ensure the message is sent immediately
            }
        }

        public Socket getSocket() {
            return socket;
        }

        @Override
        public void run() {
            if (socket == null) return; // Skip for host
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    server.handleClientMessage(this, message);
                }
            } catch (IOException e) {
                System.err.println("Client disconnected: " + e.getMessage());
            } finally {
                server.removeClient(this);
                server.broadcast("1#Disconnect#" + playerIndex);
            }
        }
    }
}