package main.java.com.jeremyseq.multiplayer_game.common.packets.C2S;

import main.java.com.jeremyseq.multiplayer_game.client.Game;
import main.java.com.jeremyseq.multiplayer_game.common.Packet;
import main.java.com.jeremyseq.multiplayer_game.common.Vec2;
import main.java.com.jeremyseq.multiplayer_game.server.ServerGame;
import main.java.com.jeremyseq.multiplayer_game.server.ServerPlayer;

import java.io.Serial;
import java.net.Socket;

public class PositionC2SPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String position;

    public PositionC2SPacket(Vec2 position) {
        this.position = position.toPacketString();
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {
        Vec2 position = Vec2.fromString(this.position);
        ServerPlayer player = serverGame.getPlayerBySocket(socket);
        player.pos = position;
    }

    @Override
    public void handle(Game game) {

    }
}
