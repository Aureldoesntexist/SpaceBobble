package com.bobble.spacebobble.controller.service;

import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.core.GameClient;
import com.bobble.spacebobble.core.entities.Bobble;
import com.bobble.spacebobble.core.world.Block;
import com.bobble.spacebobble.core.world.Trapdoor;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Classe Service pour la gestion des entrées utilisateur dans le client de jeu.
 */
public class ClientInput extends Service<Void> {
    private final GameClient gameClient;
    private final Scene scene;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean isSPressed = false;
    private boolean isZPressed = false;
    private long lastPressTime = 0;

    /**
     * Constructeur.
     *
     * @param gameClient client du jeu
     * @param scene      scène du jeu
     */
    public ClientInput(GameClient gameClient, Scene scene) {
        this.gameClient = gameClient;
        this.scene = scene;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                scene.setOnKeyPressed(e -> {
                    gameClient.getBobble().keyPressed(e.getCode());
                    trapdoorEvents(e.getCode());
                });
                scene.setOnKeyReleased(e -> {
                    keyReleaseEvents(e.getCode());
                    gameClient.getBobble().keyReleased(e.getCode());
                });
                return null;
            }
        };
    }

    /**
     * Gère les événements liés aux trapdoors.
     *
     * @param code code de la touche pressée
     */
    private void trapdoorEvents(KeyCode code) {
        if (code == KeyCode.S && !isSPressed && System.currentTimeMillis() - lastPressTime >= GameConstants.DELAY_BETWEEN) {
            lastPressTime = System.currentTimeMillis();
            isSPressed = true;
            for (Block wall : gameClient.getWalls()) {
                if (wall instanceof Trapdoor trapdoor && isTrapdoorBelowBobble(trapdoor)) {
                    trapdoor.open();
                    scheduler.schedule(trapdoor::close, 500, TimeUnit.MILLISECONDS);
                }
            }
        }

        if (code == KeyCode.Z && !isZPressed && System.currentTimeMillis() - lastPressTime >= GameConstants.DELAY_BETWEEN) {
            lastPressTime = System.currentTimeMillis();
            isZPressed = true;
            for (Block wall : gameClient.getWalls()) {
                if (wall instanceof Trapdoor trapdoor && isTrapdoorAboveBobble(trapdoor)) {
                    trapdoor.open();
                    scheduler.schedule(trapdoor::close, 500, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    /**
     * Gère les événements de relâchement de touche.
     *
     * @param code code de la touche relâchée
     */
    private void keyReleaseEvents(KeyCode code) {
        if (code == KeyCode.S) {
            isSPressed = false;
        }
        if (code == KeyCode.Z) {
            isZPressed = false;
        }
    }

    /**
     * Vérifie si la trapdoor est en dessous du Bobble.
     *
     * @param trapdoor trapdoor à vérifier
     * @return true si la trapdoor est en dessous du Bobble, sinon false
     */
    private boolean isTrapdoorBelowBobble(Trapdoor trapdoor) {
        Bobble bobble = gameClient.getBobble();
        return bobble.getPosition().getY() < trapdoor.getY()
                && bobble.getPosition().getX() < trapdoor.getX() + trapdoor.getTile().getWidth()
                && bobble.getPosition().getX() + bobble.getSprite().getWidth() > trapdoor.getX();
    }

    /**
     * Vérifie si la trapdoor est au-dessus du Bobble.
     *
     * @param trapdoor trapdoor à vérifier
     * @return true si la trapdoor est au-dessus du Bobble, sinon false
     */
    private boolean isTrapdoorAboveBobble(Trapdoor trapdoor) {
        Bobble bobble = gameClient.getBobble();
        return bobble.getPosition().getY() > trapdoor.getY()
                && bobble.getPosition().getX() < trapdoor.getX() + trapdoor.getTile().getWidth()
                && bobble.getPosition().getX() + bobble.getSprite().getWidth() > trapdoor.getX();
    }
}
