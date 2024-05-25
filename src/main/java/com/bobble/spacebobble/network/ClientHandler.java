package com.bobble.spacebobble.network;

import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.core.scores.Score;
import com.bobble.spacebobble.core.utilities.PlayerPosition;
import com.bobble.spacebobble.core.world.BlockPacket;
import com.bobble.spacebobble.network.Packet.AlienPacket;
import com.bobble.spacebobble.network.Packet.BobblePacket;
import com.bobble.spacebobble.network.Packet.GamePacket;
import com.bobble.spacebobble.network.Packet.SurvivorPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Classe gestionnaire de client qui gère les connexions des clients et la logique du jeu.
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Lock lock;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Constructeur de la classe ClientHandler.
     *
     * @param socket Le socket de la connexion client.
     * @param lock   Le verrou pour synchroniser les threads.
     */
    public ClientHandler(Socket socket, Lock lock) {
        this.clientSocket = socket;
        this.lock = lock;
    }

    /**
     * Méthode exécutée par le thread lors de la mise en marche de l'objet.
     */
    @Override
    public void run() {
        try {
            initStreams();
            waitMinPlayers();
            initLevelToClients();
            initSurvivorsToClients();
            initAliensToClient();
            scheduler.scheduleAtFixedRate(Server::infoSavedSurvivors, 0, 5, TimeUnit.SECONDS);
            while (!Thread.currentThread().isInterrupted()) {
                Object readObject = in.readObject();
                sendAllSurvivors();
                if (readObject instanceof GamePacket) {
                    GameState((GamePacket) readObject);
                }
                if (readObject instanceof Score) {
                    Server.getPlayersScores().add((Score) readObject);
                }
                sendLeaderboard();



            }
        } catch (Exception e) {
            System.out.println("Exception in ClientHandler: " + e.getMessage());
            e.fillInStackTrace();
        } finally {
            scheduler.shutdown();
            cleanStreams();
        }
    }

    /**
     * Initialise les flux d'entrée et de sortie.
     *
     * @throws IOException Si une erreur se produit lors de la création des flux d'entrée et de sortie.
     */
    private void initStreams() throws IOException {
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
    }

    /**
     * Attend que le nombre minimum de joueurs soit atteint pour lancer le jeu.
     *
     * @throws InterruptedException Si le thread est interrompu pendant l'attente.
     */
    private void waitMinPlayers() throws InterruptedException {
        synchronized (lock) {
            while (Server.getActiveConnections() < GameConstants.MIN_PLAYERS) {
                System.out.println("En attente de joueurs");
                lock.wait();
            }
            lock.notifyAll();
        }
    }

    /**
     * Envoie les données de niveau crée aux clients.
     *
     * @throws IOException Si une erreur se produit lors de l'écriture des données dans le flux de sortie.
     */
    private void initLevelToClients() throws IOException {
        List<BlockPacket> blocksToSend = Server.createLevel("/levels/lvl1.txt");
        out.writeObject(blocksToSend);
        out.flush();
    }

    /**
     * Envoie les données d'aliens aux clients lors de la création
     *
     * @throws IOException Si une erreur se produit lors de l'écriture des données dans le flux de sortie.
     */
    private void initAliensToClient() throws IOException {
        List<AlienPacket> aliens = Server.createAliens();
        out.reset();
        out.writeObject(aliens);
        out.flush();
    }

    /**
     * Envoie les données de survivants aux clients lors de la création
     *
     * @throws IOException Si une erreur se produit lors de l'écriture des données dans le flux de sortie.
     */
    private void initSurvivorsToClients() throws IOException {
        List<SurvivorPacket> survivors = Server.createSurvivors();
        out.reset();
        out.writeObject(survivors);
        out.flush();
    }

    /**
     * Envoie les données de l'état du jeu aux clients.
     *
     * @throws IOException Si une erreur se produit lors de l'écriture des données dans le flux de sortie.
     */
    private void GameState(GamePacket gamePacket) throws IOException {
        BobblePacket playerPosition = gamePacket.bobblePacket();
        Server.updatePlayerPosition(playerPosition.getPlayerID(), playerPosition);
        List<SurvivorPacket> saveSurvivors = gamePacket.savedSurvivors();
        if (saveSurvivors != null) {
            Server.addSurvivors(saveSurvivors);
            if (Server.isGameFinished()) {
                gameFinishedToClient();
            }
        }
        sendAllPlayerPositions();
    }

    /**
     * Envoie les positions de tous les joueurs aux clients.
     *
     * @throws IOException Si une erreur se produit lors de l'écriture des données dans le flux de sortie.
     */
    private void sendAllPlayerPositions() throws IOException {
        Collection<PlayerPosition> positions = Server.getAllPlayerPositions();
        out.reset();
        out.writeObject(new ArrayList<>(positions));
        out.flush();
    }

    /**
     * Envoie la liste des survivants aux clients.
     *
     * @throws IOException Si une erreur se produit lors de l'écriture des données dans le flux de sortie.
     */
    private void sendAllSurvivors() throws IOException {
        List<SurvivorPacket> survivors = Server.getUpdateSurvivors();
        if (survivors != null) {
            out.reset();
            out.writeObject(survivors);
            out.flush();
        }
    }

    /**
     * Envoie l'état boolean du jeu terminé aux clients.
     *
     * @throws IOException Si une erreur se produit lors de l'écriture des données dans le flux de sortie.
     */
    private void gameFinishedToClient() throws IOException {
        boolean gameFinished = Server.isGameFinished();
        out.reset();
        out.writeObject(gameFinished);
        out.flush();
    }

    /**
     * Envoie le leaderboard du serveur aux clients
     *
     * @throws IOException Si une erreur se produit lors de l'écriture des données dans le flux de sortie.
     */
    private void sendLeaderboard() throws IOException {
        if (Server.getPlayersScores().size() == GameConstants.MIN_PLAYERS) {
            TreeMap<String, Integer> leaderboard = Server.handleServerLeaderboard();
            out.reset();
            out.writeObject(leaderboard);
            out.flush();
        }
    }

    /**
     * Nettoie les flux utilisés.
     */
    private void cleanStreams() {
        Server.decrementActiveConnections();
        Server.removeClientHandler(this);
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
            e.fillInStackTrace();
        }
    }

}