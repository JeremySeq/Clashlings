package main.java.com.jeremyseq.multiplayer_game.client;

import main.java.com.jeremyseq.multiplayer_game.common.level.Building;
import main.java.com.jeremyseq.multiplayer_game.common.level.BuildingState;
import main.java.com.jeremyseq.multiplayer_game.common.level.BuildingType;
import main.java.com.jeremyseq.multiplayer_game.common.level.Tile;
import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class LevelRenderer {

    private final Game game;
    public HashMap<String, BufferedImage> tilemaps = new HashMap<>();
    public HashMap<BuildingType, BufferedImage> buildings = new HashMap<>();
    public HashMap<BuildingType, BufferedImage> contruction_buildings = new HashMap<>();
    public HashMap<BuildingType, BufferedImage> destroyed_buildings = new HashMap<>();

    public int drawSize = 48;
    int tileSize = 64;
    private int frameCounter = 0; // counts frames
    private int animationFrame = 0; // frame that the animations are on

    public LevelRenderer(Game game) {
        this.game = game;
        loadImages();
    }

    public void loadImages() {
        try {
            tilemaps.put("flat", ImageIO.read(Objects.requireNonNull(getClass().getResource("/TinySwordsPack/Terrain/Ground/Tilemap_Flat.png"))));
            tilemaps.put("elevation", ImageIO.read(Objects.requireNonNull(getClass().getResource("/TinySwordsPack/Terrain/Ground/Tilemap_Elevation.png"))));
            tilemaps.put("water", ImageIO.read(Objects.requireNonNull(getClass().getResource("/TinySwordsPack/Terrain/Water/Water.png"))));
            tilemaps.put("foam", ImageIO.read(Objects.requireNonNull(getClass().getResource("/TinySwordsPack/Terrain/Water/Foam/Foam.png"))));
            tilemaps.put("shadows", ImageIO.read(Objects.requireNonNull(getClass().getResource("/TinySwordsPack/Terrain/Ground/Shadows.png"))));

            for (BuildingType buildingType : BuildingType.values()) {
                buildings.put(buildingType, ImageIO.read(Objects.requireNonNull(getClass().getResource(buildingType.imageFileName))));
            }
            for (BuildingType buildingType : BuildingType.values()) {
                contruction_buildings.put(buildingType, ImageIO.read(Objects.requireNonNull(getClass().getResource(buildingType.constructionImageFileName))));
            }
            for (BuildingType buildingType : BuildingType.values()) {
                destroyed_buildings.put(buildingType, ImageIO.read(Objects.requireNonNull(getClass().getResource(buildingType.destroyedImageFileName))));
            }

        } catch (IOException exc) {
            System.out.println("Error opening image file: " + exc.getMessage());
        }
    }

    public void draw(Graphics g, ImageObserver imageObserver) {

        frameCounter++;
        if (frameCounter >= 3) {
            animationFrame++;
            frameCounter = 0;
            if (animationFrame >= 8) {
                animationFrame = 0;
            }
        }

        for (int i = 0; i < Game.WIDTH/drawSize + 1; i++) {
            for (int j = 0; j < Game.HEIGHT/drawSize + 1; j++) {
                drawTile(g, imageObserver, i*drawSize, j*drawSize, tilemaps.get("water"), 0, 0, true);
            }
        }

        // draw foam around outline of layer 1
        drawFoam(g, imageObserver);

        if (game.level != null) {
            int numberOfLayers = this.game.level.metadata.layers;
            for (int i = 1; i <= numberOfLayers*2 - 1; i++) {
                String layer;
                String prev;
                String next;
                if (i % 2 == 1) {
                    layer = String.valueOf(i - (i-1)/2);
                } else {
                    prev = String.valueOf((i-1) - (i-2)/2);
                    next = String.valueOf((i+1) - (i)/2);
                    layer = prev + "-" + next;
                }
                ArrayList<Tile> tileList = game.level.tiles.get(layer);
                if (tileList == null || tileList.isEmpty()) {
                    continue;
                }

                if (layer.contains("-")) {
                    drawShadowUnderElevationForLayer(g, imageObserver, layer);
                }

                for (Tile tile : tileList) {
                    drawTile(g, imageObserver, tile.x * drawSize, tile.y * drawSize, tilemaps.get(tile.tilemap), tile.i, tile.j);
                }

                ArrayList<Building> buildingList = game.level.buildings.get(layer);
                if (buildingList != null && !buildingList.isEmpty()) {
                    for (Building building : buildingList) {
                        drawBuilding(g, imageObserver, building);
                    }
                }

                if (layer.contains("-")) {
                    drawParticlesOnElevation(g, imageObserver, layer, i == 2);
                }
            }
        }
    }

    /**
     * draws foam around outline of layer 1
     */
    public void drawFoam(Graphics g, ImageObserver imageObserver) {
        ArrayList<Tile> outlineLayerTiles = this.game.level.getOuterTilesInLayer("1");
        for (Tile tile : outlineLayerTiles) {
            drawTile(g, imageObserver, (tile.x)*drawSize, (tile.y)*drawSize, tilemaps.get("foam"), 1+3*animationFrame, 1);

            drawTile(g, imageObserver, (tile.x)*drawSize, (tile.y-1)*drawSize, tilemaps.get("foam"), 1+3*animationFrame, 0);
            drawTile(g, imageObserver, (tile.x+1)*drawSize, (tile.y)*drawSize, tilemaps.get("foam"), 2+3*animationFrame, 1);
            drawTile(g, imageObserver, (tile.x)*drawSize, (tile.y+1)*drawSize, tilemaps.get("foam"), 1+3*animationFrame, 2);
            drawTile(g, imageObserver, (tile.x-1)*drawSize, (tile.y)*drawSize, tilemaps.get("foam"), 3*animationFrame, 1);
        }
    }

    public void drawBuilding(Graphics g, ImageObserver imageObserver, Building building) {
        Vec2 renderPos = new Vec2(building.x*drawSize, building.y*drawSize);
        renderPos = game.getRenderPositionFromWorldPosition(renderPos);
        int x2 = (int) renderPos.x;
        int y2 = (int) renderPos.y;
        BufferedImage image;
        if (building.state == BuildingState.BUILT) {
            image = buildings.get(building.type);
        } else if (building.state == BuildingState.CONSTRUCTION) {
            image = contruction_buildings.get(building.type);
        } else {
            image = destroyed_buildings.get(building.type);
        }
        g.drawImage(
                image,
                x2, y2, drawSize*building.type.tileWidth, drawSize*building.type.tileHeight,
                imageObserver
        );
    }

    /**
     * draws tile shadows for a particular sublayer
     */
    public void drawShadowUnderElevationForLayer(Graphics g, ImageObserver imageObserver, String layer) {
        for (Tile tile : game.level.tiles.get(layer)) {
            if (tile.tilemap.equals("elevation") && !(tile.j == 0 || tile.j == 4)) {
                drawTile(g, imageObserver, (tile.x)*drawSize, (tile.y)*drawSize, tilemaps.get("shadows"), 1, 1);

                drawTile(g, imageObserver, (tile.x)*drawSize, (tile.y-1)*drawSize, tilemaps.get("shadows"), 1, 0);
                drawTile(g, imageObserver, (tile.x+1)*drawSize, (tile.y)*drawSize, tilemaps.get("shadows"), 2, 1);
                drawTile(g, imageObserver, (tile.x)*drawSize, (tile.y+1)*drawSize, tilemaps.get("shadows"), 1, 2);
                drawTile(g, imageObserver, (tile.x-1)*drawSize, (tile.y)*drawSize, tilemaps.get("shadows"), 0, 1);
            }
        }
    }


    /**
     * draws particles on sublayer
     * @param isSand if true, draws sand particles instead of grass particles, should be used if the sublayer is on sand
     */
    public void drawParticlesOnElevation(Graphics g, ImageObserver imageObserver, String elevationLayer, boolean isSand) {
        for (Tile tile : game.level.tiles.get(elevationLayer)) {
            if (tile.tilemap.equals("elevation") && (tile.j == 3 || tile.j == 5 || tile.j == 7)) {
                drawTile(g, imageObserver, (tile.x)*drawSize, (tile.y)*drawSize, tilemaps.get("flat"), isSand ? 9 : 4, 0);
            }
        }
    }

    /**
     * @param x x-coordinate to draw on screen
     * @param y y-coordinate to draw on screen
     * @param tilemap tilemap buffered image
     * @param i tile on tilemap to draw
     * @param j tile on tilemap to draw
     */
    public void drawTile(Graphics g, ImageObserver imageObserver, int x, int y, BufferedImage tilemap, int i, int j) {
        drawTile(g, imageObserver, x, y, tilemap, i, j, false);
    }

    /**
     * @param x x-coordinate to draw on screen
     * @param y y-coordinate to draw on screen
     * @param tilemap tilemap buffered image
     * @param i tile on tilemap to draw
     * @param j tile on tilemap to draw
     * @param ignoreScrolling if this is true, renders the tile without converting to world position, meaning
     *                        if the player moves, this tile stays in the same spot on their screen
     */
    public void drawTile(Graphics g, ImageObserver imageObserver, int x, int y, BufferedImage tilemap, int i, int j, boolean ignoreScrolling) {
        Vec2 renderPos = new Vec2(x, y);
        if (!ignoreScrolling) {
            renderPos = game.getRenderPositionFromWorldPosition(renderPos);
        }
        int x2 = (int) renderPos.x;
        int y2 = (int) renderPos.y;
        g.drawImage(
                tilemap,
                x2, y2, x2 + drawSize, y2 + drawSize,
                tileSize*i, tileSize*j, tileSize*i + tileSize, tileSize*j + tileSize,
                imageObserver
        );
    }
}
