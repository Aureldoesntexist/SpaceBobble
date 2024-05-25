package com.bobble.spacebobble.core;


import com.bobble.spacebobble.Main;
import com.bobble.spacebobble.config.*;
import com.bobble.spacebobble.core.entities.Alien;
import com.bobble.spacebobble.core.entities.Bobble;
import com.bobble.spacebobble.core.entities.Survivor;
import com.bobble.spacebobble.core.scores.Score;
import com.bobble.spacebobble.core.utilities.CameraPlayer;
import com.bobble.spacebobble.core.utilities.Position;
import com.bobble.spacebobble.core.world.Block;
import com.bobble.spacebobble.core.world.Projectile;
import com.bobble.spacebobble.gestion.ResourceManager;
import com.bobble.spacebobble.gestion.WorldGenerator;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * La classe principale GameWorld qui gère la logique de jeu, les entités, les sprites et les collisions.
 * Cette classe contient des méthodes pour générer un niveau, mettre à jour l'état de
 * jeu, gérer les déplacements et les collisions, et afficher les entités.
 */
public class GameWorld {
    // Structure du niveau
    private final WorldGenerator level = new WorldGenerator();
    // Listes des entités statiques et mobiles
    private final CopyOnWriteArrayList<Alien> aliens = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Survivor> survivors = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Projectile> projectiles = new CopyOnWriteArrayList<>();
    private final List<Block> walls = new ArrayList<>();
    // Elements visuels globaux
    private final Group gameObjects = new Group();
    private final CameraPlayer camera = new CameraPlayer(0, 0);
    private final Mode mode;
    // Etats de la partie
    private final Score score = new Score();
    protected int numberOfLives = 2;
    private Bobble bobble;
    private Pane pane;
    private boolean gameFinished = false;
    private boolean gameOver = false;
    // Paramètres liés à la logique interne
    private long lastProjectile = 0;
    private Alien lastControlledAlien = null;
    // Paramètres de la partie
    private Levels levels = Levels.EASY;
    private int maxAliens = NumberOfAliens.EASY.getValue();
    private int maxSurvivors = NumberOfSuvivors.EASY.getValue();
    private long respawnTime = 0;
    private boolean direction = false;

    public GameWorld(Mode mode) {
        this.mode = mode;
        generateLevel();
        this.pane = new Pane();

    }

    /*
     * Passe au niveau suivant s'il y a encore des survivants et si le niveau suivant existe.
     * Si le niveau suivant n'existe pas, met la partie à "terminé".
     * Nettoie le niveau actuel et recharge un nouveau niveau avec les paramètres correspondants.
     */
    private void nextLevel() {
        if (survivors.isEmpty()) {
            Levels nextLevels = levels.getNextLevel();
            if (nextLevels != null) {
                setLevels(nextLevels);
                notifyChange();
                if (levels == Levels.MEDIUM) {
                    maxAliens = NumberOfAliens.MEDIUM.getValue();
                    maxSurvivors = NumberOfSuvivors.MEDIUM.getValue();
                    Main.getInstance().getStage().getScene().setRoot(pane);
                    ResourceManager.setComposantBackground(pane, "/asset/background/lvl2.png");
                } else if (levels == Levels.HARD) {
                    maxAliens = NumberOfAliens.HARD.getValue();
                    maxSurvivors = NumberOfSuvivors.HARD.getValue();
                    ResourceManager.setComposantBackground(pane, "/asset/background/lvl3.png");
                }
                clearLevel();
                generateLevel();
            } else {
                gameFinished = true;
            }

        }
    }

    /*
     * Génère un niveau avec les paramètres correspondants et ajoute les murs et les entités à
     * l'affichage.
     */
    private void generateLevel() {
        level.createLevel(walls, levels.ordinal());
        walls.forEach(w -> gameObjects.getChildren().add(w.getTile()));
        spawnSurvivors();
        bobble = new Bobble(5, new Position(150, 150), walls);
    }

    /*
     * Génère les aliens aléatoirement dans le niveau
     */
    private void spawnAliens() {
        Random rand = new Random();
        Alien spawn;
        while (aliens.size() <= maxAliens) {
            int line = rand.nextInt(6) + 1;
            int col = rand.nextInt(2);
            int x = (col == 0) ? 50 : 980;
            int y = 100 + (line - 1) * 100;
            spawn = new Alien(5, new Position(x, y), walls);
            aliens.add(spawn);
        }
    }

    /*
     * Génère les survivants aléatoirement dans le niveau
     */
    private void spawnSurvivors() {
        Random rand = new Random();
        Survivor spawn;
        while (survivors.size() <= maxSurvivors) {
            int line = rand.nextInt(6) + 1;
            int col = rand.nextInt(2);
            int x = (col == 0) ? 50 : 980;
            int y = 100 + (line - 1) * 100;
            spawn = new Survivor(1, new Position(x, y), walls);
            survivors.add(spawn);
        }
    }

    /*
     * Met le bobble en mode invincible, le repositionne et rétablit sa santé.
     */
    void respawn() {
        bobble.setHealth(3);
        bobble.setPosition(300, 50);
        bobble.setInvincible(true);
        bobble.getSprite().setOpacity(0.5);
        respawnTime = System.currentTimeMillis();
    }

    /*
     * Vérifie si le bobble est invincible et si le temps de réapparition est écoulé.
     * Si oui, met le bobble en mode normal et rétablit l'opacité de son sprite.
     */
    void checkInvulnerability() {
        if (bobble.isInvincible() && (System.currentTimeMillis() - respawnTime) > GameConstants.DELAY_RESPAWN) {
            bobble.setInvincible(false);
            bobble.getSprite().setOpacity(1);
        }
    }

    /*
     * Vérifie si le bobble a perdu une vie et si oui, enlève une vie et repositionne le bobble.
     * Si le bobble a perdu toutes ses vies, met la partie à terminer.
     */
    private void checkLives() {
        if (!bobble.isAlive() && numberOfLives > 0) {
            numberOfLives--;
            notifyChange();
            bobble.setInvincible(true);
            respawn();
        } else if (!bobble.isAlive() && numberOfLives == 0) {
            gameOver = true;
        }
    }

    /*
     * Met à jour les sprites des entités en fonction de leur état.
     * Ajoute ou supprime les sprites des entités en fonction de leur état.
     */
    private void updateSprites() {
        if (bobble.isAlive()) {
            bobble.updateSprite();
            if (!gameObjects.getChildren().contains(bobble.getSprite())) {
                gameObjects.getChildren().add(bobble.getSprite());
            }
        } else {
            gameObjects.getChildren().remove(bobble.getSprite());
        }

        for (Alien alien : aliens) {
            if (alien.isAlive()) {
                alien.updateSprite();
                if (alien.isControlled()) {
                    alien.getSprite().setFill(Color.RED);
                } else {
                    alien.getSprite().setFill(Color.WHITE);
                }
                if (!gameObjects.getChildren().contains(alien.getSprite())) {
                    gameObjects.getChildren().add(alien.getSprite());
                }
            } else {
                synchronized (score) {
                    score.setTotal(score.getTotal() + 1000);
                    notifyChange();
                }
                aliens.remove(alien);
                gameObjects.getChildren().remove(alien.getSprite());
            }
        }
        for (Survivor survivor : survivors) {
            if (survivor.isAlive()) {
                survivor.updateSprite();
                if (!gameObjects.getChildren().contains(survivor.getSprite())) {
                    gameObjects.getChildren().add(survivor.getSprite());
                }
            } else {
                survivors.remove(survivor);
                gameObjects.getChildren().remove(survivor.getSprite());
            }
        }
    }

    /*
     * Met en mode contrôlé un alien au hasard si aucun alien n'est déjà contrôlé.
     */
    private void randomControl() {
        boolean allFalse = true;
        for (Alien alien : aliens) {
            if (alien.isControlled()) {
                allFalse = false;
                break;
            }
        }
        if (allFalse) {
            int randomIndex;
            do {
                randomIndex = (int) (Math.random() * aliens.size());
            } while (aliens.get(randomIndex) == lastControlledAlien);
            Alien newControlledAlien = aliens.get(randomIndex);
            if (lastControlledAlien != null) {
                lastControlledAlien.setSpeed(1);
            }
            newControlledAlien.setControlled(true);
            newControlledAlien.setSpeed(4);
            lastControlledAlien = newControlledAlien;
        }
    }

    /*
     * Lance un projectile si l'espace est pressé et si le temps de recharge est écoulé.
     */
    private void throwProjectiles() {
        if (bobble.getKeyState().get(KeyCode.SPACE)) {
            double direction;
            double position;
            long now = System.nanoTime();
            if (isDirection()) {
                direction = 0;
                position = bobble.getSprite().getWidth();
            } else {
                direction = Math.PI;
                position = -bobble.getSprite().getWidth() + 5;
            }
            if (now - lastProjectile >= GameConstants.PROJECTILES_COOLDOWN) {
                Projectile newProjectile = new Projectile(
                        bobble.getPosition().getX() + position,
                        bobble.getPosition().getY() + bobble.getSprite().getHeight() / 2,
                        7.5,
                        direction);
                projectiles.add(newProjectile);
                gameObjects.getChildren().add(newProjectile.getSprite());
                lastProjectile = now;
            }
        }
    }

    /*
     * Met à jour les projectiles et les supprime s'ils sortent de l'écran ou s'ils rentrent en collision avec une entité.
     */
    private void updateProjectiles() {
        ArrayList<Projectile> garbageProjectiles = new ArrayList<>();
        for (Projectile projectile : projectiles) {
            projectile.update();
            for (Block wall : walls) {
                if (wall.collidesWith(projectile.getSprite())) {
                    garbageProjectiles.add(projectile);
                    gameObjects.getChildren().remove(projectile.getSprite());
                }
            }
            for (Alien monster : aliens) {
                if (monster.collidesWith(projectile.getSprite())) {
                    monster.setHealth(monster.getHealth() - 1);
                    garbageProjectiles.add(projectile);
                    gameObjects.getChildren().remove(projectile.getSprite());
                }
            }
            if (bobble.collidesWith(projectile.getSprite())) {
                garbageProjectiles.add(projectile);
                gameObjects.getChildren().remove(projectile.getSprite());
            }


        }
        projectiles.removeAll(garbageProjectiles);
        projectiles.forEach(projectile -> {
            if (!gameObjects.getChildren().contains(projectile.getSprite())) {
                gameObjects.getChildren().add(projectile.getSprite());
            }
        });
    }

    /*
     * Met à jour les survivants et les supprime s'ils rentrent en collision avec le bobble.
     */
    private void saveSurvivors() {
        for (Survivor survivor : survivors) {
            if (survivor.collidesWith(bobble.getSprite())) {
                survivor.setHealth(0);
                synchronized (score) {
                    score.setTotal(score.getTotal() + 1000);
                    notifyChange();
                }
            }
        }
    }

    /*
     * Vérifie les collisions entre le bobble et les aliens, et l'invincibilité du bobble.
     */
    private void alienCollision() {
        for (Alien alien : aliens) {
            if (bobble.collidesWith(alien.getSprite()) && alien.isAlive() && !bobble.isInvincible()) {
                bobble.setHealth(bobble.getHealth() - 1);
                synchronized (score) {
                    score.setTotal(score.getTotal() - 500);
                    notifyChange();
                }

            }
        }
    }

    /*
     * Met à jour les déplacements des entités.
     */
    private void moveEntities() {
        bobble.move();
        if (mode == Mode.CAMERA) {
            camera.playerCam(bobble.getPosition().getX(), bobble.getPosition().getY(), pane.getWidth(), pane.getHeight());
            camera.apply(gameObjects);
        }
        for (Alien alien : aliens) {
            if (!alien.isControlled()) {
                alien.move();
            } else {
                alien.moveByPlayer();
            }
        }
        survivors.forEach(Survivor::move);
    }


    public void update() {
        spawnAliens();
        moveEntities();
        saveSurvivors();
        alienCollision();
        if (mode == Mode.WITHOUT_CAMERA) {
            randomControl();
        }
        throwProjectiles();
        checkLives();
        checkInvulnerability();
    }

    public void render() {
        updateSprites();
        nextLevel();
        bobble.animeSprite();
        aliens.forEach(Alien::animeSprite);
        survivors.forEach(Survivor::animeSprite);
        updateProjectiles();
    }


    /*
     * Nettoie le niveau actuel et recharge un nouveau niveau avec les paramètres correspondants.
     */
    private void clearLevel() {
        aliens.forEach(m -> gameObjects.getChildren().remove(m.getSprite()));
        walls.forEach(w -> gameObjects.getChildren().remove(w.getTile()));
        gameObjects.getChildren().remove(bobble.getSprite());
        survivors.clear();
        aliens.clear();
        walls.clear();
        gameFinished = false;
        gameOver = false;
    }

    /*
     * Retourne la liste des aliens.
     */
    public List<Alien> getAliens() {
        return aliens;
    }


    /*
     * Retourne la liste des murs.
     */
    public List<Block> getWalls() {
        return walls;
    }

    /*
     * Retourne la scène avec les éléments visuels.
     */
    public Pane getScene() {
        pane = new Pane(gameObjects);
        return pane;
    }

    /*
     * Retourne le score.
     */
    public Score getScore() {
        return score;
    }

    /*
     * Retourne si la partie est terminée.
     */
    public boolean isGameFinished() {
        return gameFinished;
    }

    /*
     * Retourne si la partie est terminée.
     */
    public boolean isGameOver() {
        return gameOver;
    }


    /*
     * Retourne l'orientation actuelle.
     */
    public boolean isDirection() {
        return direction;
    }

    /*
     * Met l'orientation actuelle.
     */
    public void direction(boolean orientation) {
        this.direction = orientation;
    }

    /*
     * Retourne l'objet bobble.
     */
    public Bobble getBobble() {
        return bobble;
    }

    /*
     * Retourne le mode de jeu.
     */
    public Mode getMode() {
        return mode;
    }

    /*
     * Méthode pour mettre en attente le thread de l'afficheur des informations
     */
    public synchronized void waitChange() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Méthode pour synchroniser les threads.
     */
    public synchronized void notifyChange() {
        notifyAll();
    }

    /*
     * Retourne le niveau actuel.
     */
    public Levels getLevels() {
        return levels;
    }

    /*
     * Met le niveau actuel.
     */
    public void setLevels(Levels levels) {
        this.levels = levels;
    }

    /*
     * Retourne le nombre de vies restantes.
     */
    public int getNumberOfLives() {
        return numberOfLives;
    }

}

