package com.jeremyseq.multiplayer_game.client;

import com.jeremyseq.multiplayer_game.Client;
import com.jeremyseq.multiplayer_game.common.*;
import com.jeremyseq.multiplayer_game.common.level.Level;
import com.jeremyseq.multiplayer_game.common.packets.C2S.MovementC2SPacket;
import com.jeremyseq.multiplayer_game.common.packets.C2S.PositionC2SPacket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class Game extends JPanel implements ActionListener, Camera {

    public static final int WIDTH = 832;
    public static final int HEIGHT = 832;
    private static final float SPEED = 5;

    public final int DELAY = 20;
    public final Client client;

    public ClientPlayer clientPlayer;
    public Level level;

    public ArrayList<ClientPlayer> players = new ArrayList<>();

    public KeyHandler keyHandler = new KeyHandler();
    public MouseHandler mouseHandler = new MouseHandler(this);
    public LevelRenderer levelRenderer = new LevelRenderer(this);

    public Goblin enemy;
    public Hashtable<Long, Goblin> enemies = new Hashtable<>();

    private Timer timer;

    public Game(Client client) {
        this.client = client;

        this.clientPlayer = new ClientPlayer(this, client.username, new Vec2(0, 300));
        this.players.add(clientPlayer);

        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        Thread receiveServerResponses = new Thread(() -> {
            while (true) {
                try {
                    Packet packet = (Packet) client.in.readObject();
                    packet.handle(this);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        receiveServerResponses.start();

        this.enemy = new Goblin(this, this.level, new Vec2(100, 300));

        // this timer will call the actionPerformed() method every DELAY ms
        timer = new Timer(DELAY, this);
        timer.start();

        this.addMouseListener(mouseHandler);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
    }


    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        // draw our graphics.
        drawBackground(g);

        // send client position to server
        try {
            this.client.sendPacket(new PositionC2SPacket(clientPlayer.position));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // this smooths out animations on some systems
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        levelRenderer.draw(g, this);

        for (Goblin goblin : this.enemies.values()) {
            goblin.draw(g, this);
        }

        for (ClientPlayer player : this.players) {
            player.draw(g, this);
        }
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    public Vec2 getRenderPositionFromWorldPosition(Vec2 vec2) {
        vec2 = vec2.add(new Vec2(WIDTH/2f, HEIGHT/2f));
        vec2 = vec2.subtract(clientPlayer.position);
        return vec2;
    }

    public Vec2 getWorldPositionFromRenderPosition(Vec2 vec2) {
        vec2 = vec2.add(clientPlayer.position);
        vec2 = vec2.subtract(new Vec2(WIDTH/2f, HEIGHT/2f));
        return vec2;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // this method is called by the timer every DELAY ms.

        if (this.clientPlayer.attacking == AttackState.FALSE) {
            Vec2 dir = new Vec2(0, 0);
            if (keyHandler.leftPressed) {
                dir = dir.add(new Vec2(-1, 0));
            }
            if (keyHandler.rightPressed) {
                dir = dir.add(new Vec2(1, 0));
            }
            if (keyHandler.upPressed) {
                dir = dir.add(new Vec2(0, -1));
            }
            if (keyHandler.downPressed) {
                dir = dir.add(new Vec2(0, 1));
            }


            if (dir.x != 0 || dir.y != 0) {
                this.clientPlayer.deltaMovement = dir.normalize().multiply(SPEED);
                // if moving diagonal
                if (dir.x != 0 && dir.y != 0) {
                    this.clientPlayer.position.x = this.clientPlayer.position.add(dir.normalize().multiply(SPEED)).x;
                    this.clientPlayer.handleCollisions();
                    this.clientPlayer.position.y = this.clientPlayer.position.add(dir.normalize().multiply(SPEED)).y;
                } else {
                    this.clientPlayer.position = this.clientPlayer.position.add(dir.normalize().multiply(SPEED));
                }
            }

            // send client delta movement to server
            try {
                this.client.sendPacket(new MovementC2SPacket(dir.normalize().multiply(SPEED)));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        this.clientPlayer.handleCollisions();


        // calling repaint() will trigger paintComponent() to run again,
        // which will refresh/redraw the graphics.
        repaint();
    }

    public ClientPlayer getClientPlayerByUsername(String username) {
        ClientPlayer respectivePlayer = null;
        for (ClientPlayer player : players) {
            if (player.username.equals(username)) {
                respectivePlayer = player;
            }
        }
        return respectivePlayer;
    }
}
