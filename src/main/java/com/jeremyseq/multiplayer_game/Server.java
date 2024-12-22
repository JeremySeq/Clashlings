package main.java.com.jeremyseq.multiplayer_game;

import main.java.com.jeremyseq.multiplayer_game.common.Goblin;
import main.java.com.jeremyseq.multiplayer_game.common.Packet;
import main.java.com.jeremyseq.multiplayer_game.common.packets.S2C.*;
import main.java.com.jeremyseq.multiplayer_game.server.ServerGame;
import main.java.com.jeremyseq.multiplayer_game.server.ServerPlayer;

import java.net.*;
import java.io.*;
import java.util.HashMap;

public class Server
{
    //initialize socket and input stream
    private ServerSocket server = null;
    private final ServerGame serverGame = new ServerGame(this);
    public HashMap<Socket, ObjectOutputStream> outputStreamHashMap = new HashMap<>();

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

        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            outputStreamHashMap.put(socket, out);

            sendPacket(out, new SendLevelS2CPacket(this.serverGame.level.toJson()));

            while (true) {
                Packet packet = (Packet) in.readObject();
                packet.handle(serverGame, socket);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Received seriously messed up packet.");
        } catch (SocketException socketException) {
            System.out.println("Lost connection to client.");
        }

        System.out.println("Closing connection and removing player.");
        String username = serverGame.getPlayerBySocket(socket).username;
        serverGame.removePlayer(socket);

        this.sendToEachPlayer(new PlayerDisconnectedS2CPacket(username));

        // close connection
        socket.close();
    }

    public void sendPacket(ObjectOutputStream out, Packet packet) throws IOException {
        out.writeObject(packet);
    }

    public void sendPacket(Socket socket, Packet packet) throws IOException {
        outputStreamHashMap.get(socket).writeObject(packet);
    }

    public void serverTick() throws IOException, InterruptedException {
        while (true)
        {
            Thread.sleep(10); // 100 ticks per second

            if (!this.serverGame.players.isEmpty()) {
                for (Goblin goblin : this.serverGame.enemies.values()) {
                    goblin.tick();
                }
            }

            for (ServerPlayer player : this.serverGame.players) {
                ObjectOutputStream out = outputStreamHashMap.get(player.socket);

                for (ServerPlayer otherPlayer : this.serverGame.players) {
                    if (player == otherPlayer) {
                        continue;
                    }

                    // send other player positions to clients
                    this.sendPacket(out, new PlayerPositionS2CPacket(otherPlayer.username, otherPlayer.pos));
                    this.sendPacket(out, new PlayerMovementS2CPacket(otherPlayer.username, otherPlayer.deltaMovement));
                }

                for (Goblin goblin : this.serverGame.enemies.values()) {
                    // send server enemy positions to clients
                    this.sendPacket(out, new EnemyPositionS2CPacket(goblin.id, goblin.position));
                    this.sendPacket(out, new EnemyMovementS2CPacket(goblin.id, goblin.deltaMovement));
                }
            }
        }
    }

    public void sendToEachPlayer(Packet packet) throws IOException {
        for (ServerPlayer player : this.serverGame.players) {
            ObjectOutputStream out = outputStreamHashMap.get(player.socket);
            out.writeObject(packet);
        }
    }

    public static void main(String[] args)
    {
        Server server = new Server(5000);
    }
}
