package main.java.com.jeremyseq.multiplayer_game.client;

import main.java.com.jeremyseq.multiplayer_game.common.AttackState;
import main.java.com.jeremyseq.multiplayer_game.common.level.Tile;
import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientPlayer {
    public final SpriteRenderer spriteRenderer = new SpriteRenderer(
            "/TinySwordsPack/Factions/Knights/Troops/Warrior/Blue/Warrior_Blue.png",
            8, 6, 192, 140);
    public final String username;
    public final Game game;
    public Vec2 position;
    private boolean flipped;
    public Vec2 deltaMovement = new Vec2(0, 0);
    public AttackState attacking = AttackState.FALSE;

    public int currentLayer = 1;
    private boolean onStairs = false;
    private final int walkHitboxWidth = 40;
    private final int walkHitboxHeight = 20;
    private final int walkHitboxHeightOffset = 20;

    public ClientPlayer(Game game, String username, Vec2 position) {
        this.game = game;
        this.username = username;
        this.position = position;
    }

    public void draw(Graphics g, ImageObserver imageObserver) {

        int animation = 0;

        if (this == game.clientPlayer) {
            g.setColor(Color.WHITE);
            if (game.keyHandler.leftPressed) {
                animation = 1;
                flipped = true;
            } else if (game.keyHandler.rightPressed) {
                animation = 1;
                flipped = false;
            } else if (game.keyHandler.upPressed) {
                animation = 1;
            } else if (game.keyHandler.downPressed) {
                animation = 1;
            }

        } else {
            g.setColor(Color.RED);
            if (deltaMovement.x != 0 || deltaMovement.y != 0) {
                animation = 1;
            }
            if (deltaMovement.x < 0) {
                flipped = true;
            } else if (deltaMovement.x > 0) {
                flipped = false;
            }
        }

        if (attacking != AttackState.FALSE) {
            animation = 2;
            if (attacking == AttackState.UP) {
                animation = 6;
            } else if (attacking == AttackState.DOWN) {
                animation = 4;
            } else if (attacking == AttackState.LEFT) {
                flipped = true;
            } else if (attacking == AttackState.RIGHT) {
                flipped = false;
            }
        }

        // draw animation
        Vec2 renderPos = game.getRenderPositionFromWorldPosition(position);
        boolean finished = this.spriteRenderer.drawAnimation(g, imageObserver, animation, (int) renderPos.x, (int) renderPos.y, flipped);

        if (finished && attacking != AttackState.FALSE) {
            attacking = AttackState.FALSE;
        }

        // Draw hitbox
//        g.drawRect((int) (renderPos.x-walkHitboxWidth/2), (int) (renderPos.y-walkHitboxHeight/2 + walkHitboxHeightOffset), walkHitboxWidth, walkHitboxHeight);

        // draw name
        g.setFont(new Font("Jetbrains Mono", Font.PLAIN, 16));
        Rectangle2D bounds = g.getFont().getStringBounds(username, g.getFontMetrics().getFontRenderContext());
        g.drawString(username, (int) ((int) renderPos.x - bounds.getWidth()/2), (int) ((int) renderPos.y + bounds.getHeight() + 25));
    }

    public void attack(AttackState attackSide) {
        this.deltaMovement = new Vec2(0, 0);
        attacking = attackSide;
    }

    public void tick() {
        if (this.game.level != null) {
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

                // check if player is on stairs, if they are, set onStairs to true
                if (findTilesAtPosition(this.position.add(new Vec2(0, walkHitboxHeightOffset))).stream().anyMatch((streamTile) -> streamTile.tilemap.equals("elevation") && streamTile.j == 7)) {
                    this.onStairs = true;
                } else if (onStairs) {
                    // if we move to a position that has a tile from the layer above, we set onStairs to false and we increase currentLayer
                    if (this.currentLayer + 1 <= numberOfLayers && !findTilesAtPositionInLayer(this.position.add(new Vec2(0, walkHitboxHeightOffset)), String.valueOf(this.currentLayer + 1)).isEmpty()) {
                        // we are on the layer above
                        System.out.println("we went up to layer " + (this.currentLayer + 1));
                        this.onStairs = false;
                        this.currentLayer = currentLayer + 1;
                    }
                    // this would happen if we moved from the current layer to the stairs and then back to the current layer
                    else if (!findTilesAtPositionInLayer(this.position.add(new Vec2(0, walkHitboxHeightOffset)), String.valueOf(this.currentLayer)).isEmpty()) {
                        this.onStairs = false;
                        System.out.println("You had a moment of indecision");
                    }
                    // if we move to a position that has a tile from the layer below, we set onStairs to false and we decrease currentLayer
                    else if (this.currentLayer - 1 > 0 && !findTilesAtPositionInLayer(this.position.add(new Vec2(0, walkHitboxHeightOffset)), String.valueOf(this.currentLayer - 1)).isEmpty()) {
                        // we are on the layer below
                        System.out.println("we went down to layer " + (this.currentLayer - 1));
                        this.onStairs = false;
                        this.currentLayer = currentLayer - 1;
                    }
                    else if (findTilesAtPosition(this.position.add(new Vec2(0, walkHitboxHeightOffset))).stream().noneMatch((streamTile) -> streamTile.tilemap.equals("elevation") && streamTile.j == 7)) {
                        onStairs = false;
                    }
                }

                // don't do collisions for layers beneath current layer and the "in-between" sublayer below it
                if (i < this.currentLayer*2-1 - 1) {
                    continue;
                }
                // don't do collisions for tiles in current layer
                if (layer.equals(String.valueOf(this.currentLayer))) {
                    continue;
                }

                if (onStairs && (layer.equals(String.valueOf(this.currentLayer - 1)) || layer.equals(String.valueOf(this.currentLayer + 1)))) {
                    continue;
                }

                if (this.game.level.tiles.get(layer) == null) {
                    continue;
                }
                for (Tile tile : this.game.level.tiles.get(layer)) {
                    if (tile.tilemap.equals("elevation") && !(tile.j == 3 || tile.j == 5)) {
                        continue;
                    }
                    handleCollision(tile);
                }

                // outlines the current layer with tiles which will act as barriers so the player doesn't walk off the layer
                for (Tile tile : outlineCurrentLayer()) {
                    handleCollision(tile);
                }
            }
        }
    }


    /**
     * @return returns a list of tiles that outline the current layer
     */
    public ArrayList<Tile> outlineCurrentLayer() {
        ArrayList<Tile> outlinedTiles = new ArrayList<>();
        ArrayList<Vec2> directions = new ArrayList<>();
        directions.add(new Vec2(0, 1));
        directions.add(new Vec2(1, 0));
        directions.add(new Vec2(0, -1));
        directions.add(new Vec2(-1, 0));

        for (Tile tile : this.game.level.tiles.get(String.valueOf(this.currentLayer))) {
            for (Vec2 direction : directions) {
                Vec2 neighbor = new Vec2(tile.x, tile.y).add(direction);
                if (this.game.level.tiles.get(String.valueOf(this.currentLayer)).stream().anyMatch((streamTile) -> streamTile.x == neighbor.x && streamTile.y == neighbor.y)) {
                    continue;
                }
                if (combineTileLists(this.game.level.tiles).stream().anyMatch((streamTile) -> streamTile.x == neighbor.x && streamTile.y == neighbor.y && streamTile.tilemap.equals("elevation") && streamTile.j == 7)) {
                    continue;
                }
                outlinedTiles.add(new Tile((int) neighbor.x, (int) neighbor.y, "elevation", 2, 3));
            }
        }
        return outlinedTiles;
    }

    public void handleCollision(Tile tile) {
        int tileSize = this.game.levelRenderer.drawSize;
        if (isColliding(tile, this.position)) {
            if (position.y - walkHitboxHeight/2f + walkHitboxHeightOffset < tile.y*tileSize) { // if top of the hitbox is above the top of the tile
                position.y = tile.y*tileSize - walkHitboxHeight/2f - walkHitboxHeightOffset; // moves the bottom of hitbox to top of tile
            } else if (position.y + walkHitboxHeight/2f + walkHitboxHeightOffset > tile.y*tileSize + tileSize) { // if bottom of the hitbox is below the bottom of the tile
                position.y = tile.y*tileSize + tileSize + walkHitboxHeight/2f - walkHitboxHeightOffset; // moves the top of hitbox to bottom of tile
            }
        }
        if (isColliding(tile, this.position)) {
            if (this.position.x-walkHitboxWidth/2f < tile.x*tileSize) { // if left side of hitbox is left of the left side of the tile
                this.position.x = tile.x*tileSize - walkHitboxWidth/2f; // moves right side of hitbox to left side of tile
            } else if (this.position.x + walkHitboxWidth/2f > tile.x*tileSize + tileSize) { // if right side of hitbox is right of the tile
                position.x = tile.x * tileSize + tileSize + walkHitboxWidth/2f; // moves left side of hitbox to right side of tlie
            }
        }
    }

    public ArrayList<Tile> findTilesAtPosition(Vec2 pos) {
        ArrayList<Tile> tiles = combineTileLists(this.game.level.tiles);
        ArrayList<Tile> tilesWithPlayer = new ArrayList<>();

        for (Tile tile : tiles) {
            if (isPosInTile(tile, (int) pos.x, (int) pos.y)) {
                tilesWithPlayer.add(tile);
            }
        }
        return tilesWithPlayer;
    }
    public ArrayList<Tile> findTilesAtPositionInLayer(Vec2 pos, String layer) {
        ArrayList<Tile> tiles = this.game.level.tiles.get(layer);
        ArrayList<Tile> tilesWithPlayer = new ArrayList<>();
        if (tiles == null) {
            return null;
        }
        for (Tile tile : tiles) {
            if (isPosInTile(tile, (int) pos.x, (int) pos.y)) {
                tilesWithPlayer.add(tile);
            }
        }
        return tilesWithPlayer;
    }

    public boolean isColliding(Tile tile, Vec2 playerPos) {
        if (tile.tilemap.equals("elevation") && tile.j == 7) {
            return false;
        }
        int tileDrawSize = this.game.levelRenderer.drawSize;
        int width = walkHitboxWidth;
        int height = walkHitboxHeight;
        return tile.x*tileDrawSize < playerPos.x+width/2f &&
                tile.x*tileDrawSize + tileDrawSize > playerPos.x-width/2f &&
                tile.y*tileDrawSize < playerPos.y+height/2f+walkHitboxHeightOffset &&
                tile.y*tileDrawSize + tileDrawSize > playerPos.y-height/2f+walkHitboxHeightOffset;
    }

    public boolean isPosInTile(Tile tile, int posX, int posY) {

        return tile.x*this.game.levelRenderer.drawSize <= posX && posX < tile.x*this.game.levelRenderer.drawSize + this.game.levelRenderer.drawSize &&
                tile.y*this.game.levelRenderer.drawSize <= posY && posY < tile.y*this.game.levelRenderer.drawSize + this.game.levelRenderer.drawSize;
    }

    public static ArrayList<Tile> combineTileLists(HashMap<String, ArrayList<Tile>> tileMap) {
        ArrayList<Tile> combinedList = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Tile>> entry : tileMap.entrySet()) {
            combinedList.addAll(entry.getValue());
        }
        return combinedList;
    }
}
