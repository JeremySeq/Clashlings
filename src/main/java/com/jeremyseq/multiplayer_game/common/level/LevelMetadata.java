package main.java.com.jeremyseq.multiplayer_game.common.level;

import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

public class LevelMetadata {
    public int layers;
    public Vec2 spawn;

    public LevelMetadata(int layers, Vec2 spawn) {
        this.layers = layers;
        this.spawn = spawn;
    }
}
