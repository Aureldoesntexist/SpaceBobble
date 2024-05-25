package com.bobble.spacebobble.gestion;

import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.core.scores.Leaderboard;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.ImagePattern;

import java.io.*;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Gère les ressources du jeu, telles que les scores, les images et la musique.
 */
public class ResourceManager {

    /**
     * Sérialise le classement dans un fichier.
     *
     * @param treeMap  le classement à sérialiser
     * @param fileName le nom du fichier où sérialiser le classement
     */
    public static void serializeLeaderboard(TreeMap<String, Integer> treeMap, String fileName) {
        String jarPath = System.getProperty("java.class.path");
        File jarFile = new File(jarPath);
        String parentDir = jarFile.getParent();
        File file = new File(parentDir, fileName);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(treeMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Désérialise le classement depuis un fichier.
     *
     * @param fileName le nom du fichier depuis lequel désérialiser le classement
     * @return le classement désérialisé
     */
    public static TreeMap<String, Integer> deserializeLeaderboard(String fileName) {
        TreeMap<String, Integer> treeMap = null;
        String jarPath = System.getProperty("java.class.path");
        File jarFile = new File(jarPath);
        String parentDir = jarFile.getParent();
        File file = new File(parentDir, fileName);
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            treeMap = (TreeMap<String, Integer>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Le fichier n'existe pas.");
        }
        return Objects.requireNonNullElseGet(treeMap, TreeMap::new);

    }

    /**
     * Initialise le classement en chargeant le classement depuis un fichier ou en créant un nouveau.
     *
     * @param filename le nom du fichier à utiliser pour le classement
     * @return le classement initialisé
     */
    public static Leaderboard initLeaderboard(String filename) {
        TreeMap<String, Integer> result = ResourceManager.deserializeLeaderboard(filename);
        Leaderboard leaderboard = new Leaderboard();
        if (result != null) {
            leaderboard.setScoreTreemap(result);
        } else {
            ResourceManager.serializeLeaderboard(leaderboard.getScoreTreemap(), filename);
        }
        return leaderboard;
    }

    /**
     * Définit l'arrière-plan d'un composant avec une image.
     *
     * @param composant le composant dont l'arrière-plan doit être défini
     * @param URLImage  l'URL de l'image à utiliser pour l'arrière-plan
     */
    public static void setComposantBackground(Pane composant, String URLImage) {
        Image imageBG = new Image(String.valueOf(ResourceManager.class.getResource(URLImage)));
        BackgroundSize backgroundSize = new BackgroundSize(GameConstants.SCENE_WIDTH, GameConstants.SCENE_HEIGHT, true, true, true, true);
        BackgroundImage backgroundImage = new BackgroundImage(imageBG, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
        Background background = new Background(backgroundImage);
        composant.setBackground(background);
    }

    /**
     * Charge une image depuis un fichier et retourne son motif.
     *
     * @param filename le nom du fichier de l'image à charger
     * @return le motif de l'image chargée
     */
    public static ImagePattern loadImage(String filename) {
        Image image = new Image(String.valueOf(ResourceManager.class.getResource(filename)));
        return new ImagePattern(image, 0, 0, 1, 1, true);
    }

    /**
     * Charge un fichier musical et retourne le lecteur de média correspondant.
     *
     * @param filename le nom du fichier musical à charger
     * @return le lecteur de média pour le fichier musical chargé
     */
    public static MediaPlayer loadMusic(String filename) {
        Media sound = new Media(String.valueOf(ResourceManager.class.getResource(filename)));
        return new MediaPlayer(sound);
    }
}
