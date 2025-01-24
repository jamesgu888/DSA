package com.example.astar;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

    @Override
    public void start(Stage primaryStage) {
        Pane pane = new Pane();

        // Expanded locations (nodes)
        double[][] locations = {
                {100, 50}, {200, 50}, {300, 50}, {400, 50}, {500, 50}, {600, 50}, {700, 50},
                {100, 150}, {200, 150}, {300, 150}, {400, 150}, {500, 150}, {600, 150}, {700, 150},
                {100, 250}, {200, 250}, {300, 250}, {400, 250}, {500, 250}, {600, 250}, {700, 250},
                {100, 350}, {200, 350}, {300, 350}, {400, 350}, {500, 350}, {600, 350}, {700, 350}
        };

        // Expanded edges with weights {source, destination, weight}
        int[][] edges = {
                {0, 1, 2}, {1, 2, 2}, {2, 3, 2}, {3, 4, 2}, {4, 5, 2}, {5, 6, 2},
                {0, 7, 3}, {1, 8, 3}, {2, 9, 3}, {3, 10, 3}, {4, 11, 3}, {5, 12, 3}, {6, 13, 3},
                {7, 8, 2}, {8, 9, 2}, {9, 10, 2}, {10, 11, 2}, {11, 12, 2}, {12, 13, 2},
                {7, 14, 3}, {8, 15, 3}, {9, 16, 3}, {10, 17, 3}, {11, 18, 3}, {12, 19, 3}, {13, 20, 3},
                {14, 15, 2}, {15, 16, 2}, {16, 17, 2}, {17, 18, 2}, {18, 19, 2}, {19, 20, 2},
                {14, 21, 3}, {15, 22, 3}, {16, 23, 3}, {17, 24, 3}, {18, 25, 3}, {19, 26, 3}, {20, 27, 3},
                {21, 22, 2}, {22, 23, 2}, {23, 24, 2}, {24, 25, 2}, {25, 26, 2}, {26, 27, 2}
        };

        // Build the graph as an adjacency list
        Map<Integer, List<int[]>> graph = new HashMap<>();
        for (int[] edge : edges) {
            graph.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(new int[]{edge[1], edge[2]});
            graph.computeIfAbsent(edge[1], k -> new ArrayList<>()).add(new int[]{edge[0], edge[2]});
        }

        // Draw nodes
        Map<Circle, Integer> circleNodeMap = new HashMap<>();
        for (int i = 0; i < locations.length; i++) {
            double x = locations[i][0];
            double y = locations[i][1];

            Circle circle = new Circle(x, y, 12, Color.LIGHTBLUE);
            Text label = new Text(x - 25, y - 15, "Node " + i);

            int nodeIndex = i;
            circle.setOnMouseClicked(event -> handleNodeClick(circle, nodeIndex, graph, locations, pane));

            pane.getChildren().addAll(circle, label);
            circleNodeMap.put(circle, i);
        }

        // Draw edges
        for (int[] edge : edges) {
            int source = edge[0];
            int destination = edge[1];
            int weight = edge[2];

            double x1 = locations[source][0];
            double y1 = locations[source][1];
            double x2 = locations[destination][0];
            double y2 = locations[destination][1];

            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(Color.GRAY);

            Text weightLabel = new Text((x1 + x2) / 2, (y1 + y2) / 2, String.valueOf(weight));
            weightLabel.setFill(Color.DARKRED);

            pane.getChildren().addAll(line, weightLabel);
        }

        // Add reset button
        Button resetButton = new Button("Reset");
        resetButton.setLayoutX(350);
        resetButton.setLayoutY(400);
        resetButton.setOnAction(event -> resetSelection(pane));

        pane.getChildren().add(resetButton);

        // Set up the stage and scene
        Scene scene = new Scene(pane, 800, 450);
        primaryStage.setTitle("Dynamic Carpool Route Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleNodeClick(Circle circle, int nodeIndex, Map<Integer, List<int[]>> graph, double[][] locations, Pane pane) {
        if (!selectedNodes.contains(circle)) {
            selectedNodes.add(circle);
            circle.setFill(Color.GREEN);
            calculateAndHighlightPath(graph, locations, pane);
        }
    }

    private void calculateAndHighlightPath(Map<Integer, List<int[]>> graph, double[][] locations, Pane pane) {
        if (selectedNodes.size() < 2) return;

        List<Integer> selectedNodeIndices = new ArrayList<>();
        for (Circle selected : selectedNodes) {
            selectedNodeIndices.add(getNodeIndex(selected, locations));
        }

        List<Integer> shortestPath = findOptimizedPath(graph, selectedNodeIndices);

        clearPreviousPath(pane);
        highlightPath(shortestPath, locations, pane);
    }

    private void resetSelection(Pane pane) {
        for (Circle circle : selectedNodes) {
            circle.setFill(Color.LIGHTBLUE);
        }
        selectedNodes.clear();
        clearPreviousPath(pane);
    }

    private void clearPreviousPath(Pane pane) {
        for (Line line : drawnLines) {
            pane.getChildren().remove(line);
        }
        drawnLines.clear();
    }

    private int getNodeIndex(Circle circle, double[][] locations) {
        for (int i = 0; i < locations.length; i++) {
            if (circle.getCenterX() == locations[i][0] && circle.getCenterY() == locations[i][1]) {
                return i;
            }
        }
        return -1;
    }

    private List<Integer> findOptimizedPath(Map<Integer, List<int[]>> graph, List<Integer> selectedNodes) {
        List<Integer> fullPath = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        int current = selectedNodes.get(0);
        fullPath.add(current);
        visited.add(current);

        while (visited.size() < selectedNodes.size()) {
            int nextNode = -1;
            int shortestDistance = Integer.MAX_VALUE;

            for (int node : selectedNodes) {
                if (!visited.contains(node)) {
                    int distance = dijkstraDistance(graph, current, node);
                    if (distance < shortestDistance) {
                        shortestDistance = distance;
                        nextNode = node;
                    }
                }
            }

            if (nextNode != -1) {
                List<Integer> subPath = dijkstra(graph, current, nextNode);
                subPath.remove(0); // Avoid duplicating the current node
                fullPath.addAll(subPath);
                visited.add(nextNode);
                current = nextNode;
            }
        }

        return fullPath;
    }

    private void highlightPath(List<Integer> path, double[][] locations, Pane pane) {
        Set<String> drawnPairs = new HashSet<>(); // To track drawn edges

        for (int i = 0; i < path.size() - 1; i++) {
            int source = path.get(i);
            int destination = path.get(i + 1);

            double x1 = locations[source][0];
            double y1 = locations[source][1];
            double x2 = locations[destination][0];
            double y2 = locations[destination][1];

            // Calculate the perpendicular offset
            double dx = x2 - x1;
            double dy = y2 - y1;
            double length = Math.sqrt(dx * dx + dy * dy);
            double offsetX = -(dy / length) * 10; // Perpendicular offset scaled
            double offsetY = (dx / length) * 10;

            // Check if this is a return trip
            String pair = source + "-" + destination;
            String reversePair = destination + "-" + source;

            if (drawnPairs.contains(reversePair)) {
                // Draw the return trip (blue), on the left side relative to its reverse direction
                Line returnLine = new Line(x2 - offsetX, y2 - offsetY, x1 - offsetX, y1 - offsetY);
                returnLine.setStroke(Color.BLUE);
                returnLine.setStrokeWidth(3);

                pane.getChildren().add(returnLine);
                drawnLines.add(returnLine);

                // Add arrowhead for return trips (blue)
                if (selectedNodes.stream().anyMatch(circle ->
                        circle.getCenterX() == locations[source][0] && circle.getCenterY() == locations[source][1])) {
                    addArrowhead(x2 - offsetX, y2 - offsetY, x1 - offsetX, y1 - offsetY, pane, true);
                }
            } else {
                // Draw the outgoing trip (red), on the left side
                Line outgoingLine = new Line(x1 - offsetX, y1 - offsetY, x2 - offsetX, y2 - offsetY);
                outgoingLine.setStroke(Color.RED);
                outgoingLine.setStrokeWidth(3);

                pane.getChildren().add(outgoingLine);
                drawnLines.add(outgoingLine);

                // Add arrowhead for outgoing trips (red)
                if (selectedNodes.stream().anyMatch(circle ->
                        circle.getCenterX() == locations[destination][0] && circle.getCenterY() == locations[destination][1])) {
                    addArrowhead(x1 - offsetX, y1 - offsetY, x2 - offsetX, y2 - offsetY, pane, false);
                }
            }

            drawnPairs.add(pair);
        }
    }

    private void addArrowhead(double x1, double y1, double x2, double y2, Pane pane, boolean isReturn) {
        double arrowLength = 10; // Length of the arrowhead lines
        double arrowWidth = 5;   // Width of the arrowhead

        // Calculate the direction of the arrow
        double angle = Math.atan2(y2 - y1, x2 - x1);

        if (isReturn) {
            // Reverse the direction for blue arrows (return)
            angle = Math.atan2(y1 - y2, x1 - x2);

            // Flip the arrowhead to the starting end of the blue line
            double tempX = x1;
            double tempY = y1;
            x1 = x2;
            y1 = y2;
            x2 = tempX;
            y2 = tempY;
        }

        // Calculate the points for the arrowhead
        double xArrow1 = x2 - arrowLength * Math.cos(angle - Math.PI / 6);
        double yArrow1 = y2 - arrowLength * Math.sin(angle - Math.PI / 6);

        double xArrow2 = x2 - arrowLength * Math.cos(angle + Math.PI / 6);
        double yArrow2 = y2 - arrowLength * Math.sin(angle + Math.PI / 6);

        // Create the arrowhead lines
        Line arrowLine1 = new Line(x2, y2, xArrow1, yArrow1);
        Line arrowLine2 = new Line(x2, y2, xArrow2, yArrow2);

        // Set the color and thickness of the arrowhead
        arrowLine1.setStroke(isReturn ? Color.BLUE : Color.RED);
        arrowLine2.setStroke(isReturn ? Color.BLUE : Color.RED);
        arrowLine1.setStrokeWidth(3);
        arrowLine2.setStrokeWidth(3);

        // Add the arrowhead to the pane
        pane.getChildren().addAll(arrowLine1, arrowLine2);
        drawnLines.add(arrowLine1);
        drawnLines.add(arrowLine2);
    }

    private List<Integer> dijkstra(Map<Integer, List<int[]>> graph, int start, int end) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> distances = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();

        for (int node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        pq.add(new int[]{start, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int currentNode = current[0];
            int currentDistance = current[1];

            if (currentDistance > distances.get(currentNode)) continue;

            for (int[] neighbor : graph.get(currentNode)) {
                int nextNode = neighbor[0];
                int weight = neighbor[1];
                int newDist = currentDistance + weight;

                if (newDist < distances.get(nextNode)) {
                    distances.put(nextNode, newDist);
                    previous.put(nextNode, currentNode);
                    pq.add(new int[]{nextNode, newDist});
                }
            }
        }

        // Reconstruct the path
        List<Integer> path = new ArrayList<>();
        for (Integer at = end; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    private int dijkstraDistance(Map<Integer, List<int[]>> graph, int start, int end) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> distances = new HashMap<>();
        for (int node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        pq.add(new int[]{start, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int currentNode = current[0];
            int currentDistance = current[1];

            if (currentDistance > distances.get(currentNode)) continue;

            for (int[] neighbor : graph.get(currentNode)) {
                int nextNode = neighbor[0];
                int weight = neighbor[1];
                int newDist = currentDistance + weight;

                if (newDist < distances.get(nextNode)) {
                    distances.put(nextNode, newDist);
                    pq.add(new int[]{nextNode, newDist});
                }
            }
        }

        return distances.getOrDefault(end, Integer.MAX_VALUE);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
