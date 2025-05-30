package com.jeremyseq.clashlings.pathfinding;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        int width = 10;
        int height = 10;
        int layers = 3;
        Grid grid = new Grid(width, height, layers);

        // Set obstacles
        grid.setObstacle(4, 4, 0);
        grid.setObstacle(4, 5, 0);
        grid.setObstacle(4, 6, 0);
        grid.setObstacle(4, 7, 0);

        // Set ground nodes
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid.setGround(x, y, 0);
                grid.setGround(x, y, 1);
                grid.setGround(x, y, 2);
            }
        }

        // Set staircases
        grid.setStair(2, 2, 0, 1);
        grid.setStair(2, 2, 1, 0);
        grid.setStair(7, 7, 1, 2);
        grid.setStair(7, 7, 2, 1);

        Node start = grid.getNode(0, 0, 0);
        Node end = grid.getNode(9, 9, 2);

        AStarPathfinding aStar = new AStarPathfinding(grid);
        List<Node> path = aStar.findPath(start, end);

        grid.printGrid();
        System.out.println("Path:");
        for (Node node : path) {
            System.out.println("(" + node.getX() + ", " + node.getY() + ", " + node.getZ() + ")");
        }
    }
}
