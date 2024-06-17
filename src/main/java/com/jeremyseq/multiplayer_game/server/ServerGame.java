package main.java.com.jeremyseq.multiplayer_game.server;

import main.java.com.jeremyseq.multiplayer_game.common.level.Level;
import main.java.com.jeremyseq.multiplayer_game.common.level.LevelReader;
import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class ServerGame {
    public Level level = new LevelReader().readLevel("level1");
    public ArrayList<ServerPlayer> players = new ArrayList<>();

    public ServerGame() {

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
        players.add(new ServerPlayer(socket, username, new Vec2(0, 0)));
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
