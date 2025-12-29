package view;

import controller.GameController;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;


public class GameView extends JFrame {
    private GameController controller;
    private JPanel mainPanel, playerHandPanel, centerPanel, scorePanel;
    private JLabel player1Label, player2Label, player3Label, player4Label, deckLabel, discardLabel;
    private JTextArea chatBox, errorBox;
    private JTextField chatInput;
    private JButton sendChatButton;
    private JButton debugToggle;
    private boolean debugMode = false;
    private JPanel cpuPanelTop, cpuPanelLeft, cpuPanelRight;
    private JMenu languageMenu;
    
    private static final String CARD_BACK_PATH = "resources/cards/card_back.png";
    private JLabel scoreLabel;
    private JMenuItem startGame, pauseGame, playersInfo, rules, about, quitGame;

    
    
    //new
    private boolean isInteractionEnabled = false;
    
    private JTable scoreTable;
    private String[] columnNames = {"Player", "Score"};
    private Object[][] scoreData = {
            {"Player 1", 0},
            {"Player 2", 0},
            {"Player 3", 0},
            {"Player 4", 0}
    };
    private DefaultTableModel scoreModel;
    
 
    private JLabel deckCountLabel;

    private String currentLanguage = "English"; // default to English
    //private boolean debugMode = false;
    
    public GameView() {
        setTitle("Crazy Eights");
        setSize(1400, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        
        //new
        discardLabel = new JLabel();
        playerHandPanel = new JPanel();
        
        
        // Main panel in the center of the JFrame
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(0x7E90A6));
        getContentPane().setBackground(new Color(0x7E90A6));
        add(mainPanel, BorderLayout.CENTER);

        
        // Create the menu bar
      
        JMenuBar menuBar = new JMenuBar();
    // new
        // 1) Create the new menu items
        JMenuItem hostGame = new JMenuItem("Host Game");
        hostGame.addActionListener(e -> {
            controller.setGameMode("multiplayer"); // Set to multiplayer mode
            openHostDialog();
        });

        JMenuItem joinGame = new JMenuItem("Join Game");
        joinGame.addActionListener(e -> {
            controller.setGameMode("multiplayer"); // Set to multiplayer mode
            openClientDialog();
        });

        startGame = new JMenuItem("Local Game");
        startGame.addActionListener(e -> {
            controller.setGameMode("singleplayer"); // Set to single-player mode
            controller.startGame();
        });
        
        pauseGame = new JMenuItem("Pause");
        pauseGame.addActionListener(e -> controller.pauseGame());

        playersInfo = new JMenuItem("Player");
       // playersInfo.addActionListener(e -> JOptionPane.showMessageDialog(this, "4 Players are playing."));
        playersInfo.addActionListener(e -> {
            String message = currentLanguage.equals("French") ? "4 Joueurs jouent." : "4 Players are playing.";
            JOptionPane.showMessageDialog(this, message);
        });

        
        rules = new JMenuItem("Rules");
        rules.addActionListener(e -> showRules());

        about = new JMenuItem("About");
       // about.addActionListener(e -> JOptionPane.showMessageDialog(this, "Crazy Eights Game v1.0"));
        about.addActionListener(e -> {
            String message = currentLanguage.equals("French") ? "Crazy Eights Jeu v1.0" : "Crazy Eights Game v1.0";
            JOptionPane.showMessageDialog(this, message);
        });
        
        quitGame = new JMenuItem("Quit");
        quitGame.addActionListener(e -> confirmQuit());

        debugToggle = new JButton("Debug Mode: OFF");
        debugToggle.addActionListener(e -> {
            debugMode = !debugMode;
            debugToggle.setText("Debug Mode: " + (debugMode ? "ON" : "OFF"));
            // Optionally update the chat box immediately when toggling:
            updateDebugInfo(new ArrayList<>());  // or simply rely on the timer to update
        });

        
        languageMenu = new JMenu("Language");
        JMenuItem englishOption = new JMenuItem("English");
        JMenuItem frenchOption = new JMenuItem("Français");
        englishOption.addActionListener(e -> switchLanguage("English"));
        frenchOption.addActionListener(e -> switchLanguage("French"));
        languageMenu.add(englishOption);
        languageMenu.add(frenchOption);

        // Add menu items to menu bar
        menuBar.add(hostGame);
        menuBar.add(joinGame);
        menuBar.add(startGame);
        menuBar.add(pauseGame);
        menuBar.add(playersInfo);
        menuBar.add(rules);
        menuBar.add(debugToggle);
        menuBar.add(languageMenu);
        menuBar.add(about);
        menuBar.add(quitGame);

        setJMenuBar(menuBar);

        
        // Score panel (left side)
       
        scorePanel = new JPanel(new BorderLayout());
        scorePanel.setBackground(new Color(0x7E90A6));
        scoreModel = new DefaultTableModel(scoreData, columnNames);
        scoreTable = new JTable(scoreModel);
        scoreTable.setEnabled(false);

        scoreTable.setPreferredScrollableViewportSize(new Dimension(250, 80));
        scoreTable.setFillsViewportHeight(false);

        JScrollPane scoreScrollPane = new JScrollPane(scoreTable);
        scoreScrollPane.setPreferredSize(new Dimension(80, 100));
        scorePanel.add(scoreScrollPane, BorderLayout.CENTER);

        // Error box under the score table
        errorBox = new JTextArea(2, 20);
        errorBox.setEditable(false);
        errorBox.setBackground(Color.PINK);
        scorePanel.add(errorBox, BorderLayout.SOUTH);

        // Add the score panel to the main panel (left side)
        mainPanel.add(scorePanel, BorderLayout.WEST);

       
        // The central "table" area
        
        JPanel playersContainer = new JPanel(new BorderLayout());
        playersContainer.setBackground(new Color(0x7E90A6));
        // Deck & Discard in the center
        centerPanel = new JPanel(new GridLayout(1, 2, 3, 3));
        centerPanel.setBackground(new Color(0x7E90A6));
       // deckLabel = new JLabel(new ImageIcon("resources/cards/card_back.png"));
       // discardLabel = new JLabel(new ImageIcon("resources/cards/2_Hearts.png"));
        
        deckLabel = new JLabel(new ImageIcon(getClass().getResource("/cards/card_back.png")));
        deckCountLabel = new JLabel("Cards left: " +  "52"); 
        JPanel deckPanel = new JPanel(new BorderLayout());
        deckPanel.add(deckLabel, BorderLayout.CENTER);
        deckPanel.add(deckCountLabel, BorderLayout.SOUTH);
        
      //  centerPanel.add(deckLabel);
      //  centerPanel.add(discardLabel);

        discardLabel = new JLabel(); // No icon initially
        
        centerPanel = new JPanel(new GridLayout(1, 2, 3, 3));
        centerPanel.add(deckPanel);
        centerPanel.add(discardLabel);
        
        // Add that center panel to the middle of playersContainer
        playersContainer.add(centerPanel, BorderLayout.CENTER);

        
        player1Label = createPlayerLabel("Player 1");
        player2Label = createPlayerLabel("Player 2");
        player3Label = createPlayerLabel("Player 3");
        player4Label = createPlayerLabel("Player 4");

        
        // Player 1 (bottom)
        JPanel player1Panel = new JPanel();
        player1Panel.setLayout(new BoxLayout(player1Panel, BoxLayout.Y_AXIS));
        // label
        player1Panel.add(player1Label);
        // face-up cards
        playerHandPanel = new JPanel(new FlowLayout());
        player1Panel.add(playerHandPanel);

        playersContainer.add(player1Panel, BorderLayout.SOUTH);

        // Player 2 (top)
        JPanel player2Panel = new JPanel();
        player2Panel.setLayout(new BoxLayout(player2Panel, BoxLayout.Y_AXIS));
        player2Panel.add(player2Label);

        // CPU card backs for Player 2
        cpuPanelTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        cpuPanelTop.setBackground(Color.WHITE);
        player2Panel.add(cpuPanelTop);

        playersContainer.add(player2Panel, BorderLayout.NORTH);

        // Player 3 (left)
        JPanel player3Panel = new JPanel();
        player3Panel.setLayout(new BoxLayout(player3Panel, BoxLayout.Y_AXIS));
        player3Panel.add(player3Label);

        // CPU card backs for Player 3
        cpuPanelLeft = new JPanel();
        cpuPanelLeft.setLayout(new BoxLayout(cpuPanelLeft, BoxLayout.Y_AXIS));
        cpuPanelLeft.setBackground(Color.WHITE);
        player3Panel.add(cpuPanelLeft);

        playersContainer.add(player3Panel, BorderLayout.WEST);

        // Player 4 (right)
        JPanel player4Panel = new JPanel();
        player4Panel.setLayout(new BoxLayout(player4Panel, BoxLayout.Y_AXIS));
        player4Panel.add(player4Label);

        // CPU card backs for Player 4
        cpuPanelRight = new JPanel();
        cpuPanelRight.setLayout(new BoxLayout(cpuPanelRight, BoxLayout.Y_AXIS));
        cpuPanelRight.setBackground(Color.WHITE);
        player4Panel.add(cpuPanelRight);

        playersContainer.add(player4Panel, BorderLayout.EAST);

        // Finally, add playersContainer to the mainPanel center
        mainPanel.add(playersContainer, BorderLayout.CENTER);

        //Chat panel (right side)
        
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatBox = new JTextArea(10, 10);
        chatBox.setEditable(false);
        chatBox.setLineWrap(true);
        chatBox.setWrapStyleWord(true);

        JScrollPane chatScroll = new JScrollPane(chatBox);
        chatScroll.setPreferredSize(new Dimension(120, 150));
        chatPanel.add(chatScroll, BorderLayout.CENTER);

        chatInput = new JTextField();
        sendChatButton = new JButton("Send");
        sendChatButton.addActionListener(e -> sendMessage());

        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendChatButton, BorderLayout.EAST);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);

        mainPanel.add(chatPanel, BorderLayout.LINE_END);

        // Optional Debugging info
        
        boolean topAdded = mainPanel.isAncestorOf(cpuPanelTop);
        boolean leftAdded = mainPanel.isAncestorOf(cpuPanelLeft);
        boolean rightAdded = mainPanel.isAncestorOf(cpuPanelRight);
        System.out.println("Panels added: " + topAdded + ", " + leftAdded + ", " + rightAdded);
        System.out.println("CPU Panels in UI: "
            + (cpuPanelTop.getParent() != null) + ", "
            + (cpuPanelLeft.getParent() != null) + ", "
            + (cpuPanelRight.getParent() != null));

        // If you want to see immediate CPU card backs, call:
        // updateCPUHands(new int[] {5, 5, 5, 5});
        revalidate();
        repaint();
        setVisible(true);
       

    }


    
    
    private JButton createMenuButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(Color.GRAY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 30));
        button.addActionListener(action);
        return button;
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }


    public void updatePlayerHand(List<Card> hand) {
        playerHandPanel.removeAll();
        for (Card card : hand) {
            // Use absolute paths or ensure resources are in classpath
            //String cardImagePath = "/cards/" + card.getRank() + "_" + card.getSuit() + ".png"; 
        //	String cardImagePath = "resources/cards/" + card.getRank() + "_" + card.getSuit() + ".png";
        	//ImageIcon cardIcon = new ImageIcon(getClass().getResource(cardImagePath));
            
        	String cardImagePath = "/cards/" + card.getRank() + "_" + card.getSuit() + ".png";
        	ImageIcon cardIcon = new ImageIcon(getClass().getResource(cardImagePath));
        	
            // Resize if needed (e.g., 100x140 pixels)
            cardIcon = resizeImageIcon(cardIcon, 100, 140); 
            
            JButton cardButton = new JButton(cardIcon);
            cardButton.addActionListener(e -> controller.playerPlayCard(card));
            playerHandPanel.add(cardButton);
        }
        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }

    public void updateCPUHands(int[] cardCounts) {
        // Clear all CPU panels
        cpuPanelTop.removeAll();
        cpuPanelLeft.removeAll();
        cpuPanelRight.removeAll();

        // Load card back image from resources
        //ImageIcon backIcon = new ImageIcon(getClass().getResource("/cards/card_back.png"));
      //  ImageIcon backIcon = new ImageIcon("resources/cards/card_back.png");
       
        ImageIcon backIcon = new ImageIcon(getClass().getResource("/cards/card_back.png"));
        
        backIcon = resizeImageIcon(backIcon, 60, 84); // Smaller than player cards

        // Player 2 (Top)
        for (int i=0; i<cardCounts[1]; i++) {
            cpuPanelTop.add(new JLabel(backIcon));
        }

        // Player 3 (Left)
        for (int i=0; i<cardCounts[2]; i++) {
            cpuPanelLeft.add(new JLabel(backIcon));
        }

        // Player 4 (Right)
        for (int i=0; i<cardCounts[3]; i++) {
            cpuPanelRight.add(new JLabel(backIcon));
        }

        // Refresh UI
        cpuPanelTop.revalidate();
        cpuPanelLeft.revalidate();
        cpuPanelRight.revalidate();
    }
    
   /*public void updatePlayerHand(List<Card> hand) {
        playerHandPanel.removeAll();
        int localPlayerIndex = controller.getLocalPlayerIndex();
        boolean isMyTurn = (localPlayerIndex == currentPlayerIndex);
        for (Card card : hand) {
            // Map the rank to the shorthand used in image file names
            String rank = card.getRank();
            switch (rank) {
                case "King":
                    rank = "K";
                    break;
                case "Queen":
                    rank = "Q";
                    break;
                case "Jack":
                    rank = "J";
                    break;
                case "Ace":
                    rank = "A";
                    break;
                default:
                    // For numeric ranks (2-10), use as-is
                    break;
            }

            // Try different paths to find the image
            String[] possiblePaths = {
                "cards/" + rank + "_" + card.getSuit() + ".png",
                "resources/cards/" + rank + "_" + card.getSuit() + ".png",
                "/cards/" + rank + "_" + card.getSuit() + ".png"
            };

            java.net.URL imageURL = null;
            String imagePath = null;
            for (String path : possiblePaths) {
                imagePath = path;
                System.out.println("Attempting to load hand card image: " + imagePath);
                imageURL = getClass().getResource(imagePath);
                if (imageURL != null) {
                    System.out.println("Successfully loaded hand card image: " + imageURL);
                    break;
                } else {
                    System.err.println("Resource not found: " + imagePath);
                }
            }

            JButton cardButton;
            if (imageURL != null) {
                ImageIcon cardIcon = new ImageIcon(imageURL);
                Image image = cardIcon.getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH);
                cardIcon = new ImageIcon(image);
                cardButton = new JButton(cardIcon);
            } else {
                System.err.println("Failed to load image after trying all paths for hand card: " + rank + "_" + card.getSuit() + ".png");
                cardButton = new JButton(card.getSuit() + " " + card.getRank());
            }
            cardButton.addActionListener(e -> controller.playCard(card));
            cardButton.setEnabled(isMyTurn);
            playerHandPanel.add(cardButton);
        }
        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }*/


    public void toggleDebugMode() {
        debugMode = !debugMode;
        debugToggle.setText("Debug Mode: " + (debugMode ? "ON" : "OFF"));
        updateCPUHands(controller.getCPUCardCounts());
    }

    public void switchLanguage(String language) {
        if (language.equals("French")) {
            debugToggle.setText("Mode Debug: " + (debugMode ? "ON" : "OFF"));
            startGame.setText("Démarrer");
            pauseGame.setText("Pause");
            playersInfo.setText("Joueurs");
            rules.setText("Règles");
            about.setText("À propos");
            quitGame.setText("Quitter");
            sendChatButton.setText("Envoyer");

            //Update Player Names in the Score Table
            scoreModel.setValueAt("Joueur 1", 0, 0);
            scoreModel.setValueAt("Joueur 2", 1, 0);
            scoreModel.setValueAt("Joueur 3", 2, 0);
            scoreModel.setValueAt("Joueur 4", 3, 0);
            
            //Refresh the table
            scoreModel.fireTableDataChanged();
            
         //Update Player Labels
            player1Label.setText("Joueur 1");
            player2Label.setText("Joueur 2");
            player3Label.setText("Joueur 3");
            player4Label.setText("Joueur 4");
            


        } else {
            debugToggle.setText("Debug Mode: " + (debugMode ? "ON" : "OFF"));
            startGame.setText("Start");
            pauseGame.setText("Pause");
            playersInfo.setText("Player");
            rules.setText("Rules");
            about.setText("About");
            quitGame.setText("Quit");
            sendChatButton.setText("Send");

            // Revert Player Names to English in the Score Table
            scoreModel.setValueAt("Player 1", 0, 0);
            scoreModel.setValueAt("Player 2", 1, 0);
            scoreModel.setValueAt("Player 3", 2, 0);
            scoreModel.setValueAt("Player 4", 3, 0);
            
            // Refresh the table
            scoreModel.fireTableDataChanged();
            
            player1Label.setText("Player 1");
            player2Label.setText("Player 2");
            player3Label.setText("Player 3");
            player4Label.setText("Player 4");
            
        }
    }


    public void confirmQuit() {
        String message = currentLanguage.equals("French") ? "Êtes-vous sûr de vouloir quitter ?" : "Are you sure you want to quit?";
        String title = currentLanguage.equals("French") ? "Quitter le jeu" : "Quit Game";
        int choice = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    //new

    public void sendMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            // Append the message locally.
            chatBox.append("You: " + message + "\n");
            chatInput.setText("");
            // Inform the controller to send the message over the network.
            controller.sendChatMessage(message);
        }
    }
    
    public void refreshGameBoard(GameModel model) {
        updatePlayerHand(model.getPlayers().get(0).getHand());
        updateCPUHands(model.getCPUCardCounts());
        deckCountLabel.setText("Cards left: " + model.getDeckSize());

        // Update the discard pile to show the last played card if available.
        Card lastPlayed = model.getLastPlayedCard();
        if (lastPlayed != null) {
            String discardImagePath = "/cards/" + lastPlayed.getRank() + "_" + lastPlayed.getSuit() + ".png";
            java.net.URL resourceURL = getClass().getResource(discardImagePath);
            if (resourceURL != null) {
                ImageIcon discardIcon = new ImageIcon(resourceURL);
                discardIcon = resizeImageIcon(discardIcon, 100, 140);
                discardLabel.setIcon(discardIcon);
            } else {
                System.err.println("Resource not found: " + discardImagePath);
                discardLabel.setIcon(null);
            }
        } else {
            // If no card has been played yet, keep it empty.
            discardLabel.setIcon(null);
        }
    }
    
  /*  public void refreshGameBoard(GameModel model) {
        // Use the local player index to get the correct player's hand
        int localPlayerIndex = controller.getLocalPlayerIndex();
        updatePlayerHand(model.getPlayers().get(localPlayerIndex).getHand());
        updateCPUHands(model.getCPUCardCounts());
        deckCountLabel.setText("Cards left: " + model.getDeckSize());

        // Update the discard pile to show the last played card if available.
        Card lastPlayed = model.getLastPlayedCard();
        if (lastPlayed != null) {
            // Map the rank to the shorthand used in image file names
            String rank = lastPlayed.getRank();
            switch (rank) {
                case "King":
                    rank = "K";
                    break;
                case "Queen":
                    rank = "Q";
                    break;
                case "Jack":
                    rank = "J";
                    break;
                case "Ace":
                    rank = "A";
                    break;
                default:
                    // For numeric ranks (2-10), use as-is
                    break;
            }

            // Try different paths to find the image
            String[] possiblePaths = {
             //   "cards/" + rank + "_" + lastPlayed.getSuit() + ".png",           // cards/10_Clubs.png
               // "resources/cards/" + rank + "_" + lastPlayed.getSuit() + ".png", // resources/cards/10_Clubs.png
                "/cards/" + rank + "_" + lastPlayed.getSuit() + ".png"           // /cards/10_Clubs.png
            };

            java.net.URL resourceURL = null;
            String discardImagePath = null;
            for (String path : possiblePaths) {
                discardImagePath = path;
                System.out.println("Attempting to load discard pile image: " + discardImagePath);
                resourceURL = getClass().getResource(discardImagePath);
                if (resourceURL != null) {
                    System.out.println("Successfully loaded discard pile image: " + resourceURL);
                    break;
                } else {
                    System.err.println("Resource not found: " + discardImagePath);
                }
            }

            if (resourceURL != null) {
                ImageIcon discardIcon = new ImageIcon(resourceURL);
                discardIcon = resizeImageIcon(discardIcon, 100, 140);
                discardLabel.setIcon(discardIcon);
                discardLabel.setText(""); // Clear the text if the image is loaded
            } else {
                System.err.println("Failed to load image after trying all paths for: " + rank + "_" + lastPlayed.getSuit() + ".png");
                discardLabel.setIcon(null);
                // Set text as a fallback
                discardLabel.setText("Discard: " + lastPlayed.getSuit() + " " + lastPlayed.getRank());
            }
        } else {
            // If no card has been played yet, keep it empty.
            discardLabel.setIcon(null);
            discardLabel.setText("Discard: Empty");
        }
    }

*/


    public String promptSuitChange() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        return (String) JOptionPane.showInputDialog(this, 
                "Choose a suit:", 
                "Suit Change", 
                JOptionPane.PLAIN_MESSAGE, 
                null, suits, suits[0]);
    }

    public void showError(String message) {
        errorBox.setText(message);
    }

    
    //previous one
  /*  public void enableUserInteraction(boolean enable) {
        // Enables/disables player interactions (e.g., playing cards)
        for (Component component : playerHandPanel.getComponents()) {
            if (component instanceof JButton) {
                component.setEnabled(enable);
            }
        }
    }*/

    public void showRules() {
        String rulesMessage;
        if(currentLanguage.equals("French")){
             rulesMessage = "Règles du Crazy Eights:\n"
                  + "1. Jouez une carte qui correspond à la couleur ou à la valeur.\n"
                  + "2. Les 8 permettent de changer de couleur.\n"
                  + "3. Piochez des cartes s'il n'y a pas de coup valide.\n"
                  + "4. Cartes spéciales:\n"
                  + "   - 2 → Le joueur suivant pioche 2 (Cumulable)\n"
                  + "   - 4 → Le joueur suivant pioche 4 (Non cumulable)\n"
                  + "   - A → Inverse l'ordre de jeu\n"
                  + "   - Q → Passe le tour du joueur suivant";
        } else {
             rulesMessage = "Crazy Eights Rules:\n"
                  + "1. Play a card matching rank or suit.\n"
                  + "2. 8s allow a suit change.\n"
                  + "3. Draw cards if no valid move exists.\n"
                  + "4. Special Cards:\n"
                  + "   - 2 → Next player draws 2 (Stackable)\n"
                  + "   - 4 → Next player draws 4 (NOT Stackable)\n"
                  + "   - A → Reverse turn order\n"
                  + "   - Q → Skip next player's turn";
        }
        JTextArea rulesText = new JTextArea(rulesMessage);
        rulesText.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(rulesText);
        scrollPane.setPreferredSize(new Dimension(80, 100));
        String title = currentLanguage.equals("French") ? "Règles du jeu" : "Game Rules";
        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }


    public void updateScore(int playerIndex, int newScore) {
        scoreModel.setValueAt(newScore, playerIndex, 1);
    }

 // ✅ Method to Create Rectangular Player Labels
    private JLabel createPlayerLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(80, 40));
        label.setOpaque(true);
        label.setBackground(Color.LIGHT_GRAY);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return label;
    }

    private ImageIcon resizeImageIcon(ImageIcon icon, int width, int height) {
        Image image = icon.getImage();
        Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    public void resetUI() {
        // Clear all card displays
        playerHandPanel.removeAll();
        cpuPanelTop.removeAll();
        cpuPanelLeft.removeAll();
        cpuPanelRight.removeAll();
        
        // Reset deck visuals: show card back and update deck count
        deckLabel.setIcon(new ImageIcon(getClass().getResource("/cards/card_back.png")));
        // Reset discard area to empty
        discardLabel.setIcon(null);
        
        // Reset scores in the score table, etc.
        scoreModel.setDataVector(new Object[][]{
            {"Player 1", 0},
            {"Player 2", 0},
            {"Player 3", 0},
            {"Player 4", 0}
        }, columnNames);
        
        revalidate();
        repaint();
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    
    public boolean isDebugModeOn() {
        return debugMode;
    }
    
    public void updateDebugInfo(List<Player> players) {
        if (debugMode) {
            StringBuilder debugInfo = new StringBuilder("Debug Info:\n");
            for (Player p : players) {
                if (!p.isHuman()) {  // Only show CPU players
                    debugInfo.append(p.getName()).append(": ");
                    for (Card card : p.getHand()) {
                        debugInfo.append(card.toString()).append("  ");
                    }
                    debugInfo.append("\n");
                }
            }
            chatBox.setText(debugInfo.toString());
        } else {
            // Clear debug info when debug mode is off
            chatBox.setText("");
        }
    }
    
   // new
    private void openHostDialog() {
        HostDialog hostDialog = new HostDialog(this);
        hostDialog.setVisible(true);
        if (hostDialog.isSucceeded()) {
            String portStr = hostDialog.getPort();
            String playerName = hostDialog.getName();
            try {
                int port = Integer.parseInt(portStr);
                controller.startServerConnection(port, playerName); // Pass playerName instead of hostName
            } catch (NumberFormatException ex) {
                showError("Invalid port number.");
            }
        }
    }

    private void openClientDialog() {
        ClientDialog clientDialog = new ClientDialog(this);
        clientDialog.setVisible(true);
        if (clientDialog.isSucceeded()) {
            String address = clientDialog.getAddress();
            String portStr = clientDialog.getPort();
            String playerName = clientDialog.getName();
            // Call a controller method to connect to a server.
            try {
                int port = Integer.parseInt(portStr);
                controller.connectToServer(address, port, playerName);
            } catch (NumberFormatException ex) {
                showError("Invalid port number.");
            }
        }
    }

    //new
    public void appendChatMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatBox.append(message + "\n");
            chatBox.setCaretPosition(chatBox.getDocument().getLength()); // Auto-scroll to bottom
        });
    }
    
    //new
 // Add method to update discard pile
    public void updateDiscardPile(Card card) {
        if (card != null) {
            // Assuming card images are in /cards/ folder with format rank_suit.png
            String imagePath = "/cards/" + card.getRank() + "_" + card.getSuit() + ".png";
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            // Resize image (example dimensions: 100x140)
            Image image = icon.getImage().getScaledInstance(100, 140, Image.SCALE_SMOOTH);
            discardLabel.setIcon(new ImageIcon(image));
        } else {
            discardLabel.setIcon(null); // Clear if no card
        }
    }

    public void enableUserInteraction(boolean enable) {
        isInteractionEnabled = enable;
        for (Component component : playerHandPanel.getComponents()) {
            if (component instanceof JButton) {
                component.setEnabled(enable);
            }
        }
    }

    public boolean isUserInteractionEnabled() {
        return isInteractionEnabled;
    }
    
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Game Message", JOptionPane.INFORMATION_MESSAGE);
    }
    
 // Add this method to update player names in the UI
    public void updatePlayerNames(List<Player> players) {
        if (players.size() >= 4) {
            player1Label.setText(players.get(0).getName());
            player2Label.setText(players.get(1).getName());
            player3Label.setText(players.get(2).getName());
            player4Label.setText(players.get(3).getName());

            scoreModel.setValueAt(players.get(0).getName(), 0, 0);
            scoreModel.setValueAt(players.get(1).getName(), 1, 0);
            scoreModel.setValueAt(players.get(2).getName(), 2, 0);
            scoreModel.setValueAt(players.get(3).getName(), 3, 0);
            scoreModel.fireTableDataChanged();
        }
    }
    
    
    
}
