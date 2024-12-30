package com.jeremyseq.multiplayer_game.client;

import com.jeremyseq.multiplayer_game.common.Vec2;
import com.jeremyseq.multiplayer_game.pathfinding.Grid;
import com.jeremyseq.multiplayer_game.pathfinding.Node;

import java.awt.*;
import java.util.List;

public class DebugRenderer {
    public static void drawPathfinding(List<Node> path, Game game, Graphics g, Grid grid) {
        if (!path.isEmpty() && game != null) {

            Node previousNode = null;
            for (Node node : path) {

                // Calculate the position of the current node
                Vec2 pos = new Vec2((node.getX() + grid.getxOffset()) * game.levelRenderer.drawSize,
                        (node.getY() + grid.getyOffset()) * game.levelRenderer.drawSize);
                pos = game.getRenderPositionFromWorldPosition(pos);
                int x = (int) pos.x;
                int y = (int) pos.y;

                // Calculate the center of the current node's rectangle
                int centerX = x + game.levelRenderer.drawSize / 2;
                int centerY = y + game.levelRenderer.drawSize / 2;

                if (node.isStair()) {
                    g.setColor(Color.BLUE);
                    g.fillRect(x, y, game.levelRenderer.drawSize, game.levelRenderer.drawSize);
                }

                if (node == path.get(0)) {
                    g.setColor(Color.GREEN);
                    g.fillRect(x, y, game.levelRenderer.drawSize, game.levelRenderer.drawSize);
                } else if (node == path.get(path.size()-1)) {
                    g.setColor(Color.RED);
                    g.fillRect(x, y, game.levelRenderer.drawSize, game.levelRenderer.drawSize);
                }

                g.setColor(Color.WHITE);

                // Draw the line connecting to the previous node's center
                if (previousNode != null) {
                    // Calculate the position of the previous node
                    Vec2 prevPos = new Vec2((previousNode.getX() + grid.getxOffset()) * game.levelRenderer.drawSize,
                            (previousNode.getY() + grid.getyOffset()) * game.levelRenderer.drawSize);
                    prevPos = game.getRenderPositionFromWorldPosition(prevPos);
                    int prevX = (int) prevPos.x;
                    int prevY = (int) prevPos.y;

                    // Calculate the center of the previous node's rectangle
                    int prevCenterX = prevX + game.levelRenderer.drawSize / 2;
                    int prevCenterY = prevY + game.levelRenderer.drawSize / 2;

                    // Draw the line from the center of the previous node to the center of the current node
                    g.drawLine(prevCenterX, prevCenterY, centerX, centerY);
                }

                // Update the previous node to the current node
                previousNode = node;
            }
        }
    }

}
