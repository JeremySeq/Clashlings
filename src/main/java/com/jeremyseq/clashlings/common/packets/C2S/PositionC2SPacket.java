package com.jeremyseq.clashlings.common.packets.C2S;

import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.common.Vec2;
import com.jeremyseq.clashlings.server.ServerGame;
import com.jeremyseq.clashlings.server.ServerPlayer;

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
