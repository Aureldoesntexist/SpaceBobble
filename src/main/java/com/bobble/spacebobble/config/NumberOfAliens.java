package com.bobble.spacebobble.config;

/**
 * Classe Enum pour le nombre d'aliens en fonction du niveau.
 */
public enum NumberOfAliens {
    EASY(5),
    MEDIUM(10),
    HARD(15);
    private final int value;

    NumberOfAliens(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


}
