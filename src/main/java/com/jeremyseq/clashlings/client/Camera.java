package com.jeremyseq.clashlings.client;

import com.jeremyseq.clashlings.common.Vec2;

public interface Camera {
    int getDisplayWidth();
    int getDisplayHeight();
    Vec2 getRenderPositionFromWorldPosition(Vec2 vec2);
    Vec2 getWorldPositionFromRenderPosition(Vec2 vec2);
}
