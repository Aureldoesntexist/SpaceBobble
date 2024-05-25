package com.bobble.spacebobble.controller;

import com.bobble.spacebobble.Main;
import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.config.Mode;
import com.bobble.spacebobble.controller.service.PlayerInput;
import com.bobble.spacebobble.controller.service.SoundtrackPlayer;
import com.bobble.spacebobble.core.GameWorld;
import com.bobble.spacebobble.core.scores.Leaderboard;
import com.bobble.spacebobble.gestion.ResourceManager;
import com.bobble.spacebobble.gestion.VisualManager;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Contrôleur pour la partie en mode VS.
 */
public class VSController {

    private final String filename = "leaderboardVS.ser";
    public TextField pseudo;
    public Label info;
    private Label score;
    private Label lives;
    private Label currentLevel;
    private Pane gamePane;
    private Leaderboard leaderboard;
    private Thread infoThread;

    /**
     * Méthode appelée lors du démarrage d'une partie solo.
     *
     * @param event événement déclencheur
     */
    public void start(ActionEvent event) {
        String pseudoText = pseudo.getText();
        if (!pseudoText.isEmpty()) {
            initGame(pseudoText);
        } else {
            info.setText("Pseudo invalide");
        }
    }

    /**
     * Initialise le jeu avec le pseudo du joueur.
     *
     * @param pseudoText pseudo du joueur
     */
    private void initGame(String pseudoText) {
        GameWorld gameWorld = new GameWorld(Mode.WITHOUT_CAMERA);
        gameWorld.getScore().setName(pseudoText);
        gamePane = gameWorld.getScene();
        ResourceManager.setComposantBackground(gamePane, "/asset/background/lvl1.png");
        leaderboard = ResourceManager.initLeaderboard(filename);
        Scene scene = new Scene(gamePane);
        scene.getStylesheets().add(String.valueOf(SoloController.class.getResource("/fxml_css/style.css").toExternalForm()));
        Main.getInstance().getStage().setScene(scene);
        setupGameLoop(gameWorld, scene);
    }

    /**
     * Configure la boucle de jeu.
     *
     * @param gameWorld instance du monde du jeu
     * @param scene     scène du jeu
     */
    private void setupGameLoop(GameWorld gameWorld, Scene scene) {
        setupInfoIG();
        PlayerInput playerInput = new PlayerInput(gameWorld, scene, gameWorld.getMode());
        SoundtrackPlayer soundtrack = new SoundtrackPlayer("/music/main.mp3");
        startSoundtrack(soundtrack);
        playerInput.start();
        startInfoThread(gameWorld);
        gameLoop(gameWorld, playerInput, soundtrack);
    }


    /**
     * Boucle principale du jeu.
     *
     * @param gameWorld   instance du monde du jeu
     * @param playerInput entrées du joueur
     * @param soundtrack  lecteur de la bande son
     */
    private void gameLoop(GameWorld gameWorld, PlayerInput playerInput, SoundtrackPlayer soundtrack) {
        AnimationTimer gameLoop = new AnimationTimer() {
            long before = System.nanoTime();

            @Override
            public void handle(long now) {
                if (now - before >= GameConstants.INTERVAL) {
                    before = now;
                    gameWorld.update();
                    gameWorld.render();
                    if (gameWorld.isGameFinished() || gameWorld.isGameOver()) {
                        gameEnd(gameWorld, playerInput, soundtrack);
                        stop();
                    }
                }
            }
        };

        gameLoop.start();
    }

    /**
     * Lance le thread pour mettre à jour les informations de la partie
     *
     * @param gameWorld instance du monde du jeu
     */
    private void startInfoThread(GameWorld gameWorld) {
        infoThread = new Thread(() -> {
            while (!gameWorld.isGameFinished() || !gameWorld.isGameOver()) {
                Platform.runLater(() -> {
                    this.score.setText(gameWorld.getScore().getName() + "    : " + gameWorld.getScore().getTotal());
                    this.lives.setText("Vies restantes : " + (gameWorld.getNumberOfLives() + 1));
                    this.currentLevel.setText("Niveau : " + gameWorld.getLevels());
                });
                gameWorld.waitChange();
            }
        });
        infoThread.start();
    }

    /**
     * Configure le lecteur de la bande son du jeu.
     *
     * @param soundtrack lecteur de la bande son
     */
    private void startSoundtrack(SoundtrackPlayer soundtrack) {
        soundtrack.setOnSucceeded(event -> {
            MediaPlayer mediaPlayer = soundtrack.getValue();
            mediaPlayer.play();
            System.out.println("Musique en cours");
        });
        soundtrack.start();
    }

    /**
     * Gère la fin de la partie.
     *
     * @param gameWorld   instance du monde du jeu
     * @param playerInput entrées du joueur
     * @param soundtrack  lecteur de la bande son
     */
    private void gameEnd(GameWorld gameWorld, PlayerInput playerInput, SoundtrackPlayer soundtrack) {
        if (!gameWorld.isGameOver()) {
            leaderboard.addScore(gameWorld.getScore().getName(), gameWorld.getScore().getTotal());
            ResourceManager.serializeLeaderboard(leaderboard.getScoreTreemap(), filename);
        }
        ResourceManager.serializeLeaderboard(leaderboard.getScoreTreemap(), filename);
        playerInput.cancel();
        soundtrack.getValue().stop();
        VisualManager.displayEndGameScreen(gameWorld);
    }

    /**
     * Configure l'affichage du bandeau d'information en jeu
     */
    private void setupInfoIG() {
        HBox gameScore = new HBox();
        gameScore.setAlignment(Pos.CENTER);
        gameScore.setSpacing(100);
        gameScore.setPrefSize(GameConstants.SCENE_WIDTH, 40);
        gameScore.setAlignment(Pos.CENTER);
        gameScore.getStyleClass().add("paneScore");
        Label highscore;
        if (!leaderboard.getScoreTreemap().isEmpty()) {
            List<String> listPlayer = new ArrayList<>(leaderboard.getScoreTreemap().keySet());
            listPlayer.sort(Comparator.comparing(leaderboard.getScoreTreemap()::get).reversed());
            highscore = new Label("Highscore :" + leaderboard.getScoreTreemap().get(listPlayer.get(0)));
        } else {
            highscore = new Label("Highscore :");
        }
        highscore.getStyleClass().add("highscore");
        score = new Label();
        score.getStyleClass().add("labelScore");
        lives = new Label();
        lives.getStyleClass().add("lives");
        currentLevel = new Label();
        currentLevel.getStyleClass().add("currentLevel");
        gameScore.getChildren().addAll(score, lives, currentLevel, highscore);
        gamePane.getChildren().add(gameScore);
    }

}
