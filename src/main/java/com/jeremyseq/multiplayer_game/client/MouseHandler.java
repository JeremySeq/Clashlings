package main.java.com.jeremyseq.multiplayer_game.client;

import main.java.com.jeremyseq.multiplayer_game.common.AttackState;
import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class MouseHandler implements MouseListener {

    private final Game game;

    public MouseHandler(Game game) {
        this.game = game;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point mousePos = e.getPoint();
        Vec2 playerToMouseVec = new Vec2(mousePos.x - game.clientPlayer.position.x, mousePos.y - game.clientPlayer.position.y).normalize();
        if (!playerToMouseVec.equals(new Vec2(0, 0))) {
            boolean right = true;
            boolean up = true;
            if (playerToMouseVec.x < 0) {
                right = false;
            }
            if (playerToMouseVec.y > 0) {
                up = false;
            }


            AttackState attackSide;
            if (Math.abs(playerToMouseVec.x) > Math.abs(playerToMouseVec.y)) {
                if (right) {
                    attackSide = AttackState.RIGHT;
                } else {
                    attackSide = AttackState.LEFT;
                }
            } else {
                if (up) {
                    attackSide = AttackState.UP;
                } else {
                    attackSide = AttackState.DOWN;
                }
            }

            if (attackSide != this.game.clientPlayer.attacking) {
                try {
                    game.client.out.writeUTF("$attack:" + attackSide.name());
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
                game.clientPlayer.attack(attackSide);
            }

        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
