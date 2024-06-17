package main.java.com.jeremyseq.multiplayer_game.client;

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

    public int drawSize = 48;
    int tileSize = 64;


    public LevelRenderer(Game game) {
        this.game = game;
        loadImages();
    }

    public void loadImages() {
        try {
            tilemaps.put("flat", ImageIO.read(Objects.requireNonNull(getClass().getResource("/TinySwordsPack/Terrain/Ground/Tilemap_Flat.png"))));
            tilemaps.put("elevation", ImageIO.read(Objects.requireNonNull(getClass().getResource("/TinySwordsPack/Terrain/Ground/Tilemap_Elevation.png"))));
            tilemaps.put("water", ImageIO.read(Objects.requireNonNull(getClass().getResource("/TinySwordsPack/Terrain/Water/Water.png"))));
        } catch (IOException exc) {
            System.out.println("Error opening image file: " + exc.getMessage());
        }
    }

    public void draw(Graphics g, ImageObserver imageObserver) {

        for (int i = 0; i < Game.WIDTH/drawSize + drawSize; i++) {
            for (int j = 0; j < Game.HEIGHT/drawSize + drawSize; j++) {
                drawTile(g, imageObserver, i*drawSize, j*drawSize, tilemaps.get("water"), 0, 0, true);
            }
        }

        if (game.level != null) {
            int numberOfLayers = this.game.level.metadata.layers;
            for (int i = 1; i <= numberOfLayers*2 - 1; i++) {
                String layer;
                if (i % 2 == 1) {
                    layer = String.valueOf(i - (i-1)/2);
                } else {
                    String prev = String.valueOf((i-1) - (i-2)/2);
                    String next = String.valueOf((i+1) - (i)/2);
                    layer = prev + "-" + next;
                }
                ArrayList<Tile> tileList = game.level.tiles.get(layer);
                if (tileList == null || tileList.isEmpty()) {
                    continue;
                }
                for (Tile tile : tileList) {
                    drawTile(g, imageObserver, tile.x * drawSize, tile.y * drawSize, tilemaps.get(tile.tilemap), tile.i, tile.j);
                }
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
