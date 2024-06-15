package main.java.com.jeremyseq.multiplayer_game.client;

import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.File;

public class ClientPlayer {
    public final SpriteLoader spriteLoader = new SpriteLoader(
            new File("src/main/resources/TinySwordsPack/Factions/Knights/Troops/Warrior/Blue/Warrior_Blue.png"),
            8, 6, 192, 120);
    public final String username;
    public final Game game;
    public Vec2 position;

    private int hitboxSize = 65;

    public ClientPlayer(Game game, String username, Vec2 position) {
        this.game = game;
        this.username = username;
        this.position = position;
    }

    public void draw(Graphics g, ImageObserver imageObserver) {
        this.spriteLoader.drawAnimation(g, imageObserver, 0, (int) position.x, (int) position.y);
        if (this == game.clientPlayer) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.RED);
        }
//        g.drawRect((int) (position.x-hitboxSize/2), (int) (position.y-hitboxSize/2), hitboxSize, hitboxSize);
        g.setFont(new Font("Jetbrains Mono", Font.PLAIN, 16));
        Rectangle2D bounds = g.getFont().getStringBounds(username, g.getFontMetrics().getFontRenderContext());
        g.drawString(username, (int) ((int) position.x - bounds.getWidth()/2), (int) ((int) position.y + bounds.getHeight() + 25));
    }
}
