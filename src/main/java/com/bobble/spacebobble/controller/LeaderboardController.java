package com.bobble.spacebobble.controller;

import com.bobble.spacebobble.gestion.VisualManager;
import javafx.event.ActionEvent;

public class LeaderboardController {

    /**
     * Affiche le leaderboard en mode solo
     */
    public void solo(ActionEvent event) {
        VisualManager.initLeaderboardPage("Leaderboard Mode Solo", "leaderboard.ser");
    }

    /**
     * Affiche le leaderboard en mode VS
     */
    public void vs(ActionEvent event) {
        VisualManager.initLeaderboardPage("Leaderboard Mode VS", "leaderboardVS.ser");
    }

    /**
     * Affiche le leaderboard en mode coop
     */
    public void coop(ActionEvent event) {
        VisualManager.initLeaderboardPage("Leaderboard Mode Coop", "leaderboardCoopLocal.ser");
    }
}