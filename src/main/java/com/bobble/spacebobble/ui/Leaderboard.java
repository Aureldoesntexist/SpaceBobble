package com.bobble.spacebobble.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * La classe Leaderboard est utilisée pour afficher le menu des tableaux des scores de l'application.
 */
public class Leaderboard {
    private final Stage stage;

    /**
     * Constructeur de la classe Leaderboard.
     *
     * @param stage La scène principale de l'application.
     */
    public Leaderboard(Stage stage) {
        this.stage = stage;
    }

    /**
     * Affiche le menu des tableaux des scores de l'application.
     *
     * @throws IOException Si une erreur se produit lors du chargement de la scène.
     */
    public void show() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Solo.class.getResource("/fxml_css/LeaderboardMenu.fxml"));
        Scene scene = fxmlLoader.load();
        scene.getStylesheets().add(String.valueOf(Solo.class.getResource("/fxml_css/style.css").toExternalForm()));
        stage.setScene(scene);
    }

}
