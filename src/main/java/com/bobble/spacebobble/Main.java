package com.bobble.spacebobble;

import com.bobble.spacebobble.ui.MainMenu;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * La classe Main est le point d'entrée de l'application.
 * Elle initialise le menu principal et configure la scène principale.
 */
public class Main extends Application {

    private static Stage stage;

    private static Main instance;

    /**
     * Constructeur d'une nouvelle instance Main.
     */
    public Main() {
        instance = this;
    }

    /**
     * Renvoie l'instance unique de la classe Main. (Singleton)
     *
     * @return L'instance unique de la classe Main.
     */
    public static Main getInstance() {
        return instance;
    }

    /**
     * Renvoie la scène principale de l'application.
     *
     * @return La scène principale de l'application.
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Initialise le menu principal et configure la scène principale.
     *
     * @param stage La scène principale de l'application.
     * @throws IOException Si une erreur se produit lors du chargement du menu principal.
     */
    @Override
    public void start(Stage stage) throws IOException {
        Main.stage = stage;
        stage.setResizable(false);
        MainMenu mainMenu = new MainMenu(stage);
        mainMenu.show();
        stage.setOnCloseRequest(event -> {
            System.exit(0);
        });
    }

}