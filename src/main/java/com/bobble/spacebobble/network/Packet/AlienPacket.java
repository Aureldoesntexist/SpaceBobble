package com.bobble.spacebobble.network.Packet;

import java.io.Serial;
import java.io.Serializable;

/**
 * Classe contenant les données d'un alien pour l'envoi vers le serveur
 */
public class AlienPacket implements Serializable {
    @Serial
    private static final long serialVersionUID = 4518285035916045585L;
    private final int id;
    private final boolean direction;
    private double x;
    private double y;

    public AlienPacket(int id, double x, double y, boolean direction) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public boolean isDirection() {
        return direction;
    }

    public int getId() {
        return id;
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

}

