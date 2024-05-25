package com.bobble.spacebobble.core.entities;

import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.core.utilities.Position;
import com.bobble.spacebobble.core.world.Block;
import com.bobble.spacebobble.gestion.ResourceManager;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * Classe représentant l'entité Survivor dans le jeu.
 */
public class Survivor extends MovingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1695752909181586890L;

    /**
     * Générateur de nombres aléatoires pour la direction.
     */
    private final Random random = new Random();

    /**
     * Direction de l'entité (true pour gauche, false pour droite).
     */
    private boolean direction;

    /**
     * Constructeur de la classe Survivor.
     *
     * @param health   la santé initiale de l'entité
     * @param position la position initiale de l'entité
     * @param walls    la liste des blocs du jeu
     */
    public Survivor(int health, Position position, List<Block> walls) {
        super(health, position, walls);
        this.health = 1;
        this.sprite = new Rectangle(30, 30);
        this.sprite.setX(this.getPosition().getX());
        this.sprite.setY(this.getPosition().getY());
        this.speed = 0.3;
        randomizeDirection();
    }

    /**
     * Initialise la direction de l'entité aléatoirement.
     */
    private void randomizeDirection() {
        direction = random.nextBoolean();
    }

    /**
     * Déplace l'entité en fonction de sa direction et des collisions avec les murs.
     */
    public void move() {
        Rectangle sprite = this.getSprite();
        double newX = this.getPosition().getX();
        double newY = this.getPosition().getY();

        if (direction) {
            newX -= this.getSpeed();
        } else {
            newX += this.getSpeed();
        }

        this.setVerticalVelocity(this.getVerticalVelocity() + GameConstants.GRAVITY);
        newY += this.getVerticalVelocity();

        for (Block wall : walls) {
            if (wall.collidesWith(new Rectangle(newX, sprite.getY(), sprite.getWidth(), sprite.getHeight()))) {
                if ((this.isDirectionL() && wall.getY() == 0)) {
                    flipDirection();
                    newX = this.getPosition().getX();
                } else if (this.isDirectionR() && wall.getY() < 1000) {
                    flipDirection();
                    newX = this.getPosition().getX();
                }
            }
        }

        for (Block wall : walls) {
            if (wall.collidesWith(new Rectangle(sprite.getX(), newY, sprite.getWidth(), sprite.getHeight()))) {
                if (this.getVerticalVelocity() > 0) {
                    newY = (wall.getY() - sprite.getHeight());
                    this.setOnGround(true);
                } else {
                    newY = wall.getY() + wall.getTile().getHeight();
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
     * Anime le sprite de l'entité en fonction de sa direction.
     */
    @Override
    public void animeSprite() {
        String imagePath;
        if (direction) {
            imagePath = "/asset/sprites/survivorL.png";
        } else {
            imagePath = "/asset/sprites/survivorR.png";
        }
        ImagePattern tile = ResourceManager.loadImage(imagePath);
        this.getSprite().setFill(tile);
    }


    /**
     * Vérifie si la direction de l'entité est vers la gauche.
     *
     * @return true si la direction est vers la gauche, sinon false
     */
    private boolean isDirectionL() {
        return this.getSpeed() < 0;
    }

    /**
     * Vérifie si la direction de l'entité est vers la droite.
     *
     * @return true si la direction est vers la droite, sinon false
     */
    private boolean isDirectionR() {
        return this.getSpeed() > 0;
    }

    /**
     * Inverse la direction de l'entité.
     */
    private void flipDirection() {
        direction = !direction;
    }
}
