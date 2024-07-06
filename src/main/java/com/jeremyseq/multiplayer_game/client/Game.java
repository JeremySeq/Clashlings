package main.java.com.jeremyseq.multiplayer_game.client;

import main.java.com.jeremyseq.multiplayer_game.Client;
import main.java.com.jeremyseq.multiplayer_game.common.AttackState;
import main.java.com.jeremyseq.multiplayer_game.common.Goblin;
import main.java.com.jeremyseq.multiplayer_game.common.level.Level;
import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

public class Game extends JPanel implements ActionListener {

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

    private Timer timer;

    public Game(Client client) {
        this.client = client;

        this.clientPlayer = new ClientPlayer(this, client.username, new Vec2(0, 300));
        this.players.add(clientPlayer);

        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // get level from server
        try {
            String string = client.server_response.readUTF();
            ClientInterpretPacket.interpretPacket(this, string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Thread receiveServerResponses = new Thread(() -> {
            while (true) {
                try {
                    ClientInterpretPacket.interpretPacket(this, client.server_response.readUTF());
                } catch (IOException e) {
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
        // when calling g.drawImage() we can use "this" for the ImageObserver
        // because Component implements the ImageObserver interface, and JPanel
        // extends from Component. So "this" main.java.com.seq.chess.Board instance, as a Component, can
        // react to imageUpdate() events triggered by g.drawImage()

        // draw our graphics.
        drawBackground(g);

        try {
            client.out.writeUTF("$pos:" + clientPlayer.position.toPacketString());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        // this smooths out animations on some systems
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        levelRenderer.draw(g, this);

        enemy.draw(g, this);

        for (ClientPlayer player : this.players) {
            player.draw(g, this);
        }
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

        this.enemy.tick();

        // this method is called by the timer every DELAY ms.
        // use this space to update the state of your game or animation
        // before the graphics are redrawn.

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
            try {
                client.out.writeUTF("$movement:" + dir.normalize().multiply(SPEED).toPacketString());
            } catch (IOException ex) {
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
