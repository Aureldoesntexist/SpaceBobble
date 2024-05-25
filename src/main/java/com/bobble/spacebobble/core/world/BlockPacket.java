package com.bobble.spacebobble.core.world;

import java.io.Serial;
import java.io.Serializable;

/**
 * Record contenant les données d'un block pour l'envoi vers le serveur.
 */
public record BlockPacket(double x, double y, String type) implements Serializable {
    @Serial
    private static final long serialVersionUID = 8877823308182676061L;
}
