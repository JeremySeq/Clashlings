package main.java.com.jeremyseq.multiplayer_game.common.packets.C2S;


import main.java.com.jeremyseq.multiplayer_game.client.Game;
import main.java.com.jeremyseq.multiplayer_game.common.Packet;
import main.java.com.jeremyseq.multiplayer_game.server.ServerGame;

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
        System.out.println("Connected player: " + username);
    }

    @Override
    public void handle(Game game) {

    }
}
