package com.bobble.spacebobble.network.Packet;


import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Record contenant les données de l'état du jeu pour l'envoi vers le serveur.
 */
public record GamePacket(BobblePacket bobblePacket, List<SurvivorPacket> savedSurvivors) implements Serializable {
    @Serial
    private static final long serialVersionUID = 136647494438870742L;
}
