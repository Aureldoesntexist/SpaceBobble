package com.bobble.spacebobble.core.utilities;

import javafx.scene.Group;

/**
 * Classe gérant la caméra du joueur pour suivre et zoomer sur le joueur dans la scène.
 */
public class CameraPlayer {
    private final double zoom = 1.5;
    private double x;
    private double y;

    /**
     * Constructeur de la classe CameraPlayer.
     *
     * @param x la coordonnée x initiale de la caméra
     * @param y la coordonnée y initiale de la caméra
     */
    public CameraPlayer(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Met à jour la position de la caméra en fonction de la position du joueur et de la taille de la scène.
     *
     * @param playerX     la coordonnée x du joueur
     * @param playerY     la coordonnée y du joueur
     * @param sceneWidth  la largeur de la scène
     * @param sceneHeight la hauteur de la scène
     */
    public void playerCam(double playerX, double playerY, double sceneWidth, double sceneHeight) {
        this.x = ((playerX - 40) * zoom) - ((sceneWidth - 1)) / zoom;
        this.y = (playerY * zoom) - ((sceneHeight - 1)) / zoom;
    }

    /**
     * Applique les transformations de la caméra au groupe d'objets du jeu.
     *
     * @param gameObjects le groupe d'objets du jeu à transformer
     */
    public void apply(Group gameObjects) {
        gameObjects.setTranslateX(-x);
        gameObjects.setTranslateY(-y);
        gameObjects.setScaleX(zoom);
        gameObjects.setScaleY(zoom);
    }
}
