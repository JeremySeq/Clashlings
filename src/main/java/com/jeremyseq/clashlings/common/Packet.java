package com.jeremyseq.clashlings.common;

import com.jeremyseq.clashlings.client.Game;
import com.jeremyseq.clashlings.server.ServerGame;

import java.io.Serial;
import java.io.Serializable;
import java.net.Socket;

public abstract class Packet implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public abstract void handle(ServerGame serverGame, Socket socket);

    public abstract void handle(Game game);
}