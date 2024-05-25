package com.bobble.spacebobble.config;

/**
 * Classe Enum pour le système de niveau.
 */
public enum Levels {
    EASY,
    MEDIUM,
    HARD;

    public Levels getNextLevel() {
        if (this == EASY) {
            return MEDIUM;
        } else if (this == MEDIUM) {
            return HARD;
        } else {
            return null;
        }
    }

}
