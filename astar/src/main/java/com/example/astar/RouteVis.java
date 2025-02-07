package com.example.astar;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.CubicCurve;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;

public class RouteVis extends Application {
    // List for selected houses (instead of circles)
    private final List<House> selectedHouses = new ArrayList<>();
    // List for all houses (nodes)
    private final List<House> houses = new ArrayList<>();
    private Integer startNode = null;
    private Integer endNode = null;
    private TextField startField;
    private TextField endField;
    private Map<Integer, List<int[]>> graph;
    // Hard-coded positions for the houses with irregular spacing
    private double[][] locations;
    private Pane pane;

    // List to keep track of drawn path lines and arrowheads
    private final List<Line> drawnLines = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        pane = new Pane();

        // Text fields for start and end nodes
        startField = new TextField();
        startField.setPromptText("Start Node");
        startField.setLayoutX(50);
        startField.setLayoutY(750); // placed at the bottom

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
        // Fixed irregular positions for 40 houses (nodes)
        // The coordinates are tweaked slightly from a perfect grid.
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

        // ******************************************************************
        // Original edge definitions (without highways)
        // Each edge is defined as {source, dest, weight}
        // ******************************************************************
        int[][] edges = {
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

        // Build the graph from the edge list.
        // Each neighbor is stored as {destination, weight}
        graph = new HashMap<>();
        for (int[] edge : edges) {
            graph.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(new int[] { edge[1], edge[2] });
            graph.computeIfAbsent(edge[1], k -> new ArrayList<>()).add(new int[] { edge[0], edge[2] });
        }

        // Draw houses (nodes) at their fixed irregular positions
        houses.clear();

        // Draw edges (roads) using a randomized connection style for each edge.
        Random rand = new Random();
        for (int[] edge : edges) {
            int source = edge[0];
            int dest = edge[1];
            int weight = edge[2];

            double x1 = locations[source][0];
            double y1 = locations[source][1];
            double x2 = locations[dest][0];
            double y2 = locations[dest][1];

            double connectionType = rand.nextDouble();
            if (connectionType < 0.33) {
                // Draw a straight road
                Line road = new Line(x1, y1, x2, y2);
                road.setStroke(Color.DARKGRAY);
                road.setStrokeWidth(8);
                pane.getChildren().add(road);

                // Draw the yellow dashed center line
                Line centerLine = new Line(x1, y1, x2, y2);
                centerLine.setStroke(Color.YELLOW);
                centerLine.setStrokeWidth(2);
                centerLine.getStrokeDashArray().addAll(10d, 10d);
                pane.getChildren().add(centerLine);

                // Add the weight label
                Text weightLabel = new Text((x1 + x2) / 2, (y1 + y2) / 2, String.valueOf(weight));
                weightLabel.setFill(Color.DARKRED);
                pane.getChildren().add(weightLabel);
            } else if (connectionType < 0.66) {
                // Draw a quadratic curve road
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
                // Compute a perpendicular (normalized) vector:
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

                // Draw the yellow dashed center line along the quadratic curve
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

                // Compute label position using the quadratic Bézier formula at t = 0.5:
                double labelX = 0.25 * x1 + 0.5 * ctrlX + 0.25 * x2;
                double labelY = 0.25 * y1 + 0.5 * ctrlY + 0.25 * y2;
                Text weightLabel = new Text(labelX, labelY, String.valueOf(weight));
                weightLabel.setFill(Color.DARKRED);
                pane.getChildren().add(weightLabel);
            } else {
                // Draw a cubic curve road
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
                // First control point (with random offset)
                double cp1X = x1 + dx / 3;
                double cp1Y = y1 + dy / 3;
                double offset1 = rand.nextDouble() * 100 - 50;
                cp1X += px * offset1;
                cp1Y += py * offset1;
                // Second control point (with random offset)
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

                // Draw the yellow dashed center line along the cubic curve
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

                // Compute label position using the cubic Bézier formula at t = 0.5:
                double labelX = 0.125 * x1 + 0.375 * cp1X + 0.375 * cp2X + 0.125 * x2;
                double labelY = 0.125 * y1 + 0.375 * cp1Y + 0.375 * cp2Y + 0.125 * y2;
                Text weightLabel = new Text(labelX, labelY, String.valueOf(weight));
                weightLabel.setFill(Color.DARKRED);
                pane.getChildren().add(weightLabel);
            }
        }

        for (int i = 0; i < locations.length; i++) {
            double x = locations[i][0];
            double y = locations[i][1];

            // Create a House at the given location (using 12 as the "size" parameter)
            House house = new House(x, y, 12, Color.LIGHTBLUE);
            Text label = new Text(x - 25, y - 25, "House " + i);
            label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            label.toFront(); // Ensure the label stays on top

            final int nodeIndex = i;
            house.setOnMouseClicked(e -> handleNodeClick(house, nodeIndex));

            houses.add(house);
            pane.getChildren().addAll(house, label);
        }

        // Ensure houses are in front of the roads
        for (House house : houses) {
            house.toFront();
        }

        // Reset button remains as before
        Button resetButton = new Button("Reset");
        resetButton.setLayoutX(500);
        resetButton.setLayoutY(750);
        resetButton.setOnAction(e -> resetSelection());
        pane.getChildren().add(resetButton);

        // Increase scene size to accommodate the irregular positioning
        Scene scene = new Scene(pane, 1300, 800);
        primaryStage.setTitle("Dynamic Carpool Route Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setStartEndNodes() {
        try {
            // Reset previous start/end colors
            if (startNode != null)
                houses.get(startNode).setFill(Color.LIGHTBLUE);
            if (endNode != null)
                houses.get(endNode).setFill(Color.LIGHTBLUE);

            int newStart = Integer.parseInt(startField.getText());
            int newEnd = Integer.parseInt(endField.getText());

            if (newStart < 0 || newStart >= houses.size() || newEnd < 0 || newEnd >= houses.size()) {
                System.out.println("Invalid node numbers.");
                return;
            }

            startNode = newStart;
            endNode = newEnd;

            // Set new colors for start and end houses
            houses.get(startNode).setFill(Color.BLACK);
            houses.get(endNode).setFill(Color.BLACK);

            calculateAndHighlightPath();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid node numbers.");
        }
    }

    private void handleNodeClick(House house, int nodeIndex) {
        if (nodeIndex == startNode || nodeIndex == endNode)
            return;
        if (!selectedHouses.contains(house)) {
            selectedHouses.add(house);
            house.setFill(Color.GREEN);
            calculateAndHighlightPath();
        }
    }

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

        List<Integer> path = findOptimizedPath(graph, selectedNodeIndices);
        clearPreviousPath();
        highlightPath(path, selectedNodeIndices);
    }

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
                // Return path (blue)
                Line line = new Line(x2 - offsetX, y2 - offsetY, x1 - offsetX, y1 - offsetY);
                line.setStroke(Color.BLUE);
                line.setStrokeWidth(3);
                pane.getChildren().add(line);
                drawnLines.add(line);
                if (selectedNodes.contains(src))
                    addArrowhead(x2 - offsetX, y2 - offsetY, x1 - offsetX, y1 - offsetY, true);
            } else {
                // Forward path (red)
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

    private void clearPreviousPath() {
        pane.getChildren().removeAll(drawnLines);
        drawnLines.clear();
    }

    private List<Integer> findOptimizedPath(Map<Integer, List<int[]>> graph, List<Integer> nodes) {
        List<Integer> path = new ArrayList<>();
        int current = nodes.get(0);
        path.add(current);

        for (int i = 1; i < nodes.size(); i++) {
            List<Integer> segment = dijkstra(graph, current, nodes.get(i));
            if (segment.isEmpty())
                continue;
            path.addAll(segment.subList(1, segment.size()));
            current = nodes.get(i);
        }
        return path;
    }

    private List<Integer> dijkstra(Map<Integer, List<int[]>> graph, int start, int end) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();

        for (int node : graph.keySet())
            dist.put(node, Integer.MAX_VALUE);
        dist.put(start, 0);
        pq.add(new int[] { start, 0 });

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int node = current[0], cost = current[1];
            if (cost > dist.get(node))
                continue;

            for (int[] neighbor : graph.get(node)) {
                int newCost = cost + neighbor[1];
                if (newCost < dist.get(neighbor[0])) {
                    dist.put(neighbor[0], newCost);
                    prev.put(neighbor[0], node);
                    pq.add(new int[] { neighbor[0], newCost });
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        for (Integer at = end; at != null; at = prev.get(at))
            path.add(at);
        Collections.reverse(path);
        return path.isEmpty() || path.get(0) != start ? Collections.emptyList() : path;
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * A simple House shape that replaces the circle.
     * The house is drawn as a rectangle (the body) with a triangular roof.
     */
    private static class House extends Group {
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

        public void setFill(Color color) {
            body.setFill(color);
            roof.setFill(color.darker());
        }
    }
}
