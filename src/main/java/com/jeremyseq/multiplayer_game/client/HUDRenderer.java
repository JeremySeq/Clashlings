package com.jeremyseq.multiplayer_game.client;

import com.jeremyseq.multiplayer_game.server.ServerPlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class HUDRenderer {

    public final Game game;

    private BufferedImage gui_texture;

    public HUDRenderer(Game game) {
        this.game = game;
        this.loadImages();
    }

    private void loadImages() {
        try {
            gui_texture = ImageIO.read(Objects.requireNonNull(getClass().getResource("/GUI.png")));
        } catch (IOException exc) {
            Game.LOGGER.warning("Error loading GUI texture: " + exc.getMessage());
        }
    }

    public void draw(Graphics g) {
        this.renderHealthBar(g);
    }

    private void renderHealthBar(Graphics g) {
        ClientPlayer clientPlayer = game.clientPlayer;

        float healthPercent = clientPlayer.health / ServerPlayer.DEFAULT_HEALTH;

        this.drawHealthBarPart(g, 0, 1);
        this.drawHealthBarPart(g, 1, healthPercent);
        this.drawHealthBarPart(g, 2, healthPercent);
    }

    /**
     * @param piece 0 = background, 1 = orange semi-bg, 2 = red main
     * @param percent 0.0 - 1.0, percent of health bar part to render
     */
    private void drawHealthBarPart(Graphics g, int piece, float percent) {
        int sStartX;
        if (piece == 0) {
            sStartX = 135;
            percent = 1;
        } else if (piece == 1) {
            sStartX = 6;
        } else {
            sStartX = 72;
        }

        final int sStartY = 20;
        int originalSWidth = 52;
        final int sHeight = 7;

        int sWidth = (int) Math.ceil(percent * originalSWidth);
        if (piece == 1 && sWidth < originalSWidth) {
            sWidth++;
        }

        sStartX += originalSWidth - sWidth;

        int sEndX = sStartX + sWidth;
        int sEndY = sStartY + sHeight;

        // destination
        int dStartX = 15;
        int dStartY = 15;
        int scale = 5;
        if (piece != 0) dStartX += scale;

        int dEndX = dStartX + (sEndX - sStartX)*scale;
        int dEndY = dStartY + (sEndY - sStartY)*scale;

        g.drawImage(
                gui_texture,
                dStartX, dStartY, dEndX, dEndY,
                sStartX, sStartY, sEndX, sEndY,
                this.game
        );
    }
}
