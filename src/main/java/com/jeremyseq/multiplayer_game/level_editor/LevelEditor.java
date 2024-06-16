package main.java.com.jeremyseq.multiplayer_game.level_editor;

import main.java.com.jeremyseq.multiplayer_game.client.Game;
import main.java.com.jeremyseq.multiplayer_game.common.Level;
import main.java.com.jeremyseq.multiplayer_game.common.LevelReader;
import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.util.HashMap;
import java.util.Objects;

public class LevelEditor extends JPanel implements ActionListener, KeyListener {

    public static final int WIDTH = 900;
    public static final int HEIGHT = 900;

    public final int DELAY = 20;
    public HashMap<String, BufferedImage> tilemaps = new HashMap<>();

    int drawSize = 64;
    int tileSize = 64;
    public String tilemap = "flat";
    public int tilemapI = 0;
    public int tilemapJ = 0;

    public Level level = new LevelReader().readLevel("level1");

    private Timer timer;
    private boolean dPressed = false;

    public LevelEditor() {
        this.loadImages();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // this timer will call the actionPerformed() method every DELAY ms
        timer = new Timer(DELAY, this);
        timer.start();

        Thread receiveInput = new Thread(() -> {
            while (true) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(System.in));
                // Reading data using readLine
                try {
                    String tileChange = reader.readLine();
                    tilemap = tileChange.split(":")[0];
                    String tilePos = tileChange.split(":")[1];
                    tilemapI = Integer.parseInt(tilePos.split(",")[0]);
                    tilemapJ = Integer.parseInt(tilePos.split(",")[1]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        receiveInput.start();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Vec2 mousePos = new Vec2(getMousePosition().x, getMousePosition().y);
                for (int i = 0; i < Game.WIDTH/drawSize + drawSize; i++) {
                    for (int j = 0; j < Game.HEIGHT/drawSize + drawSize; j++) {
                        if (mousePos.x > i*drawSize && mousePos.x < i*drawSize + drawSize && mousePos.y > j*drawSize && mousePos.y < j*drawSize + drawSize) {
                            Vec2 tilePos = new Vec2(i, j).subtract(new Vec2(7, 7)); // I don't know why its 7 it just is
                            if (dPressed) {
                                System.out.println("Deleting");
                                level.tiles.removeIf(tile -> tile.x == (int) tilePos.x && tile.y == (int) tilePos.y);
                            } else {
                                level.tiles.add(new Level.Tile((int) tilePos.x, (int) tilePos.y, tilemap, tilemapI, tilemapJ));
                            }
                        }
                    }
                }
            }
        });

        this.addKeyListener(this);
        this.setFocusable(true);
    }

    public void loadImages() {
        try {
            tilemaps.put("flat", ImageIO.read(Objects.requireNonNull(getClass().getResource("/TinySwordsPack/Terrain/Ground/Tilemap_Flat.png"))));
            tilemaps.put("elevation", ImageIO.read(Objects.requireNonNull(getClass().getResource("/TinySwordsPack/Terrain/Ground/Tilemap_Elevation.png"))));
            tilemaps.put("water", ImageIO.read(Objects.requireNonNull(getClass().getResource("/TinySwordsPack/Terrain/Water/Water.png"))));
        } catch (IOException exc) {
            System.out.println("Error opening image file: " + exc.getMessage());
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        // when calling g.drawImage() we can use "this" for the ImageObserver
        // because Component implements the ImageObserver interface, and JPanel
        // extends from Component. So "this" main.java.com.seq.chess.Board instance, as a Component, can
        // react to imageUpdate() events triggered by g.drawImage()

        // draw our graphics.
        drawBackground(g);


        // this smooths out animations on some systems
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawBackground(Graphics g) {
        draw(g, this);
    }

    public void draw(Graphics g, ImageObserver imageObserver) {
        Vec2 mousePos = null;
        if (this.getMousePosition() != null) {
            mousePos = new Vec2(this.getMousePosition().x, this.getMousePosition().y);
        }

        for (int i = 0; i < Game.WIDTH/drawSize + drawSize; i++) {
            for (int j = 0; j < Game.HEIGHT/drawSize + drawSize; j++) {
                drawTile(g, imageObserver, i*drawSize, j*drawSize, tilemaps.get("water"), 0, 0, true);
            }
        }

        for (Level.Tile tile : level.tiles) {
            drawTile(g, imageObserver, tile.x * drawSize, tile.y * drawSize, tilemaps.get(tile.tilemap), tile.i, tile.j);
        }

        for (int i = 0; i < Game.WIDTH/drawSize + drawSize; i++) {
            for (int j = 0; j < Game.HEIGHT/drawSize + drawSize; j++) {
                if (mousePos != null) {
                    if (mousePos.x > i*drawSize && mousePos.x < i*drawSize + drawSize && mousePos.y > j*drawSize && mousePos.y < j*drawSize + drawSize) {
                        g.drawRect(i*drawSize+1, j*drawSize+1, drawSize, drawSize);
                    }
                }
            }
        }

        drawTile(g, imageObserver, 0, 0, tilemaps.get(this.tilemap), tilemapI, tilemapJ, true);
    }

    /**
     * @param x x-coordinate to draw on screen
     * @param y y-coordinate to draw on screen
     * @param tilemap tilemap buffered image
     * @param i tile on tilemap to draw
     * @param j tile on tilemap to draw
     */
    public void drawTile(Graphics g, ImageObserver imageObserver, int x, int y, BufferedImage tilemap, int i, int j) {
        drawTile(g, imageObserver, x, y, tilemap, i, j, false);
    }

    /**
     * @param x x-coordinate to draw on screen
     * @param y y-coordinate to draw on screen
     * @param tilemap tilemap buffered image
     * @param i tile on tilemap to draw
     * @param j tile on tilemap to draw
     * @param ignoreScrolling if this is true, renders the tile without converting to world position, meaning
     *                        if the player moves, this tile stays in the same spot on their screen
     */
    public void drawTile(Graphics g, ImageObserver imageObserver, int x, int y, BufferedImage tilemap, int i, int j, boolean ignoreScrolling) {
        Vec2 renderPos = new Vec2(x, y);
        if (!ignoreScrolling) {
            renderPos = getRenderPositionFromWorldPosition(renderPos);
        }
        int x2 = (int) renderPos.x;
        int y2 = (int) renderPos.y;
        g.drawImage(
                tilemap,
                x2, y2, x2 + drawSize, y2 + drawSize,
                tileSize*i, tileSize*j, tileSize*i + tileSize, tileSize*j + tileSize,
                imageObserver
        );
    }

    public Vec2 getRenderPositionFromWorldPosition(Vec2 vec2) {
        vec2 = vec2.add(new Vec2(WIDTH/2f, HEIGHT/2f));
        return vec2;
    }

    public Vec2 getWorldPositionFromRenderPosition(Vec2 vec2) {
        vec2 = vec2.subtract(new Vec2(WIDTH/2f, HEIGHT/2f));
        return vec2;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

        if (e.getKeyChar() == 's') {
            System.out.println("Saving");
            try {
                FileWriter writer = new FileWriter("src/main/resources/levels/level1.json");
                writer.write(level.toJson());
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_D) {
            this.dPressed = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            tilemapJ -= 1;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            tilemapJ += 1;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            tilemapI -= 1;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            tilemapI += 1;
        }

        if (e.getKeyCode() == KeyEvent.VK_1) {
            tilemap = "flat";
        }
        if (e.getKeyCode() == KeyEvent.VK_2) {
            tilemap = "elevation";
        }
        System.out.println(tilemapI + ", " + tilemapJ);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_D) {
            this.dPressed = false;
        }
    }
}
