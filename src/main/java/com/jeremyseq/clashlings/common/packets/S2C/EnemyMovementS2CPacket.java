package com.jeremyseq.clashlings.common.packets.S2C;

import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.common.Goblin;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.common.Vec2;
import com.jeremyseq.clashlings.server.ServerGame;

import java.io.Serial;
import java.net.Socket;

public class EnemyMovementS2CPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final long id;
    private final String movement;

    public EnemyMovementS2CPacket(long id, Vec2 movement) {
        this.id = id;
        this.movement = movement.toPacketString();
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {

    }

    @Override
    public void handle(Game game) {
        Vec2 movementVec = Vec2.fromString(this.movement);
        Goblin enemy = game.enemies.get(this.id);
        if (enemy != null) {
            enemy.deltaMovement = movementVec;
        }
    }
}
