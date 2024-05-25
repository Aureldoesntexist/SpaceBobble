package com.bobble.spacebobble.core.world;

import com.bobble.spacebobble.config.GameConstants;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.io.Serial;
import java.io.Serializable;

/**
 * La classe Block est la classe de base pour tous les blocs du monde du jeu.
 */
public abstract class Block implements Serializable {
    @Serial
    private static final long serialVersionUID = -4260167455575606492L;

    protected Rectangle tile;

    /**
     * Constructeur de la classe Block.
     *
     * @param x La position x du bloc.
     * @param y La position y du bloc.
     */
    public Block(double x, double y) {
        this.tile = new Rectangle(x * GameConstants.SIZE_CASE, y * GameConstants.SIZE_CASE,
                GameConstants.SIZE_CASE, GameConstants.SIZE_CASE);
    }

    /**
     * Renvoie le rectangle représentant le bloc.
     *
     * @return Le rectangle représentant le bloc.
     */
    public Rectangle getTile() {
        return tile;
    }

    /**
     * Renvoie la taille du bloc.
     *
     * @return La taille du bloc.
     */
    public int getSize() {
        return GameConstants.SIZE_CASE;
    }

    /**
     * Vérifie si le bloc entre en collision avec un autre objet.
     *
     * @param other L'objet avec lequel le bloc peut entrer en collision.
     * @return true si le bloc entre en collision avec l'objet, false sinon.
     */
    public boolean collidesWith(Shape other) {
        return tile.getBoundsInParent().intersects(other.getBoundsInParent());
    }

    /**
     * Renvoie la position y du bloc.
     *
     * @return La position y du bloc.
     */
    public double getY() {
        return (int) this.tile.getY() - 0.1;
    }

    /**
     * Renvoie la position x du bloc.
     *
     * @return La position x du bloc.
     */
    public double getX() {
        return (int) this.tile.getX() - 0.1;
    }

}
