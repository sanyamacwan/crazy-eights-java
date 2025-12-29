package controller;

import model.*;
import view.GameView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javax.swing.Timer; // Add this line
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

//new
import network.GameServer;
import network.NetworkMessageListener;
import network.GameClient;

import model.GameModel;

import javax.swing.SwingWorker;


public class GameController implements NetworkMessageListener {
	private GameModel model;
    private GameView view;
    private int currentPlayerIndex;
    private boolean reverseOrder = false;
    private boolean isPaused = false;
    private javax.swing.Timer debugTimer;
    private GameServer gameServer;
    private GameClient gameClient;
    private int localPlayerIndex;
    private int maxPlayers = 4;
    private String gameMode = "singleplayer"; // Default to single-player mode

    public GameController(GameModel model, GameView view) {
        this.model = model;
        this.view = view;
        this.currentPlayerIndex = 0;
        view.setController(this);

        debugTimer = new javax.swing.Timer(1000, e -> {
            if (view.isDebugModeOn()) {
                view.updateDebugInfo(model.getPlayers());
            }
        });
        debugTimer.start();
    }

    public void setGameMode(String mode) {
        this.gameMode = mode;
    }

    public void startGame() {
        // Reset turn order to player 1
        currentPlayerIndex = 0;

        // Reset game state flags
        model.markGameStarted(false);
        model.markGameEnded(false);

        // Reset scores for a complete new game (if not in tournament mode)
        for (Player player : model.getPlayers()) {
            player.resetScore();
        }

        // Reinitialize the model based on game mode
        model = new GameModel(maxPlayers, gameMode.equals("singleplayer"));

        // Initialize game state
        if (model.isTournamentActive()) {
            model.initializeRound();
        } else {
            model.initializeGame();
        }

        // Update the UI with the fresh game state
        updateView();
        view.updateCPUHands(model.getCPUCardCounts());
        view.updatePlayerNames(model.getPlayers());
        // Start the first turn (Player 1)
        playTurn();
    }
    

    public void pauseGame() {
        // Only pause if not already paused
        if (!isPaused) {
            isPaused = true;
            String pauseMsg = view.getCurrentLanguage().equals("French") 
                    ? "Jeu en pause. Cliquez sur OK pour reprendre." 
                    : "Game Paused. Click OK to resume.";
            JOptionPane.showMessageDialog(view, pauseMsg);
            isPaused = false;
            // Resume the game by restarting the current turn.
            playTurn();
        }
    }

    

    private void playTurn() {
        if (isPaused || model.isGameEnded()) return;

        Player currentPlayer = model.getPlayers().get(currentPlayerIndex);
        updateTurnMessage(currentPlayerIndex);

        if (currentPlayer.isHuman()) {
            SwingUtilities.invokeLater(() -> {
                view.updatePlayerHand(currentPlayer.getHand());
                view.enableUserInteraction(true);
            });
        } else {
            new javax.swing.Timer(1000, e -> {
                cpuTurn(currentPlayer);
                ((javax.swing.Timer) e.getSource()).stop(); // Run once
            }).start();
        }
    }
    
    public Card getCPUCard(int playerIndex, int cardIndex) {
        List<Card> cpuHand = model.getPlayers().get(playerIndex).getHand();
        if (cardIndex < cpuHand.size()) {
            return cpuHand.get(cardIndex);
        }
        return null; // Return null if no card exists (prevents errors)
    }
    
    public int[] getCPUCardCounts() {
        return model.getCPUCardCounts(); // Calls GameModel to get CPU player card counts
    }

    
    
    //new
    public void playerPlayCard(Card card) {
        if (isPaused || model.isGameEnded()) {
            return;
        }

        // If networked (gameClient exists), send the action to the server
       /* if (gameClient != null) {
            if (view.isUserInteractionEnabled()) { // Ensure it’s the player’s turn
                String message = "4#PlayCard#" + card.getSuit() + "#" + card.getRank();
                if (card.getRank().equals("8")) {
                    String newSuit = view.promptSuitChange();
                    if (newSuit != null) {
                        message += "#" + newSuit;
                    }
                }
                gameClient.sendMessage(message);
                view.enableUserInteraction(false); // Disable until server responds
            }
            return;
        }*/
        
        if (gameClient != null) {
            if (view.isUserInteractionEnabled()) { // Ensure it's the player's turn
                String message = "4#" + card.getSuit() + "#" + card.getRank();
                if (card.getRank().equals("8")) {
                    String newSuit = view.promptSuitChange();
                    if (newSuit != null) {
                        message += "#" + newSuit;
                    }
                }
                gameClient.sendMessage(message);
                view.enableUserInteraction(false); // Disable until server responds
            }
            return;
        }


        // Local single-player logic (unchanged for non-networked mode)
        Player currentPlayer = model.getPlayers().get(currentPlayerIndex);
        if (!model.isGameStarted()) {
            if (currentPlayer.isHuman()) {
                model.markGameStarted(true);
                view.enableUserInteraction(true);
                model.playCard(currentPlayer, card);
                if (card.getRank().equals("8")) {
                    String newSuit = view.promptSuitChange();
                    model.chooseNewSuit(newSuit);
                } else {
                    model.processSpecialCard(card, currentPlayerIndex);
                }
                model.applyPenalties(currentPlayerIndex);
                updateView();
                nextTurn();
            } else {
                view.showError("Game must be started by Player 1.");
            }
            return;
        }
        if (model.isValidMove(card)) {
            model.playCard(currentPlayer, card);
            if (card.getRank().equals("8") && currentPlayer.isHuman()) {
                String newSuit = view.promptSuitChange();
                model.chooseNewSuit(newSuit);
            } else {
                model.processSpecialCard(card, currentPlayerIndex);
            }
            model.applyPenalties(currentPlayerIndex);
            if (model.checkTournamentEnd()) {
                handleGameEnd();
                return;
            }
            checkWinCondition();
            updateView();
            nextTurn();
        } else {
            view.showError("Invalid move. Drawing card(s) until a valid move is found or hand is full.");
          /*  while (!model.hasValidMove(currentPlayer) && currentPlayer.getHandSize() < 12) {
                model.drawCard(currentPlayer);
                updateView();
            }
            if (model.hasValidMove(currentPlayer)) {
                view.showError("You now have a valid move. Please select a valid card.");
            } else {
                view.showError("No valid moves and hand is full. Skipping turn.");
                nextTurn();
            }
        }
    }*/
            
         // Offload the card-drawing loop to a SwingWorker so the EDT is not blocked.
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Keep drawing cards until a valid move is available or hand size reaches 12.
                    while (!model.hasValidMove(currentPlayer) && currentPlayer.getHandSize() < 12) {
                        model.drawCard(currentPlayer);
                        // Optionally, add a small delay:
                        Thread.sleep(100);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    updateView(); // Ensure UI updates occur on the EDT.
                    if (model.hasValidMove(currentPlayer)) {
                        view.showError("You now have a valid move. Please select a valid card.");
                    } else {
                        view.showError("No valid moves and hand is full. Skipping turn.");
                        nextTurn();
                    }
                }
            };
            worker.execute();
        }
    }


    private void cpuTurn(Player cpuPlayer) {
        if (!model.isGameStarted() || model.isGameEnded()) return;
        
        // Attempt to find a valid move.
        Card chosenCard = model.getCPUMove(cpuPlayer);
        // Repeatedly draw cards until a valid move is found or the hand reaches 12.
        while (chosenCard == null && cpuPlayer.getHandSize() < 12) {
             model.drawCard(cpuPlayer);
             chosenCard = model.getCPUMove(cpuPlayer);
        }
        
        if (chosenCard != null) {
             model.playCard(cpuPlayer, chosenCard);
             model.processSpecialCard(chosenCard, currentPlayerIndex);
            // model.applyPenalties();
             model.applyPenalties(currentPlayerIndex);

             // Check for tournament/game end conditions.
             if (model.checkTournamentEnd()) {
                 handleGameEnd();
                 return;
             }
        } else {
             // No valid move available and hand is full; skip CPU's turn.
             System.out.println("CPU player " + cpuPlayer.getName() + " has no valid moves and is skipping turn.");
        }
        
        checkWinCondition();
        updateView();
        nextTurn();
    }

    

    private void processSpecialCard(Card card) {
        // Let the Model handle all special card logic
        model.processSpecialCard(card, currentPlayerIndex);
       // model.applyPenalties(); // Apply any accumulated penalties
        model.applyPenalties(currentPlayerIndex);

    }

    private void nextTurn() {
        int numPlayers = model.getPlayers().size();
        // Use the model's reverse order flag via a getter (add this method in GameModel if not already present)
        if (model.isReverseOrder()) {
            currentPlayerIndex = (currentPlayerIndex - 1 + numPlayers) % numPlayers;
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % numPlayers;
        }
        playTurn();
    }

    
    private void skipNextTurn() {
        currentPlayerIndex = model.getNextPlayerIndex(currentPlayerIndex, reverseOrder);
    }

    private void checkWinCondition() {
        // Check both round win and tournament end
        boolean roundOver = model.isGameOver();
        boolean tournamentOver = model.checkTournamentEnd();
        
        if (roundOver || tournamentOver) {
            handleGameEnd();
        }
    }
    
    public GameModel getModel() {
        return model;
    }

    private void updateView() {
    	view.refreshGameBoard(model);
        view.updatePlayerHand(model.getPlayers().get(0).getHand()); // Update Player 1 (human)
        view.updateCPUHands(getCPUCardCounts()); 
    }
    
    public void updateScores() {
        int[] scores = model.getScores();
        for (int i = 0; i < scores.length; i++) {
            view.updateScore(i, scores[i]);
        }
    }

    private void handleGameEnd() {
        // Block further interactions
        model.markGameEnded(true);
        view.enableUserInteraction(false);
        
        // Calculate scores and build the end-of-game message
        model.calculateScores();
        String message;
        if (model.checkTournamentEnd()) {
            List<Player> winners = model.getTournamentWinners();
            message = view.getCurrentLanguage().equals("French")
                    ? "Tournoi terminé !\nGagnants: " + winnersToString(winners)
                    : "Tournament Over!\nWinners: " + winnersToString(winners);
        } else {
            Player gameWinner = model.getWinner();
            message = view.getCurrentLanguage().equals("French")
                    ? gameWinner.getName() + " remporte cette manche!\nScores:\n" + getScoreString()
                    : gameWinner.getName() + " wins this round!\nScores:\n" + getScoreString();
        }
        
        // Show a popup with two options: New Game or Quit.
        Object[] options = view.getCurrentLanguage().equals("French")
                ? new Object[]{"Nouvelle Partie", "Quitter"}
                : new Object[]{"New Game", "Quit"};
        int choice = JOptionPane.showOptionDialog(
            view,
            message,
            view.getCurrentLanguage().equals("French") ? "Fin de Partie" : "Game Over",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        // Reset game state after popup closes.
        model.markGameStarted(false);
        model.markGameEnded(false);
        
        if (choice == JOptionPane.YES_OPTION) {
            // Start a new game (reset game state so that player 1 starts)
            startGame();
        } else {
            System.exit(0);
        }
        
        view.enableUserInteraction(true);
    }


    private String winnersToString(List<Player> winners) {
        return winners.stream()
            .map(Player::getName)
            .collect(Collectors.joining(", "));
    }

    
    private String getScoreString() {
        StringBuilder sb = new StringBuilder();
        int[] scores = model.getScores();
        for (int i = 0; i < scores.length; i++) {
            sb.append("Player ").append(i+1).append(": ").append(scores[i]).append("\n");
        }
        return sb.toString();
    }

    private void updateTurnMessage(int playerIndex) {
        SwingUtilities.invokeLater(() -> {
            String message;
            if(view.getCurrentLanguage().equals("French")){
                message = "Tour du Joueur " + (playerIndex + 1);
            } else {
                message = "Player " + (playerIndex + 1) + "'s turn";
            }
            view.showError(message);
        });
    }

    
    //new
    public void startServerConnection(int port, String playerName) {
        try {
            gameServer = new GameServer(port, "localhost"); // Use "localhost" as the hostname
            gameServer.setHostPlayerName(playerName); // Store the host player's name
            new Thread(() -> gameServer.start()).start();
            
            // Create a loopback client connection for the host so that the host can receive broadcasts
            // Wait briefly to ensure the server is up before connecting
            Thread.sleep(500);
            connectToServer("localhost", port, playerName);
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            view.showError("Failed to start server: " + e.getMessage());
        }
    }


    public void connectToServer(String address, int port, String playerName) {
        gameClient = new GameClient(address, port);
        gameClient.setMessageListener(this);
        try {
            gameClient.connect();
            gameClient.sendMessage("2#" + playerName);
            System.out.println("Attempting to connect to server at " + address + ":" + port + " as " + playerName);
        } catch (Exception e) {
            view.showError("Failed to connect to server: " + e.getMessage());
        }
    }
    
    
    //new
    @Override
    public void onNetworkMessage(String message) {
        System.out.println("Network message received: " + message);
        String[] parts = message.split("#");
        System.out.println("Message parts: " + Arrays.toString(parts));
        if (parts.length < 2) {
            System.err.println("Invalid message format: " + message);
            return;
        }
        String protocolId = parts[0];

        switch (protocolId) {
            case "5": // PlayerIndex
                if (parts.length < 3) {
                    System.err.println("Invalid PlayerIndex message: " + message);
                    return;
                }
                localPlayerIndex = Integer.parseInt(parts[2]);
                model = new GameModel(maxPlayers);
                break;
            case "6": // Hand
                if (parts.length < 3) { // Update to check for 3 parts since the format is 6#Hand#<cards>
                    System.err.println("Invalid Hand message: " + message);
                    return;
                }
                String[] cardStrs = parts[2].split(","); // Use parts[2] instead of parts[1]
                List<Card> hand = new ArrayList<>();
                for (String cardStr : cardStrs) {
                    String[] cardParts = cardStr.split("_");
                    if (cardParts.length != 2) {
                        System.err.println("Invalid card format: " + cardStr);
                        continue;
                    }
                    hand.add(new Card(cardParts[0], cardParts[1]));
                }
                model.getPlayers().get(localPlayerIndex).setHand(hand);
                view.updatePlayerHand(hand);
                view.updatePlayerNames(model.getPlayers());
                break;
            case "3": // Chat
                view.appendChatMessage(parts[1]);
                break;
            case "7": // Turn
                currentPlayerIndex = Integer.parseInt(parts[2]);
                playTurn();
                break;
            case "10": // Discard
                Card discardCard = new Card(parts[2], parts[3]);
                model.setLastPlayedCard(discardCard);
                model.setActiveSuit(discardCard.getSuit());
                updateView();
                break;
         /*   case "11": // CardPlayed
                int playerIndex = Integer.parseInt(parts[1]);
                Card playedCard = new Card(parts[2], parts[3]);
                model.getPlayers().get(playerIndex).removeCard(playedCard);
                model.setLastPlayedCard(playedCard);
                if (parts.length > 4) {
                    model.setActiveSuit(parts[4]);
                }
                updateView();
                break;*/
                
                
            case "11": // CardPlayed
                int playerIndex;
                Card playedCard;
                if (parts[1].equals("CardPlayed")) {
                    playerIndex = Integer.parseInt(parts[2]);
                    playedCard = new Card(parts[3], parts[4]);
                    if (parts.length > 5) { // For suit change if provided
                        model.setActiveSuit(parts[5]);
                    }
                } else {
                    // Fallback in case the message format is different
                    playerIndex = Integer.parseInt(parts[1]);
                    playedCard = new Card(parts[2], parts[3]);
                    if (parts.length > 4) {
                        model.setActiveSuit(parts[4]);
                    }
                }
                model.getPlayers().get(playerIndex).removeCard(playedCard);
                model.setLastPlayedCard(playedCard);
                updateView();
                break;

                
                
                
            case "15": // Error
                view.showError(parts[1]);
                if (currentPlayerIndex == localPlayerIndex) {
                    view.enableUserInteraction(true);
                }
                break;
            case "16": // GameOver
                int winnerIndex = Integer.parseInt(parts[1]);
                view.showMessage("Game Over! Winner: " + model.getPlayers().get(winnerIndex).getName());
                model.markGameEnded(true);
                break;
        }
    }
    
    
    //new
    public void sendChatMessage(String message) {
        // Use protocol ID 3 for chat messages.
        if (gameClient != null) {
            // This is a client sending a message.
            gameClient.sendMessage("3#" + message);
        } else if (gameServer != null) {
            // The host is active.
            // Broadcast the chat message to all connected clients.
            gameServer.broadcast("3#" + message);
            // Also show the message on the host's own chat box.
            view.appendChatMessage("You: " + message);
        } else {
            // No network is active; handle as a local chat message.
            view.appendChatMessage("You: " + message);
        }
    }

    
    public int getLocalPlayerIndex() {
        return localPlayerIndex;
    }
    
}
