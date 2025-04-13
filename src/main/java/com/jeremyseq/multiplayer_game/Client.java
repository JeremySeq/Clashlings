package com.jeremyseq.multiplayer_game;

import com.jeremyseq.multiplayer_game.client.App;
import com.jeremyseq.multiplayer_game.client.Game;
import com.jeremyseq.multiplayer_game.common.Constants;
import com.jeremyseq.multiplayer_game.common.packets.C2S.ConnectC2SPacket;
import com.jeremyseq.multiplayer_game.common.Packet;

import java.io.*;
import java.net.*;

public class Client {
    // initialize socket and input output streams
    private Socket socket = null;
    private ObjectOutputStream out;
    public ObjectInputStream in;
    private DataInputStream input = null;
    public String username;

    // constructor to put ip address and port
    public Client(String address, int port)
    {
        // establish a connection
        try {
            SocketAddress socketAddress = new InetSocketAddress(address, port);
            socket = new Socket();
            socket.connect(socketAddress, 3000); // three second connection timeout

            input = new DataInputStream(System.in);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            Game.LOGGER.info("Connected to server");
        }
        catch (SocketTimeoutException e) {
            Game.LOGGER.error("Connection timed out: " + e.getMessage());
            return;
        }
        catch (ConnectException e) {
            Game.LOGGER.error("Couldn't connect to server: " + e.getMessage());
            return;
        }
        catch (IOException i) {
            Game.LOGGER.error("I/O error: " + i.getMessage());
            return;
        }

        try {
            System.out.print("Username: ");
            username = input.readLine();
            sendPacket(new ConnectC2SPacket(username));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Thread gameThread = new Thread(() -> {
            App.runApp(this);
        });
        gameThread.start();
    }

    public void sendPacket(Packet packet) throws IOException {
        out.writeObject(packet);
    }

    public static void main(String[] args) throws IOException {
        String ip = null;
        int port = Constants.DEFAULT_PORT;

        for (String arg : args) {
            if (arg.startsWith("--ip=")) {
                ip = arg.substring("--ip=".length());
            } else if (arg.equals("--debug")) {
                Game.LOGGER.DEBUG_MODE = true;
            }
        }

        if (ip == null) {
            System.out.print("IP Address: ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            ip = reader.readLine();
        }

        if (ip.contains(":")) {
            String[] parts = ip.split(":", 2);
            ip = parts[0];
            port = Integer.parseInt(parts[1]);
        }

        Client client = new Client(ip, port);
    }

}