package com.jeremyseq.clashlings.common.packets.S2C;

import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.common.Goblin;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.common.Vec2;
import com.jeremyseq.clashlings.server.ServerGame;

import java.io.Serial;
import java.net.Socket;

public class EnemyPositionS2CPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final long id;
    private final String position;

    public EnemyPositionS2CPacket(long id, Vec2 position) {
        this.id = id;
        this.position = position.toPacketString();
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {

    }

    @Override
    public void handle(Game game) {
        Vec2 position = Vec2.fromString(this.position);
        Goblin enemy = game.enemies.get(this.id);
        if (enemy == null) {
            enemy = new Goblin(this.id, game, game.level, position);
            game.enemies.put(this.id, enemy);
        } else {
            enemy.position = position;
        }
    }
}
