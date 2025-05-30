package com.jeremyseq.clashlings.common;

import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.client.LevelRenderer;
import com.jeremyseq.clashlings.client.SpriteRenderer;
import com.jeremyseq.clashlings.client.sound.SoundPlayer;
import com.jeremyseq.clashlings.common.level.Level;
import com.jeremyseq.clashlings.common.level.Tile;
import com.jeremyseq.clashlings.common.packets.S2C.GoblinAttackS2CPacket;
import com.jeremyseq.clashlings.pathfinding.AStarPathfinding;
import com.jeremyseq.clashlings.pathfinding.Grid;
import com.jeremyseq.clashlings.pathfinding.Node;
import com.jeremyseq.clashlings.server.ServerGame;
import com.jeremyseq.clashlings.server.ServerPlayer;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

public class Goblin implements Hitbox {
    public Game game; // on client only
    public ServerGame serverGame; // on server only
    public final long id;
    public Level level;
    public Vec2 position;
    public Grid grid;
    public ArrayList<Node> currentPath = new ArrayList<>();
    private Vec2 targetPos;
    public static final float MOVE_SPEED = 1.6f;
    public static final float ATTACK_RANGE = 30; // the targeted range for the AI
    public static final float SLASH_RANGE = 60; // the actual range of the attack

    private boolean flipped = false;
    public Vec2 deltaMovement = new Vec2(0, 0);

    /**
     * Set to true within hurt() if the goblin is dead.
     * Later used to remove from game without Concurrent Modification Exception
     */
    private boolean isDead = false;

    public AttackState attacking = AttackState.FALSE;
    private int attackTick = 0;

    private ServerPlayer closestPlayer = null;

    private Vec2 playerTargetPos = null;

    private boolean animateHurt = false;

    public static final int HEALTH = 20;

    public int health = HEALTH;

    private AStarPathfinding pathfinder = null;

    // for use on client
    public Goblin(long id, Game game, Level level, Vec2 position) {
        this.id = id;
        this.game = game;
        this.level = level;
        this.position = position;
    }

    // for use on server
    public Goblin(long id, ServerGame serverGame, Level level, Vec2 position) {
        this.id = id;
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
            int animation = 0;
            if (attacking != AttackState.FALSE) {
                animation = 2;
                if (attacking == AttackState.UP) {
                    animation = 4;
                } else if (attacking == AttackState.DOWN) {
                    animation = 3;
                } else if (attacking == AttackState.LEFT) {
                    flipped = true;
                } else if (attacking == AttackState.RIGHT) {
                    flipped = false;
                }
            }

            boolean finished = spriteRenderer.drawAnimation(g, imageObserver, animation, (int) renderPos.x, (int) renderPos.y, flipped, animateHurt);
            if (this.attacking != AttackState.FALSE && finished) {
                this.attacking = AttackState.FALSE;
            }
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
     * Per-tick method to handle all movement logic.
     * If a path exists (currentPath is not empty), the entity will follow it node by node,
     * updating its target position as it moves.
     * If no path is present but a target position (targetPos) exists (e.g. from another AI goal),
     * the entity will move directly to that target.
     * If neither a path nor a target exists, the entity remains idle.
     */
    public void handleMovement() {

        // SPECIFICALLY FOR USING PATHS

        if (!currentPath.isEmpty()) {

            if (targetPos == null) {
                // Initialize target position
                Node node = this.currentPath.getFirst();
                Vec2 tilePos = grid.gridPosToTilePos(node.getX(), node.getY());
                if (this.game != null) {
                    targetPos = this.game.level.getWorldPositionFromTilePosition(tilePos, Level.WORLD_TILE_SIZE);
                } else {
                    targetPos = this.serverGame.level.getWorldPositionFromTilePosition(tilePos, Level.WORLD_TILE_SIZE);
                }
                targetPos = targetPos.add(new Vec2(Level.WORLD_TILE_SIZE / 2f, Level.WORLD_TILE_SIZE / 2f));
            }

//            ServerGame.LOGGER.debug("nodes left in path: " + currentPath.size());

            if (targetPos != null && targetPos.equals(this.position)) {
                this.currentPath.removeFirst();
                targetPos = null;
                handleMovement();
                return;
            }

        }

        // FOR GENERAL MOVEMENT USING TARGET POS

        if (targetPos == null) {
            return;
        }

        // Calculate the direction to the target position
        Vec2 direction = targetPos.subtract(this.position).normalize();

        // Calculate the movement step
        this.deltaMovement = direction.multiply(MOVE_SPEED);

        // Move gradually towards the target position
        this.position = this.position.add(this.deltaMovement);

        // Check if the enemy has reached the target position
        if (this.position.distance(targetPos) <= MOVE_SPEED) {
            // Snap to target position and remove the node from the path
            this.position = targetPos;

            // if we are on a path
            if (!this.currentPath.isEmpty()) {
                this.currentPath.removeFirst();

                // Set the next target position if there are more nodes in the path
                if (!this.currentPath.isEmpty()) {
                    Node nextNode = this.currentPath.getFirst();
                    Vec2 tilePos = grid.gridPosToTilePos(nextNode.getX(), nextNode.getY());
                    targetPos = new Vec2(tilePos.x * Level.WORLD_TILE_SIZE, tilePos.y * Level.WORLD_TILE_SIZE);
                    targetPos = targetPos.add(new Vec2(Level.WORLD_TILE_SIZE / 2f, Level.WORLD_TILE_SIZE / 2f));
                } else {
                    targetPos = null; // Clear target position when path is empty
                }
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

    public void serverTick() {
        if (this.attacking != AttackState.FALSE) {
            this.deltaMovement = new Vec2(0, 0);
            this.attackTick++;
            if (this.attackTick == 14) {
                if (this.closestPlayer != null && this.closestPlayer.pos.distance(this.position) <= SLASH_RANGE) {
                    this.closestPlayer.hurt(3);
                }
            }

            final int attackDuration = 60; // number of server ticks to wait after beginning attack anim to reset and attack again
            // basically attack cooldown
            if (this.attackTick >= attackDuration) {
                this.attackTick = 0;
                this.attacking = AttackState.FALSE;
            }
        }

        // sets the path to follow the player
        boolean followPlayerResult = followPlayerGoal();

        // if goblin is next to player, attack
        if (followPlayerResult) {
            attackPlayerGoal();
        }

        if (this.attacking == AttackState.FALSE) {
            // follows the set path
            handleMovement();
        }
    }

    public boolean getisDead() {
        return isDead;
    }

    /**
     * Attacks players
     * Assumes player is in adjacent tile
     * If necessary, moves goblin toward player
     * Target is this.closestPlayer
     */
    private void attackPlayerGoal() {
        final float buffer = 10;

        // direction from player to this enemy
        Vec2 direction = this.position.subtract(closestPlayer.pos);

        // prevent divide-by-zero
        if (direction.length() == 0) {
            direction = new Vec2(1, 0); // default to the right
        } else {
            direction = direction.normalize();
        }

        // set targetPos to be ATTACK_RANGE from the player in that direction
        targetPos = closestPlayer.pos.add(direction.multiply(ATTACK_RANGE));

        if (this.closestPlayer != null && this.closestPlayer.pos.distance(this.position) <= ATTACK_RANGE+buffer) {
            // determine attack direction
            AttackState attackState = getAttackState();

            // if the goblin is not already attacking, set the attack state
            if (this.attacking == AttackState.FALSE) {
                this.attacking = attackState;

                // send the attack animation to clients
                if (this.serverGame != null) {
                    this.serverGame.server.sendToEachPlayer(new GoblinAttackS2CPacket(this.id, attackState));
                }
            }
        }
    }

    /**
     * Calculate the attack direction based on the position of the goblin and the closest player.
     */
    private AttackState getAttackState() {
        float dx = this.position.x - closestPlayer.pos.x;
        float dy = this.position.y - closestPlayer.pos.y;

        AttackState attackState;
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) {
                attackState = AttackState.LEFT;
            } else {
                attackState = AttackState.RIGHT;
            }
        } else {
            if (dy > 0) {
                attackState = AttackState.UP;
            } else {
                attackState = AttackState.DOWN;
            }
        }
        return attackState;
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
        if (targetLayer == 0) {
            ServerGame.LOGGER.warning("Goblin pathfinding crash: targetLayer=0 because player is not on a tile (Goblin.setPathToTile)");
            return;
        }
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
                this.isDead = true;
                SoundPlayer.playSound(SoundPlayer.Sounds.ENEMY_DEATH);
            }
            // if server side
            if (serverGame != null) {
                this.isDead = true;
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
