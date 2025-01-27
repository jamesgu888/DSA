package com.example.astar;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;

public class RouteVisualizerWithHouses extends Application {
    private final List<ImageView> selectedNodes = new ArrayList<>();
    private final List<Line> drawnLines = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        Pane pane = new Pane();

        // 40 node coordinates (5 rows × 8 columns)
        double[][] locations = {
                // Row 0 (nodes 0..7)
                { 100, 50 }, { 200, 50 }, { 300, 50 }, { 400, 50 },
                { 500, 50 }, { 600, 50 }, { 700, 50 }, { 800, 50 },
                // Row 1 (nodes 8..15)
                { 100, 150 }, { 200, 150 }, { 300, 150 }, { 400, 150 },
                { 500, 150 }, { 600, 150 }, { 700, 150 }, { 800, 150 },
                // Row 2 (nodes 16..23)
                { 100, 250 }, { 200, 250 }, { 300, 250 }, { 400, 250 },
                { 500, 250 }, { 600, 250 }, { 700, 250 }, { 800, 250 },
                // Row 3 (nodes 24..31)
                { 100, 350 }, { 200, 350 }, { 300, 350 }, { 400, 350 },
                { 500, 350 }, { 600, 350 }, { 700, 350 }, { 800, 350 },
                // Row 4 (nodes 32..39)
                { 100, 450 }, { 200, 450 }, { 300, 450 }, { 400, 450 },
                { 500, 450 }, { 600, 450 }, { 700, 450 }, { 800, 450 }
        };

        // Edges {source, destination, weight} — includes horizontal, vertical, and some
        // diagonals.
        int[][] edges = {
                { 0, 1, 4 }, { 1, 2, 2 }, { 2, 3, 5 }, { 3, 4, 1 }, { 4, 5, 3 }, { 5, 6, 4 }, { 6, 7, 2 },
                { 8, 9, 3 }, { 9, 10, 5 }, { 10, 11, 1 }, { 11, 12, 4 }, { 12, 13, 3 }, { 13, 14, 2 }, { 14, 15, 5 },
                { 16, 17, 2 }, { 17, 18, 4 }, { 18, 19, 5 }, { 19, 20, 1 }, { 20, 21, 3 }, { 21, 22, 5 }, { 22, 23, 4 },
                { 24, 25, 2 }, { 25, 26, 5 }, { 26, 27, 2 }, { 27, 28, 4 }, { 28, 29, 1 }, { 29, 30, 3 }, { 30, 31, 5 },
                { 32, 33, 1 }, { 33, 34, 3 }, { 34, 35, 2 }, { 35, 36, 5 }, { 36, 37, 4 }, { 37, 38, 1 }, { 38, 39, 2 },
                { 0, 8, 3 }, { 1, 9, 5 }, { 2, 10, 2 }, { 3, 11, 4 }, { 4, 12, 1 }, { 5, 13, 3 }, { 6, 14, 2 },
                { 7, 15, 5 },
                { 8, 16, 4 }, { 9, 17, 1 }, { 10, 18, 3 }, { 11, 19, 5 }, { 12, 20, 2 }, { 13, 21, 4 }, { 14, 22, 5 },
                { 15, 23, 1 }
        };

        // Draw houses as nodes
        for (int i = 0; i < locations.length; i++) {
            double x = locations[i][0];
            double y = locations[i][1];

            // Load house icon
            Image houseIcon = new Image("https://cdn-icons-png.flaticon.com/512/25/25694.png", 24, 24, true, true);
            ImageView house = new ImageView(houseIcon);
            house.setX(x - 12);
            house.setY(y - 12);

            Text label = new Text(x - 25, y - 15, "Node " + i);
            final int nodeIndex = i;

            house.setOnMouseClicked(e -> handleNodeClick(house, nodeIndex, pane));

            pane.getChildren().addAll(house, label);
        }

        // Draw edges (roads)
        for (int[] edge : edges) {
            int source = edge[0];
            int destination = edge[1];
            int weight = edge[2];

            double x1 = locations[source][0];
            double y1 = locations[source][1];
            double x2 = locations[destination][0];
            double y2 = locations[destination][1];

            Line road = new Line(x1, y1, x2, y2);
            road.setStroke(Color.LIGHTGRAY);
            road.setStrokeWidth(2);

            Text weightLabel = new Text((x1 + x2) / 2, (y1 + y2) / 2, String.valueOf(weight));
            weightLabel.setFill(Color.DARKRED);

            pane.getChildren().addAll(road, weightLabel);
        }

        // Set up stage
        Scene scene = new Scene(pane, 900, 600);
        primaryStage.setTitle("Node-House Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleNodeClick(ImageView house, int nodeIndex, Pane pane) {
        if (!selectedNodes.contains(house)) {
            selectedNodes.add(house);
            house.setOpacity(0.5); // Highlight selected node
        } else {
            selectedNodes.remove(house);
            house.setOpacity(1.0); // Deselect node
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
