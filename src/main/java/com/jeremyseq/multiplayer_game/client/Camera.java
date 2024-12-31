package com.jeremyseq.multiplayer_game.client;

import com.jeremyseq.multiplayer_game.common.Vec2;

public interface Camera {
    int getDisplayWidth();
    int getDisplayHeight();
    Vec2 getRenderPositionFromWorldPosition(Vec2 vec2);
    Vec2 getWorldPositionFromRenderPosition(Vec2 vec2);
}
