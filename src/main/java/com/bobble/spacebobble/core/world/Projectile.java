package com.bobble.spacebobble.core.world;

import com.bobble.spacebobble.gestion.VisualManager;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Classe représentant un projectile.
 */
public class Projectile {
    private final double speed;
    private final double direction;
    private final Rectangle sprite;
    private final GaussianBlur neonEffect = new GaussianBlur();
    private double x;
    private double y;

    /**
     * Constructeur du projectile.
     *
     * @param x         Position initiale en abscisse
     * @param y         Position initiale en ordonnée
     * @param speed     Vitesse du projectile
     * @param direction Direction du projectile en radians
     */
    public Projectile(double x, double y, double speed, double direction) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
        Color randomColor = VisualManager.getRandomColor();
        this.sprite = new Rectangle(x, y, 10, 7);
        this.sprite.setFill(randomColor);
    }

    /**
     * Met à jour la position du projectile en fonction de sa vitesse et de sa direction.
     */
    public void update() {
        x += speed * Math.cos(direction);
        y += speed * Math.sin(direction);
        sprite.setX(x);
        sprite.setY(y);
    }

    /**
     * Retourne le sprite du projectile.
     *
     * @return Sprite du projectile
     */
    public Rectangle getSprite() {
        return sprite;
    }
}
