//*******************************************************************
//  James Gu, Shamak Gowda, Ryan Li
//  Capstone: Data Structures and Algorithms
//  2/7/2025
//
//  Careful Carpool is an intelligent route optimization and visualization tool designed to efficiently plan carpool routes while minimizing
//  travel time and distance. The system employs advanced data structures and algorithms, including Dijkstra's shortest path algorithm, to calculate optimized
//  routes based on user-defined start and end locations.
//*******************************************************************

package com.example.astar;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.CubicCurve;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.*;

public class CarefulCarpool extends Application {
    // List for selected houses by the user
    private final List<House> selectedHouses = new ArrayList<>();
    // List for all houses.
    private final List<House> houses = new ArrayList<>();
    // Hard-coded positions for the houses
    private double[][] locations;

    // List to keep track of drawn path lines and arrowheads
    private final List<Line> drawnLines = new ArrayList<>();

    private Integer startNode = null;
    private Integer endNode = null;
    private TextField startField;
    private TextField endField;
    private Graph graph;

    private Pane pane;

    /**
     * Creates the external application for the CarefulCarpool
     * 
     * @param primaryStage main external application window
     */
    @Override
    public void start(Stage primaryStage) {
        pane = new Pane();

        // Set up text fields and buttons
        startField = new TextField();
        startField.setPromptText("Start Node");
        startField.setLayoutX(50);
        startField.setLayoutY(750);
        endField = new TextField();
        endField.setPromptText("End Node");
        endField.setLayoutX(200);
        endField.setLayoutY(750);

        Button setStartEndButton = new Button("Set Start & End");
        setStartEndButton.setLayoutX(350);
        setStartEndButton.setLayoutY(750);
        setStartEndButton.setOnAction(e -> setStartEndNodes());

        pane.getChildren().addAll(startField, endField, setStartEndButton);

        // ******************************************************************
        // Define locations for 40 houses (with irregular positioning)
        // ******************************************************************
        locations = new double[][] {
                // Row 0 (Nodes 0 - 7)
                { 150, 75 }, { 310, 90 }, { 460, 70 }, { 630, 85 }, { 780, 80 }, { 920, 100 }, { 1070, 60 },
                { 1220, 80 },
                // Row 1 (Nodes 8 - 15)
                { 140, 235 }, { 290, 215 }, { 460, 230 }, { 610, 240 }, { 750, 220 }, { 890, 235 }, { 1040, 225 },
                { 1190, 245 },
                // Row 2 (Nodes 16 - 23)
                { 160, 385 }, { 310, 370 }, { 470, 395 }, { 620, 380 }, { 760, 390 }, { 910, 365 }, { 1070, 400 },
                { 1220, 380 },
                // Row 3 (Nodes 24 - 31)
                { 140, 535 }, { 300, 515 }, { 450, 540 }, { 600, 530 }, { 750, 520 }, { 900, 550 }, { 1050, 510 },
                { 1200, 545 },
                // Row 4 (Nodes 32 - 39)
                { 160, 675 }, { 310, 680 }, { 460, 670 }, { 620, 690 }, { 770, 660 }, { 920, 680 }, { 1070, 670 },
                { 1220, 690 }
        };

        // **********************************************************************************
        // Define edges: Each edge is defined as {source, destination, weight}.
        // **********************************************************************************
        int[][] edgeData = {
                // Horizontal edges
                { 0, 1, 4 }, { 1, 2, 2 }, { 2, 3, 5 }, { 3, 4, 1 }, { 4, 5, 3 }, { 5, 6, 4 }, { 6, 7, 2 },
                { 8, 9, 3 }, { 9, 10, 5 }, { 10, 11, 1 }, { 11, 12, 4 }, { 12, 13, 3 }, { 13, 14, 2 }, { 14, 15, 5 },
                { 16, 17, 2 }, { 17, 18, 4 }, { 18, 19, 5 }, { 19, 20, 1 }, { 20, 21, 3 }, { 21, 22, 5 }, { 22, 23, 4 },
                { 24, 25, 2 }, { 25, 26, 5 }, { 26, 27, 2 }, { 27, 28, 4 }, { 28, 29, 1 }, { 29, 30, 3 }, { 30, 31, 5 },
                { 32, 33, 1 }, { 33, 34, 3 }, { 34, 35, 2 }, { 35, 36, 5 }, { 36, 37, 4 }, { 37, 38, 1 }, { 38, 39, 2 },
                // Vertical edges
                { 0, 8, 3 }, { 1, 9, 5 }, { 2, 10, 2 }, { 3, 11, 4 }, { 4, 12, 1 }, { 5, 13, 3 }, { 6, 14, 2 },
                { 7, 15, 5 },
                { 8, 16, 4 }, { 9, 17, 1 }, { 10, 18, 3 }, { 11, 19, 5 }, { 12, 20, 2 }, { 13, 21, 4 }, { 14, 22, 5 },
                { 15, 23, 1 },
                { 16, 24, 3 }, { 17, 25, 2 }, { 18, 26, 5 }, { 19, 27, 1 }, { 20, 28, 4 }, { 21, 29, 2 }, { 22, 30, 5 },
                { 23, 31, 3 },
                { 24, 32, 1 }, { 25, 33, 4 }, { 26, 34, 2 }, { 27, 35, 5 }, { 28, 36, 3 }, { 29, 37, 1 }, { 30, 38, 4 },
                { 31, 39, 2 },
                // Diagonal edges
                { 0, 9, 3 }, { 1, 10, 2 }, { 2, 11, 4 }, { 8, 17, 3 }, { 9, 18, 5 }, { 10, 19, 2 },
                { 16, 25, 3 }, { 17, 26, 4 }, { 18, 27, 2 }, { 24, 33, 5 }, { 25, 34, 1 }, { 26, 35, 4 },
                { 27, 36, 3 }, { 28, 37, 2 }, { 29, 38, 5 }, { 30, 39, 1 }
        };

        // Build the graph using our Graph and Edge classes.
        graph = new Graph();
        for (int[] ed : edgeData) {
            int src = ed[0], dest = ed[1], weight = ed[2];
            graph.addEdge(new Edge(src, dest, weight));
        }

        // ******************************************************************
        // Draw roads (edges) with a randomized connection style.
        // ******************************************************************
        Random rand = new Random();
        for (int[] ed : edgeData) {
            int source = ed[0];
            int dest = ed[1];
            int weight = ed[2];

            double x1 = locations[source][0];
            double y1 = locations[source][1];
            double x2 = locations[dest][0];
            double y2 = locations[dest][1];

            double connectionType = rand.nextDouble();
            if (connectionType < 0.33) {
                // Draw a straight road.
                Line road = new Line(x1, y1, x2, y2);
                road.setStroke(Color.DARKGRAY);
                road.setStrokeWidth(8);
                pane.getChildren().add(road);

                // Draw the yellow dashed center line.
                Line centerLine = new Line(x1, y1, x2, y2);
                centerLine.setStroke(Color.YELLOW);
                centerLine.setStrokeWidth(2);
                centerLine.getStrokeDashArray().addAll(10d, 10d);
                pane.getChildren().add(centerLine);

                // Add the weight label.
                Text weightLabel = new Text((x1 + x2) / 2, (y1 + y2) / 2, String.valueOf(weight));
                weightLabel.setFill(Color.DARKRED);
                pane.getChildren().add(weightLabel);
            } else if (connectionType < 0.66) {
                // Draw a quadratic curve road.
                QuadCurve quadCurve = new QuadCurve();
                quadCurve.setStartX(x1);
                quadCurve.setStartY(y1);
                quadCurve.setEndX(x2);
                quadCurve.setEndY(y2);
                double midX = (x1 + x2) / 2;
                double midY = (y1 + y2) / 2;
                double dx = x2 - x1;
                double dy = y2 - y1;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0)
                    len = 1;

                double px = -dy / len;
                double py = dx / len;
                double offset = rand.nextDouble() * 100 - 50;
                double ctrlX = midX + px * offset;
                double ctrlY = midY + py * offset;
                quadCurve.setControlX(ctrlX);
                quadCurve.setControlY(ctrlY);
                quadCurve.setStroke(Color.DARKGRAY);
                quadCurve.setStrokeWidth(8);
                quadCurve.setFill(null);
                pane.getChildren().add(quadCurve);

                // Draw the yellow dashed center line along the quadratic curve.
                QuadCurve centerQuad = new QuadCurve();
                centerQuad.setStartX(x1);
                centerQuad.setStartY(y1);
                centerQuad.setEndX(x2);
                centerQuad.setEndY(y2);
                centerQuad.setControlX(ctrlX);
                centerQuad.setControlY(ctrlY);
                centerQuad.setStroke(Color.YELLOW);
                centerQuad.setStrokeWidth(2);
                centerQuad.getStrokeDashArray().addAll(10d, 10d);
                centerQuad.setFill(null);
                pane.getChildren().add(centerQuad);

                double labelX = 0.25 * x1 + 0.5 * ctrlX + 0.25 * x2;
                double labelY = 0.25 * y1 + 0.5 * ctrlY + 0.25 * y2;
                Text weightLabel = new Text(labelX, labelY, String.valueOf(weight));
                weightLabel.setFill(Color.DARKRED);
                pane.getChildren().add(weightLabel);
            } else {
                // Draw a cubic curve road.
                CubicCurve cubicCurve = new CubicCurve();
                cubicCurve.setStartX(x1);
                cubicCurve.setStartY(y1);
                cubicCurve.setEndX(x2);
                cubicCurve.setEndY(y2);
                double dx = x2 - x1;
                double dy = y2 - y1;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0)
                    len = 1;
                double px = -dy / len;
                double py = dx / len;

                double cp1X = x1 + dx / 3;
                double cp1Y = y1 + dy / 3;
                double offset1 = rand.nextDouble() * 100 - 50;
                cp1X += px * offset1;
                cp1Y += py * offset1;

                double cp2X = x1 + 2 * dx / 3;
                double cp2Y = y1 + 2 * dy / 3;
                double offset2 = rand.nextDouble() * 100 - 50;
                cp2X += px * offset2;
                cp2Y += py * offset2;
                cubicCurve.setControlX1(cp1X);
                cubicCurve.setControlY1(cp1Y);
                cubicCurve.setControlX2(cp2X);
                cubicCurve.setControlY2(cp2Y);
                cubicCurve.setStroke(Color.DARKGRAY);
                cubicCurve.setStrokeWidth(8);
                cubicCurve.setFill(null);
                pane.getChildren().add(cubicCurve);

                CubicCurve centerCubic = new CubicCurve();
                centerCubic.setStartX(x1);
                centerCubic.setStartY(y1);
                centerCubic.setEndX(x2);
                centerCubic.setEndY(y2);
                centerCubic.setControlX1(cp1X);
                centerCubic.setControlY1(cp1Y);
                centerCubic.setControlX2(cp2X);
                centerCubic.setControlY2(cp2Y);
                centerCubic.setStroke(Color.YELLOW);
                centerCubic.setStrokeWidth(2);
                centerCubic.getStrokeDashArray().addAll(10d, 10d);
                centerCubic.setFill(null);
                pane.getChildren().add(centerCubic);

                double labelX = 0.125 * x1 + 0.375 * cp1X + 0.375 * cp2X + 0.125 * x2;
                double labelY = 0.125 * y1 + 0.375 * cp1Y + 0.375 * cp2Y + 0.125 * y2;
                Text weightLabel = new Text(labelX, labelY, String.valueOf(weight));
                weightLabel.setFill(Color.DARKRED);
                pane.getChildren().add(weightLabel);
            }
        }

        // ******************************************************************
        // Draw houses (nodes) using the new House class.
        // ******************************************************************
        houses.clear();
        for (int i = 0; i < locations.length; i++) {
            double x = locations[i][0];
            double y = locations[i][1];

            House house = new House(x, y, 12, Color.LIGHTBLUE);
            Text label = new Text(x - 25, y - 25, "House " + i);
            label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            label.toFront();

            final int nodeIndex = i;
            house.setOnMouseClicked(e -> handleNodeClick(house, nodeIndex));

            houses.add(house);
            pane.getChildren().addAll(house, label);
        }

        for (House house : houses) {
            house.toFront();
        }

        Button resetButton = new Button("Reset");
        resetButton.setLayoutX(500);
        resetButton.setLayoutY(750);
        resetButton.setOnAction(e -> resetSelection());
        pane.getChildren().add(resetButton);

        Scene scene = new Scene(pane, 1300, 800);
        primaryStage.setTitle("Dynamic Carpool Route Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Allow the user to define the start and end of the path
     */
    private void setStartEndNodes() {
        try {
            if (startNode != null)
                houses.get(startNode).setFill(Color.LIGHTBLUE);
            if (endNode != null)
                houses.get(endNode).setFill(Color.LIGHTBLUE);

            int newStart = Integer.parseInt(startField.getText().trim());
            int newEnd = Integer.parseInt(endField.getText().trim());

            if (newStart < 0 || newStart >= houses.size() || newEnd < 0 || newEnd >= houses.size()) {
                showAlert("Invalid Input", "Please enter valid node numbers within the range of available houses.");
                return;
            }

            if (newStart == newEnd) {
                showAlert("Selection Error", "Start and end nodes cannot be the same. Please choose different nodes.");
                return;
            }

            startNode = newStart;
            endNode = newEnd;

            houses.get(startNode).setFill(Color.BLACK);
            houses.get(endNode).setFill(Color.BLACK);

            calculateAndHighlightPath();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter positive integer values for the node numbers.");
        }
    }

    /**
     * Detects if the user clicks on a certain house
     * 
     * @param house     the house that was clicked on
     * @param nodeIndex index of the house that was clicked on
     */
    private void handleNodeClick(House house, int nodeIndex) {
        if (startNode == null || endNode == null) {
            showAlert("Selection Error", "Please set both start and end nodes before selecting intermediate nodes.");
            return;
        }
        if (nodeIndex == startNode || nodeIndex == endNode)
            return;
        if (!selectedHouses.contains(house)) {
            selectedHouses.add(house);
            house.setFill(Color.GREEN);
            calculateAndHighlightPath();
        }
    }

    /**
     * Finds the optimal path between all the selected houses and calls to display
     * it
     */
    private void calculateAndHighlightPath() {
        if (startNode == null || endNode == null)
            return;

        List<Integer> selectedNodeIndices = new ArrayList<>();
        selectedNodeIndices.add(startNode);
        for (House h : selectedHouses) {
            int idx = houses.indexOf(h);
            if (idx != startNode && idx != endNode)
                selectedNodeIndices.add(idx);
        }
        selectedNodeIndices.add(endNode);

        List<Integer> path = findOptimizedPath(selectedNodeIndices);
        clearPreviousPath();
        highlightPath(path, selectedNodeIndices);
    }

    /**
     * Displays the optimal path
     * 
     * @param path          sequence of nodes representing the shortest path between
     *                      the selected houses
     * @param selectedNodes all the selected houses
     */
    private void highlightPath(List<Integer> path, List<Integer> selectedNodes) {
        Set<String> drawnPairs = new HashSet<>();
        double offsetScale = 5.0;

        for (int i = 0; i < path.size() - 1; i++) {
            int src = path.get(i);
            int dest = path.get(i + 1);

            double x1 = locations[src][0], y1 = locations[src][1];
            double x2 = locations[dest][0], y2 = locations[dest][1];

            double dx = x2 - x1, dy = y2 - y1;
            double length = Math.sqrt(dx * dx + dy * dy);
            double offsetX = -(dy / length) * offsetScale;
            double offsetY = (dx / length) * offsetScale;

            String pairKey = src + "-" + dest;
            String reverseKey = dest + "-" + src;

            if (drawnPairs.contains(reverseKey)) {
                Line line = new Line(x2 - offsetX, y2 - offsetY, x1 - offsetX, y1 - offsetY);
                line.setStroke(Color.BLUE);
                line.setStrokeWidth(3);
                pane.getChildren().add(line);
                drawnLines.add(line);
                if (selectedNodes.contains(src))
                    addArrowhead(x2 - offsetX, y2 - offsetY, x1 - offsetX, y1 - offsetY, true);
            } else {
                Line line = new Line(x1 - offsetX, y1 - offsetY, x2 - offsetX, y2 - offsetY);
                line.setStroke(Color.RED);
                line.setStrokeWidth(3);
                pane.getChildren().add(line);
                drawnLines.add(line);
                if (selectedNodes.contains(dest))
                    addArrowhead(x1 - offsetX, y1 - offsetY, x2 - offsetX, y2 - offsetY, false);
            }
            drawnPairs.add(pairKey);
        }
    }

    /**
     * Draw an arrowhead to signify direction
     * 
     * @param x1       start x coordinate
     * @param y1       start y coordinate
     * @param x2       end x coordinate
     * @param y2       end y coordinate
     * @param isReturn if it is a return trip
     */
    private void addArrowhead(double x1, double y1, double x2, double y2, boolean isReturn) {
        double arrowLength = 15.0;
        double angle = Math.atan2(y2 - y1, x2 - x1);
        if (isReturn) {
            angle += Math.PI;
        }

        double tipX = isReturn ? x1 : x2;
        double tipY = isReturn ? y1 : y2;

        Line arrow1 = new Line(
                tipX,
                tipY,
                tipX - arrowLength * Math.cos(angle - Math.PI / 6),
                tipY - arrowLength * Math.sin(angle - Math.PI / 6));
        Line arrow2 = new Line(
                tipX,
                tipY,
                tipX - arrowLength * Math.cos(angle + Math.PI / 6),
                tipY - arrowLength * Math.sin(angle + Math.PI / 6));

        arrow1.setStrokeWidth(3);
        arrow2.setStrokeWidth(3);
        arrow1.setStroke(isReturn ? Color.BLUE : Color.RED);
        arrow2.setStroke(isReturn ? Color.BLUE : Color.RED);

        pane.getChildren().addAll(arrow1, arrow2);
        drawnLines.add(arrow1);
        drawnLines.add(arrow2);
    }

    /**
     * Resets the start/end and selected houses
     */
    private void resetSelection() {
        for (House house : selectedHouses) {
            house.setFill(Color.LIGHTBLUE);
        }
        selectedHouses.clear();

        if (startNode != null) {
            houses.get(startNode).setFill(Color.LIGHTBLUE);
            startNode = null;
        }
        if (endNode != null) {
            houses.get(endNode).setFill(Color.LIGHTBLUE);
            endNode = null;
        }

        startField.clear();
        endField.clear();
        clearPreviousPath();
    }

    /**
     * Finds the optimized path and saves the node sequence to path
     * 
     * @param graph map of all the nodes and edges
     * @param nodes all house indexes
     * @return optimized path
     */
    private List<Integer> findOptimizedPath(List<Integer> nodes) {
        List<Integer> path = new ArrayList<>();
        int current = nodes.get(0);
        path.add(current);

        for (int i = 1; i < nodes.size(); i++) {
            List<Integer> segment = dijkstra(current, nodes.get(i));
            if (segment.isEmpty())
                continue;
            path.addAll(segment.subList(1, segment.size()));
            current = nodes.get(i);
        }
        return path;
    }

    /**
     * 
     * @param start the start of the node
     * @param end   the end of the node
     * @return list of nodes representing the shortest path
     */
    private List<Integer> dijkstra(int start, int end) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();

        // Initialize distances for all nodes in the graph.
        for (Integer node : graph.getNodes()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(start, 0);
        pq.add(new int[] { start, 0 });

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int node = current[0], cost = current[1];

            if (cost > dist.get(node))
                continue;

            for (Edge edge : graph.getNeighbors(node)) {
                int newCost = cost + edge.getWeight();
                if (newCost < dist.getOrDefault(edge.getDestination(), Integer.MAX_VALUE)) {
                    dist.put(edge.getDestination(), newCost);
                    prev.put(edge.getDestination(), node);
                    pq.add(new int[] { edge.getDestination(), newCost });
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        for (Integer at = end; at != null; at = prev.get(at))
            path.add(at);
        Collections.reverse(path);
        return path.isEmpty() || path.get(0) != start ? Collections.emptyList() : path;
    }

    /**
     * 
     * @param title   the title of the alert
     * @param message the message in the alert
     * @description have a pop-up alert box for the user
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * @param none
     * @return none
     * @description Helper method to remove all lines drawn
     */
    private void clearPreviousPath() {
        pane.getChildren().removeAll(drawnLines);
        drawnLines.clear();
    }

    // launch everything
    public static void main(String[] args) {
        launch(args);
    }
}
