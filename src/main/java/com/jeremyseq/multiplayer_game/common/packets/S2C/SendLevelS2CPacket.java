package main.java.com.jeremyseq.multiplayer_game.common.packets.S2C;

import main.java.com.jeremyseq.multiplayer_game.client.Game;
import main.java.com.jeremyseq.multiplayer_game.common.Packet;
import main.java.com.jeremyseq.multiplayer_game.common.level.LevelReader;
import main.java.com.jeremyseq.multiplayer_game.server.ServerGame;

import java.io.Serial;
import java.net.Socket;

public class SendLevelS2CPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String level;

    public SendLevelS2CPacket(String level) {
        this.level = level;
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {

    }

    @Override
    public void handle(Game game) {
        game.level = new LevelReader().readLevelString(level);
    }
}
