package com.jeremyseq.clashlings.common.level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jeremyseq.clashlings.client.LevelRenderer;
import com.jeremyseq.clashlings.common.Vec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Level {

    public static final int WORLD_TILE_SIZE = 48;
    public LevelMetadata metadata;
    public HashMap<String, ArrayList<Tile>> tiles;
    public HashMap<String, ArrayList<Building>> buildings;

    public Level(LevelMetadata metadata, HashMap<String, ArrayList<Tile>> tiles, HashMap<String, ArrayList<Building>> buildings) {
        this.metadata = metadata;
        this.tiles = tiles;
        this.buildings = buildings;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public ArrayList<Tile> outlineLayer(String layer) {
        return outlineLayer(layer, true);
    }

    /**
     * @param doNotOutlineOnStairs if true, will not put a tile on top of stairs, no matter what layer they are on,
     *                             probably only going to be used for collisions
     * @return returns a list of tiles that outline the current layer
     */
    public ArrayList<Tile> outlineLayer(String layer, boolean doNotOutlineOnStairs) {
        ArrayList<Tile> outlinedTiles = new ArrayList<>();
        ArrayList<Vec2> directions = new ArrayList<>();
        directions.add(new Vec2(0, 1));
        directions.add(new Vec2(1, 0));
        directions.add(new Vec2(0, -1));
        directions.add(new Vec2(-1, 0));

        for (Tile tile : tiles.get(String.valueOf(layer))) {
            for (Vec2 direction : directions) {
                Vec2 neighbor = new Vec2(tile.x, tile.y).add(direction);
                if (tiles.get(String.valueOf(layer)).stream().anyMatch((streamTile) -> streamTile.x == neighbor.x && streamTile.y == neighbor.y)) {
                    continue;
                }
                if (doNotOutlineOnStairs && combineTileLists(tiles).stream().anyMatch((streamTile) -> streamTile.x == neighbor.x && streamTile.y == neighbor.y && streamTile.tilemap.equals("elevation") && streamTile.j == 7)) {
                    continue;
                }
                outlinedTiles.add(new Tile((int) neighbor.x, (int) neighbor.y, "elevation", 2, 3));
            }
        }
        return outlinedTiles;
    }

    /**
     * @return returns tiles in the given layer that are an edge tile
     */
    public ArrayList<Tile> getOuterTilesInLayer(String layer) {
        ArrayList<Tile> outerTiles = new ArrayList<>();
        ArrayList<Vec2> directions = new ArrayList<>();
        directions.add(new Vec2(0, 1));
        directions.add(new Vec2(1, 0));
        directions.add(new Vec2(0, -1));
        directions.add(new Vec2(-1, 0));

        for (Tile tile : tiles.get(String.valueOf(layer))) {
            for (Vec2 direction : directions) {
                Vec2 neighbor = new Vec2(tile.x, tile.y).add(direction);
                if (tiles.get(String.valueOf(layer)).stream().anyMatch((streamTile) -> streamTile.x == neighbor.x && streamTile.y == neighbor.y)) {
                    continue;
                }
                outerTiles.add(tile);
            }
        }
        return outerTiles;
    }

    public static ArrayList<Tile> combineTileLists(HashMap<String, ArrayList<Tile>> tileMap) {
        ArrayList<Tile> combinedList = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Tile>> entry : tileMap.entrySet()) {
            combinedList.addAll(entry.getValue());
        }
        return combinedList;
    }

    public ArrayList<Tile> findTilesAtPosition(Vec2 pos) {
        ArrayList<Tile> tileList = Level.combineTileLists(tiles);
        ArrayList<Tile> tilesWithPlayer = new ArrayList<>();

        for (Tile tile : tileList) {
            if (isPosInTile(tile, (int) pos.x, (int) pos.y, LevelRenderer.DRAW_SIZE)) {
                tilesWithPlayer.add(tile);
            }
        }
        return tilesWithPlayer;
    }

    public ArrayList<Tile> findTilesAtPositionInLayer(Vec2 pos, String layer) {
        ArrayList<Tile> tileList = tiles.get(layer);
        ArrayList<Tile> tilesWithPlayer = new ArrayList<>();
        if (tileList == null) {
            return null;
        }
        for (Tile tile : tileList) {
            if (isPosInTile(tile, (int) pos.x, (int) pos.y, LevelRenderer.DRAW_SIZE)) {
                tilesWithPlayer.add(tile);
            }
        }
        return tilesWithPlayer;
    }

    public boolean isPosInTile(Tile tile, int posX, int posY, int tileSize) {
        return tile.x*tileSize <= posX && posX < tile.x*tileSize + tileSize &&
                tile.y*tileSize <= posY && posY < tile.y*tileSize + tileSize;
    }


    public Vec2 getTilePositionFromWorldPosition(Vec2 worldPos, int tileSize) {
        // convert world position to tile position
        Vec2 tilePos = new Vec2(worldPos.x / tileSize, worldPos.y / tileSize);
        // if coordinates are negative we want to floor not truncate, meaning -7.3 -> -8
        tilePos.x = tilePos.x < 0 ? (float) Math.floor(tilePos.x) : tilePos.x;
        tilePos.y = tilePos.y < 0 ? (float) Math.floor(tilePos.y) : tilePos.y;
        return new Vec2((int) tilePos.x, (int) tilePos.y);
    }

    public Vec2 getWorldPositionFromTilePosition(Vec2 tilePos, int tileSize) {
        return new Vec2(tilePos.x * tileSize, tilePos.y * tileSize);
    }
}
