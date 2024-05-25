package com.bobble.spacebobble.core.entities.Server;

import com.bobble.spacebobble.core.utilities.Position;
import com.bobble.spacebobble.gestion.ResourceManager;
import com.bobble.spacebobble.network.Packet.BobblePacket;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.Serial;
import java.io.Serializable;

/**
 * Classe représentant un autre Bobble dans le jeu, reconstruite avec les données serveurs.
 */
public class OtherBobble implements Serializable {
    @Serial
    private static final long serialVersionUID = 6403409415087491446L;

    private final Rectangle sprite;

    private Position position;

    private int health;

    /**
     * Dernière position horizontale du Bobble.
     */
    private double lastX;

    /**
     * Constructeur de la classe OtherBobble.
     *
     * @param bobblePacket paquet contenant les informations du Bobble
     */
    public OtherBobble(BobblePacket bobblePacket) {
        this.position = new Position(bobblePacket.getX(), bobblePacket.getY());
        this.health = bobblePacket.getLives();
        this.sprite = new Rectangle(30, 30, Color.RED);
        this.lastX = position.getX();
        updateSpritePosition();
        animeSprite();
    }

    /**
     * Met à jour la position et les points de vie du Bobble.
     *
     * @param x      nouvelle position horizontale du Bobble
     * @param y      nouvelle position verticale du Bobble
     * @param health nouveaux points de vie du Bobble
     */
    public void updatePositionAndLives(int x, int y, int health) {
        if (x != position.getX()) {
            lastX = position.getX();
            ImagePattern tile = ResourceManager.loadImage("/asset/sprites/waitOPR.png");
            this.sprite.setFill(tile);
        }
        this.position = new Position(x, y);
        this.health = health;
        updateSpritePosition();
        animeSprite();
    }

    /**
     * Met à jour la position du sprite du Bobble.
     */
    private void updateSpritePosition() {
        sprite.setX(position.getX());
        sprite.setY(position.getY());
    }

    /**
     * Anime le sprite du Bobble en fonction de sa direction de déplacement.
     */
    private void animeSprite() {
        String imagePath;
        if (position.getX() > lastX) {
            imagePath = "/asset/sprites/rightOP.png";
        } else if (position.getX() < lastX) {
            imagePath = "/asset/sprites/leftOP.png";
        } else {
            // Ne change pas l'image si la position est la même
            return;
        }
        ImagePattern tile = ResourceManager.loadImage(imagePath);
        this.sprite.setFill(tile);
    }


    /**
     * Retourne le sprite du Bobble.
     *
     * @return le sprite du Bobble
     */
    public Rectangle getSprite() {
        return sprite;
    }

    /**
     * Retourne les points de vie du Bobble.
     *
     * @return les points de vie du Bobble
     */
    public int getHealth() {
        return health;
    }

    /**
     * Vérifie si le Bobble est toujours en vie.
     *
     * @return true si le Bobble est en vie, sinon false
     */
    public boolean isAlive() {
        return this.health > 0;
    }
}
