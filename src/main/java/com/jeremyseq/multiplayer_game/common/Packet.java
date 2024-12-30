package com.jeremyseq.multiplayer_game.common;

import com.jeremyseq.multiplayer_game.client.Game;
import com.jeremyseq.multiplayer_game.server.ServerGame;

import java.io.Serial;
import java.io.Serializable;
import java.net.Socket;

public abstract class Packet implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public abstract void handle(ServerGame serverGame, Socket socket);

    public abstract void handle(Game game);
}