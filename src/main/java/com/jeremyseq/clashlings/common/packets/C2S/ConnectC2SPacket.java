package com.jeremyseq.clashlings.common.packets.C2S;


import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.common.Packet;
import com.jeremyseq.clashlings.server.ServerGame;

import java.io.Serial;
import java.net.Socket;

public class ConnectC2SPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;

    public ConnectC2SPacket(String username) {
        this.username = username;
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {
        serverGame.connectPlayer(this.username, socket);
        ServerGame.LOGGER.info(username + " joined the game");
    }

    @Override
    public void handle(Game game) {

    }
}
