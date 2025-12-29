package view;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {
    private static final int SPLASH_DURATION = 3000; // 5 seconds
    private GameView mainView;

    public SplashScreen(GameView view) {
        this.mainView = view; // Store the passed GameView instance

        // Set up the splash screen UI
        JLabel splashLabel = new JLabel(new ImageIcon("resources/splash.png"));
        getContentPane().add(splashLabel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null); // Center the splash screen
        setVisible(true); // Show the splash screen

        // Start a timer to close the splash and show the main game
        new Timer(SPLASH_DURATION, e -> {
            dispose(); // Close the splash screen
            mainView.setVisible(true); // Show the main game window
        }).start();
    }
}