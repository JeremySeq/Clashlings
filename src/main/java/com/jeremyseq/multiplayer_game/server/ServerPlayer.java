package main.java.com.jeremyseq.multiplayer_game.server;

import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

import java.net.Socket;

public class ServerPlayer {
    public final Socket socket;
    public String username;
    public Vec2 pos;
    public Vec2 deltaMovement = new Vec2(0, 0);
    public ServerPlayer(Socket socket, String username, Vec2 pos) {
        this.socket = socket;
        this.username = username;
        this.pos = pos;
    }
}
