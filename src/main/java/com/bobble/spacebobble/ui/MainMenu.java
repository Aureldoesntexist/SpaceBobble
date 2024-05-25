package com.bobble.spacebobble.ui;

import com.bobble.spacebobble.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * La classe MainMenu est utilisée pour afficher le menu principal de l'application.
 */
public class MainMenu {
    private final Stage stage;

    /**
     * Constructeur de la classe MainMenu.
     *
     * @param stage La scène principale de l'application.
     */
    public MainMenu(Stage stage) {
        this.stage = stage;
    }

    /**
     * Affiche le menu principal de l'application.
     *
     * @throws IOException Si une erreur se produit lors du chargement de la scène.
     */
    public void show() throws IOException {
        FXMLLoader mainMenu = new FXMLLoader(Main.class.getResource("/fxml_css/MainMenu.fxml"));
        Scene nameScene = mainMenu.load();
        nameScene.getStylesheets().add(String.valueOf(Solo.class.getResource("/fxml_css/style.css").toExternalForm()));
        stage.setScene(nameScene);
        stage.show();
    }
}
