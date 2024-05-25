package com.bobble.spacebobble.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * La classe VS est utilisée pour afficher la scène de sélection du nom du mode VS.
 */
public class VS {
    private final Stage stage;

    /**
     * Constructeur de la classe Solo.
     *
     * @param stage La scène principale de l'application.
     */
    public VS(Stage stage) {
        this.stage = stage;
    }

    /**
     * Affiche la scène de sélection du nom due mode VS.
     *
     * @throws IOException Si une erreur se produit lors du chargement de la scène.
     */

    public void show() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(VS.class.getResource("/fxml_css/NameChooserVS.fxml"));
        Scene scene = fxmlLoader.load();
        scene.getStylesheets().add(String.valueOf(VS.class.getResource("/fxml_css/style.css").toExternalForm()));
        stage.setScene(scene);
    }

}