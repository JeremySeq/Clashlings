package com.jeremyseq.clashlings.common.packets.S2C;

import com.jeremyseq.clashlings.client.ClientPlayer;
import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.server.ServerGame;

import java.io.Serial;
import java.net.Socket;

public class PlayerHitS2CPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final float newHealth;

    public PlayerHitS2CPacket(String username, float newHealth) {
        this.username = username;
        this.newHealth = newHealth;
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {

    }

    @Override
    public void handle(Game game) {
        ClientPlayer player = game.getClientPlayerByUsername(this.username);
        player.hurt(this.newHealth);
    }
}
