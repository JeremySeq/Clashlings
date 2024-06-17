package main.java.com.jeremyseq.multiplayer_game;

import main.java.com.jeremyseq.multiplayer_game.server.ServerInterpretPacket;
import main.java.com.jeremyseq.multiplayer_game.server.ServerGame;
import main.java.com.jeremyseq.multiplayer_game.server.ServerPlayer;

import java.net.*;
import java.io.*;

public class Server
{
    //initialize socket and input stream
    private ServerSocket server = null;
    private final ServerGame serverGame = new ServerGame();

    // constructor with port
    public Server(int port)
    {
        // starts server and waits for a connection
        try
        {
            server = new ServerSocket(port);
            System.out.println("Server started");

            Thread serverTickThread = new Thread(() -> {
                try {
                    serverTick();
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            });
            serverTickThread.start();

            while (true) {
                Socket socket = server.accept();
                System.out.println("Client accepted");

                Thread clientListener = new Thread(() -> {
                    try {
                        acceptClient(socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                clientListener.start();

            }
        }
        catch(IOException i)
        {
            i.printStackTrace();
        }
    }

    public void acceptClient(Socket socket) throws IOException {
        // takes input from the client socket
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(
                socket.getOutputStream());

        out.writeUTF("$level:" + this.serverGame.level.toJson());

        String line = "";
        // reads message from client until "Over" is sent
        while (!line.equals("Over"))
        {
            try
            {
                line = in.readUTF();
                ServerInterpretPacket.interpretPacket(socket, this.serverGame, out, line);
            }
            catch(IOException i)
            {
                break;
            }
        }
        System.out.println("Closing connection and removing player");
        serverGame.removePlayer(socket);

        // close connection
        socket.close();
        in.close();
    }

    public void serverTick() throws InterruptedException, IOException {
        while (true)
        {
            Thread.sleep(10); // 100 ticks per second

            for (ServerPlayer player : this.serverGame.players) {
                DataOutputStream out = new DataOutputStream(player.socket.getOutputStream());

                for (ServerPlayer otherPlayer : this.serverGame.players) {
                    if (player == otherPlayer) {
                        continue;
                    }
                    out.writeUTF("$pos:username=" + otherPlayer.username + "$" + "pos=" + otherPlayer.pos.toPacketString());
                    out.writeUTF("$movement." + otherPlayer.username + ":" + otherPlayer.deltaMovement.toPacketString());
                }
            }
        }
    }

    public static void main(String[] args)
    {
        Server server = new Server(5000);
    }
}
