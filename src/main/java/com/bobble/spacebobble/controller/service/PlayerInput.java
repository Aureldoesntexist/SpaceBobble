package com.bobble.spacebobble.controller.service;

import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.config.Mode;
import com.bobble.spacebobble.core.GameWorld;
import com.bobble.spacebobble.core.entities.Alien;
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
 * Classe service pour la gestion des entrées utilisateur des joueurs pour le mode Solo et VS
 */
public class PlayerInput extends Service<Void> {
    private final GameWorld gameWorld;
    private final Scene scene;
    private final Mode mode;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private boolean isSPressed = false;
    private boolean isZPressed = false;
    private boolean isDownPressed = false;
    private boolean isUPPressed = false;
    private boolean is0Pressed = false;
    private long lastPressTime = 0;

    /**
     * Constructeur du service
     *
     * @param gameWorld monde du jeu
     * @param scene     scène du jeu
     * @param mode      mode du jeu
     */
    public PlayerInput(GameWorld gameWorld, Scene scene, Mode mode) {
        this.gameWorld = gameWorld;
        this.scene = scene;
        this.mode = mode;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                scene.setOnKeyPressed(e -> {
                    if (mode == Mode.WITHOUT_CAMERA) {
                        for (Alien alien : gameWorld.getAliens()) {
                            if (alien.isControlled()) {
                                alien.keyPressed(e.getCode());
                            }
                        }
                        changeControl(e.getCode());
                    }
                    gameWorld.getBobble().keyPressed(e.getCode());
                    direction(e.getCode(), gameWorld);
                    trapdoorEvents(e.getCode());

                });
                scene.setOnKeyReleased(e -> {
                    keyReleaseEvents(e.getCode());
                    if (mode == Mode.WITHOUT_CAMERA) {
                        for (Alien alien : gameWorld.getAliens()) {
                            if (alien.isControlled()) {
                                alien.keyReleased(e.getCode());
                            }
                        }
                    }
                    gameWorld.getBobble().keyReleased(e.getCode());
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
            for (Block wall : gameWorld.getWalls()) {
                if (wall instanceof Trapdoor trapdoor && isTrapdoorBelowBobble(trapdoor)) {
                    trapdoor.open();
                    scheduler.schedule(trapdoor::close, 500, TimeUnit.MILLISECONDS);
                }
            }
        }

        if (code == KeyCode.Z && !isZPressed && System.currentTimeMillis() - lastPressTime >= GameConstants.DELAY_BETWEEN) {
            lastPressTime = System.currentTimeMillis();
            isZPressed = true;
            for (Block wall : gameWorld.getWalls()) {
                if (wall instanceof Trapdoor trapdoor && isTrapdoorAboveBobble(trapdoor)) {
                    trapdoor.open();
                    scheduler.schedule(trapdoor::close, 500, TimeUnit.MILLISECONDS);
                }
            }
        }

        if (code == KeyCode.DOWN && !isDownPressed && System.currentTimeMillis() - lastPressTime >= GameConstants.DELAY_BETWEEN) {
            lastPressTime = System.currentTimeMillis();
            isDownPressed = true;
            for (Block wall : gameWorld.getWalls()) {
                if (wall instanceof Trapdoor && isTrapdoorBelow((Trapdoor) wall)) {
                    ((Trapdoor) wall).open();
                    scheduler.schedule(((Trapdoor) wall)::close, 500, TimeUnit.MILLISECONDS);
                }
            }
        }

        if (code == KeyCode.UP && !isUPPressed && System.currentTimeMillis() - lastPressTime >= GameConstants.DELAY_BETWEEN) {
            lastPressTime = System.currentTimeMillis();
            isUPPressed = true;
            for (Block wall : gameWorld.getWalls()) {
                if (wall instanceof Trapdoor && isTrapdoorAbove((Trapdoor) wall)) {
                    ((Trapdoor) wall).open();
                    scheduler.schedule(((Trapdoor) wall)::close, 500, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    /**
     * Change le contrôle de l'alien.
     *
     * @param code code de la touche pressée
     */
    private void changeControl(KeyCode code) {
        if (code == KeyCode.NUMPAD0 && !is0Pressed && System.currentTimeMillis() - lastPressTime >= GameConstants.DELAY_BETWEEN) {
            lastPressTime = System.currentTimeMillis();
            isUPPressed = true;
            Alien alienControlled = gameWorld.getAliens().stream().filter(Alien::isControlled).findFirst().orElse(null);
            assert alienControlled != null;
            alienControlled.setControlled(false);
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

        if (code == KeyCode.DOWN) {
            isDownPressed = false;
        }

        if (code == KeyCode.UP) {
            isUPPressed = false;
        }
        if (code == KeyCode.NUMPAD0) {
            is0Pressed = false;
        }
    }

    /**
     * Gère la direction du Bobble.
     *
     * @param key       code de la touche pressée
     * @param gameWorld monde du jeu
     */
    private void direction(KeyCode key, GameWorld gameWorld) {
        switch (key) {
            case Q:
                gameWorld.direction(false);
                break;
            case D:
                gameWorld.direction(true);
                break;
            default:
                break;
        }
    }

    /**
     * Vérifie si la trapdoor est en dessous du Bobble.
     *
     * @param trapdoor trapdoor à vérifier
     * @return true si la trapdoor est en dessous du Bobble, sinon false
     */
    private boolean isTrapdoorBelowBobble(Trapdoor trapdoor) {
        Bobble bobble = gameWorld.getBobble();
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
        Bobble bobble = gameWorld.getBobble();
        return bobble.getPosition().getY() > trapdoor.getY()
                && bobble.getPosition().getX() < trapdoor.getX() + trapdoor.getTile().getWidth()
                && bobble.getPosition().getX() + bobble.getSprite().getWidth() > trapdoor.getX();
    }

    /**
     * Vérifie si la trapdoor est en dessous de l'alien contrôlé.
     *
     * @param trapdoor trapdoor à vérifier
     * @return true si la trapdoor est en dessous de l'alien contrôlé, sinon false
     */
    private boolean isTrapdoorBelow(Trapdoor trapdoor) {
        Alien alienControlled = gameWorld.getAliens().stream().filter(Alien::isControlled).findFirst().orElse(null);
        return alienControlled != null && alienControlled.getPosition().getY() < trapdoor.getY()
                && alienControlled.getPosition().getX() < trapdoor.getX() + trapdoor.getTile().getWidth()
                && alienControlled.getPosition().getX() + alienControlled.getSprite().getWidth() > trapdoor.getX();
    }

    /**
     * Vérifie si la trapdoor est au-dessus de l'alien contrôlé.
     *
     * @param trapdoor trapdoor à vérifier
     * @return true si la trapdoor est au-dessus de l'alien contrôlé, sinon false
     */
    private boolean isTrapdoorAbove(Trapdoor trapdoor) {
        Alien alienControlled = gameWorld.getAliens().stream().filter(Alien::isControlled).findFirst().orElse(null);
        return alienControlled != null && alienControlled.getPosition().getY() > trapdoor.getY()
                && alienControlled.getPosition().getX() < trapdoor.getX() + trapdoor.getTile().getWidth()
                && alienControlled.getPosition().getX() + alienControlled.getSprite().getWidth() > trapdoor.getX();
    }
}
