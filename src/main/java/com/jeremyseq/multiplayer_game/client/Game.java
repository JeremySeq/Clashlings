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
//
//        int width = 10;
//        int height = 10;
//        int layers = 3;
//        Grid grid = new Grid(width, height, layers);
//
//        grid.setObstacle(4, 4, 0);
//        grid.setObstacle(4, 5, 0);
//        grid.setObstacle(4, 6, 0);
//        grid.setObstacle(4, 7, 0);
//
//        // Set ground nodes
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                grid.setGround(x, y, 0);
//                grid.setGround(x, y, 1);
//                grid.setGround(x, y, 2);
//            }
//        }
//
//        // Set staircases
//        grid.setStair(5, 5, 0, 1);
//        grid.setStair(5, 5, 1, 0);
//
//        grid.setStair(7, 7, 1, 2);
//        grid.setStair(7, 7, 2, 1);
//
//        for (int k = 0; k < grid.getLayers(); k++) {
//            for (int i = 0; i < grid.getWidth(); i++) {
//                for (int j = 0; j < grid.getWidth(); j++) {
//                    if (grid.getNode(i, j, k).isStair()) {
//                        Vec2 renderPos = new Vec2(grid.getNode(i, j, k).getX() * levelRenderer.drawSize, grid.getNode(i, j, k).getY() * levelRenderer.drawSize);
//                        renderPos = this.getRenderPositionFromWorldPosition(renderPos);
//                        g.setColor(Color.BLUE);
//                        g.fillRect((int) renderPos.x, (int) renderPos.y, levelRenderer.drawSize, levelRenderer.drawSize);
//                    }
//
//                }
//            }
//        }
//
//        AStarPathfinding pathfinding = new AStarPathfinding(grid);
//        Node start = grid.getNode(0, 0, 0);
//        Node end = grid.getNode(9, 9, 2);
//        List<Node> path = pathfinding.findPath(start, end);
//        for (Node node : path) {
//            Vec2 renderPos = new Vec2(node.getX() * levelRenderer.drawSize, node.getY() * levelRenderer.drawSize);
//            renderPos = this.getRenderPositionFromWorldPosition(renderPos);
//            int x2 = (int) renderPos.x;
//            int y2 = (int) renderPos.y;
//            g.setColor(Color.WHITE);
//            g.drawRect(x2, y2, levelRenderer.drawSize, levelRenderer.drawSize);
//        }
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
