package com.jeremyseq.clashlings.common.packets.C2S;

import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.common.AttackState;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.common.packets.S2C.AttackS2CPacket;
import com.jeremyseq.clashlings.server.ServerGame;
import com.jeremyseq.clashlings.server.ServerPlayer;

import java.io.IOException;
import java.io.Serial;
import java.net.Socket;

public class BeginAttackC2SPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String attackSide;

    public BeginAttackC2SPacket(String attackSide) {
        this.attackSide = attackSide;
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {
        ServerPlayer player = serverGame.getPlayerBySocket(socket);
        AttackState attackState = AttackState.valueOf(this.attackSide);
        player.beginAttack(attackState);

        for (ServerPlayer otherPlayer : serverGame.players) {
            if (player == otherPlayer) {
                continue;
            }

            // send the attack from the server to all clients
            try {
                serverGame.server.sendPacket(serverGame.server.outputStreamHashMap.get(otherPlayer.socket),
                        new AttackS2CPacket(player.username, this.attackSide));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void handle(Game game) {

    }
}
