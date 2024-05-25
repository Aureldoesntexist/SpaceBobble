package com.bobble.spacebobble.network;

import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.config.Team;
import com.bobble.spacebobble.core.scores.Leaderboard;
import com.bobble.spacebobble.core.scores.Score;
import com.bobble.spacebobble.core.utilities.PlayerPosition;
import com.bobble.spacebobble.core.world.BlockPacket;
import com.bobble.spacebobble.gestion.ResourceManager;
import com.bobble.spacebobble.gestion.WorldGenerator;
import com.bobble.spacebobble.network.Packet.AlienPacket;
import com.bobble.spacebobble.network.Packet.SurvivorPacket;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * La classe principale du serveur qui gère les connexions des clients et la logique du jeu.
 */
public class Server {
    private static final Lock lock = new ReentrantLock();
    private static final ConcurrentHashMap<Integer, PlayerPosition> playerPositions = new ConcurrentHashMap<>();
    private static final List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());
    private static final List<SurvivorPacket> totalSavedSurvivors = Collections.synchronizedList(new ArrayList<>());
    private static final List<Score> playersScores = Collections.synchronizedList(new ArrayList<>());
    private static final WorldGenerator worldCreation = new WorldGenerator();
    private static int activeConnections = 0;
    private static int nextClientID = 1;
    private static volatile boolean survivorsUpdate = false;
    private static Leaderboard leaderboard;


    /**
     * La méthode principale qui démarre le serveur et gère les connexions des clients.
     *
     * @param args Les arguments de la ligne de commande.
     */
    public static void main(String[] args) {
        //int port = Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            System.out.println("Serveur en écoute sur le port : " + 1234);
            while (nextClientID <= GameConstants.MIN_PLAYERS) {
                Socket socket = serverSocket.accept();
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                int clientID;
                incrementActiveConnections();
                clientID = nextClientID++;
                out.writeObject(clientID);
                out.flush();
                System.out.println("Connexion acceptée: " + socket.getInetAddress().getHostAddress() + " Nombre de connexion : " + activeConnections);
                ClientHandler handler = new ClientHandler(socket, lock);
                synchronized (clientHandlers) {
                    clientHandlers.add(handler);
                    System.out.println("Clients connectés : " + getAllClientHandlers());
                }
                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
            e.fillInStackTrace();
        }

    }

    /**
     * Crée une liste d'aliens pour le jeu.
     *
     * @return Une liste d'objets AlienData.
     */
    public static List<AlienPacket> createAliens() {
        List<AlienPacket> aliens = new ArrayList<>();
        aliens.add(new AlienPacket(1, 450, 100, true));
        aliens.add(new AlienPacket(2, 150, 200, false));
        aliens.add(new AlienPacket(3, 720, 400, true));
        aliens.add(new AlienPacket(4, 55, 500, false));
        aliens.add(new AlienPacket(5, 800, 500, false));
        aliens.add(new AlienPacket(6, 450, 100, true));
        aliens.add(new AlienPacket(7, 900, 700, false));
        aliens.add(new AlienPacket(8, 200, 700, true));
        return aliens;
    }

    /**
     * Crée une liste de survivants pour le jeu.
     *
     * @return Une liste d'objets SurvivorData.
     */
    public static List<SurvivorPacket> createSurvivors() {
        List<SurvivorPacket> survivorData = new ArrayList<>();
        survivorData.add(new SurvivorPacket(1, 760, 100, false));
        survivorData.add(new SurvivorPacket(2, 555, 300, true));
        survivorData.add(new SurvivorPacket(3, 50, 300, false));
        survivorData.add(new SurvivorPacket(4, 45, 500, true));
        survivorData.add(new SurvivorPacket(5, 800, 500, false));
        survivorData.add(new SurvivorPacket(6, 450, 100, true));
        survivorData.add(new SurvivorPacket(7, 825, 700, false));
        survivorData.add(new SurvivorPacket(8, 55, 700, true));
        return survivorData;
    }

    /**
     * Crée une liste de données de blocs à partir d'un fichier.
     *
     * @param fileName Le nom du fichier à lire.
     * @return Une liste d'objets BlockData.
     */
    public static List<BlockPacket> createLevel(String fileName) {
        List<BlockPacket> blockData = new ArrayList<>();
        worldCreation.createLevelOnline(blockData, fileName);
        return blockData;
    }

    /**
     * Affiche la liste des survivants sauvés.
     */
    public static void infoSavedSurvivors() {
        synchronized (totalSavedSurvivors) {
            System.out.println("Survivants sauvés : " + totalSavedSurvivors);
        }
    }
    /**
     * Ajoute une liste de survivants à la liste des survivants sauvés.
     *
     * @param newSurvivors La liste de survivants à ajouter.
     */
    public static synchronized void addSurvivors(List<SurvivorPacket> newSurvivors) {
        if (!newSurvivors.isEmpty()) {
            totalSavedSurvivors.addAll(newSurvivors);
            survivorsUpdate = true;
        }
    }

    /**
     * Renvoie la liste de tous les gestionnaires de clients.
     *
     * @return Une liste d'objets ClientHandler.
     */
    public static List<ClientHandler> getAllClientHandlers() {
        synchronized (clientHandlers) {
            return new ArrayList<>(clientHandlers);
        }
    }

    /**
     * Renvoie la liste des survivants mis à jour.
     *
     * @return Une liste d'objets SurvivorData.
     */
    public static List<SurvivorPacket> getUpdateSurvivors() {
        if (survivorsUpdate) {
            survivorsUpdate = false;
            return new ArrayList<>(totalSavedSurvivors);
        }
        return null;
    }

    /**
     * Renvoie la collection de toutes les positions des joueurs.
     *
     * @return Une collection d'objets PlayerPosition.
     */
    public static Collection<PlayerPosition> getAllPlayerPositions() {
        return playerPositions.values();
    }

    /**
     * Met à jour la position d'un joueur.
     *
     * @param playerId L'ID du joueur.
     * @param position La nouvelle position du joueur.
     */
    public static void updatePlayerPosition(int playerId, PlayerPosition position) {
        playerPositions.put(playerId, position);
    }

    /**
     * Vérifie si le jeu est terminé.
     *
     * @return Vrai si le jeu est terminé, faux sinon.
     */
    public static boolean isGameFinished() {
        synchronized (totalSavedSurvivors) {
            return totalSavedSurvivors.size() == createSurvivors().size();
        }
    }

    /**
     * Récupère et calcule la somme des deux membres de chaque team
     * Compare le total et ajoute le meilleur dans le leaderboard du serveur
     *
     * @return Le leaderboard
     */
    public static TreeMap<String, Integer> handleServerLeaderboard() {
        int scoreTeamRed = 0;
        int scoreTeamBlue = 0;
        StringBuilder nameRed = new StringBuilder();
        StringBuilder nameBlue = new StringBuilder();

        for (Score score : Server.playersScores) {
            if (score.getTeam() == Team.RED) {
                scoreTeamRed += score.getTotal();
                if (!nameRed.toString().contains(score.getName())) {
                    if (!nameRed.isEmpty()) {
                        nameRed.append(", ");
                    }
                    nameRed.append(score.getName());
                }
            }
            if (score.getTeam() == Team.BLUE) {
                scoreTeamBlue += score.getTotal();
                if (!nameBlue.toString().contains(score.getName())) {
                    if (!nameBlue.isEmpty()) {
                        nameBlue.append(", ");
                    }
                    nameBlue.append(score.getName());
                }
            }
        }
        leaderboard = ResourceManager.initLeaderboard("leaderboardCoopServer.ser");
        if (scoreTeamRed > scoreTeamBlue) {
            leaderboard.addScore(nameRed.toString(), scoreTeamRed);
        } else {
            leaderboard.addScore(nameBlue.toString(), scoreTeamBlue);
        }
        ResourceManager.serializeLeaderboard(leaderboard.getScoreTreemap(), "leaderboardCoopServer.ser");
        return leaderboard.getScoreTreemap();
    }

    /**
     * Supprime un gestionnaire de client de la liste des gestionnaires de clients.
     *
     * @param handler Le gestionnaire de client à supprimer.
     */
    public static void removeClientHandler(ClientHandler handler) {
        synchronized (clientHandlers) {
            clientHandlers.remove(handler);
        }
    }

    /**
     * Renvoie le nombre de connexions actives.
     *
     * @return Le nombre de connexions actives.
     */
    public static int getActiveConnections() {
        lock.lock();
        try {
            return activeConnections;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Incrémente le nombre de connexions actives.
     */
    public static void incrementActiveConnections() {
        lock.lock();
        try {
            activeConnections++;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Décrémente le nombre de connexions actives.
     */
    public static void decrementActiveConnections() {
        lock.lock();
        try {
            activeConnections--;
            if (activeConnections < GameConstants.MIN_PLAYERS) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Renvoie la liste de tous les scores.
     *
     * @return the all scores
     */
    public static List<Score> getPlayersScores() {
        return playersScores;
    }


    public static Leaderboard getLeaderboard() {
        return leaderboard;
    }
}