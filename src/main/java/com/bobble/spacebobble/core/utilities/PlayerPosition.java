package com.bobble.spacebobble.core.utilities;

import java.io.Serial;
import java.io.Serializable;

/**
 * Classe représentant une position dans un espace bidimensionnel en prenant en compte l'ID d'un joueur
 */
public class PlayerPosition implements Serializable {
    @Serial
    private static final long serialVersionUID = 6529685098267757690L;
    private final int playerID;
    private final double x;
    private final double y;

    public PlayerPosition(int playerID, double x, double y) {
        this.playerID = playerID;
        this.x = x;
        this.y = y;
    }

    public int getPlayerID() {
        return playerID;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Player " + playerID + " Position: (" + x + ", " + y + ")";
    }
}
