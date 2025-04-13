package com.jeremyseq.multiplayer_game.server;

import com.jeremyseq.multiplayer_game.Server;
import com.jeremyseq.multiplayer_game.client.LevelRenderer;
import com.jeremyseq.multiplayer_game.common.Goblin;
import com.jeremyseq.multiplayer_game.common.Logger;
import com.jeremyseq.multiplayer_game.common.level.Level;
import com.jeremyseq.multiplayer_game.common.level.LevelReader;
import com.jeremyseq.multiplayer_game.common.Vec2;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;

public class ServerGame {
    public Level level = new LevelReader().readLevel("level1");
    public ArrayList<ServerPlayer> players = new ArrayList<>();
    public Hashtable<Long, Goblin> enemies = new Hashtable<>();
    public Server server;

    public final Logger LOGGER = new Logger("Server");

    public ServerGame(Server server) {
        this.server = server;
        enemies.put(0L, new Goblin(this, level, new Vec2(0, 0)));
    }

    public void tick() {
        for (ServerPlayer player : players) {
            player.tick();
        }
    }

    public ServerPlayer getPlayerBySocket(Socket socket) {
        for (ServerPlayer player : players) {
            if (player.socket.equals(socket)) {
                return player;
            }
        }
        return null;
    }

    public void removePlayer(Socket socket) {
        ServerPlayer serverPlayer = getPlayerBySocket(socket);
        players.remove(serverPlayer);
    }

    public void connectPlayer(String username, Socket socket) {
        // add player with spawn position specified by level
        players.add(new ServerPlayer(this, socket, username,
                this.level.getWorldPositionFromTilePosition(level.metadata.spawn, LevelRenderer.DRAW_SIZE)));
    }

    public boolean isValidUsername(String username) {
        if (username.contains(",") || username.contains("$") || username.contains(":")) {
            return false;
        }
        for (ServerPlayer player : players) {
            if (Objects.equals(player.username, username)) {
                return false;
            }
        }
        return true;
    }
}
