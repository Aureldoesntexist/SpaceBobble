package com.bobble.spacebobble.core.world;

import com.bobble.spacebobble.gestion.ResourceManager;

import java.io.Serial;
import java.io.Serializable;

/**
 * Classe représentant un mur / sol simple.
 */
public class Solid extends Block implements Serializable {
    @Serial
    private static final long serialVersionUID = -3153451098700555260L;

    public Solid(double x, double y) {
        super(x, y);
        this.getTile().setFill(ResourceManager.loadImage("/asset/tile.png"));
    }
}
