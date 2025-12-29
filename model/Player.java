package model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private boolean isHuman;
    private List<Card> hand;
    private int score;

   
    public Player(String name, boolean isHuman) {
        this.name = name;
        this.isHuman = isHuman;
        this.hand = new ArrayList<>();
    }

    public void resetScore() {
        this.score = 0;
    }

    public String getName() {
        return name;
    }

    public boolean isHuman() {
        return isHuman;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public int getHandSize() {
        return hand.size();
    }

    public void clearHand() {
        hand.clear();
    }
    
  //  private int score = 0; 

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score += points;
    }

    //new
    // Add this method to set the hand
    public void setHand(List<Card> newHand) {
        this.hand = newHand; // Directly assign the new hand
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
