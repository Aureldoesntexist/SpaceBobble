package com.bobble.spacebobble.controller.service;

import com.bobble.spacebobble.gestion.ResourceManager;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Classe service pour la lecture de la bande sonore du jeu.
 */
public class SoundtrackPlayer extends Service<MediaPlayer> {
    private final String filename;

    /**
     * Constructeur du lecteur de bande sonore.
     *
     * @param filename chemin du fichier audio
     */
    public SoundtrackPlayer(String filename) {
        this.filename = filename;
    }

    @Override
    protected Task<MediaPlayer> createTask() {
        return new Task<>() {
            @Override
            protected MediaPlayer call() throws Exception {
                MediaPlayer mediaPlayer = ResourceManager.loadMusic(filename);
                mediaPlayer.setOnEndOfMedia(() -> {
                    mediaPlayer.seek(Duration.ZERO);
                });
                mediaPlayer.setOnError(() -> {
                    System.out.println("Une erreur média est survenue : " + mediaPlayer.getError());
                });

                return mediaPlayer;
            }
        };
    }
}
