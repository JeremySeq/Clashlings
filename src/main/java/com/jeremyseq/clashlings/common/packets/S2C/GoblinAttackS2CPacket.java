package com.jeremyseq.clashlings.common.packets.S2C;

import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.common.AttackState;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.server.ServerGame;

import java.io.Serial;
import java.net.Socket;

public class GoblinAttackS2CPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final long id;
    private final AttackState attackState;

    public GoblinAttackS2CPacket(long id, AttackState attackState) {
        this.id = id;
        this.attackState = attackState;
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {

    }

    @Override
    public void handle(Game game) {
        game.enemies.get(id).attacking = attackState;
    }
}
