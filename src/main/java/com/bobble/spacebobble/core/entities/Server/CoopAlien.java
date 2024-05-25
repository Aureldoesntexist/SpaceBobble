package com.bobble.spacebobble.core.entities.Server;


import com.bobble.spacebobble.config.GameConstants;
import com.bobble.spacebobble.core.entities.MovingEntity;
import com.bobble.spacebobble.core.utilities.Position;
import com.bobble.spacebobble.core.world.Block;
import com.bobble.spacebobble.gestion.ResourceManager;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Classe d'un alien pour le mode Coop.
 */
public class CoopAlien extends MovingEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -3866945824726353863L;
    private boolean direction;

    public CoopAlien(int health, Position position, List<Block> walls, boolean direction) {
        super(health, position, walls);
        this.sprite = new Rectangle(35, 35, Color.WHITESMOKE);
        this.sprite.setX(this.getPosition().getX());
        this.sprite.setY(this.getPosition().getY());
        this.speed = 1;
        this.direction = direction;
    }


    @Override
    public void move() {
        Rectangle sprite = this.getSprite();
        double newX = this.getPosition().getX();
        double newY = this.getPosition().getY();
        this.setVerticalVelocity(this.getVerticalVelocity() + GameConstants.GRAVITY);
        newY += this.getVerticalVelocity();

        if (direction) {
            newX -= this.getSpeed();
        } else {
            newX += this.getSpeed();
        }

        for (Block wall : walls) {
            if (wall.collidesWith(new Rectangle(newX, sprite.getY(), sprite.getWidth(), sprite.getHeight()))) {
                if ((this.isMovingLeft() && wall.getY() == 0)) {
                    flipDirection();
                    newX = this.getPosition().getX();
                } else if (this.isMovingRight() && wall.getY() < 1000) {
                    flipDirection();
                    newX = this.getPosition().getX();
                }
            }
        }

        for (Block wall : walls) {
            if (wall.collidesWith(new Rectangle(sprite.getX(), newY, sprite.getWidth(), sprite.getHeight()))) {
                if (this.getVerticalVelocity() > 0) {
                    newY = (wall.getY() - sprite.getHeight());
                    this.setOnGround(true);
                } else {
                    newY = wall.getY() + wall.getTile().getHeight();
                }
                this.setVerticalVelocity(0);
            }
        }

        for (Block wall : walls) {
            if (wall.collidesWith(new Rectangle(newX, sprite.getY(), sprite.getWidth(), sprite.getHeight()))) {
                newX = this.getPosition().getX();
            }

        }
        this.getPosition().setX(newX);
        this.getPosition().setY(newY);
    }

    private boolean isMovingLeft() {
        return this.getSpeed() < 0;
    }

    private boolean isMovingRight() {
        return this.getSpeed() > 0;
    }

    private void flipDirection() {
        direction = !direction;
    }

    @Override
    public void animeSprite() {
        ImagePattern tile;
        if (direction) {
            String leftImagePath = "/asset/sprites/leftA.png";

            tile = ResourceManager.loadImage(leftImagePath);
            this.getSprite().setFill(tile);

        } else {
            String rightImagePath = "/asset/sprites/rightA.png";
            tile = ResourceManager.loadImage(rightImagePath);
            this.getSprite().setFill(tile);
        }

    }
}
