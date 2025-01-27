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

        // 40 node coordinates (5 rows × 8 columns)
        double[][] locations = {
                // Row 0 (nodes 0..7)
                {100,  50}, {200,  50}, {300,  50}, {400,  50},
                {500,  50}, {600,  50}, {700,  50}, {800,  50},
                // Row 1 (nodes 8..15)
                {100, 150}, {200, 150}, {300, 150}, {400, 150},
                {500, 150}, {600, 150}, {700, 150}, {800, 150},
                // Row 2 (nodes 16..23)
                {100, 250}, {200, 250}, {300, 250}, {400, 250},
                {500, 250}, {600, 250}, {700, 250}, {800, 250},
                // Row 3 (nodes 24..31)
                {100, 350}, {200, 350}, {300, 350}, {400, 350},
                {500, 350}, {600, 350}, {700, 350}, {800, 350},
                // Row 4 (nodes 32..39)
                {100, 450}, {200, 450}, {300, 450}, {400, 450},
                {500, 450}, {600, 450}, {700, 450}, {800, 450}
        };

        // Edges {source, destination, weight} — includes horizontal, vertical, and some diagonals.
        int[][] edges = {
                // Horizontal edges by row:
                // Row 0 (0..7)
                {0,1,4}, {1,2,2}, {2,3,5}, {3,4,1}, {4,5,3}, {5,6,4}, {6,7,2},
                // Row 1 (8..15)
                {8,9,3}, {9,10,5}, {10,11,1}, {11,12,4}, {12,13,3}, {13,14,2}, {14,15,5},
                // Row 2 (16..23)
                {16,17,2}, {17,18,4}, {18,19,5}, {19,20,1}, {20,21,3}, {21,22,5}, {22,23,4},
                // Row 3 (24..31)
                {24,25,2}, {25,26,5}, {26,27,2}, {27,28,4}, {28,29,1}, {29,30,3}, {30,31,5},
                // Row 4 (32..39)
                {32,33,1}, {33,34,3}, {34,35,2}, {35,36,5}, {36,37,4}, {37,38,1}, {38,39,2},

                // Vertical edges (column‐wise):
                // Between row 0 & 1
                {0,8,3}, {1,9,5}, {2,10,2}, {3,11,4}, {4,12,1}, {5,13,3}, {6,14,2}, {7,15,5},
                // Between row 1 & 2
                {8,16,4}, {9,17,1}, {10,18,3}, {11,19,5}, {12,20,2}, {13,21,4}, {14,22,5}, {15,23,1},
                // Between row 2 & 3
                {16,24,3}, {17,25,2}, {18,26,5}, {19,27,1}, {20,28,4}, {21,29,2}, {22,30,5}, {23,31,3},
                // Between row 3 & 4
                {24,32,1}, {25,33,4}, {26,34,2}, {27,35,5}, {28,36,3}, {29,37,1}, {30,38,4}, {31,39,2},

                // Diagonal edges (random selection):
                {0,9,3}, {1,10,2}, {2,11,4}, {8,17,3}, {9,18,5}, {10,19,2},
                {16,25,3}, {17,26,4}, {18,27,2}, {24,33,5}, {25,34,1}, {26,35,4},
                {27,36,3}, {28,37,2}, {29,38,5}, {30,39,1}
        };

        // Build adjacency list
        Map<Integer, List<int[]>> graph = new HashMap<>();
        for (int[] edge : edges) {
            graph.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(new int[]{edge[1], edge[2]});
            graph.computeIfAbsent(edge[1], k -> new ArrayList<>()).add(new int[]{edge[0], edge[2]});
        }

        // Draw nodes
        for (int i = 0; i < locations.length; i++) {
            double x = locations[i][0];
            double y = locations[i][1];

            Circle circle = new Circle(x, y, 12, Color.LIGHTBLUE);
            Text label = new Text(x - 25, y - 15, "Node " + i);

            final int nodeIndex = i;
            circle.setOnMouseClicked(e -> handleNodeClick(circle, nodeIndex, graph, locations, pane));

            pane.getChildren().addAll(circle, label);
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

        // Reset button
        Button resetButton = new Button("Reset");
        resetButton.setLayoutX(350);
        resetButton.setLayoutY(800);
        resetButton.setOnAction(e -> resetSelection(pane));
        pane.getChildren().add(resetButton);

        // Set up stage
        Scene scene = new Scene(pane, 800, 450);
        primaryStage.setTitle("Dynamic Carpool Route Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleNodeClick(Circle circle, int nodeIndex,
                                 Map<Integer, List<int[]>> graph,
                                 double[][] locations, Pane pane) {
        if (!selectedNodes.contains(circle)) {
            selectedNodes.add(circle);
            circle.setFill(Color.GREEN);
            calculateAndHighlightPath(graph, locations, pane);
        }
    }

    private void calculateAndHighlightPath(Map<Integer, List<int[]>> graph,
                                           double[][] locations, Pane pane) {
        // Need at least two selected nodes for a path
        if (selectedNodes.size() < 2) return;

        // Get node indices
        List<Integer> selectedNodeIndices = new ArrayList<>();
        for (Circle selected : selectedNodes) {
            selectedNodeIndices.add(getNodeIndex(selected, locations));
        }

        // Compute multi‐stop route
        List<Integer> route = findOptimizedPath(graph, selectedNodeIndices);

        // Clear old lines and draw the new path
        clearPreviousPath(pane);
        highlightPath(route, selectedNodeIndices, locations, pane);
    }

    private void highlightPath(List<Integer> path,
                               List<Integer> selectedNodeIndices,
                               double[][] locations, Pane pane) {
        Set<String> drawnPairs = new HashSet<>();
        double offsetScale = 5.0; // offset for parallel lines

        for (int i = 0; i < path.size() - 1; i++) {
            int source = path.get(i);
            int dest = path.get(i + 1);

            // Calculate offset
            double x1 = locations[source][0];
            double y1 = locations[source][1];
            double x2 = locations[dest][0];
            double y2 = locations[dest][1];
            double dx = x2 - x1;
            double dy = y2 - y1;
            double length = Math.sqrt(dx * dx + dy * dy);

            double offsetX = -(dy / length) * offsetScale;
            double offsetY =  (dx / length) * offsetScale;

            String pair = source + "-" + dest;
            String reversePair = dest + "-" + source;

            if (drawnPairs.contains(reversePair)) {
                // Return segment (blue)
                Line returnLine = new Line(x2 - offsetX, y2 - offsetY, x1 - offsetX, y1 - offsetY);
                returnLine.setStroke(Color.BLUE);
                returnLine.setStrokeWidth(3);
                pane.getChildren().add(returnLine);
                drawnLines.add(returnLine);

                // Only add arrowhead if the tip (source) is a selected node
                if (selectedNodeIndices.contains(source)) {
                    addArrowhead(x2 - offsetX, y2 - offsetY, x1 - offsetX, y1 - offsetY, pane, true);
                }
            } else {
                // Outgoing segment (red)
                Line outgoingLine = new Line(x1 - offsetX, y1 - offsetY, x2 - offsetX, y2 - offsetY);
                outgoingLine.setStroke(Color.RED);
                outgoingLine.setStrokeWidth(3);
                pane.getChildren().add(outgoingLine);
                drawnLines.add(outgoingLine);

                // Only add arrowhead if the tip (destination) is a selected node
                if (selectedNodeIndices.contains(dest)) {
                    addArrowhead(x1 - offsetX, y1 - offsetY, x2 - offsetX, y2 - offsetY, pane, false);
                }
            }
            drawnPairs.add(pair);
        }
    }

    private void addArrowhead(double x1, double y1, double x2, double y2,
                              Pane pane, boolean isReturn) {
        // Larger arrowhead
        double arrowLength    = 15.0;
        double arrowHalfAngle = Math.PI / 6; // a bit wider than before

        // Direction from (x1,y1) to (x2,y2)
        double dx = x2 - x1;
        double dy = y2 - y1;
        double theta = Math.atan2(dy, dx);

        // For the return line, flip direction
        if (isReturn) {
            theta += Math.PI;
        }

        // Arrow tip is the line's end
        double tipX = isReturn ? x1 : x2;
        double tipY = isReturn ? y1 : y2;

        // Base is arrowLength back from tip
        double baseX = tipX - arrowLength * Math.cos(theta);
        double baseY = tipY - arrowLength * Math.sin(theta);

        // Arrow “legs”
        double legX1 = baseX + (arrowLength / 2) * Math.sin(theta + arrowHalfAngle);
        double legY1 = baseY - (arrowLength / 2) * Math.cos(theta + arrowHalfAngle);

        double legX2 = baseX - (arrowLength / 2) * Math.sin(theta - arrowHalfAngle);
        double legY2 = baseY + (arrowLength / 2) * Math.cos(theta - arrowHalfAngle);

        // Create lines
        Line arrow1 = new Line(tipX, tipY, legX1, legY1);
        Line arrow2 = new Line(tipX, tipY, legX2, legY2);

        arrow1.setStrokeWidth(3);
        arrow2.setStrokeWidth(3);
        arrow1.setStroke(isReturn ? Color.BLUE : Color.RED);
        arrow2.setStroke(isReturn ? Color.BLUE : Color.RED);

        pane.getChildren().addAll(arrow1, arrow2);
        drawnLines.add(arrow1);
        drawnLines.add(arrow2);
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
            if (circle.getCenterX() == locations[i][0]
                    && circle.getCenterY() == locations[i][1]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds a path visiting each selected node in a sequence of shortest hops.
     */
    private List<Integer> findOptimizedPath(Map<Integer, List<int[]>> graph,
                                            List<Integer> selectedNodes) {
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
                // Dijkstra path from current to nextNode
                List<Integer> subPath = dijkstra(graph, current, nextNode);
                // Remove the first node so we don't repeat it
                subPath.remove(0);
                fullPath.addAll(subPath);
                visited.add(nextNode);
                current = nextNode;
            }
        }
        return fullPath;
    }

    /**
     * Returns the actual shortest path from start to end.
     */
    private List<Integer> dijkstra(Map<Integer, List<int[]>> graph, int start, int end) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();

        for (int node : graph.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(start, 0);
        pq.add(new int[]{start, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int currentNode = current[0];
            int currentDist = current[1];

            if (currentDist > dist.get(currentNode)) continue;

            for (int[] neighbor : graph.get(currentNode)) {
                int nextNode = neighbor[0];
                int weight = neighbor[1];
                int newDist = currentDist + weight;

                if (newDist < dist.get(nextNode)) {
                    dist.put(nextNode, newDist);
                    prev.put(nextNode, currentNode);
                    pq.add(new int[]{nextNode, newDist});
                }
            }
        }

        // Reconstruct path
        List<Integer> path = new ArrayList<>();
        for (Integer at = end; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Returns only the numeric distance from start to end.
     */
    private int dijkstraDistance(Map<Integer, List<int[]>> graph, int start, int end) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> dist = new HashMap<>();
        for (int node : graph.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(start, 0);
        pq.add(new int[]{start, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int currentNode = current[0];
            int currentDist = current[1];
            if (currentDist > dist.get(currentNode)) continue;

            for (int[] neighbor : graph.get(currentNode)) {
                int nextNode = neighbor[0];
                int weight = neighbor[1];
                int newDist = currentDist + weight;
                if (newDist < dist.get(nextNode)) {
                    dist.put(nextNode, newDist);
                    pq.add(new int[]{nextNode, newDist});
                }
            }
        }
        return dist.getOrDefault(end, Integer.MAX_VALUE);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
