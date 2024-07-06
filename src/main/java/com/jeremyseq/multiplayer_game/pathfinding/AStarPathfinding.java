package main.java.com.jeremyseq.multiplayer_game.pathfinding;

import java.util.*;
import java.util.List;

public class AStarPathfinding {
    private final Grid grid;

    public AStarPathfinding(Grid grid) {
        this.grid = grid;
    }

    public List<Node> findPath(Node start, Node end) {
        // Priority queue to store open nodes, sorted by their fCost
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
        // Set to store closed nodes
        Set<Node> closedList = new HashSet<>();

        // Initialize start node costs and add to open list
        start.setGCost(0);
        start.setHCost(calculateHeuristic(start, end));
        openList.add(start);

        // Main loop to process nodes
        while (!openList.isEmpty()) {
            // Get the node with the lowest fCost
            Node currentNode = openList.poll();
            closedList.add(currentNode);

            // If we reached the end node, reconstruct the path
            if (currentNode.equals(end)) {
                return reconstructPath(end);
            }

            // Process each neighbor of the current node
            for (Node neighbor : getNeighbors(currentNode)) {
                // Skip if the neighbor is an obstacle, not ground, or already in closed list
                if (neighbor.isObstacle() || !neighbor.isGround() || closedList.contains(neighbor)) continue;

                // Calculate tentative gCost for the neighbor
                double tentativeGCost = currentNode.getGCost() + calculateHeuristic(currentNode, neighbor);
                // If this path to neighbor is better or neighbor is not in open list
                if (tentativeGCost < neighbor.getGCost() || !openList.contains(neighbor)) {
                    // Update costs and parent for the neighbor
                    neighbor.setGCost(tentativeGCost);
                    neighbor.setHCost(calculateHeuristic(neighbor, end));
                    neighbor.setParent(currentNode);
                    // Add neighbor to open list if not already present
                    if (!openList.contains(neighbor)) {
                        openList.add(neighbor);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    // Method to reconstruct the path from end node to start node
    private List<Node> reconstructPath(Node end) {
        List<Node> path = new ArrayList<>();
        for (Node at = end; at != null; at = at.getParent()) {
            path.add(at);
        }
        Collections.reverse(path); // Reverse the path to get it from start to end
        return path;
    }

    // Heuristic function to estimate the cost from node a to node b
    private double calculateHeuristic(Node a, Node b) {
        // Using Manhattan distance
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    // Method to get the neighbors of a given node
    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int[][] directions = { {0, -1, 0}, {0, 1, 0}, {-1, 0, 0}, {1, 0, 0} };

        // Check for neighbors in the same layer
        for (int[] direction : directions) {
            int newX = node.getX() + direction[0];
            int newY = node.getY() + direction[1];
            int newZ = node.getZ();
            if (newX >= 0 && newX < grid.getWidth() && newY >= 0 && newY < grid.getHeight()) {
                Node neighbor = grid.getNode(newX, newY, newZ);
                if (neighbor.isGround()) {
                    neighbors.add(neighbor);
                }
            }
        }

        // Check for staircases and add target layer nodes as neighbors
        if (node.isStair()) {
            neighbors.add(grid.getNode(node.getX(), node.getY(), node.getTargetLayer()));
        }

        return neighbors;
    }
}
