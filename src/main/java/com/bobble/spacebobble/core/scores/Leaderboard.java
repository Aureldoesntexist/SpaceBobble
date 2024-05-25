package com.bobble.spacebobble.core.scores;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Leaderboard implements Serializable {
    @Serial
    private static final long serialVersionUID = -6262903752573345302L;
    private TreeMap<String, Integer> scoreTreemap;

    public Leaderboard() {
        scoreTreemap = new TreeMap<>();
    }

    public TreeMap<String, Integer> getScoreTreemap() {
        return scoreTreemap;
    }

    public void setScoreTreemap(TreeMap<String, Integer> scoreTreemap) {
        this.scoreTreemap = scoreTreemap;
    }

    public void addScore(String playerName, int score) {
        scoreTreemap.put(playerName, score);
    }

    public void displayScore() {
        System.out.println("Leaderboard:");
        System.out.println("Player\tScore");
        for (Map.Entry<String, Integer> scorePlayer : scoreTreemap.entrySet()) {
            System.out.println(scorePlayer.getKey() + "\t" + scorePlayer.getValue());
        }
    }
}