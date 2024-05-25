package com.bobble.spacebobble.controller;


import com.bobble.spacebobble.Main;
import com.bobble.spacebobble.ui.Coop;
import com.bobble.spacebobble.ui.Leaderboard;
import com.bobble.spacebobble.ui.Solo;
import com.bobble.spacebobble.ui.VS;
import javafx.event.ActionEvent;

import java.io.IOException;

public class MenuController {


    public void solo(ActionEvent event) throws IOException {
        Solo solo = new Solo(Main.getInstance().getStage());
        solo.show();

    }

    public void coop(ActionEvent event) throws IOException {
        Coop coop = new Coop(Main.getInstance().getStage());
        coop.show();

    }

    public void vs(ActionEvent event) throws IOException {
        VS versus = new VS(Main.getInstance().getStage());
        versus.show();

    }

    public void leaderboard(ActionEvent event) throws IOException {
        Leaderboard solo = new Leaderboard(Main.getInstance().getStage());
        solo.show();
    }

}

