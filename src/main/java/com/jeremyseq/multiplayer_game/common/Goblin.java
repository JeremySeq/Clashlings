package com.jeremyseq.multiplayer_game.common;

import com.jeremyseq.multiplayer_game.client.Game;
import com.jeremyseq.multiplayer_game.client.LevelRenderer;
import com.jeremyseq.multiplayer_game.client.SpriteRenderer;
import com.jeremyseq.multiplayer_game.client.sound.SoundPlayer;
import com.jeremyseq.multiplayer_game.common.level.Level;
import com.jeremyseq.multiplayer_game.common.level.Tile;
import com.jeremyseq.multiplayer_game.pathfinding.AStarPathfinding;
import com.jeremyseq.multiplayer_game.pathfinding.Grid;
import com.jeremyseq.multiplayer_game.pathfinding.Node;
import com.jeremyseq.multiplayer_game.server.ServerGame;
import com.jeremyseq.multiplayer_game.server.ServerPlayer;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

public class Goblin implements Hitbox {
    public Game game; // on client only
    public ServerGame serverGame; // on server only
    public long id;
    public Level level;
    public Vec2 position;
    public Grid grid;
    public ArrayList<Node> currentPath = new ArrayList<>();
    private Vec2 targetPos;
    public static final float MOVE_SPEED = 1.6f;
    private boolean flipped = false;
    public Vec2 deltaMovement = new Vec2(0, 0);

    private ServerPlayer closestPlayer = null;

    private Vec2 playerTargetPos = null;

    private boolean animateHurt = false;

    public static final int HEALTH = 20;

    public int health = HEALTH;

    private AStarPathfinding pathfinder = null;

    // for use on client
    public Goblin(Game game, Level level, Vec2 position) {
        this.game = game;
        this.level = level;
        this.position = position;
    }

    // for use on server
    public Goblin(ServerGame serverGame, Level level, Vec2 position) {
        this.serverGame = serverGame;
        this.level = level;
        this.position = position;
        this.initializePathfinding();
    }

    public final SpriteRenderer spriteRenderer = new SpriteRenderer(
            "/TinySwordsPack/Factions/Goblins/Troops/Torch/Red/Torch_Red.png",
            new int[]{7, 6, 6, 6, 6}, 192, 144);

    public void draw(Graphics g, ImageObserver imageObserver) {

        // draw pathfinding debugging, doesn't work anymore, because client never receives path from server
//        DebugRenderer.drawPathfinding(this.currentPath, this.game, g, this.grid);

        Vec2 renderPos = this.position;
        if (game != null) {
            renderPos = game.getRenderPositionFromWorldPosition(renderPos);
        }

        if (this.deltaMovement.equals(new Vec2(0, 0))) {
            spriteRenderer.drawAnimation(g, imageObserver, 0, (int) renderPos.x, (int) renderPos.y, flipped, animateHurt);
        } else {
            if (this.deltaMovement.x < 0) {
                flipped = true;
            } else if (this.deltaMovement.x > 0) {
                flipped = false;
            }

            spriteRenderer.drawAnimation(g, imageObserver, 1, (int) renderPos.x, (int) renderPos.y, flipped, animateHurt);
        }

        // draw hitbox
//        DebugRenderer.drawBox(this.game, g, (int) (this.position.x - this.getHitboxSize().x/2), (int) (this.position.y - this.getHitboxSize().y/2),
//                (int) (this.position.x + this.getHitboxSize().x/2), (int) (this.position.y + this.getHitboxSize().y/2));

        animateHurt = false;
    }

    public void initializePathfinding() {
        this.grid = Grid.levelToGrid(level);
        this.pathfinder = new AStarPathfinding(grid);
    }

    /**
     * Per-tick method to follow the path specified in currentPath.
     */
    public void followPath() {
        if (this.currentPath.isEmpty()) {
            this.deltaMovement = new Vec2(0, 0);
            return;
        }

        if (targetPos == null) {
            // Initialize target position
            Node node = this.currentPath.get(0);
            Vec2 tilePos = grid.gridPosToTilePos(node.getX(), node.getY());
            if (this.game != null) {
                targetPos = this.game.level.getWorldPositionFromTilePosition(tilePos, Level.WORLD_TILE_SIZE);
            } else if (this.serverGame != null) {
                targetPos = this.serverGame.level.getWorldPositionFromTilePosition(tilePos, Level.WORLD_TILE_SIZE);
            }
            targetPos = targetPos.add(new Vec2(Level.WORLD_TILE_SIZE / 2f, Level.WORLD_TILE_SIZE / 2f));
        }

//        System.out.println("nodes left in path: " + currentPath.size());

        if (targetPos != null && targetPos.equals(this.position)) {
            this.currentPath.remove(0);
            targetPos = null;
            followPath();
            return;
        }

        // Calculate the direction to the target position
        Vec2 direction = targetPos.subtract(this.position).normalize();

        // Calculate the movement step
        Vec2 step = direction.multiply(MOVE_SPEED);
        this.deltaMovement = step;

        // Move gradually towards the target position
        this.position = this.position.add(step);

        // Check if the enemy has reached the target position
        if (this.position.distance(targetPos) <= MOVE_SPEED) {
            // Snap to target position and remove the node from the path
            this.position = targetPos;
            this.currentPath.remove(0);

            // Set the next target position if there are more nodes in the path
            if (!this.currentPath.isEmpty()) {
                Node nextNode = this.currentPath.get(0);
                Vec2 tilePos = grid.gridPosToTilePos(nextNode.getX(), nextNode.getY());
                targetPos = new Vec2(tilePos.x * Level.WORLD_TILE_SIZE, tilePos.y * Level.WORLD_TILE_SIZE);
                targetPos = targetPos.add(new Vec2(Level.WORLD_TILE_SIZE / 2f, Level.WORLD_TILE_SIZE / 2f));
            } else {
                targetPos = null; // Clear target position when path is empty
            }
        }
    }

    /**
     * Per-tick method to follow the nearest player
     * @return true if follow player is complete (i.e. goblin is adjacent to player)
     */
    private boolean followPlayerGoal() {
        if (!this.serverGame.players.isEmpty()) {

            // update this.closestPlayer
            ServerPlayer newClosestPlayer = null;
            Double closestDist = null;
            for (ServerPlayer player : this.serverGame.players) {
                if (newClosestPlayer == null || player.pos.distance(newClosestPlayer.pos) < closestDist) {
                    newClosestPlayer = player;
                    closestDist = player.pos.distance(newClosestPlayer.pos);
                }
            }
            this.closestPlayer = newClosestPlayer;

            // get the tile position of the closest player
            Vec2 tilePos = this.level.getTilePositionFromWorldPosition(this.closestPlayer.pos, LevelRenderer.DRAW_SIZE);

            // if the goblin is one tile away from the player, stop following and return true
            Vec2 goblinTP = this.level.getTilePositionFromWorldPosition(this.position, LevelRenderer.DRAW_SIZE);
            if (goblinTP.distance(tilePos) <= 1) {
                // player is within attack distance, clear path
                this.currentPath.clear();
                return true;
            }

            // if the new target tile is different from the previous target tile, recalculate the path
            if (tilePos != playerTargetPos) {
                setPathToTile((int) tilePos.x, (int) tilePos.y);

                playerTargetPos = tilePos;
            }
        }

        return false;
    }

    public void tick() {
        // sets the path to follow the player
        boolean followPlayerResult = followPlayerGoal();

        // if goblin is next to player, attack
        if (followPlayerResult) {
            // TODO: Attack player
            System.out.println("Next to " + closestPlayer.username + ", attacking!");
            attackPlayerGoal();
        }

        // follows the set path
        followPath();
    }


    private void attackPlayerGoal() {

    }

    /**
     * Calculates the path to the given tile and updates currentPath.
     * This is an expensive operation and should be used sparingly
     */
    public void setPathToTile(int x, int y) {
        int[] nodeCoordinates = getNodeCoordinatesAtPosition(this.position);
        Node start = grid.getNode(nodeCoordinates[0], nodeCoordinates[1], nodeCoordinates[2]);
        int targetLayer = getLayerFromPosition(new Vec2(
                x * Level.WORLD_TILE_SIZE + Level.WORLD_TILE_SIZE/2f,
                y * Level.WORLD_TILE_SIZE + Level.WORLD_TILE_SIZE/2f
        ));
        x -= grid.getxOffset();
        y -= grid.getyOffset();
        Node end = grid.getNode(x, y, targetLayer-1);
        List<Node> path = pathfinder.findPath(start, end);
        this.currentPath = new ArrayList<>(path);
    }

    public int getLayerFromPosition(Vec2 worldPos) {
        for (int i = this.level.metadata.layers; i >= 1; i--) {
            for (Tile tile : this.level.tiles.get(String.valueOf(i))) {
                if (worldPos.x > tile.x * Level.WORLD_TILE_SIZE
                        && worldPos.x < tile.x * Level.WORLD_TILE_SIZE + Level.WORLD_TILE_SIZE) {
                    if (worldPos.y > tile.y * Level.WORLD_TILE_SIZE
                            && worldPos.y < tile.y * Level.WORLD_TILE_SIZE + Level.WORLD_TILE_SIZE) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    public int[] getNodeCoordinatesAtPosition(Vec2 worldPos) {
        for (int i = this.level.metadata.layers; i >= 1; i--) {
            for (Tile tile : this.level.tiles.get(String.valueOf(i))) {
                if (worldPos.x >= tile.x * Level.WORLD_TILE_SIZE
                        && worldPos.x < tile.x * Level.WORLD_TILE_SIZE + Level.WORLD_TILE_SIZE) {
                    if (worldPos.y >= tile.y * Level.WORLD_TILE_SIZE
                            && worldPos.y < tile.y * Level.WORLD_TILE_SIZE + Level.WORLD_TILE_SIZE) {
                        Vec2 gridPos = grid.tilePosToGridPos(tile.x, tile.y);
                        return new int[]{(int) gridPos.x, (int) gridPos.y, i-1};
                    }
                }
            }
        }
        return new int[]{0, 0, 0};
    }

    public void hurt(int damage) {
        // damage the goblin
        health -= damage;

        // if goblin has no health left
        if (health <= 0) {
            // if client side
            if (game != null) {
                game.enemies.remove(this.id);
                SoundPlayer.playSound(SoundPlayer.Sounds.ENEMY_DEATH);
            }
            // if server side
            if (serverGame != null) {
                serverGame.enemies.remove(this.id);
            }
        } else {
            if (game != null) {
                SoundPlayer.playSound(SoundPlayer.Sounds.ENEMY_HURT);
            }
        }

        this.animateHurt = true;
    }

    @Override
    public Vec2 getHitboxSize() {
        return new Vec2(30, 30);
    }
}
