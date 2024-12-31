package com.jeremyseq.multiplayer_game.level_editor;

import com.jeremyseq.multiplayer_game.client.Camera;
import com.jeremyseq.multiplayer_game.client.LevelRenderer;
import com.jeremyseq.multiplayer_game.common.level.*;
import com.jeremyseq.multiplayer_game.common.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.*;
import java.util.ArrayList;

public class LevelEditor extends JPanel implements ActionListener, KeyListener, Camera {

    public static final int WIDTH = 900;
    public static final int HEIGHT = 900;

    public final int DELAY = 20;

    public String tilemap = "flat";
    public int tilemapI = 0;
    public int tilemapJ = 0;

    public LevelEditorMouseHandler mouseHandler = new LevelEditorMouseHandler(this);

    public Vec2 camPos = new Vec2(0, 0);

    public static final String LEVEL_TO_EDIT = "level1";

    public Level level = new LevelReader().readLevel(LEVEL_TO_EDIT);
    public LevelRenderer levelRenderer = new LevelRenderer(this.level, this);

    public String layer = String.valueOf(this.level.metadata.layers);

    private Timer timer;
    private boolean dPressed = false;
    private boolean moveUp = false;
    private boolean moveDown = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;

    public LevelEditor() {
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

        this.addMouseListener(mouseHandler);
        this.addMouseWheelListener(mouseHandler);

        this.addKeyListener(this);
        this.setFocusable(true);
    }

    public void mousePressed(MouseEvent e) {
        Vec2 mousePos = new Vec2(getMousePosition().x, getMousePosition().y);
        Vec2 worldPos = this.getWorldPositionFromRenderPosition(mousePos);

        // convert world position to tile position
        Vec2 tilePos = getTilePositionFromWorldPosition(worldPos);

        if (dPressed) {
            System.out.println("Deleting");
            level.tiles.get(layer).removeIf(tile -> tile.x == (int) tilePos.x && tile.y == (int) tilePos.y);
        } else {
            level.tiles.computeIfAbsent(layer, k -> new ArrayList<>());
            level.tiles.get(layer).add(new Tile((int) tilePos.x, (int) tilePos.y, tilemap, tilemapI, tilemapJ));
        }
    }

    public void mouseWheelUp() {
        if (layer.equals(String.valueOf(level.metadata.layers))) {
            return;
        }
        if (layer.contains("-")) {
            layer = layer.split("-")[1];
        } else {
            layer = layer + "-" + (Integer.parseInt(layer) + 1);
        }
        System.out.println(layer);
    }

    public void mouseWheelDown() {
        if (layer.equals("1")) {
            return;
        }
        if (layer.contains("-")) {
            layer = layer.split("-")[0];
        } else {
            layer = (Integer.parseInt(layer) - 1) + "-" + layer;
        }
        System.out.println(layer);
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        // when calling g.drawImage() we can use "this" for the ImageObserver
        // because Component implements the ImageObserver interface, and JPanel
        // extends from Component. So "this" com.seq.chess.Board instance, as a Component, can
        // react to imageUpdate() events triggered by g.drawImage()

        // draw our graphics.
        drawBackground(g);


        // this smooths out animations on some systems
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        levelRenderer.drawTillLayer(g, this, layer);

        draw(g, this);
    }

    public void draw(Graphics g, ImageObserver imageObserver) {
        if (this.getMousePosition() != null) {
            Vec2 mousePos = new Vec2(getMousePosition().x, getMousePosition().y);
            Vec2 worldPos = this.getWorldPositionFromRenderPosition(mousePos);
            Vec2 tilePos = getTilePositionFromWorldPosition(worldPos);
            Vec2 startPos = new Vec2(tilePos.x * this.levelRenderer.drawSize, tilePos.y * this.levelRenderer.drawSize);
            startPos = this.getRenderPositionFromWorldPosition(startPos);
            g.drawRect((int) startPos.x, (int) startPos.y, this.levelRenderer.drawSize, this.levelRenderer.drawSize);
            g.setFont(new Font("Jetbrains Mono", Font.BOLD, 22));
            g.drawString("Pos: " + (int) tilePos.x + ", " + (int) tilePos.y, 5, HEIGHT-20);
        }

        this.levelRenderer.drawTile(g, imageObserver, 0, 0,
                this.levelRenderer.tilemaps.get(this.tilemap), tilemapI, tilemapJ, true);
        g.setFont(new Font("Jetbrains Mono", Font.BOLD, 22));
        Rectangle2D bounds = g.getFont().getStringBounds(layer, g.getFontMetrics().getFontRenderContext());
        g.drawString("Layer: " + layer, this.levelRenderer.drawSize + 10, (int) (bounds.getHeight()+2));
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    public Vec2 getTilePositionFromWorldPosition(Vec2 worldPos) {
        // convert world position to tile position
        Vec2 tilePos = new Vec2(worldPos.x / this.levelRenderer.drawSize, worldPos.y / this.levelRenderer.drawSize);
        // if coordinates are negative we want to floor not truncate, meaning -7.3 -> -8
        tilePos.x = tilePos.x < 0 ? (float) Math.floor(tilePos.x) : tilePos.x;
        tilePos.y = tilePos.y < 0 ? (float) Math.floor(tilePos.y) : tilePos.y;
        return new Vec2((int) tilePos.x, (int) tilePos.y);
    }

    public Vec2 getRenderPositionFromWorldPosition(Vec2 vec2) {
        vec2 = vec2.add(new Vec2(WIDTH/2f, HEIGHT/2f));
        vec2 = vec2.subtract(camPos);
        return vec2;
    }

    public Vec2 getWorldPositionFromRenderPosition(Vec2 vec2) {
        vec2 = vec2.add(camPos);
        vec2 = vec2.subtract(new Vec2(WIDTH/2f, HEIGHT/2f));
        return vec2;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (moveUp) {
            camPos = camPos.add(new Vec2(0, -20));
        } else if (moveDown) {
            camPos = camPos.add(new Vec2(0, 20));
        } else if (moveLeft) {
            camPos = camPos.add(new Vec2(-20, 0));
        } else if (moveRight) {
            camPos = camPos.add(new Vec2(20, 0));
        }

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

        if (e.getKeyChar() == 't') {
            System.out.println("Saving");
            try {
                FileWriter writer = new FileWriter("src/main/resources/levels/" + LEVEL_TO_EDIT + ".json");
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
        if (e.getKeyCode() == KeyEvent.VK_W) {
            this.moveUp = true;
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            this.moveDown = true;
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            this.moveLeft = true;
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            this.moveRight = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_R) {
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

        if (e.getKeyCode() == KeyEvent.VK_W) {
            this.moveUp = false;
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            this.moveDown = false;
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            this.moveLeft = false;
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            this.moveRight = false;
        }

        if (e.getKeyCode() == KeyEvent.VK_R) {
            this.dPressed = false;
        }

    }
}
