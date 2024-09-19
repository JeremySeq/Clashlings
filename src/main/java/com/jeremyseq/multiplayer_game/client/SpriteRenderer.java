package main.java.com.jeremyseq.multiplayer_game.client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class SpriteRenderer {
    private final String imageFile;
    private BufferedImage image;
    private BufferedImage damage_tinted_image; // TODO: a damage tinted image doesn't need to be made for every sprite
    private int frameCounter = 0;
    private int animationFrame = 0;

    private int hurtTintCounter = -1;

    private final int tileWidth;
    private final int tileHeight;
    private final int drawSize;

    private int animationCount;
    private final int[] framesPerAnimation;
    public SpriteRenderType renderType;

    private int playingAnimation;

    public enum SpriteRenderType {
        /**
         * used for sprites with a single texture and no animation
         */
        SIMPLE,
        /**
         * used for sprites with multiple animations in the form of a sheet with a consistent amount of frames per animation
         */
        ANIMATED_TILES,
        /**
         * used for sprites with multiple animations in the form of a sheet with a varying amount of frames per animation
         */
        ANIMATED_TILES_VARYING,
    }

    public SpriteRenderer(String imageFile, int tileSize, int drawSize) {
        this.renderType = SpriteRenderType.SIMPLE;
        this.imageFile = imageFile;
        this.framesPerAnimation = new int[]{1};
        this.animationCount = 1;
        this.tileHeight = tileSize;
        this.tileWidth = tileSize;
        this.drawSize = drawSize;
    }

    public SpriteRenderer(String imageFile, int[] framesPerAnimation, int tileSize, int drawSize) {
        this.renderType = SpriteRenderType.ANIMATED_TILES_VARYING;
        this.imageFile = imageFile;
        this.framesPerAnimation = framesPerAnimation;
        this.animationCount = framesPerAnimation.length;
        this.tileHeight = tileSize;
        this.tileWidth = tileSize;
        this.drawSize = drawSize;
    }

    public SpriteRenderer(String imageFile, int animationCount, int framesPerAnimation, int tileSize, int drawSize) {
        this.renderType = SpriteRenderType.ANIMATED_TILES;
        this.imageFile = imageFile;
        this.framesPerAnimation = new int[animationCount];
        Arrays.fill(this.framesPerAnimation, framesPerAnimation);
        this.animationCount = animationCount;
        this.tileHeight = tileSize;
        this.tileWidth = tileSize;
        this.drawSize = drawSize;
    }

    public void loadImage() {
        if (image == null) {
            try {
                image = ImageIO.read(Objects.requireNonNull(getClass().getResource(this.imageFile)));
                damage_tinted_image = ImageIO.read(Objects.requireNonNull(getClass().getResource(this.imageFile)));
                ImageFilters.tint(damage_tinted_image, new Color(191, 41, 41, 154));
            } catch (IOException exc) {
                System.out.println("Error opening image file: " + exc.getMessage());
            }
        }
    }

    public boolean drawAnimation(Graphics g, ImageObserver imageObserver, int x, int y) {
        return drawAnimation(g, imageObserver, x, y, false);
    }

    public boolean drawAnimation(Graphics g, ImageObserver imageObserver, int x, int y, boolean flipped) {
        return drawAnimation(g, imageObserver, 0, x, y, flipped);
    }

    public boolean drawAnimation(Graphics g, ImageObserver imageObserver, int animation, int x, int y, boolean flipped) {
        return drawAnimation(g, imageObserver, animation, x, y, flipped, false);
    }

    public boolean drawAnimation(Graphics g, ImageObserver imageObserver, int animation, int x, int y, boolean flipped, boolean startHurtTint) {
        boolean finished = false;
        loadImage();

        if (animation != playingAnimation) {
            playingAnimation = animation;
            animationFrame = 0;
            frameCounter = 0;
        }

        if (startHurtTint) {
            hurtTintCounter = 20;
        }

        boolean doTint = false;

        if (hurtTintCounter != -1) {
            doTint = true;
            hurtTintCounter -= 1;
            if (hurtTintCounter == 0) {
                hurtTintCounter = -1;
            }
        }


        if (flipped) {
            g.drawImage(
                    doTint ? this.damage_tinted_image : this.image,
                    x + drawSize/2, y - drawSize/2, x - drawSize/2, y + drawSize/2,
                    animationFrame*tileWidth, animation*tileHeight, animationFrame*tileWidth+tileWidth, animation*tileHeight+tileHeight,
                    imageObserver
            );
        } else {
            g.drawImage(
                    doTint ? this.damage_tinted_image : this.image,
                    x - drawSize/2, y - drawSize/2, x + drawSize/2, y + drawSize/2,
                    animationFrame*tileWidth, animation*tileHeight, animationFrame*tileWidth+tileWidth, animation*tileHeight+tileHeight,
                    imageObserver
            );
        }

        frameCounter++;
        if (frameCounter >= 3) {
            animationFrame++;
            frameCounter = 0;
            if (animationFrame >= framesPerAnimation[animation]) {
                finished = true;
                animationFrame = 0;
            }
        }

        return finished;
    }
}
