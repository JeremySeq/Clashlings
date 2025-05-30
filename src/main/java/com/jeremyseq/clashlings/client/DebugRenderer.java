package com.jeremyseq.clashlings.client;

import com.jeremyseq.clashlings.common.Vec2;
import com.jeremyseq.clashlings.pathfinding.Grid;
import com.jeremyseq.clashlings.pathfinding.Node;

import java.awt.*;
import java.util.List;

public class DebugRenderer {
    public static void drawPathfinding(List<Node> path, Game game, Graphics g, Grid grid) {
        if (!path.isEmpty() && game != null) {

            Node previousNode = null;
            for (Node node : path) {

                // Calculate the position of the current node
                Vec2 pos = new Vec2((node.getX() + grid.getxOffset()) * LevelRenderer.DRAW_SIZE,
                        (node.getY() + grid.getyOffset()) * LevelRenderer.DRAW_SIZE);
                pos = game.getRenderPositionFromWorldPosition(pos);
                int x = (int) pos.x;
                int y = (int) pos.y;

                // Calculate the center of the current node's rectangle
                int centerX = x + LevelRenderer.DRAW_SIZE / 2;
                int centerY = y + LevelRenderer.DRAW_SIZE / 2;

                if (node.isStair()) {
                    g.setColor(Color.BLUE);
                    g.fillRect(x, y, LevelRenderer.DRAW_SIZE, LevelRenderer.DRAW_SIZE);
                }

                if (node == path.get(0)) {
                    g.setColor(Color.GREEN);
                    g.fillRect(x, y, LevelRenderer.DRAW_SIZE, LevelRenderer.DRAW_SIZE);
                } else if (node == path.get(path.size()-1)) {
                    g.setColor(Color.RED);
                    g.fillRect(x, y, LevelRenderer.DRAW_SIZE, LevelRenderer.DRAW_SIZE);
                }

                g.setColor(Color.WHITE);

                // Draw the line connecting to the previous node's center
                if (previousNode != null) {
                    // Calculate the position of the previous node
                    Vec2 prevPos = new Vec2((previousNode.getX() + grid.getxOffset()) * LevelRenderer.DRAW_SIZE,
                            (previousNode.getY() + grid.getyOffset()) * LevelRenderer.DRAW_SIZE);
                    prevPos = game.getRenderPositionFromWorldPosition(prevPos);
                    int prevX = (int) prevPos.x;
                    int prevY = (int) prevPos.y;

                    // Calculate the center of the previous node's rectangle
                    int prevCenterX = prevX + LevelRenderer.DRAW_SIZE / 2;
                    int prevCenterY = prevY + LevelRenderer.DRAW_SIZE / 2;

                    // Draw the line from the center of the previous node to the center of the current node
                    g.drawLine(prevCenterX, prevCenterY, centerX, centerY);
                }

                // Update the previous node to the current node
                previousNode = node;
            }
        }
    }

    /**
     * Draws a box given world coordinates
     */
    public static void drawBox(Game game, Graphics g, int x, int y, int x2, int y2) {
        g.setColor(Color.WHITE);
        Vec2 drawStart = game.getRenderPositionFromWorldPosition(new Vec2(x, y));
        Vec2 drawEnd = game.getRenderPositionFromWorldPosition(new Vec2(x2, y2));
        g.drawRect((int) drawStart.x, (int) drawStart.y, (int) (drawEnd.x - drawStart.x), (int) (drawEnd.y - drawStart.y));
    }
}
