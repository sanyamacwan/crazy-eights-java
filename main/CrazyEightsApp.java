package main;

import model.GameModel;
import view.GameView;
import controller.GameController;
import javax.swing.SwingUtilities;

public class CrazyEightsApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModel model = new GameModel(4, true); // Start in single-player mode by default
            GameView view = new GameView();
            new GameController(model, view);
            new view.SplashScreen(view);
        });
    }
}