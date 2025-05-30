package com.jeremyseq.clashlings.common.packets.S2C;

import com.jeremyseq.clashlings.client.ClientPlayer;
import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.common.Vec2;
import com.jeremyseq.clashlings.server.ServerGame;

import java.io.Serial;
import java.net.Socket;

public class PlayerPositionS2CPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String position;
    private final String username;

    public PlayerPositionS2CPacket(String username, Vec2 position) {
        this.username = username;
        this.position = position.toPacketString();
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {

    }

    @Override
    public void handle(Game game) {
        Vec2 position = Vec2.fromString(this.position);
        ClientPlayer player = game.getClientPlayerByUsername(username);
        if (player == null) {
            player = new ClientPlayer(game, username, position);
            game.players.add(player);
        } else {
            player.position = position;
        }
    }
}
