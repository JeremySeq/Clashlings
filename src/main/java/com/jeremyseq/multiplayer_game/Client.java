package com.jeremyseq.multiplayer_game;

import com.jeremyseq.multiplayer_game.client.App;
import com.jeremyseq.multiplayer_game.client.Game;
import com.jeremyseq.multiplayer_game.common.packets.C2S.ConnectC2SPacket;
import com.jeremyseq.multiplayer_game.common.Packet;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

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
            socket = new Socket(address, port);

            input = new DataInputStream(System.in);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            Game.LOGGER.info("Connected to server");
        }
        catch (ConnectException e) {
            Game.LOGGER.error(String.valueOf(e));
            Game.LOGGER.error("Couldn't connect to server. Closing.");
            return;
        }
        catch (IOException i) {
            Game.LOGGER.error(String.valueOf(i));
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
        // Enter data using BufferReader
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        // Reading data using readLine
        String ip;
        if (args.length > 0 && args[0] != null) {
            ip = args[0];
        } else {
            System.out.print("IP Address: ");
            ip = reader.readLine();
        }
//        System.out.print("Port: ");
//        String port = reader.readLine();
        Client client = new Client(ip, 5000);
    }
}