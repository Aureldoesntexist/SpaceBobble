package com.bobble.spacebobble.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * La classe Solo est utilisée pour afficher la scène de sélection du nom dans le mode Solo.
 */
public class Solo {
    private final Stage stage;

    /**
     * Constructeur de la classe Solo.
     *
     * @param stage La scène principale de l'application.
     */
    public Solo(Stage stage) {
        this.stage = stage;
    }

    /**
     * Affiche la scène de sélection du nom dans le mode Solo.
     *
     * @throws IOException Si une erreur se produit lors du chargement de la scène.
     */
    public void show() throws IOException {
        FXMLLoader nameChooser = new FXMLLoader(Solo.class.getResource("/fxml_css/NameChooser.fxml"));
        Scene nameScene = nameChooser.load();
        nameScene.getStylesheets().add(String.valueOf(Solo.class.getResource("/fxml_css/style.css").toExternalForm()));
        stage.setScene(nameScene);
    }


}
