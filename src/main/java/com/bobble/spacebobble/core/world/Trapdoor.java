package com.bobble.spacebobble.core.world;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.Serial;
import java.io.Serializable;

/**
 * La classe Trapdoor représente une trappe dans le monde du jeu.
 */
public class Trapdoor extends Block implements Serializable {
    @Serial
    private static final long serialVersionUID = -6485821687189158156L;

    // Indique si la trappe est ouverte ou non
    private boolean isOpened = false;

    /**
     * Constructeur de la classe Trapdoor.
     *
     * @param x La position x de la trappe.
     * @param y La position y de la trappe.
     */
    public Trapdoor(double x, double y) {
        super(x, y);
        this.tile = new Rectangle(x * this.getSize(), y * this.getSize(), this.getSize() * 4, this.getSize());
        this.getTile().setFill(Color.web("#4C6885"));
        this.getTile().setOpacity(0.5);
    }

    /**
     * Ouvre la trappe.
     */
    public synchronized void open() {
        while (isOpened) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        isOpened = true;
        updateAppearance();

    }

    /**
     * Ferme la trappe.
     */
    public synchronized void close() {
        isOpened = false;
        notifyAll();
        updateAppearance();
    }

    /**
     * Met à jour l'apparence de la trappe en fonction de son état.
     */
    private void updateAppearance() {
        this.getTile().setVisible(!isOpened);
        this.getTile().setFill(isOpened ? Color.TRANSPARENT : Color.web("#4C6885"));
        this.getTile().setOpacity(isOpened ? 0 : 0.7);
    }

    /**
     * Renvoie l'état de la trappe.
     *
     * @return true si la trappe est ouverte, false sinon.
     */
    public boolean isOpened() {
        return isOpened;
    }

}
