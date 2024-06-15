package main.java.com.jeremyseq.multiplayer_game.client;

import main.java.com.jeremyseq.multiplayer_game.Client;
import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;

public class Game extends JPanel implements ActionListener {

    public static final int WIDTH = 640;
    public static final int HEIGHT = 900;
    private static final float SPEED = 5;

    public final int DELAY = 20;
    private final Client client;

    public Vec2 position = new Vec2(0, 0);

    public ArrayList<ClientPlayer> players = new ArrayList<>();

    private KeyHandler keyHandler = new KeyHandler();

    private Timer timer;

    public Game(Client client) {
        this.client = client;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

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


        // this timer will call the actionPerformed() method every DELAY ms
        timer = new Timer(DELAY, this);
        timer.start();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

            }
        });

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
            client.out.writeUTF("$pos:" + position.toPacketString());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        // this smooths out animations on some systems
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        for (ClientPlayer player : this.players) {
            g.setColor(Color.RED);
            g.fillRect((int) player.position.x - 10, (int) player.position.y - 10, 20, 20);

            g.setFont(new Font("Jetbrains Mono", Font.PLAIN, 16));
            Rectangle2D bounds = g.getFont().getStringBounds(player.username, g.getFontMetrics().getFontRenderContext());
            g.drawString(player.username, (int) ((int) player.position.x - bounds.getWidth()/2), (int) ((int) player.position.y + bounds.getHeight() + 4));
        }

        g.setColor(Color.WHITE);
        g.fillRect((int) position.x - 10, (int) position.y - 10, 20, 20);
        g.setFont(new Font("Jetbrains Mono", Font.PLAIN, 16));
        Rectangle2D bounds = g.getFont().getStringBounds(client.username, g.getFontMetrics().getFontRenderContext());
        g.drawString(client.username, (int) ((int) position.x - bounds.getWidth()/2), (int) ((int) position.y + bounds.getHeight() + 4));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // this method is called by the timer every DELAY ms.
        // use this space to update the state of your game or animation
        // before the graphics are redrawn.

        if (keyHandler.leftPressed) {
            position.x -= SPEED;
        }
        if (keyHandler.rightPressed) {
            position.x += SPEED;
        }
        if (keyHandler.upPressed) {
            position.y -= SPEED;
        }
        if (keyHandler.downPressed) {
            position.y += SPEED;
        }

        // calling repaint() will trigger paintComponent() to run again,
        // which will refresh/redraw the graphics.
        repaint();
    }

    public void keyPressed(KeyEvent e) {

    }
}
