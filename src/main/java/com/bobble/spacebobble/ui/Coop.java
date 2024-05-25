package com.bobble.spacebobble.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * La classe Solo est utilisée pour afficher la scène de sélection du nom dans le mode Coop.
 */
public class Coop {

    private final Stage stage;

    /**
     * Constructeur de la classe Coop.
     *
     * @param stage La scène principale de l'application.
     */
    public Coop(Stage stage) {
        this.stage = stage;
    }

    /**
     * Affiche la scène de sélection du nom du mode Coop.
     *
     * @throws IOException Si une erreur se produit lors du chargement de la scène.
     */
    public void show() throws IOException {
        FXMLLoader nameChooser = new FXMLLoader(Solo.class.getResource("/fxml_css/Connexion.fxml"));
        Scene nameScene = nameChooser.load();
        nameScene.getStylesheets().add(String.valueOf(Solo.class.getResource("/fxml_css/style.css").toExternalForm()));
        stage.setScene(nameScene);

    }
}
