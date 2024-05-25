package com.bobble.spacebobble.core;

import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.config.Team;
import com.bobble.spacebobble.controller.CoopController;
import com.bobble.spacebobble.core.entities.Bobble;
import com.bobble.spacebobble.core.entities.Server.CoopAlien;
import com.bobble.spacebobble.core.entities.Server.CoopSurvivor;
import com.bobble.spacebobble.core.entities.Server.OtherBobble;
import com.bobble.spacebobble.core.scores.Leaderboard;
import com.bobble.spacebobble.core.scores.Score;
import com.bobble.spacebobble.core.utilities.Position;
import com.bobble.spacebobble.core.world.Block;
import com.bobble.spacebobble.gestion.ResourceManager;
import com.bobble.spacebobble.gestion.WorldServerGenerator;
import com.bobble.spacebobble.network.Packet.AlienPacket;
import com.bobble.spacebobble.network.Packet.BobblePacket;
import com.bobble.spacebobble.network.Packet.GamePacket;
import com.bobble.spacebobble.network.Packet.SurvivorPacket;
import javafx.scene.Group;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * La classe GameClient principale qui gère la logique de jeu pour un client connecté.
 * Cette classe crée et gère les objets de jeu, les animations et la communication réseau.
 */
public class GameClient {
    private final ConcurrentHashMap<Integer, CoopAlien> aliens;
    private final ConcurrentHashMap<Integer, CoopSurvivor> survivors;
    private final ConcurrentHashMap<Integer, OtherBobble> otherBobble;
    private final List<SurvivorPacket> survivorsSaved;
    private final Socket socket;
    private final Group gameObjects;
    private final Pane pane;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final int playerID;
    private final Score score;
    private WorldServerGenerator worldServerGenerator;
    private List<SurvivorPacket> survivorPackets;
    private List<AlienPacket> alienPackets;
    private Bobble bobble;
    private long respawnTime;
    private int numberOfLives = 2;
    private boolean gameOver = false;
    private boolean gameFinished = false;


    /**
     * Constructeur de la classe GameClient.
     *
     * @param socket   Le socket utilisé pour la communication réseau.
     * @param playerID L'ID du joueur connecté.
     * @param in       Le flux d'entrée utilisé pour recevoir les données.
     * @param out      Le flux de sortie utilisé pour envoyer les données.
     */
    public GameClient(Socket socket, int playerID, ObjectInputStream in, ObjectOutputStream out) {

        // Initialisation des attributs de la classe
        this.out = out;
        this.in = in;
        this.socket = socket;
        this.gameObjects = new Group();
        this.pane = new Pane(gameObjects);
        this.playerID = playerID;
        Team teamColor = (playerID % 2 == 0) ? Team.BLUE : Team.RED;
        this.score = new Score();
        this.score.setTeam(teamColor);

        // Initialisation des ConcurrentHashMaps
        this.aliens = new ConcurrentHashMap<>();
        this.survivors = new ConcurrentHashMap<>();
        this.otherBobble = new ConcurrentHashMap<>();
        this.survivorsSaved = new ArrayList<>();
    }

    /**
     * Génère le niveau de jeu et initialise l'état de jeu initial.
     *
     * @throws IOException            S'il y a un problème avec la connexion socket.
     * @throws ClassNotFoundException S'il y a un problème avec les données reçues.
     */
    public void generateLevel() throws IOException, ClassNotFoundException {

        // Initialisation du gestionnaire de niveau et chargement du niveau
        worldServerGenerator = new WorldServerGenerator(pane, socket);
        worldServerGenerator.loadLevel();

        // Réception des données des survivants et des aliens depuis le serveur
        survivorPackets = (List<SurvivorPacket>) in.readObject();
        alienPackets = (List<AlienPacket>) in.readObject();

        // Initialisation de l'objet bobble principal
        bobble = new Bobble(5, new Position(50, 50), worldServerGenerator.getWalls());
        gameObjects.getChildren().add(bobble.getSprite());
    }

    /**
     * Traite les données reçues du serveur et met à jour l'état de jeu en conséquence.
     *
     * @param receivedData Les données reçues depuis le serveur.
     */
    public void receivedData(Object receivedData) {
        if (receivedData instanceof List<?> dataList) {
            for (Object item : dataList) {
                if (item instanceof BobblePacket bobblePacket) {
                    handlePlayerState(bobblePacket, otherBobble, gameObjects, playerID);
                } else if (item instanceof SurvivorPacket survivorPacket) {
                    CoopSurvivor survivor = survivors.computeIfAbsent(survivorPacket.getID(), id -> new CoopSurvivor(survivorPacket.getID(), new Position(survivorPacket.getX(), survivorPacket.getY()), worldServerGenerator.getWalls(), survivorPacket.isDirection()));
                    if (!gameObjects.getChildren().contains(survivor.getSprite())) {
                        gameObjects.getChildren().add(survivor.getSprite());
                    }
                    if (survivorPacket.isSaved()) {
                        gameObjects.getChildren().remove(survivor.getSprite());
                        survivor.setSaved(true);
                    }
                }
            }
        }
        if (receivedData instanceof Boolean) {
            System.out.println("Tous les survivants ont été sauvés");
            gameFinished = (boolean) receivedData;
            System.out.println(gameFinished);

        }

    }

    /**
     * Traite les données reçues du serveur et met à jour le leaderboard du mode Coop coté client
     *
     * @param receivedData   Les données reçues depuis le serveur.
     * @param coopController Interface visuelle du mode coop
     */
    public void receivedLeaderboard(Object receivedData, CoopController coopController) {
        Leaderboard leaderboard = ResourceManager.initLeaderboard("leaderboardCoopLocal.ser");
        leaderboard.setScoreTreemap((TreeMap<String, Integer>) receivedData);
        ResourceManager.serializeLeaderboard(leaderboard.getScoreTreemap(), "leaderboardCoopLocal.ser");
        coopController.getGameLoop().stop();
    }

    /**
     * Gère les données des états des joueurs reçues depuis le serveur et met à jour l'état de jeu en conséquence.
     *
     * @param bobblePacket Les données des états des joueurs reçues depuis le serveur.
     * @param otherBobble  La ConcurrentHashMap contenant les autres joueurs.
     * @param gameObjects  Le groupe des objets de jeu.
     * @param playerID     L'ID du joueur connecté.
     */
    private void handlePlayerState(BobblePacket bobblePacket, ConcurrentHashMap<Integer, OtherBobble> otherBobble, Group gameObjects, int playerID) {
        if (bobblePacket.getPlayerID() != playerID) {
            // Si aucun objet OtherPlayer n'existe pour l'ID du joueur, alors nous en créons un avec la bobblePacket donnée.
            OtherBobble other = otherBobble.computeIfAbsent(bobblePacket.getPlayerID(), id -> {
                OtherBobble newPlayer = new OtherBobble(bobblePacket);
                // Nous ajoutons également le sprite du nouvel objet au groupe d'objets de jeu.
                gameObjects.getChildren().add(newPlayer.getSprite());
                // Nous retournons le nouvel objet afin qu'il puisse être ajouté à la ConcurrentHashmap.
                return newPlayer;
            });
            // Nous mettons à jour la bobblePacket et le nombre de vies de l'objet OtherPlayer.
            other.updatePositionAndLives((int) bobblePacket.getX(), (int) bobblePacket.getY(), bobblePacket.getLives());
        }
    }

    /**
     * Gère les données des aliens reçues depuis le serveur et met à jour l'état de jeu en conséquence.
     *
     * @param alienPacket Les données des aliens reçues depuis le serveur.
     * @param walls       La liste des murs dans le niveau de jeu.
     */
    private void handleAlienData(AlienPacket alienPacket, List<Block> walls) {
        CoopAlien alien = aliens.computeIfAbsent(alienPacket.getId(), id -> {
            CoopAlien coopAlien = new CoopAlien(alienPacket.getId(), new Position(alienPacket.getX(), alienPacket.getY()), walls, alienPacket.isDirection());
            gameObjects.getChildren().add(coopAlien.getSprite());
            return coopAlien;
        });

        if (bobble.collidesWith(alien.getSprite()) && !bobble.isInvincible()) {
            bobble.setHealth(bobble.getHealth() - 1);
            score.setTotal(score.getTotal() - 150);
            notifyChange();

        }
        alien.move();
        alien.updateSprite();
        alien.animeSprite();
    }

    /**
     * Gère les données des survivants reçues depuis le serveur et met à jour l'état de jeu en conséquence.
     *
     * @param survivorPacket Les données des survivants reçues depuis le serveur.
     * @param walls          La liste des murs dans le niveau de jeu.
     * @param coopSurvivors  La ConcurrentHashMap contenant les survivants.
     */
    private void handleSurvivorsData(SurvivorPacket survivorPacket, List<Block> walls, ConcurrentHashMap<Integer, CoopSurvivor> coopSurvivors) {
        CoopSurvivor coopSurvivor = coopSurvivors.computeIfAbsent(survivorPacket.getID(), id -> {
            CoopSurvivor newSurvivors = new CoopSurvivor(survivorPacket.getID(), new Position(survivorPacket.getX(),
                    survivorPacket.getY()), walls, survivorPacket.isDirection());
            gameObjects.getChildren().add(newSurvivors.getSprite());
            return newSurvivors;
        });
        if (bobble.collidesWith(coopSurvivor.getSprite()) && !bobble.isInvincible() && !coopSurvivor.isSaved() && bobble.isAlive()) {
            gameObjects.getChildren().remove(coopSurvivor.getSprite());
            coopSurvivor.setSaved(true);
            synchronized (score) {
                score.setTotal(score.getTotal() + 500);
                notifyChange();
            }
            survivorsSaved.add(survivorPacket);
        } else {
            coopSurvivor.move();
            coopSurvivor.updateSprite();
            coopSurvivor.animeSprite();
        }
    }

    /**
     * Met à jour les entités dans l'état de jeu.
     */
    public void updateEntities() {
        // Supprime le bobble et les objets de jeu des autres joueurs s'ils ne sont plus en vie
        if (!bobble.isAlive()) {
            gameObjects.getChildren().remove(bobble.getSprite());
        }
        for (Map.Entry<Integer, OtherBobble> entry : otherBobble.entrySet()) {
            OtherBobble otherBobble = entry.getValue();
            if (!otherBobble.isAlive()) {
                gameObjects.getChildren().remove(otherBobble.getSprite());
                this.otherBobble.remove(entry.getKey());
            }
        }
    }

    /**
     * Vérifie l'état de la partie en fonction de l'état actuel du bobble et des autres joueurs.
     */
    private synchronized void checkGameStatus() {
        // Vérifie si le bobble n'est plus en vie
        if (!bobble.isAlive()) {
            // Diminue les points de vie du bobble et le rend invincible s'il reste des points de vie
            if (numberOfLives > 0) {
                numberOfLives--;
                notifyChange();
                respawn();
                bobble.setInvincible(true);


            } else {
                // Vérifie si tous les joueurs sont morts
                boolean allPlayersDead = true;
                for (OtherBobble other : otherBobble.values()) {
                    if (other.isAlive() || other.getHealth() > 0) {
                        allPlayersDead = false;
                        break;
                    }
                }
                // Met l'état de la partie à "terminé".
                if (allPlayersDead) {
                    gameOver = true;
                }
            }
        } else {
            // Vérifie si tous les joueurs sont morts
            boolean allPlayersDead = true;
            for (OtherBobble other : otherBobble.values()) {
                if (other.isAlive() || other.getHealth() > 0) {
                    allPlayersDead = false;
                    break;
                }
            }
            // Met l'état de la partie à "terminé" et envoie le score si tous les joueurs sont morts et que le bobble n'est plus en vie
            if (allPlayersDead && numberOfLives == 0 && !bobble.isAlive()) {
                gameOver = true;
            }
        }
        // Passe au niveau suivant si la partie n'est pas terminée
        if (gameOver) {
            gameFinished = true;
            System.out.println("Vous êtes mort.");
        }
    }



    /**
     * Met à jour les aliens dans l'état de jeu.
     *
     * @param alienPacketList La liste des données d'aliens reçues depuis le serveur.
     * @param walls           La liste des murs dans le niveau de jeu.
     */
    private void updateAliens(List<AlienPacket> alienPacketList, List<Block> walls) {

        // Boucle à travers la liste des données d'aliens et gère chaque élément
        for (AlienPacket alienPacket : alienPacketList) {
            handleAlienData(alienPacket, walls);
        }
    }

    /**
     * Met à jour les survivants dans l'état de jeu.
     *
     * @param survivorPacketList La liste des données de survivants reçues depuis le serveur.
     * @param walls              La liste des murs dans le niveau de jeu.
     */
    private void updateSurvivors(List<SurvivorPacket> survivorPacketList, List<Block> walls) {

        // Boucle à travers la liste des données de survivants et gère chaque élément
        for (SurvivorPacket survivorPacket : survivorPacketList) {
            handleSurvivorsData(survivorPacket, walls, survivors);
        }
    }

    /**
     * Envoie l'état de jeu actuel au serveur.
     *
     * @param x              La coordonnée x du bobble.
     * @param y              La coordonnée y du bobble.
     * @param health         Les points de vie du bobble.
     * @param savedSurvivors La liste des survivants sauvés.
     * @throws IOException S'il y a un problème avec la connexion socket.
     */
    public void sendGameState(double x, double y, int health, List<SurvivorPacket> savedSurvivors) throws IOException {

        // Crée un nouvel objet d'état de joueur
        BobblePacket bobblePacket = new BobblePacket(playerID, x, y, health);

        // Crée un nouvel objet d'état de jeu
        GamePacket gamePacket = new GamePacket(bobblePacket, savedSurvivors);

        // Boucle à travers la liste des survivants sauvés et met leur statut de sauvé à vrai
        for (SurvivorPacket survivorPacket : savedSurvivors) {
            if (!survivorPacket.isSaved()) {
                survivorPacket.setSaved(true);
            }
        }

        // Envoie l'état de jeu au serveur
        out.writeObject(gamePacket);
        out.flush();
        out.reset();

        // Vide la liste des survivants sauvés
        survivorsSaved.clear();
    }

    /**
     * Envoie le score au serveur.
     *
     * @throws IOException S'il y a un problème avec la connexion socket.
     */
    public void sendScore() throws IOException {
        out.writeObject(score);
        out.flush();
        out.reset();
    }

    /**
     * Met à jour l'état de jeu localement.
     */
    public void update() throws IOException {
        // Met à jour les aliens et les survivants
        updateAliens(alienPackets, worldServerGenerator.getWalls());
        updateSurvivors(survivorPackets, worldServerGenerator.getWalls());
        // Déplace le bobble et met à jour son affichage
        bobble.move();
        bobble.updateSprite();
        bobble.animeSprite();
        // Vérifie l'état d'invincibilité du bobble
        checkInvulnerability();
        // Vérifie l'état de la partie
        checkGameStatus();

    }

    /**
     * Mise à jour de l'envoi de l'état du jeu.
     */
    public void sendNewGameState() throws IOException {
        sendGameState(bobble.getPosition().getX(), bobble.getPosition().getY(), bobble.getHealth(), survivorsSaved);
    }

    /**
     * Réinitialise la position et l'état d'invincibilité du bobble.
     */
    private void respawn() {
        // Réinitialise la position, les points de vie et l'état d'invincibilité du bobble
        bobble.setHealth(3);
        bobble.setPosition(300, 50);
        bobble.setInvincible(true);
        bobble.getSprite().setOpacity(0.5);
        // Met à jour le temps de réapparition
        respawnTime = System.currentTimeMillis();

    }

    /**
     * Vérifie l'état d'invincibilité du bobble.
     */
    private void checkInvulnerability() {
        // Vérifie si le bobble est invincible et que le temps de réapparition est passé
        if (bobble.isInvincible() && (System.currentTimeMillis() - respawnTime) > GameConstants.DELAY_RESPAWN) {
            // Met l'état d'invincibilité du bobble à faux et rétablit l'opacité de son objet de son sprite
            bobble.setInvincible(false);
            bobble.getSprite().setOpacity(1);
        }
    }

    /**
     * Retourne l'objet bobble.
     *
     * @return L'objet bobble.
     */
    public Bobble getBobble() {
        return bobble;
    }

    /**
     * Retourne l'ID du joueur connecté.
     *
     * @return L'ID du joueur connecté.
     */
    public int getPlayerID() {
        return playerID;
    }

    /**
     * Retourne le panneau utilisé pour afficher le jeu.
     *
     * @return Le panneau utilisé pour afficher le jeu.
     */
    public Pane getPane() {
        return pane;
    }

    /**
     * Retourne la liste des murs dans le niveau de jeu.
     *
     * @return La liste des murs dans le niveau de jeu.
     */
    public List<Block> getWalls() {
        return worldServerGenerator.getWalls();
    }

    /**
     * Retourne l'objet score.
     *
     * @return L'objet score.
     */
    public Score getScore() {
        return score;
    }

    /**
     * Renvoie l'état de fin total du jeu.
     *
     * @return Etat de fin totale
     */
    public boolean isGameFinished() {
        return gameFinished;
    }

    /**
     * Renvoie le nombre de vies du joueur
     *
     * @return Nombre de vies
     */
    public int getNumberOfLives() {
        return numberOfLives;
    }

    /*
     * Méthode pour mettre en attente le thread de l'afficheur des informations
     */
    public synchronized void waitChange() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Méthode pour synchroniser les threads.
     */
    public synchronized void notifyChange() {
        notifyAll();
    }
}