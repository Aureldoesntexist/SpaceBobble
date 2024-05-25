package com.bobble.spacebobble.gestion;

import com.bobble.spacebobble.core.world.Block;
import com.bobble.spacebobble.core.world.BlockPacket;
import com.bobble.spacebobble.core.world.Solid;
import com.bobble.spacebobble.core.world.Trapdoor;
import com.bobble.spacebobble.network.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère la génération des niveaux du monde de jeu.
 */
public class WorldGenerator {

    private ArrayList<String> data;


    /**
     * Construit un nouveau WorldGenerator.
     */
    public WorldGenerator() {
        this.data = new ArrayList<>();
    }

    /**
     * Lit un fichier et stocke son contenu dans la liste data.
     *
     * @param fileName le nom du fichier à lire
     * @throws RuntimeException si une erreur survient lors de la lecture du fichier
     */
    public void readFile(String fileName) {
        data = new ArrayList<>();
        try {
            InputStream file = ResourceManager.class.getResourceAsStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                data.add(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Erreur : " + fileName, e);
        }
    }

    /**
     * Crée un niveau en fonction de la difficulté spécifiée.
     *
     * @param walls      la liste pour ajouter les blocs générés
     * @param difficulty le niveau de difficulté du niveau à générer
     */
    public void createLevel(List<Block> walls, int difficulty) {
        switch (difficulty) {
            case 0:
                readFile("/levels/lvl1.txt");
                break;
            case 1:
                readFile("/levels/lvl2.txt");
                break;
            case 2:
                readFile("/levels/lvl3.txt");
                break;
        }
        for (int line = 0; line < data.size(); line++) {
            for (int c = 0; c < data.get(line).length(); c++) {
                if (data.get(line).charAt(c) == '1') {
                    walls.add(new Solid(c, line));
                } else if (data.get(line).charAt(c) == '2') {
                    walls.add(new Trapdoor(c, line));
                }
            }
        }
    }

    /**
     * Crée un niveau pour le jeu en ligne en fonction du fichier spécifié.
     *
     * @param blockData la liste pour ajouter les données de bloc générées
     * @param fileName  le nom du fichier à lire
     * @throws RuntimeException si une erreur survient lors de la lecture du fichier
     */
    public void createLevelOnline(List<BlockPacket> blockData, String fileName) {
        try {
            InputStream file = Server.class.getResourceAsStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            int line = 0;
            String dataString;
            while ((dataString = reader.readLine()) != null) {
                for (int c = 0; c < dataString.length(); c++) {
                    char blockType = dataString.charAt(c);
                    switch (blockType) {
                        case '1':
                            blockData.add(new BlockPacket(c, line, "Solid"));
                            break;
                        case '2':
                            blockData.add(new BlockPacket(c, line, "Trapdoor"));
                            break;
                    }
                }
                line++;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Erreur : " + fileName, e);
        }
    }
}
