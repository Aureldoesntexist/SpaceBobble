package com.bobble.spacebobble.controller;

import com.bobble.spacebobble.Main;
import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.controller.service.ClientInput;
import com.bobble.spacebobble.controller.service.SoundtrackPlayer;
import com.bobble.spacebobble.core.GameClient;
import com.bobble.spacebobble.core.scores.Leaderboard;
import com.bobble.spacebobble.gestion.ResourceManager;
import com.bobble.spacebobble.ui.MainMenu;
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Contrôleur pour le mode coopératif.
 */
public class CoopController {

    private final String filename = "leaderboardCoopLocal.ser";
    public TextField pseudo;
    public Label info;
    public TextField ip;
    public TextField portF;
    private Label score;
    private Label lives;
    private Label currentLevel;
    private Pane gamePane;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private AnimationTimer gameLoop;
    private Leaderboard leaderboard;
    private Thread infoThread;

    /**
     * Initialise le jeu en mode coopératif.
     *
     * @param event Événement de démarrage
     * @throws IOException            S'il y a une erreur d'entrée/sortie
     * @throws ClassNotFoundException S'il y a une erreur de classe
     */
    public void start(ActionEvent event) throws IOException, ClassNotFoundException {
        String pseudoText = pseudo.getText();
        String ipAdress = ip.getText();
        int portClient = Integer.parseInt(portF.getText());
        if (!pseudoText.isEmpty() && !ipAdress.isEmpty()) {
            info.setText("Connexion en cours...");
            initGame(pseudoText, ipAdress, portClient);
        } else {
            info.setText("Pseudo invalide");
        }
    }

    /**
     * Initialise le jeu en mode coopératif.
     *
     * @param pseudoText Pseudo du joueur
     * @throws IOException            S'il y a une erreur d'entrée/sortie
     * @throws ClassNotFoundException S'il y a une erreur de classe
     */
    private void initGame(String pseudoText, String ipAddress, int portClient) throws IOException, ClassNotFoundException {
        socket = new Socket(ipAddress, portClient);
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        int playerID = (Integer) in.readObject();
        GameClient gameClient = new GameClient(socket, playerID, in, out);
        gameClient.getScore().setName(pseudoText);
        System.out.println("Connecté avec l'ID : " + gameClient.getPlayerID());
        gamePane = gameClient.getPane();
        gameClient.generateLevel();
        leaderboard = ResourceManager.initLeaderboard(filename);
        Scene scene = new Scene(gamePane);
        gamePane.setPrefSize(GameConstants.SCENE_WIDTH, GameConstants.SCENE_HEIGHT);
        ResourceManager.setComposantBackground(gamePane, "/asset/background/lvl1.png");
        scene.getStylesheets().add(String.valueOf(CoopController.class.getResource("/fxml_css/style.css").toExternalForm()));
        Main.getInstance().getStage().setScene(scene);
        setupGame(gameClient, scene);
    }

    /**
     * Configure le jeu en mode coopératif.
     *
     * @param gameClient Client de jeu
     * @param scene      Scène du jeu
     * @throws IOException S'il y a une erreur d'entrée/sortie
     */
    private void setupGame(GameClient gameClient, Scene scene) throws IOException {
        setupInfoIG();
        ClientInput clientInput = new ClientInput(gameClient, scene);
        SoundtrackPlayer soundtrack = new SoundtrackPlayer("/music/main.mp3");
        setupSoundtrack(soundtrack);
        clientInput.start();
        gameClient.sendNewGameState();
        gameLoop(gameClient, clientInput, soundtrack);
        startInfoThread(gameClient);
    }

    /**
     * Lance la boucle de jeu en mode coopératif.
     *
     * @param gameClient  Client de jeu
     * @param clientInput Entrée du client
     * @param soundtrack  Lecteur de bande son
     */
    private void gameLoop(GameClient gameClient, ClientInput clientInput, SoundtrackPlayer soundtrack) {
        gameLoop = new AnimationTimer() {
            long before = System.nanoTime();
            long lastDataTime = System.nanoTime();

            @Override
            public void handle(long now) {
                if (now - before >= GameConstants.INTERVAL) {
                    before = now;
                    try {
                        gameClient.update();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (now - lastDataTime >= GameConstants.SEND_DELAY) {
                        lastDataTime = now;
                        try {
                            gameClient.sendNewGameState();
                            Object receivedData = in.readObject();
                            if (gameClient.isGameFinished()) {
                                gameClient.sendScore();
                            }
                            if (receivedData instanceof TreeMap<?,?>){
                                gameClient.receivedLeaderboard(receivedData, CoopController.this);
                                gameLoopEnd(clientInput, soundtrack);
                            }
                            gameClient.receivedData(receivedData);
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    gameClient.updateEntities();
                }
            }
        };
        gameLoop.start();
    }

    /**
     * Démarre un thread pour mettre à jour les informations du jeu en cours.
     *
     * @param gameClient Client de jeu
     */
    private void startInfoThread(GameClient gameClient) {
        infoThread = new Thread(() -> {
            while (!gameClient.isGameFinished()) {
                Platform.runLater(() -> {
                    this.score.setText(gameClient.getScore().getTeam() + gameClient.getScore().getName() + "    : " + gameClient.getScore().getTotal());
                    this.lives.setText("Vies restantes : " + (gameClient.getNumberOfLives() + 1));
                    this.currentLevel.setText("Niveau : Coop");
                });
                gameClient.waitChange();
            }
        });
        infoThread.start();
    }

    /**
     * Configure la lecture de la bande son.
     *
     * @param soundtrack Lecteur de bande son
     */
    private void setupSoundtrack(SoundtrackPlayer soundtrack) {
        soundtrack.setOnSucceeded(event -> {
            MediaPlayer mediaPlayer = soundtrack.getValue();
            mediaPlayer.play();
            System.out.println("Musique en cours");
        });
        soundtrack.start();
    }

    /**
     * Configure l'affichage du bandeau d'information en jeu.
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

    /**
     * Gère la fin de la boucle de jeu et revient au menu principal.
     *
     * @param clientInput Entrée du client
     * @param soundtrack  Lecteur de bande sonore
     * @throws IOException S'il y a une erreur d'entrée/sortie
     */
    private void gameLoopEnd(ClientInput clientInput, SoundtrackPlayer soundtrack) throws IOException {
        out.close();
        in.close();
        socket.close();
        clientInput.cancel();
        soundtrack.getValue().stop();
        backMainMenu();
    }

    /**
     * Retourne au menu principal.
     *
     * @throws IOException S'il y a une erreur d'entrée/sortie
     */
    private void backMainMenu() throws IOException {
        MainMenu menu = new MainMenu(Main.getInstance().getStage());
        menu.show();
    }

    /**
     * Renvoie la boucle de jeu.
     *
     * @return La boucle de jeu
     */
    public AnimationTimer getGameLoop() {
        return gameLoop;
    }
}
