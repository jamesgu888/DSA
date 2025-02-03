package com.example.astar;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;

public class RouteVisualizer extends Application {
    private final List<Circle> selectedNodes = new ArrayList<>();
    private final List<Line> drawnLines = new ArrayList<>();
    private final List<Circle> circles = new ArrayList<>();
    private Integer startNode = null;
    private Integer endNode = null;
    private TextField startField;
    private TextField endField;
    private Map<Integer, List<int[]>> graph;
    private double[][] locations;
    private Pane pane;

    @Override
    public void start(Stage primaryStage) {
        pane = new Pane();

        // Text fields for start and end nodes
        startField = new TextField();
        startField.setPromptText("Start Node");
        startField.setLayoutX(50);
        startField.setLayoutY(500);

        endField = new TextField();
        endField.setPromptText("End Node");
        endField.setLayoutX(200);
        endField.setLayoutY(500);

        Button setStartEndButton = new Button("Set Start & End");
        setStartEndButton.setLayoutX(350);
        setStartEndButton.setLayoutY(500);
        setStartEndButton.setOnAction(e -> setStartEndNodes());

        pane.getChildren().addAll(startField, endField, setStartEndButton);

        // Node coordinates (5 rows × 8 columns)
        locations = new double[][]{
                {100, 50}, {200, 50}, {300, 50}, {400, 50}, {500, 50}, {600, 50}, {700, 50}, {800, 50},   // Row 0
                {100, 150}, {200, 150}, {300, 150}, {400, 150}, {500, 150}, {600, 150}, {700, 150}, {800, 150}, // Row 1
                {100, 250}, {200, 250}, {300, 250}, {400, 250}, {500, 250}, {600, 250}, {700, 250}, {800, 250}, // Row 2
                {100, 350}, {200, 350}, {300, 350}, {400, 350}, {500, 350}, {600, 350}, {700, 350}, {800, 350}, // Row 3
                {100, 450}, {200, 450}, {300, 450}, {400, 450}, {500, 450}, {600, 450}, {700, 450}, {800, 450}  // Row 4
        };

        // Build adjacency list
        graph = new HashMap<>();
        int[][] edges = {
                // Horizontal edges
                {0,1,4}, {1,2,2}, {2,3,5}, {3,4,1}, {4,5,3}, {5,6,4}, {6,7,2},
                {8,9,3}, {9,10,5}, {10,11,1}, {11,12,4}, {12,13,3}, {13,14,2}, {14,15,5},
                {16,17,2}, {17,18,4}, {18,19,5}, {19,20,1}, {20,21,3}, {21,22,5}, {22,23,4},
                {24,25,2}, {25,26,5}, {26,27,2}, {27,28,4}, {28,29,1}, {29,30,3}, {30,31,5},
                {32,33,1}, {33,34,3}, {34,35,2}, {35,36,5}, {36,37,4}, {37,38,1}, {38,39,2},

                // Vertical edges
                {0,8,3}, {1,9,5}, {2,10,2}, {3,11,4}, {4,12,1}, {5,13,3}, {6,14,2}, {7,15,5},
                {8,16,4}, {9,17,1}, {10,18,3}, {11,19,5}, {12,20,2}, {13,21,4}, {14,22,5}, {15,23,1},
                {16,24,3}, {17,25,2}, {18,26,5}, {19,27,1}, {20,28,4}, {21,29,2}, {22,30,5}, {23,31,3},
                {24,32,1}, {25,33,4}, {26,34,2}, {27,35,5}, {28,36,3}, {29,37,1}, {30,38,4}, {31,39,2},

                // Diagonal edges
                {0,9,3}, {1,10,2}, {2,11,4}, {8,17,3}, {9,18,5}, {10,19,2},
                {16,25,3}, {17,26,4}, {18,27,2}, {24,33,5}, {25,34,1}, {26,35,4},
                {27,36,3}, {28,37,2}, {29,38,5}, {30,39,1}
        };

        for (int[] edge : edges) {
            graph.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(new int[]{edge[1], edge[2]});
            graph.computeIfAbsent(edge[1], k -> new ArrayList<>()).add(new int[]{edge[0], edge[2]});
        }

        // Draw nodes
        circles.clear();
        for (int i = 0; i < locations.length; i++) {
            double x = locations[i][0];
            double y = locations[i][1];

            Circle circle = new Circle(x, y, 12, Color.LIGHTBLUE);
            Text label = new Text(x - 25, y - 15, "Node " + i);

            final int nodeIndex = i;
            circle.setOnMouseClicked(e -> handleNodeClick(circle, nodeIndex));

            circles.add(circle);
            pane.getChildren().addAll(circle, label);
        }

        // Draw edges
        for (int[] edge : edges) {
            int source = edge[0];
            int dest = edge[1];
            int weight = edge[2];

            Line line = new Line(
                    locations[source][0], locations[source][1],
                    locations[dest][0], locations[dest][1]
            );
            line.setStroke(Color.GRAY);

            Text weightLabel = new Text(
                    (locations[source][0] + locations[dest][0]) / 2,
                    (locations[source][1] + locations[dest][1]) / 2,
                    String.valueOf(weight)
            );
            weightLabel.setFill(Color.DARKRED);

            pane.getChildren().addAll(line, weightLabel);
        }

        // Reset button
        Button resetButton = new Button("Reset");
        resetButton.setLayoutX(500);
        resetButton.setLayoutY(500);
        resetButton.setOnAction(e -> resetSelection());
        pane.getChildren().add(resetButton);

        Scene scene = new Scene(pane, 800, 600);
        primaryStage.setTitle("Dynamic Carpool Route Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setStartEndNodes() {
        try {
            // Reset previous start/end colors
            if (startNode != null) circles.get(startNode).setFill(Color.LIGHTBLUE);
            if (endNode != null) circles.get(endNode).setFill(Color.LIGHTBLUE);

            int newStart = Integer.parseInt(startField.getText());
            int newEnd = Integer.parseInt(endField.getText());

            if (newStart < 0 || newStart >= circles.size() || newEnd < 0 || newEnd >= circles.size()) {
                System.out.println("Invalid node numbers.");
                return;
            }

            startNode = newStart;
            endNode = newEnd;

            // Set new colors
            circles.get(startNode).setFill(Color.BLACK);
            circles.get(endNode).setFill(Color.BLACK);

            calculateAndHighlightPath();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid node numbers.");
        }
    }

    private void handleNodeClick(Circle circle, int nodeIndex) {
        if (nodeIndex == startNode || nodeIndex == endNode) return;
        if (!selectedNodes.contains(circle)) {
            selectedNodes.add(circle);
            circle.setFill(Color.GREEN);
            calculateAndHighlightPath();
        }
    }

    private void calculateAndHighlightPath() {
        if (startNode == null || endNode == null) return;

        List<Integer> selectedNodeIndices = new ArrayList<>();
        selectedNodeIndices.add(startNode);
        for (Circle c : selectedNodes) {
            int idx = circles.indexOf(c);
            if (idx != startNode && idx != endNode) selectedNodeIndices.add(idx);
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
                if (selectedNodes.contains(src)) addArrowhead(x2 - offsetX, y2 - offsetY, x1 - offsetX, y1 - offsetY, true);
            } else {
                // Forward path (red)
                Line line = new Line(x1 - offsetX, y1 - offsetY, x2 - offsetX, y2 - offsetY);
                line.setStroke(Color.RED);
                line.setStrokeWidth(3);
                pane.getChildren().add(line);
                drawnLines.add(line);
                if (selectedNodes.contains(dest)) addArrowhead(x1 - offsetX, y1 - offsetY, x2 - offsetX, y2 - offsetY, false);
            }
            drawnPairs.add(pairKey);
        }
    }

    private void addArrowhead(double x1, double y1, double x2, double y2, boolean isReturn) {
        double arrowLength = 15.0;
        double angle = Math.atan2(y2 - y1, x2 - x1);

        // Reverse angle for return paths
        if(isReturn) {
            angle += Math.PI;
        }

        // Calculate arrowhead points
        double tipX = isReturn ? x1 : x2;
        double tipY = isReturn ? y1 : y2;

        Line arrow1 = new Line(
                tipX,
                tipY,
                tipX - arrowLength * Math.cos(angle - Math.PI/6),
                tipY - arrowLength * Math.sin(angle - Math.PI/6)
        );

        Line arrow2 = new Line(
                tipX,
                tipY,
                tipX - arrowLength * Math.cos(angle + Math.PI/6),
                tipY - arrowLength * Math.sin(angle + Math.PI/6)
        );

        arrow1.setStrokeWidth(3);
        arrow2.setStrokeWidth(3);
        arrow1.setStroke(isReturn ? Color.BLUE : Color.RED);
        arrow2.setStroke(isReturn ? Color.BLUE : Color.RED);

        pane.getChildren().addAll(arrow1, arrow2);
        drawnLines.add(arrow1);
        drawnLines.add(arrow2);
    }

    private void resetSelection() {
        // Reset selected intermediate nodes
        for (Circle circle : selectedNodes) {
            circle.setFill(Color.LIGHTBLUE);
        }
        selectedNodes.clear();

        // Reset start and end nodes
        if (startNode != null) {
            circles.get(startNode).setFill(Color.LIGHTBLUE);
            startNode = null;
        }
        if (endNode != null) {
            circles.get(endNode).setFill(Color.LIGHTBLUE);
            endNode = null;
        }

        // Clear text fields
        startField.clear();
        endField.clear();

        // Remove path lines
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
            if (segment.isEmpty()) continue;
            path.addAll(segment.subList(1, segment.size()));
            current = nodes.get(i);
        }
        return path;
    }

    private List<Integer> dijkstra(Map<Integer, List<int[]>> graph, int start, int end) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();

        for (int node : graph.keySet()) dist.put(node, Integer.MAX_VALUE);
        dist.put(start, 0);
        pq.add(new int[]{start, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int node = current[0], cost = current[1];
            if (cost > dist.get(node)) continue;

            for (int[] neighbor : graph.get(node)) {
                int newCost = cost + neighbor[1];
                if (newCost < dist.get(neighbor[0])) {
                    dist.put(neighbor[0], newCost);
                    prev.put(neighbor[0], node);
                    pq.add(new int[]{neighbor[0], newCost});
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        for (Integer at = end; at != null; at = prev.get(at)) path.add(at);
        Collections.reverse(path);
        return path.isEmpty() || path.get(0) != start ? Collections.emptyList() : path;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
