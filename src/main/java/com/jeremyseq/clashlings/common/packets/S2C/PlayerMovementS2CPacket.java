package com.jeremyseq.clashlings.common.packets.S2C;

import com.jeremyseq.clashlings.client.ClientPlayer;
import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.common.Vec2;
import com.jeremyseq.clashlings.server.ServerGame;

import java.io.Serial;
import java.net.Socket;

public class PlayerMovementS2CPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String movement;
    private final String username;

    public PlayerMovementS2CPacket(String username, Vec2 movement) {
        this.username = username;
        this.movement = movement.toPacketString();
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {

    }

    @Override
    public void handle(Game game) {
        Vec2 movementVec = Vec2.fromString(this.movement);
        ClientPlayer player = game.getClientPlayerByUsername(username);
        if (player != null) {
            player.deltaMovement = movementVec;
        }
    }
}
