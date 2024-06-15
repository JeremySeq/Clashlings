package main.java.com.jeremyseq.multiplayer_game.client;

import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

public class ClientPlayer {
    public final SpriteLoader spriteLoader = new SpriteLoader(
            "/TinySwordsPack/Factions/Knights/Troops/Warrior/Blue/Warrior_Blue.png",
            8, 6, 192, 120);
    public final String username;
    public final Game game;
    public Vec2 position;
    private boolean flipped;
    public Vec2 deltaMovement = new Vec2(0, 0);
    public ATTACK attacking = ATTACK.FALSE;

    private int hitboxSize = 65;

    public enum ATTACK {
        FALSE,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public ClientPlayer(Game game, String username, Vec2 position) {
        this.game = game;
        this.username = username;
        this.position = position;
    }

    public void draw(Graphics g, ImageObserver imageObserver) {

        int animation = 0;

        if (attacking != ATTACK.FALSE) {
            animation = 2;
            if (attacking == ATTACK.UP) {
                animation = 6;
            } else if (attacking == ATTACK.DOWN) {
                animation = 4;
            } else if (attacking == ATTACK.LEFT) {
                flipped = true;
            } else if (attacking == ATTACK.RIGHT) {
                flipped = false;
            }
        }

        if (this == game.clientPlayer) {
            g.setColor(Color.WHITE);

            if (game.keyHandler.leftPressed) {
                animation = 1;
                attacking = ATTACK.FALSE;
                flipped = true;
            } else if (game.keyHandler.rightPressed) {
                animation = 1;
                attacking = ATTACK.FALSE;
                flipped = false;
            } else if (game.keyHandler.upPressed) {
                attacking = ATTACK.FALSE;
                animation = 1;
            } else if (game.keyHandler.downPressed) {
                attacking = ATTACK.FALSE;
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

        // draw animation
        boolean finished = this.spriteLoader.drawAnimation(g, imageObserver, animation, (int) position.x, (int) position.y, flipped);

        if (finished && attacking != ATTACK.FALSE) {
            attacking = ATTACK.FALSE;
        }

//        g.drawRect((int) (position.x-hitboxSize/2), (int) (position.y-hitboxSize/2), hitboxSize, hitboxSize);

        // draw name
        g.setFont(new Font("Jetbrains Mono", Font.PLAIN, 16));
        Rectangle2D bounds = g.getFont().getStringBounds(username, g.getFontMetrics().getFontRenderContext());
        g.drawString(username, (int) ((int) position.x - bounds.getWidth()/2), (int) ((int) position.y + bounds.getHeight() + 25));
    }

    public void attack(ATTACK attackSide) {
        if (attackSide != this.attacking) {
            attacking = attackSide;
        }
    }
}
