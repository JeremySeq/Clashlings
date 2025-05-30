package com.jeremyseq.clashlings;

import com.jeremyseq.clashlings.common.Constants;
import com.jeremyseq.clashlings.common.Goblin;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.common.packets.S2C.*;
import com.jeremyseq.clashlings.server.ServerGame;
import com.jeremyseq.clashlings.server.ServerPlayer;

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
            ServerGame.LOGGER.info("Server started on port " + port);

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
                ServerGame.LOGGER.debug("Client accepted");

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
            ServerGame.LOGGER.warning("Received seriously messed up packet.");
        } catch (SocketException socketException) {
            ServerGame.LOGGER.debug("Lost connection to client.");
        }

        String username = serverGame.getPlayerBySocket(socket).username;
        serverGame.removePlayer(socket);
        ServerGame.LOGGER.info(username + " left the game");

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

            this.serverGame.tick();

            if (!this.serverGame.players.isEmpty()) {
                for (Goblin goblin : this.serverGame.enemies.values()) {
                    goblin.serverTick();
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

    public void sendToEachPlayer(Packet packet) {
        for (ServerPlayer player : this.serverGame.players) {
            ObjectOutputStream out = outputStreamHashMap.get(player.socket);
            try {
                out.writeObject(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args)
    {
        int port = Constants.DEFAULT_PORT;

        for (String arg : args) {
            if (arg.equals("--debug")) {
                ServerGame.LOGGER.DEBUG_MODE = true;
            } else if (arg.startsWith("--port=")) {
                try {
                    int parsedPort = Integer.parseInt(arg.substring("--port=".length()));
                    if (parsedPort < 0 || parsedPort > 65535) {
                        ServerGame.LOGGER.warning("Invalid port. Using default: " + port);
                        continue;
                    }
                    port = parsedPort;
                } catch (NumberFormatException e) {
                    ServerGame.LOGGER.warning("Invalid port. Using default: " + port);
                }
            }
        }

        Server server = new Server(port);
    }

}
