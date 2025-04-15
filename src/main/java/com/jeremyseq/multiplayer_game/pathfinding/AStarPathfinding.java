package com.jeremyseq.multiplayer_game.pathfinding;

import java.util.*;
import java.util.List;

public class AStarPathfinding {
    private final Grid grid;

    // Priority queue to store open nodes, sorted by their fCost
    private final PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));

    private final Set<Node> closedList = new HashSet<>();

    public AStarPathfinding(Grid grid) {
        this.grid = grid;
    }

    /**
     * resets all node's parents in preparation for more pathfinding
     */
    public void resetGrid() {
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                for (int z = 0; z < grid.getLayers(); z++) {
                    grid.getNode(x, y, z).setParent(null);
                }
            }
        }
    }

    public List<Node> findPath(Node start, Node end) {
        return findPath(start, end, true);
    }

    public List<Node> findPath(Node start, Node end, boolean diagonalMovement) {
        this.resetGrid();
        openList.clear();
        closedList.clear();

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
                double tentativeGCost = currentNode.getGCost() + getMovementCost(currentNode, neighbor);
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
        double dx = Math.abs(a.getX() - b.getX());
        double dy = Math.abs(a.getY() - b.getY());
        return Math.max(dx, dy) + (Math.sqrt(2) - 1) * Math.min(dx, dy);
    }

    private double getMovementCost(Node a, Node b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());

        if (dx == 1 && dy == 1) {
            return Math.sqrt(2); // diagonal
        } else if ((dx == 1 && dy == 0) || (dx == 0 && dy == 1)) {
            return 1.0; // cardinal
        } else {
            return 0.0; // shouldn't happen, but fallback
        }
    }


    // Method to get the neighbors of a given node
    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int[][] directions = {
                {0, -1, 0},
                {0, 1, 0},
                {-1, 0, 0},
                {1, 0, 0},
                {1, 1, 0},
                {-1, -1, 0},
                {-1, 1, 0},
                {1, -1, 0}
        };

        // Check for neighbors in the same layer
        for (int[] direction : directions) {
            int dx = direction[0];
            int dy = direction[1];
            int newX = node.getX() + dx;
            int newY = node.getY() + dy;
            int newZ = node.getZ();

            if (newX < 0 || newX >= grid.getWidth() || newY < 0 || newY >= grid.getHeight()) continue;

            Node neighbor = grid.getNode(newX, newY, newZ);

            // diagonal clipping prevention
            if (dx != 0 && dy != 0) {
                Node horizontal = grid.getNode(node.getX() + dx, node.getY(), node.getZ());
                Node vertical = grid.getNode(node.getX(), node.getY() + dy, node.getZ());
                if (horizontal.isObstacle() || vertical.isObstacle() || !horizontal.isGround() || !vertical.isGround()) {
                    continue; // can't cut through corners
                }
            }

            if (neighbor.isGround()) {
                neighbors.add(neighbor);
            }
        }


        // Check for staircases and add target layer nodes as neighbors
        if (node.isStair()) {
            neighbors.add(grid.getNode(node.getX(), node.getY(), node.getTargetLayer()));
        }

        return neighbors;
    }
}
