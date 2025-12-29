package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameModel {
	private List<Player> players;
    private Deck deck;
    private Card lastPlayedCard;
    private String activeSuit;
    private boolean gameOver;
    private boolean reverseDirection;
    private int skippedPlayers;
    private boolean reverseOrder = false;
    private int pendingPenalty = 0;
    private boolean tournamentActive = false;
    private int penaltySourceIndex = -1;
    private boolean gameStarted = false;
    private boolean gameEnded = false;
    private int maxPlayers;

    // Constructor for single-player mode
    public GameModel(int numPlayers, boolean isSinglePlayer) {
        this.maxPlayers = numPlayers;
        players = new ArrayList<>();
        if (isSinglePlayer) {
            // Player 1 is human, others are CPUs
            players.add(new Player("Player 1", true)); // Human
            for (int i = 1; i < numPlayers; i++) {
                players.add(new Player("Player " + (i + 1), false)); // CPUs
            }
        } else {
            // All players are human (multiplayer mode)
            for (int i = 0; i < numPlayers; i++) {
                players.add(new Player("Player " + (i + 1), true));
            }
        }
        deck = new Deck();
    }

    // Existing constructor (for backward compatibility, but we'll update calls to use the new one)
    public GameModel(int numPlayers) {
        this(numPlayers, false); // Default to multiplayer mode
    }
	
    public void processSpecialCard(Card card, int playerIndex) {
        this.penaltySourceIndex = playerIndex;
        switch (card.getRank()) {
            case "2" -> pendingPenalty += 2;
            case "4" -> pendingPenalty += 4;
            case "8" -> activeSuit = chooseNewSuit(); // Now uses the method we added
            case "A" -> reverseOrder = !reverseOrder;  // Affects turn order
            case "Q" -> skippedPlayers++;
        }
    }
    
    public void applyPenalties(int currentPlayerIndex) {
        if (pendingPenalty == 0 || penaltySourceIndex == -1) return;
        
        // Calculate the target using the passed currentPlayerIndex
        Player target = getNextPlayer(currentPlayerIndex, reverseOrder);
        Player source = players.get(penaltySourceIndex);
        
        // Apply penalties to the target first
        int targetSpace = 12 - target.getHandSize();
        int targetDraw = Math.min(pendingPenalty, targetSpace);
        drawCards(target, targetDraw);
        
        // Calculate any remaining penalty cards
        int remaining = pendingPenalty - targetDraw;
        if (remaining > 0) {
            // Apply remaining penalty cards to the source
            int sourceSpace = 12 - source.getHandSize();
            int sourceDraw = Math.min(remaining, sourceSpace);
            drawCards(source, sourceDraw);
            
            // Any extra cards are converted to penalty points for the source
            int penalty = remaining - sourceDraw;
            if (penalty > 0) {
                source.addScore(penalty);
            }
        }
        
        // Reset penalty-related fields
        pendingPenalty = 0;
        penaltySourceIndex = -1;
    }

    
    private void drawCards(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Card card = deck.drawCard();
            if (card != null) player.addCard(card);
        }
    }
    
 
    public void markGameStarted(boolean started) {
        this.gameStarted = started;
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public boolean isGameOver() {
        return players.stream().anyMatch(p -> p.getHandSize() == 0);
    }
    
    public void markGameEnded(boolean ended) {
        this.gameEnded = ended;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

   /* public void calculateScores() {
        Player winner = players.stream()
                .filter(p -> p.getHandSize() == 0)
                .findFirst()
                .orElseThrow();
        
        players.forEach(p -> {
            if (p != winner) {
                winner.addScore(p.getHandSize());
            }
        });
    }*/
    
    private void initializePlayers() {
        players.add(new Player("Player 1", true)); // Human Player
        players.add(new Player("Player 2", false));
        players.add(new Player("Player 3", false));
        players.add(new Player("Player 4", false));
    }

    public void initializeGame() {
        gameStarted = false; // Reset start flag
        gameEnded = false;
        
        deck.shuffle();
        for (Player player : players) {
            player.clearHand();
            for (int i = 0; i < 6; i++) {
                Card drawnCard = deck.drawCard();
                if (drawnCard != null) {
                    player.addCard(drawnCard);
                }
            }
        }
        
        // Instead of drawing a card for the discard pile, set it to null.
        lastPlayedCard = null;
        
        // Optionally, set a default active suit.
        activeSuit = "Hearts";  // or leave it until the first card is played.
    }


    public boolean isValidMove(Card card) {
        if (card == null || lastPlayedCard == null) {
            return false;
        }
        return card.getSuit().equals(activeSuit) || card.getRank().equals(lastPlayedCard.getRank()) || card.getRank().equals("8");
    }

    public void playCard(Player player, Card card) {
        player.removeCard(card);
        lastPlayedCard = card; // Set the most recent played card.
        activeSuit = card.getSuit();
    }


    public List<Card> getValidCards(Player player) {
        List<Card> validCards = new ArrayList<>();
        for (Card card : player.getHand()) {
            if (isValidMove(card)) {
                validCards.add(card);
            }
        }
        return validCards;
    }

    public Player getNextPlayer(int currentIndex, boolean reverseOrder) {
        int nextIndex = reverseOrder ? (currentIndex - 1 + players.size()) % players.size() : (currentIndex + 1) % players.size();
        return players.get(nextIndex);
    }

    public int getNextPlayerIndex(int currentIndex, boolean reverseOrder) {
        return reverseOrder ? (currentIndex - 1 + players.size()) % players.size() : (currentIndex + 1) % players.size();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public String getActiveSuit() {
        return activeSuit;
    }

    public int[] getCPUCardCounts() {
        int[] cardCounts = new int[4]; // 4 players total
        for (int i = 0; i < players.size(); i++) {
            cardCounts[i] = players.get(i).getHandSize(); // Get each player's hand size
        }
        return cardCounts;
    }

    
    public void drawCard(Player player) {
        Card drawnCard = deck.drawCard();
        if (drawnCard != null) {
            player.addCard(drawnCard);
        } else {
            // Reshuffle deck if empty
            deck = new Deck();
            deck.shuffle();
            player.addCard(deck.drawCard());
        }
    }

    public void setActiveSuit(String suit) {
        this.activeSuit = suit;
    }
    
    public int[] getScores() {
        int[] scores = new int[players.size()];
        for (int i = 0; i < players.size(); i++) {
            scores[i] = players.get(i).getScore(); // Ensure Player class has `getScore()`
        }
        return scores;
    }

    private String chooseNewSuit() {
        // For CPU players - random suit selection
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        return suits[new Random().nextInt(suits.length)];
    }

    // Overload for human player (to be called from Controller)
    public void chooseNewSuit(String selectedSuit) {
        this.activeSuit = selectedSuit;
    }
    
    public Card getCPUMove(Player cpu) {
        List<Card> validCards = getValidCards(cpu);
        if (validCards.isEmpty()) return null;

        // Simple AI: Prioritize 8s, then other cards
        return validCards.stream()
            .filter(c -> c.getRank().equals("8"))
            .findFirst()
            .orElse(validCards.get(new Random().nextInt(validCards.size())));
    }

  
  //  public int getNextPlayerIndex() {
    //    return getNextPlayerIndex(currentPlayerIndex, reverseOrder);
   // }

    
    public Player getWinner() {
        return players.stream()
            .filter(p -> p.getHandSize() == 0)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No winner yet"));
    }
    
    public void calculateScores() {
        Player roundWinner = getWinner();
        players.forEach(player -> {
            if (player != roundWinner) {
                int penalty = player.getHandSize();
                roundWinner.addScore(penalty);
                player.addScore(-penalty); // Negative score for others (if needed)
            }
        });
    }

   
    public boolean isTournamentActive() {
        return tournamentActive;
    }

    public void initializeRound() {
        // Reset game state but keep scores
        initializeGame();
        tournamentActive = true;
    }

    public void resetTournament() {
        players.forEach(Player::resetScore);
        tournamentActive = false;
        initializeGame();
    }

    public List<Player> getTournamentWinners() {
        int minScore = players.stream()
            .mapToInt(Player::getScore)
            .min()
            .orElse(0);
        
        return players.stream()
            .filter(p -> p.getScore() == minScore)
            .collect(Collectors.toList());
    }
    
    public boolean checkTournamentEnd() {
        return players.stream()
            .anyMatch(p -> p.getScore() >= 50);
    }
    
 
    public boolean hasValidMove(Player player) {
        return !getValidCards(player).isEmpty();
    }

    public boolean isReverseOrder() {
        return reverseOrder;
    }

 
    public int getDeckSize() {
        return deck.getCardsRemaining();
    }

    //new
 // Add setter method
    public void setLastPlayedCard(Card card) {
        this.lastPlayedCard = card;
    }

    // Optional getter
    public Card getLastPlayedCard() {
        return lastPlayedCard;
    }
    
    public Deck getDeck() {
        return deck;
    }
    
}
