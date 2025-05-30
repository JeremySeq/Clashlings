package com.jeremyseq.clashlings.common.packets.S2C;

import com.jeremyseq.clashlings.client.ClientPlayer;
import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.common.AttackState;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.server.ServerGame;

import java.io.Serial;
import java.net.Socket;

public class AttackS2CPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String attackSide;

    public AttackS2CPacket(String username, String attackSide) {
        this.username = username;
        this.attackSide = attackSide;
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {

    }

    @Override
    public void handle(Game game) {
        ClientPlayer player = game.getClientPlayerByUsername(username);
        AttackState facing = AttackState.valueOf(this.attackSide);
        player.attack(facing);
    }
}
