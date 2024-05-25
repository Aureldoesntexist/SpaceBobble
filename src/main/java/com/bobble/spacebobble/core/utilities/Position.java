package com.bobble.spacebobble.core.utilities;

import java.io.Serial;
import java.io.Serializable;

/**
 * Classe représentant une position dans un espace bidimensionnel.
 */
public class Position implements Serializable {

    @Serial
    private static final long serialVersionUID = 4981392933595849145L;

    private double x;

    private double y;

    /**
     * Constructeur de la classe Position.
     *
     * @param x la coordonnée x de la position
     * @param y la coordonnée y de la position
     */
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Retourne la coordonnée x de la position.
     *
     * @return la coordonnée x
     */
    public double getX() {
        return x;
    }

    /**
     * Définit la coordonnée x de la position.
     *
     * @param x la nouvelle coordonnée x
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Retourne la coordonnée y de la position.
     *
     * @return la coordonnée y
     */
    public double getY() {
        return y;
    }

    /**
     * Définit la coordonnée y de la position.
     *
     * @param y la nouvelle coordonnée y
     */
    public void setY(double y) {
        this.y = y;
    }

    public String toString() {
        return "(" + getX() + " , " + getY() + ")";
    }
}
