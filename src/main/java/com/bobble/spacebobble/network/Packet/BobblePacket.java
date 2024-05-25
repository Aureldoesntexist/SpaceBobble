package com.bobble.spacebobble.network.Packet;

import com.bobble.spacebobble.core.utilities.PlayerPosition;

import java.io.Serial;
import java.io.Serializable;

/**
 * Classe contenant les données d'un autre joueur pour l'envoi vers les serveur
 */
public class BobblePacket extends PlayerPosition implements Serializable {
    @Serial
    private static final long serialVersionUID = 1234567890123456789L;
    private final int lives;

    public BobblePacket(int playerID, double x, double y, int lives) {
        super(playerID, x, y);
        this.lives = lives;

    }

    public int getLives() {
        return lives;
    }

    @Override
    public String toString() {
        return super.toString() + " | Lives: " + lives + " | ";
    }
}

