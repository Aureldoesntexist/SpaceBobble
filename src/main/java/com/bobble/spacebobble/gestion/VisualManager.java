package com.bobble.spacebobble.gestion;

import com.bobble.spacebobble.Main;
import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.controller.LeaderboardController;
import com.bobble.spacebobble.core.GameWorld;
import com.bobble.spacebobble.ui.MainMenu;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Classe permettant la gestion des élements visuels dans différentes parties de l'application Space Bobble
 */
public class VisualManager {

    /**
     * Ajoute les éléments de leaderboard à l'UI
     *
     * @param leaderboardBox La boîte de leaderboard
     * @param listPlayer     La liste des joueurs
     * @param scores         Les scores des joueurs
     */
    private static void addLeaderboardElement(VBox leaderboardBox, List<String> listPlayer, TreeMap<String, Integer> scores) {
        int rank = 1;
        for (String name : listPlayer) {
            if (rank <= 10) {
                Label scoreLabel = new Label(name + " : " + scores.get(name));
                scoreLabel.setAlignment(Pos.CENTER);
                scoreLabel.getStyleClass().add("listPlayers");
                leaderboardBox.getChildren().add(scoreLabel);
                rank++;
            }
        }
    }

    /*
     *
     *
     * Les méthodes ci-dessous sont principalement de l'initialisation d'objets pour l'interface graphique
     * qui ne sont pas dans les fichiers FXML et CSS
     *
     *  Bouton de retour au menu - Elements pour les pages de leaderboard
     *  Panneau de fin de jeu - Couleurs aléatoires pour les projectiles etc
     *
     *
     */

    /**
     * Bouton de retour au menu principal
     */
    public static void BackToMenu(VBox composant) {
        Button btmBackToMenu = new Button("Retour au menu");
        btmBackToMenu.getStyleClass().add("backMenu");
        btmBackToMenu.setAlignment(Pos.CENTER);
        btmBackToMenu.setOnAction(event -> {
            MainMenu mainMenu = new MainMenu(Main.getInstance().getStage());
            try {
                mainMenu.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        composant.getChildren().add(btmBackToMenu);
    }

    /**
     * Composant pour l'ajout du contenu du leaderboard
     */
    public static VBox createLeaderboardBox() {
        VBox lbBox = new VBox();
        lbBox.setSpacing(10);
        lbBox.setAlignment(Pos.CENTER);
        lbBox.setMaxSize(1000, 200);
        lbBox.getStyleClass().add("leaderBox");
        return lbBox;
    }

    /**
     * Titre du leaderboard
     *
     * @param titleText Titre du leaderboard
     */
    public static Label createTitleLabel(String titleText) {
        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("titleLeader");
        titleLabel.setAlignment(Pos.CENTER);
        return titleLabel;
    }

    /**
     * Initialisation de la page du leaderboard.
     *
     * @param title    Catégorie du leaderboard (Solo, VS, Coop);
     * @param filename Fichier contenant les données
     */
    public static void initLeaderboardPage(String title, String filename) {
        // Initialise le layout
        VBox background = new VBox();
        background.setAlignment(Pos.CENTER);
        background.setSpacing(10);
        // Crée le visuel du leaderboard et ajoute un titre
        VBox lbPane = VisualManager.createLeaderboardBox();
        lbPane.getChildren().add(VisualManager.createTitleLabel(title));
        background.getChildren().add(lbPane);
        // Désérialise le leaderboard
        TreeMap<String, Integer> leaderboard = ResourceManager.deserializeLeaderboard(filename);
        if (leaderboard != null) {
            // Trie le leaderboard
            List<String> listPlayer = new ArrayList<>(leaderboard.keySet());
            listPlayer.sort(Comparator.comparing(leaderboard::get, Comparator.reverseOrder()));
            // Ajoute les éléments dans l'UI
            addLeaderboardElement(lbPane, listPlayer, leaderboard);
        }
        // Ajoute le bouton "Retour au menu"
        VisualManager.BackToMenu(lbPane);
        // Crée la scène et définit le fond et la feuille de style
        Scene lbScene = new Scene(background, GameConstants.SCENE_WIDTH, GameConstants.SCENE_HEIGHT);
        ResourceManager.setComposantBackground(background, "/asset/background/vapor.jpg");
        lbScene.getStylesheets().add(String.valueOf(LeaderboardController.class.getResource("/fxml_css/style.css").toExternalForm()));
        // Définit la scène pour la scène principale
        Main.getInstance().getStage().setScene(lbScene);
    }

    /**
     * Affichage d'un panneau de fin avec son score selon s'il a fait tous les niveaux ou s'il est mort.
     *
     * @param gameWorld Logique du jeu
     */
    public static VBox gameEndPane(GameWorld gameWorld) {
        VBox pane = new VBox();
        pane.setPrefSize(GameConstants.SCENE_WIDTH, GameConstants.SCENE_HEIGHT);
        pane.setSpacing(10);
        pane.setAlignment(Pos.CENTER);
        if (gameWorld.isGameOver()) {
            pane.getStyleClass().add("gameOver");
            Label gameOverLabel = new Label("Vous avez perdu!");
            gameOverLabel.getStyleClass().add("gameOverLabel");
            gameOverLabel.setAlignment(Pos.CENTER);
            pane.getChildren().add(gameOverLabel);
        } else if (gameWorld.isGameFinished()) {
            pane.getStyleClass().add("gameFinished");
            Label gameFinishedLabel = new Label("Vous avez réussi à sauver tout le monde!");
            gameFinishedLabel.setAlignment(Pos.CENTER);
            gameFinishedLabel.getStyleClass().add("gameFinishedLabel");
            pane.getChildren().add(gameFinishedLabel);
        }
        Label displayScore = new Label("Votre score est de " + gameWorld.getScore().getTotal() + " points.");
        displayScore.setAlignment(Pos.CENTER);
        displayScore.getStyleClass().add("displayScore");
        pane.getChildren().add(displayScore);
        return pane;
    }

    /**
     * Affiche l'écran de fin de jeu.
     *
     * @param gameWorld instance du monde du jeu
     */
    public static void displayEndGameScreen(GameWorld gameWorld) {
        VBox endGamePane = VisualManager.gameEndPane(gameWorld);
        Main.getInstance().getStage().getScene().setRoot(endGamePane);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> {
            try {
                MainMenu menu = new MainMenu(Main.getInstance().getStage());
                menu.show();
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        });
        pause.play();
    }


    /**
     * Sélecteur d'une couleur aléatoire parmi 6.
     */
    public static Color getRandomColor() {
        int randomChoice = (int) (Math.random() * 6);
        return switch (randomChoice) {
            case 0 -> Color.RED;
            case 1 -> Color.LIGHTGREEN;
            case 2 -> Color.BLUE;
            case 3 -> Color.YELLOW;
            case 4 -> Color.MAGENTA;
            case 5 -> Color.CYAN;
            default -> throw new IllegalStateException("Erreur de la couleur");
        };
    }

}
