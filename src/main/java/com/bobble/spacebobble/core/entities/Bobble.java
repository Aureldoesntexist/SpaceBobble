package com.bobble.spacebobble.core.entities;

import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.core.utilities.Position;
import com.bobble.spacebobble.core.world.Block;
import com.bobble.spacebobble.core.world.Trapdoor;
import com.bobble.spacebobble.gestion.ResourceManager;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Classe représentant l'entité du joueur principal.
 */
public class Bobble extends MovingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -2984281187483132993L;

    /**
     * Indique si le joueur est invincible.
     */
    private boolean invincible;

    /**
     * Indique la direction dans laquelle le joueur est orienté (true pour droite, false pour gauche).
     */
    private boolean direction = true;

    /**
     * Constructeur de la classe Bobble.
     *
     * @param health   la santé initiale du joueur
     * @param position la position initiale du joueur
     * @param walls    la liste des blocs du jeu
     */
    public Bobble(int health, Position position, List<Block> walls) {
        super(health, position, walls);
        this.health = 3;
        this.speed = 3;
        this.invincible = false;
        this.sprite = new Rectangle(30, 30, Color.WHITE);
        this.sprite.setX(this.getPosition().getX());
        this.sprite.setY(this.getPosition().getY());
    }

    /**
     * Déplace le joueur en fonction des touches pressées et des collisions avec les murs.
     */
    @Override
    public void move() {
        double newX = this.getPosition().getX();
        double newY = this.getPosition().getY();
        Rectangle sprite = this.getSprite();
        if (keyState.get(KeyCode.Z) && this.getOnGround()) {
            this.setVerticalVelocity(GameConstants.JUMP_FORCE);
            this.setOnGround(false);
        }
        if (keyState.get(KeyCode.Q)) {
            newX -= this.getSpeed();
        } else if (keyState.get(KeyCode.D)) {
            newX += this.getSpeed();
        }
        this.setVerticalVelocity(this.getVerticalVelocity() + GameConstants.GRAVITY);
        newY += this.getVerticalVelocity();
        for (Block wall : walls) {
            Rectangle spriteBounds = new Rectangle(sprite.getX(), newY, sprite.getWidth(), sprite.getHeight());
            if (wall instanceof Trapdoor && ((Trapdoor) wall).isOpened()) {
                continue;
            }
            if (wall.collidesWith(spriteBounds)) {
                if (this.getVerticalVelocity() > 0) {
                    newY = (wall.getY() - sprite.getHeight());
                    this.setOnGround(true);
                } else {
                    newY = wall.getY() + wall.getTile().getHeight() + 0.1;
                }
                this.setVerticalVelocity(0);
            }
        }
        for (Block wall : walls) {
            if (wall.collidesWith(new Rectangle(newX, sprite.getY(), sprite.getWidth(), sprite.getHeight()))) {
                newX = this.getPosition().getX();
            }
        }
        updatePosition(newX, newY);
    }

    /**
     * Anime le sprite du joueur en fonction de son état.
     */
    @Override
    public void animeSprite() {
        String imagePath;

        if (this.isInvincible()) {
            imagePath = "/asset/sprites/invi.png";
        } else if (keyState.get(KeyCode.D)) {
            imagePath = "/asset/sprites/right.png";
            direction = true;
        } else if (keyState.get(KeyCode.Q)) {
            imagePath = "/asset/sprites/left.png";
            direction = false;
        } else if (keyState.get(KeyCode.S)) {
            imagePath = direction ? "/asset/sprites/downR.png" : "/asset/sprites/downL.png";
        } else if (keyState.get(KeyCode.Z)) {
            imagePath = "/asset/sprites/jump.png";
        } else {
            imagePath = direction ? "/asset/sprites/waitR.png" : "/asset/sprites/waitL.png";
        }

        ImagePattern tile = ResourceManager.loadImage(imagePath);
        this.getSprite().setFill(tile);
    }


    /**
     * Vérifie si le joueur est invincible.
     *
     * @return true si le joueur est invincible, sinon false
     */
    public boolean isInvincible() {
        return this.invincible;
    }

    /**
     * Définit l'état d'invincibilité du joueur.
     *
     * @param invincible l'état d'invincibilité à définir
     */
    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }
}
