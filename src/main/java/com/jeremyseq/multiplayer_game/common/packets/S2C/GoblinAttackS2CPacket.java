package com.jeremyseq.multiplayer_game.common.packets.S2C;

import com.jeremyseq.multiplayer_game.client.Game;
import com.jeremyseq.multiplayer_game.common.AttackState;
import com.jeremyseq.multiplayer_game.common.Packet;
import com.jeremyseq.multiplayer_game.server.ServerGame;

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
