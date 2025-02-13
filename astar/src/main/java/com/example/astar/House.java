package com.example.astar;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;

/**
 * A simple House shape that replaces the circle
 * The house is drawn as a rectangle (the body) with a triangular roof
 */
public class House extends Group {
    private final Rectangle body;
    private final Polygon roof;

    public House(double centerX, double centerY, double size, Color color) {
        double width = size * 2;
        double height = size * 2;

        body = new Rectangle(centerX - width / 2, centerY - height / 2, width, height);
        body.setFill(color);

        roof = new Polygon(
                centerX - width / 2, centerY - height / 2,
                centerX + width / 2, centerY - height / 2,
                centerX, centerY - height / 2 - size);
        roof.setFill(color.darker());

        getChildren().addAll(body, roof);
    }

    /**
     * 
     * @param color color of the house allows us to easily change the house color
     */
    public void setFill(Color color) {
        body.setFill(color);
        roof.setFill(color.darker());
    }
}
