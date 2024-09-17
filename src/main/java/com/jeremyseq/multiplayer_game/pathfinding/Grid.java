package main.java.com.jeremyseq.multiplayer_game.pathfinding;


import main.java.com.jeremyseq.multiplayer_game.common.Vec2;
import main.java.com.jeremyseq.multiplayer_game.common.level.Level;
import main.java.com.jeremyseq.multiplayer_game.common.level.Tile;

import java.util.ArrayList;

public class Grid {
    private final Node[][][] nodes;
    private final int width;
    private final int height;
    private final int layers;
    private final int xOffset;
    private final int yOffset;

    public Grid(int width, int height, int layers) {
        this.width = width;
        this.height = height;
        this.layers = layers;
        this.xOffset = 0;
        this.yOffset = 0;
        nodes = new Node[width][height][layers];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < layers; z++) {
                    nodes[x][y][z] = new Node(x, y, z);
                }
            }
        }
    }

    public Grid(int width, int height, int layers, int xOffset, int yOffset) {
        this.width = width;
        this.height = height;
        this.layers = layers;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        nodes = new Node[width][height][layers];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < layers; z++) {
                    nodes[x][y][z] = new Node(x, y, z);
                }
            }
        }
    }

    public static Grid levelToGrid(Level level) {

        // find min x and y coordinates in level
        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;
        for (String layer : level.tiles.keySet()) {
            for (Tile tile : level.tiles.get(layer)) {
                if (tile.x < minX) {
                    minX = tile.x;
                }
                if (tile.y < minY) {
                    minY = tile.y;
                }
                if (tile.x > maxX) {
                    maxX = tile.x;
                }
                if (tile.y > maxY) {
                    maxY = tile.y;
                }
            }
        }

        // create grid with the min x and y being 0, 0
        Grid grid = new Grid(maxX - minX + 1, maxY - minY + 1, level.metadata.layers, minX, minY);

        int numberOfLayers = level.metadata.layers;
        for (int i = 1; i <= numberOfLayers*2 - 1; i++) {
            String layer;
            String prev = "";
            String next = "";
            if (i % 2 == 1) {
                layer = String.valueOf(i - (i - 1) / 2);
            } else {
                prev = String.valueOf((i - 1) - (i - 2) / 2);
                next = String.valueOf((i + 1) - (i) / 2);
                layer = prev + "-" + next;
            }
            ArrayList<Tile> tileList = level.tiles.get(layer);
            if (tileList == null || tileList.isEmpty()) {
                continue;
            }

            for (Tile tile : tileList) {
                if (i % 2 == 1 && tile.tilemap.equals("flat")) {
                    grid.setGround(tile.x - minX, tile.y - minY, Integer.parseInt(layer)-1);
                }
                if (i % 2 == 0 && tile.tilemap.equals("elevation") && tile.j == 7) {
                    grid.setGround(tile.x - minX, tile.y - minY, Integer.parseInt(prev)-1);
                    grid.setGround(tile.x - minX, tile.y - minY, Integer.parseInt(next)-1);
                    grid.setStair(tile.x - minX, tile.y - minY, Integer.parseInt(prev)-1, Integer.parseInt(next)-1);
                    grid.setStair(tile.x - minX, tile.y - minY, Integer.parseInt(next)-1, Integer.parseInt(prev)-1);
                } else if (i % 2 == 0 && tile.tilemap.equals("elevation")) {
                    grid.setObstacle(tile.x - minX, tile.y - minY, Integer.parseInt(prev)-1);
                }
            }
        }

        return grid;
    }

    public Vec2 gridPosToTilePos(int x, int y) {
        return new Vec2(x + xOffset, y + yOffset);
    }

    public Vec2 tilePosToGridPos(int x, int y) {
        return new Vec2(x - xOffset, y - yOffset);
    }

    public int getxOffset() {
        return xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public Node getNode(int x, int y, int z) {
        return nodes[x][y][z];
    }

    public void setObstacle(int x, int y, int z) {
        nodes[x][y][z].setObstacle(true);
    }

    public void setStair(int x, int y, int z, int targetLayer) {
        nodes[x][y][z].setStair(true, targetLayer);
    }

    public void setGround(int x, int y, int z) {
        nodes[x][y][z].setGround(true);
    }

    public void printGrid() {
        for (int z = 0; z < layers; z++) {
            System.out.println("Layer " + z + ":");
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (nodes[x][y][z].isObstacle()) {
                        System.out.print("X ");
                    } else if (nodes[x][y][z].isStair()) {
                        System.out.print("S ");
                    } else if (nodes[x][y][z].isGround()) {
                        System.out.print(". ");
                    } else {
                        System.out.print("  ");
                    }
                }
                System.out.println();
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLayers() {
        return layers;
    }
}
