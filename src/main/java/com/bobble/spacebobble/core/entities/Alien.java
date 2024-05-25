package com.bobble.spacebobble.core.entities;

import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.core.utilities.Position;
import com.bobble.spacebobble.core.world.Block;
import com.bobble.spacebobble.core.world.Trapdoor;
import com.bobble.spacebobble.gestion.ResourceManager;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * Classe représentant l'entité Alien dans le jeu.
 */
public class Alien extends MovingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 9197557623908493367L;

    /**
     * Générateur de nombres aléatoires pour la vitesse et la direction.
     */
    private final Random random = new Random();

    /**
     * Indique si l'Alien est contrôlé par le joueur.
     */
    private boolean isControlled = false;

    /**
     * Direction de l'Alien (true pour gauche, false pour droite).
     */
    private boolean direction;

    /**
     * Constructeur de la classe Alien.
     *
     * @param health   la santé initiale de l'Alien
     * @param position la position initiale de l'Alien
     * @param walls    la liste des blocs du jeu
     */
    public Alien(int health, Position position, List<Block> walls) {
        super(health, position, walls);
        this.health = 5;
        this.sprite = new Rectangle(30, 30);
        this.sprite.setX(this.getPosition().getX());
        this.sprite.setY(this.getPosition().getY());
        this.speed = 1;
        randomizeDirection();
    }

    /**
     * Initialise la direction de l'Alien aléatoirement.
     */
    private void randomizeDirection() {
        direction = random.nextBoolean();
    }

    /**
     * Déplace l'Alien en fonction de sa direction et des collisions avec les murs.
     */
    @Override
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
                if ((this.isMovingLeft() && wall.getY() == 0)) {
                    flipDirection();
                    newX = this.getPosition().getX();
                } else if (this.isDirection() && wall.getY() < 1000) {
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
     * Anime le sprite de l'Alien en fonction de sa direction et de son état contrôlé.
     */
    @Override
    public void animeSprite() {
        String imagePath;
        if (keyState.get(KeyCode.LEFT) || direction) {
            imagePath = this.isControlled() ? "/asset/sprites/leftCA.png" : "/asset/sprites/leftA.png";
        } else if (keyState.get(KeyCode.RIGHT) || !direction) {
            imagePath = this.isControlled() ? "/asset/sprites/rightCA.png" : "/asset/sprites/rightA.png";
        } else {
            // Ne change pas l'image si aucune touche n'est pressée et la direction est indéfinie
            return;
        }
        ImagePattern tile = ResourceManager.loadImage(imagePath);
        this.getSprite().setFill(tile);
    }


    /**
     * Déplace l'Alien en fonction des entrées du joueur.
     */
    public void moveByPlayer() {
        double newX = this.getPosition().getX();
        double newY = this.getPosition().getY();
        Rectangle sprite = this.getSprite();

        if (keyState.get(KeyCode.UP) && this.getOnGround()) {
            this.setVerticalVelocity(GameConstants.JUMP_FORCE);
            this.setOnGround(false);
        }

        if (keyState.get(KeyCode.LEFT)) {
            newX -= this.getSpeed();
        } else if (keyState.get(KeyCode.RIGHT)) {
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
     * Vérifie si l'Alien se déplace vers la gauche.
     *
     * @return true si l'Alien se déplace vers la gauche, sinon false
     */
    private boolean isMovingLeft() {
        return this.getSpeed() < 0;
    }

    /**
     * Vérifie si l'Alien se déplace vers la droite.
     *
     * @return true si l'Alien se déplace vers la droite, sinon false
     */
    private boolean isDirection() {
        return this.getSpeed() > 0;
    }

    /**
     * Inverse la direction de l'Alien.
     */
    private void flipDirection() {
        direction = !direction;
    }

    /**
     * Vérifie si l'Alien est contrôlé par le joueur.
     *
     * @return true si l'Alien est contrôlé, sinon false
     */
    public boolean isControlled() {
        return isControlled;
    }

    /**
     * Définit si l'Alien est contrôlé par le joueur.
     *
     * @param controlled true pour contrôlé par le joueur, sinon false
     */
    public void setControlled(boolean controlled) {
        isControlled = controlled;
    }
}
