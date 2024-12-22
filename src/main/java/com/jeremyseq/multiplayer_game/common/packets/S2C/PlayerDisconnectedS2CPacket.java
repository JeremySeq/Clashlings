package main.java.com.jeremyseq.multiplayer_game.common.packets.S2C;

import main.java.com.jeremyseq.multiplayer_game.client.Game;
import main.java.com.jeremyseq.multiplayer_game.common.Packet;
import main.java.com.jeremyseq.multiplayer_game.server.ServerGame;

import java.io.Serial;
import java.net.Socket;

public class PlayerDisconnectedS2CPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;

    public PlayerDisconnectedS2CPacket(String username) {
        this.username = username;
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {

    }

    @Override
    public void handle(Game game) {
        game.players.removeIf((clientPlayer -> clientPlayer.username.equals(this.username)));
    }
}
