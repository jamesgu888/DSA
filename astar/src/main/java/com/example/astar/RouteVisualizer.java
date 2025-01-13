package com.example.astar;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;

public class RouteVisualizer extends Application {

    private Circle selectedNode1 = null;
    private Circle selectedNode2 = null;
    private final List<Line> drawnLines = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        Pane pane = new Pane();

        // Define locations (nodes)
        double[][] locations = {
                {150, 100}, // Node 0
                {300, 50},  // Node 1
                {300, 150}, // Node 2
                {450, 100}, // Node 3
                {600, 50},  // Node 4
                {600, 150}, // Node 5
                {750, 100}, // Node 6
                {450, 250}, // Node 7
                {300, 250}, // Node 8
                {600, 250}  // Node 9
        };

        // Define edges with weights {source, destination, weight}
        int[][] edges = {
                {0, 1, 2}, {0, 2, 4}, {1, 2, 1}, {1, 3, 7},
                {2, 3, 3}, {3, 4, 2}, {4, 5, 5}, {5, 6, 1},
                {3, 5, 2}, {3, 7, 6}, {7, 8, 3}, {8, 2, 4},
                {5, 9, 3}, {9, 7, 2}, {9, 6, 8},
        };

        // Build the graph as an adjacency list
        Map<Integer, List<int[]>> graph = new HashMap<>();
        for (int[] edge : edges) {
            graph.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(new int[]{edge[1], edge[2]});
            graph.computeIfAbsent(edge[1], k -> new ArrayList<>()).add(new int[]{edge[0], edge[2]}); // Undirected graph
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

        // Set up the stage and scene
        Scene scene = new Scene(pane, 800, 400);
        primaryStage.setTitle("Complex Route Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleNodeClick(Circle circle, int nodeIndex, Map<Integer, List<int[]>> graph, double[][] locations, Pane pane) {
        if (selectedNode1 == null) {
            selectedNode1 = circle;
            circle.setFill(Color.GREEN);
        } else if (selectedNode2 == null) {
            selectedNode2 = circle;
            circle.setFill(Color.GREEN);

            int start = getNodeIndex(selectedNode1, locations);
            int end = getNodeIndex(selectedNode2, locations);
            List<Integer> shortestPath = dijkstra(graph, start, end);

            clearPreviousPath(pane);
            highlightPath(shortestPath, locations, pane);

            resetNodeSelection();
        }
    }

    private void resetNodeSelection() {
        selectedNode1.setFill(Color.LIGHTBLUE);
        selectedNode2.setFill(Color.LIGHTBLUE);
        selectedNode1 = null;
        selectedNode2 = null;
    }

    private void clearPreviousPath(Pane pane) {
        for (Line line : drawnLines) {
            pane.getChildren().remove(line);
        }
        drawnLines.clear();
    }

    private void highlightPath(List<Integer> path, double[][] locations, Pane pane) {
        for (int i = 0; i < path.size() - 1; i++) {
            int source = path.get(i);
            int destination = path.get(i + 1);

            double x1 = locations[source][0];
            double y1 = locations[source][1];
            double x2 = locations[destination][0];
            double y2 = locations[destination][1];

            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(Color.RED);
            line.setStrokeWidth(3);

            pane.getChildren().add(line);
            drawnLines.add(line);
        }
    }

    private int getNodeIndex(Circle circle, double[][] locations) {
        for (int i = 0; i < locations.length; i++) {
            if (circle.getCenterX() == locations[i][0] && circle.getCenterY() == locations[i][1]) {
                return i;
            }
        }
        return -1;
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

        List<Integer> path = new ArrayList<>();
        for (Integer at = end; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
