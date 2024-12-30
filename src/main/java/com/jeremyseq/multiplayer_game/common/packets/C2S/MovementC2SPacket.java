package com.jeremyseq.multiplayer_game.common.packets.C2S;

import com.jeremyseq.multiplayer_game.client.Game;
import com.jeremyseq.multiplayer_game.common.Packet;
import com.jeremyseq.multiplayer_game.common.Vec2;
import com.jeremyseq.multiplayer_game.server.ServerGame;
import com.jeremyseq.multiplayer_game.server.ServerPlayer;

import java.io.Serial;
import java.net.Socket;

public class MovementC2SPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String movement;

    public MovementC2SPacket(Vec2 movement) {
        this.movement = movement.toPacketString();
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {
        Vec2 movementVec = Vec2.fromString(this.movement);
        ServerPlayer player = serverGame.getPlayerBySocket(socket);
        player.deltaMovement = movementVec;
    }

    @Override
    public void handle(Game game) {

    }
}
