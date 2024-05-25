package com.bobble.spacebobble.config;

/**
 * Classe contenant les constantes du jeu et du serveur.
 */
public class GameConstants {

    /**
     * Largeur de la scène du jeu.
     */
    public static final int SCENE_WIDTH = 1280;

    /**
     * Hauteur de la scène du jeu.
     */
    public static final int SCENE_HEIGHT = 720;

    /**
     * Délai de réapparition du joueur en millisecondes.
     */
    public static final int DELAY_RESPAWN = 2000;

    /**
     * Délai entre les actions du joueur en millisecondes.
     */
    public static final int DELAY_BETWEEN = 500;

    /**
     * Temps de recharge des projectiles en nanosecondes.
     */
    public static final int PROJECTILES_COOLDOWN = 100_000_000;


    /**
     * Intervalle de temps en nanosecondes pour la mise à jour du jeu.
     */
    public static final int INTERVAL = 1000000000 / 60;

    /**
     * Taille d'une case du jeu en pixels.
     */
    public static final int SIZE_CASE = 25;

    /**
     * Force du saut du joueur.
     */
    public static final int JUMP_FORCE = -15;

    /**
     * Gravité du jeu.
     */
    public static final double GRAVITY = 0.5;


    /**
     * Nombre minimum de joueurs pour démarrer une partie.
     */
    public static final int MIN_PLAYERS = 2;

    /**
     * Délai d'envoi de l'état du jeu par le client
     */
    public static final long SEND_DELAY = 50_000_000;
}
