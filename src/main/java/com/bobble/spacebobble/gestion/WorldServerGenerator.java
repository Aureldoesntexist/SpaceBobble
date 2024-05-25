package com.bobble.spacebobble.gestion;

import com.bobble.spacebobble.core.world.Block;
import com.bobble.spacebobble.core.world.BlockPacket;
import com.bobble.spacebobble.core.world.Solid;
import com.bobble.spacebobble.core.world.Trapdoor;
import javafx.scene.Group;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * La classe ServerLevelManager est utilisée pour gérer les niveaux du jeu côté serveur.
 */
public class WorldServerGenerator implements Serializable {
    @Serial
    private static final long serialVersionUID = 7512961299550821455L;

    /**
     * La scène principale du jeu
     */
    private final Pane gamePane;

    /**
     * Le groupe contenant les objets du jeu
     */
    private final Group gameObjects;

    /**
     * La liste des murs du niveau
     */
    private final List<Block> walls = new ArrayList<>();

    /**
     * Le socket utilisé pour communiquer avec le client
     */
    private final Socket socket;

    /**
     * Constructeur de la classe ServerLevelManager.
     *
     * @param gamePane La scène principale du jeu.
     * @param socket   Le socket utilisé pour communiquer avec le client.
     */
    public WorldServerGenerator(Pane gamePane, Socket socket) {
        this.gamePane = gamePane;
        this.socket = socket;
        this.gameObjects = new Group();
        this.gamePane.getChildren().add(gameObjects);
    }

    /**
     * Renvoie la liste des murs du niveau.
     *
     * @return La liste des murs du niveau.
     */
    public List<Block> getWalls() {
        return walls;
    }

    /**
     * Charge le niveau à partir du serveur.
     */
    public void loadLevel() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            List<BlockPacket> blocksData = (List<BlockPacket>) in.readObject();
            createBlocksFromData(blocksData);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Crée les blocs du niveau à partir des données reçues du serveur.
     *
     * @param blocksData Les données des blocs du niveau.
     */
    private void createBlocksFromData(List<BlockPacket> blocksData) {
        for (BlockPacket data : blocksData) {
            Block block;
            switch (data.type()) {
                case "Solid":
                    block = new Solid(data.x(), data.y());
                    break;
                case "Trapdoor":
                    block = new Trapdoor(data.x(), data.y());
                    break;
                default:
                    continue;
            }
            walls.add(block);
            gameObjects.getChildren().add(block.getTile());
        }
    }
}