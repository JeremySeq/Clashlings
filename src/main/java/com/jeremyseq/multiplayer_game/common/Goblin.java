package main.java.com.jeremyseq.multiplayer_game.common;

import main.java.com.jeremyseq.multiplayer_game.client.DebugRenderer;
import main.java.com.jeremyseq.multiplayer_game.client.Game;
import main.java.com.jeremyseq.multiplayer_game.client.SpriteRenderer;
import main.java.com.jeremyseq.multiplayer_game.common.level.Level;
import main.java.com.jeremyseq.multiplayer_game.pathfinding.AStarPathfinding;
import main.java.com.jeremyseq.multiplayer_game.pathfinding.Grid;
import main.java.com.jeremyseq.multiplayer_game.pathfinding.Node;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

public class Goblin {
    public Game game; // on client only
    public Level level;
    public Vec2 position;
    public Grid grid;
    public ArrayList<Node> currentPath = new ArrayList<>();
    private Vec2 targetPos;
    public static final float MOVE_SPEED = 3;
    private boolean flipped = false;

    // for use on client
    public Goblin(Game game, Level level, Vec2 position) {
        this.game = game;
        this.level = level;
        this.position = position;
        this.initializePathfinding();
        this.moveTo(new Vec2(5, 7));
    }

    // for use on server
    public Goblin(Level level, Vec2 position) {
        this.level = level;
        this.position = position;
    }

    public final SpriteRenderer spriteRenderer = new SpriteRenderer(
            "/TinySwordsPack/Factions/Goblins/Troops/Torch/Red/Torch_Red.png",
            new int[]{7, 6, 6, 6, 6}, 192, 140);

    public void draw(Graphics g, ImageObserver imageObserver) {
        Vec2 renderPos = this.position;
        if (game != null) {
            renderPos = game.getRenderPositionFromWorldPosition(renderPos);
        }

        if (this.targetPos == null) {
            spriteRenderer.drawAnimation(g, imageObserver, 0, (int) renderPos.x, (int) renderPos.y, flipped);
        } else {

            if (this.position.x - this.targetPos.x < 0) {
                flipped = false;
            } else if (this.position.x - this.targetPos.x > 0) {
                flipped = true;
            }

            spriteRenderer.drawAnimation(g, imageObserver, 1, (int) renderPos.x, (int) renderPos.y, flipped);
        }

        // draw pathfinding debugging
        DebugRenderer.drawPathfinding(this.currentPath, this.game, g, this.grid);
    }

    public void initializePathfinding() {
        this.grid = Grid.levelToGrid(level);
    }

    public void tick() {
        if (this.currentPath.isEmpty()) {
            return;
        }

        if (targetPos == null) {
            // Initialize target position
            Node node = this.currentPath.get(0);
            Vec2 tilePos = grid.gridPosToTilePos(node.getX(), node.getY());
            targetPos = new Vec2(tilePos.x * game.levelRenderer.drawSize, tilePos.y * game.levelRenderer.drawSize);
            targetPos = targetPos.add(new Vec2(game.levelRenderer.drawSize / 2f, game.levelRenderer.drawSize / 2f));
        }

        // Calculate the direction to the target position
        Vec2 direction = targetPos.subtract(this.position).normalize();

        // Calculate the movement step
        Vec2 step = direction.multiply(MOVE_SPEED);

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
                targetPos = new Vec2(tilePos.x * game.levelRenderer.drawSize, tilePos.y * game.levelRenderer.drawSize);
                targetPos = targetPos.add(new Vec2(game.levelRenderer.drawSize / 2f, game.levelRenderer.drawSize / 2f));
            } else {
                targetPos = null; // Clear target position when path is empty
            }
        }
    }

    public void moveTo(Vec2 moveToPosition) {
        Node start = grid.getNode(9, 2, 2);
        Node end = grid.getNode(4, 6, 0);
        List<Node> path = new AStarPathfinding(grid).findPath(start, end);
        System.out.println("Path:");
        for (Node node : path) {
            System.out.println("(" + node.getX() + ", " + node.getY() + ", " + node.getZ() + ")");
        }
        this.currentPath = new ArrayList<>(path);
    }
}
