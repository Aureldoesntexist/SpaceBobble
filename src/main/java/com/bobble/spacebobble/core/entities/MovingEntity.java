package com.bobble.spacebobble.core.entities;

import com.bobble.spacebobble.core.utilities.Position;
import com.bobble.spacebobble.core.world.Block;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe abstraite représentant une entité mobile dans le jeu.
 */
public abstract class MovingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 8209048162745041573L;

    /**
     * État actuel des touches du clavier.
     */
    protected final ConcurrentHashMap<KeyCode, Boolean> keyState = new ConcurrentHashMap<>();

    /**
     * Position de l'entité.
     */
    private final Position position;

    /**
     * Vitesse de déplacement de l'entité.
     */
    protected double speed;

    /**
     * Sprite représentant l'entité.
     */
    protected Rectangle sprite;

    /**
     * Santé de l'entité.
     */
    protected int health;

    /**
     * Liste des blocs du jeu.
     */
    protected List<Block> walls;

    /**
     * Vitesse verticale de l'entité.
     */
    private double verticalVelocity;

    /**
     * Indique si l'entité est au sol ou non.
     */
    private Boolean isOnGround;

    /**
     * Constructeur de la classe MovingEntity.
     *
     * @param health   la santé initiale de l'entité
     * @param position la position initiale de l'entité
     * @param walls    la liste des blocs du jeu
     */
    public MovingEntity(int health, Position position, List<Block> walls) {
        this.position = position;
        this.health = health;
        this.walls = walls;
        this.verticalVelocity = 0;
        initializeKeyState();
    }

    /**
     * Retourne l'état actuel des touches du clavier.
     *
     * @return l'état des touches du clavier
     */
    public ConcurrentHashMap<KeyCode, Boolean> getKeyState() {
        return keyState;
    }

    /**
     * Retourne le sprite de l'entité.
     *
     * @return le sprite de l'entité
     */
    public Rectangle getSprite() {
        return sprite;
    }

    /**
     * Retourne la santé de l'entité.
     *
     * @return la santé de l'entité
     */
    public int getHealth() {
        return health;
    }

    /**
     * Définit la santé de l'entité.
     *
     * @param health la santé à définir
     */
    public void setHealth(int health) {
        this.health = health;
    }

    /**
     * Retourne la position de l'entité.
     *
     * @return la position de l'entité
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Définit la position de l'entité.
     *
     * @param x la coordonnée x de la nouvelle position
     * @param y la coordonnée y de la nouvelle position
     */
    public void setPosition(double x, double y) {
        this.position.setX(x);
        this.position.setY(y);
    }

    /**
     * Retourne la vitesse de déplacement de l'entité.
     *
     * @return la vitesse de déplacement de l'entité
     */
    public double getSpeed() {
        return this.speed;
    }

    /**
     * Définit la vitesse de déplacement de l'entité.
     *
     * @param speed la vitesse de déplacement à définir
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * Retourne la vitesse verticale de l'entité.
     *
     * @return la vitesse verticale de l'entité
     */
    public double getVerticalVelocity() {
        return verticalVelocity;
    }

    /**
     * Définit la vitesse verticale de l'entité.
     *
     * @param verticalVelocity la vitesse verticale à définir
     */
    public void setVerticalVelocity(double verticalVelocity) {
        this.verticalVelocity = verticalVelocity;
    }

    /**
     * Indique si l'entité est au sol ou non.
     *
     * @return true si l'entité est au sol, sinon false
     */
    public Boolean getOnGround() {
        return this.isOnGround;
    }

    /**
     * Définit si l'entité est au sol ou non.
     *
     * @param onGround l'état à définir
     */
    public void setOnGround(Boolean onGround) {
        this.isOnGround = onGround;
    }

    /**
     * Vérifie si l'entité est vivante.
     *
     * @return true si l'entité est vivante, sinon false
     */
    public boolean isAlive() {
        return this.health > 0;
    }

    /**
     * Méthode abstraite pour déplacer l'entité.
     */
    public abstract void move();

    public abstract void animeSprite();

    /**
     * Vérifie si l'entité entre en collision avec un autre rectangle.
     *
     * @param other le rectangle avec lequel vérifier la collision
     * @return true si il y a une collision, sinon false
     */
    public boolean collidesWith(Rectangle other) {
        return sprite.getBoundsInParent().intersects(other.getBoundsInParent());
    }

    /**
     * Met à jour la position du sprite avec la position actuelle de l'entité.
     */
    public void updateSprite() {
        this.sprite.setX(this.getPosition().getX());
        this.sprite.setY(this.getPosition().getY());
    }

    /**
     * Met à jour la position de l'entité avec de nouvelles coordonnées.
     *
     * @param newX la nouvelle coordonnée x
     * @param newY la nouvelle coordonnée y
     */
    protected void updatePosition(double newX, double newY) {
        this.getPosition().setX(newX);
        this.getPosition().setY(newY);
    }

    /**
     * Enregistre la pression d'une touche du clavier.
     *
     * @param keyCode le code de la touche pressée
     */
    public void keyPressed(KeyCode keyCode) {
        keyState.put(keyCode, true);
    }

    /**
     * Enregistre le relâchement d'une touche du clavier.
     *
     * @param keyCode le code de la touche relâchée
     */
    public void keyReleased(KeyCode keyCode) {
        keyState.put(keyCode, false);
    }

    /**
     * Initialise l'état des touches du clavier à false.
     */
    private void initializeKeyState() {
        for (KeyCode keyCode : KeyCode.values()) {
            this.keyState.put(keyCode, false);
        }
    }
}
