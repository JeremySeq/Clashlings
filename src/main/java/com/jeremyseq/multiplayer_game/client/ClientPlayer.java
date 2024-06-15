package main.java.com.jeremyseq.multiplayer_game.client;

import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

public class ClientPlayer {
    public final String username;
    public Vec2 position;

    public ClientPlayer(String username, Vec2 position) {
        this.username = username;
        this.position = position;
    }
}
