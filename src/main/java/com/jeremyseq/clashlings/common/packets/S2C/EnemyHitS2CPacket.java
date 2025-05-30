package com.jeremyseq.clashlings.common.packets.S2C;

import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.common.Goblin;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.server.ServerGame;

import java.io.Serial;
import java.net.Socket;

public class EnemyHitS2CPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final long id;
    private final int newHealth;

    public EnemyHitS2CPacket(long id, int newHealth) {
        this.id = id;
        this.newHealth = newHealth;
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {

    }

    @Override
    public void handle(Game game) {
        Goblin enemy = game.enemies.get(this.id);
        if (enemy != null) {
            int damageDone = enemy.health - newHealth;
            enemy.hurt(damageDone);
        }
    }
}
