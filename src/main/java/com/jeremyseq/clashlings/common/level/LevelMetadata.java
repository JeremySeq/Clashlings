package com.jeremyseq.clashlings.common.level;

import com.jeremyseq.clashlings.common.Vec2;

public class LevelMetadata {
    public int layers;
    public Vec2 spawn;

    public LevelMetadata(int layers, Vec2 spawn) {
        this.layers = layers;
        this.spawn = spawn;
    }
}
