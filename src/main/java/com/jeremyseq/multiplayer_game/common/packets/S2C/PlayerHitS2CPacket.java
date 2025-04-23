package com.jeremyseq.multiplayer_game.common.packets.S2C;

import com.jeremyseq.multiplayer_game.client.ClientPlayer;
import com.jeremyseq.multiplayer_game.client.Game;
import com.jeremyseq.multiplayer_game.common.Packet;
import com.jeremyseq.multiplayer_game.server.ServerGame;

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
