package com.bobble.spacebobble.network.Packet;

import java.io.Serial;
import java.io.Serializable;

/**
 * Classe contenant les données d'un survivant pour l'envoi vers les serveur
 */
public class SurvivorPacket implements Serializable {
    @Serial
    private static final long serialVersionUID = -9197790410424500934L;
    private final int ID;
    private final boolean direction;
    private double x;
    private double y;
    private boolean isSaved;

    public SurvivorPacket(int ID, double x, double y, boolean direction) {
        this.ID = ID;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.isSaved = false;
    }

    public boolean isDirection() {
        return direction;
    }

    public int getID() {
        return ID;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    @Override
    public String toString() {
        return ("ID: " + ID + " Sauvé ? " + isSaved());
    }
}
