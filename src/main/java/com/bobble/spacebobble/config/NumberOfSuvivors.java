package com.bobble.spacebobble.config;

/**
 * Classe Enum pour le nombre d'aliens en fonction du niveau.
 */
public enum NumberOfSuvivors {
    EASY(4),
    MEDIUM(6),
    HARD(8);
    private final int value;

    NumberOfSuvivors(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


}
