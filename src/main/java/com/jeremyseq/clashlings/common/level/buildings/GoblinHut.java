package com.jeremyseq.clashlings.common.level.buildings;

import com.jeremyseq.clashlings.common.Goblin;
import com.jeremyseq.clashlings.common.Vec2;
import com.jeremyseq.clashlings.common.level.Building;
import com.jeremyseq.clashlings.common.level.BuildingState;
import com.jeremyseq.clashlings.common.level.BuildingType;
import com.jeremyseq.clashlings.common.level.Level;
import com.jeremyseq.clashlings.server.ServerGame;

public class GoblinHut extends Building {
    private int tickCounter = 0;
    private static final int SPAWN_INTERVAL = 500; // Adjust the interval as neede

    public GoblinHut(BuildingType type, int x, int y, BuildingState state) {
        super(type, x, y, state);
    }

    @Override
    public void tick(ServerGame serverGame) {
        tickCounter++;
        if (tickCounter >= SPAWN_INTERVAL) {
            spawnGoblin(serverGame);
            tickCounter = 0;
        }
    }

    private void spawnGoblin(ServerGame serverGame) {
        long time = System.nanoTime();
        Vec2 worldPosition = serverGame.level.getWorldPositionFromTilePosition(new Vec2(this.x, this.y+2), Level.WORLD_TILE_SIZE);
        worldPosition.x += Level.WORLD_TILE_SIZE / 2f;
        serverGame.enemies.put(time, new Goblin(time, serverGame, serverGame.level, worldPosition));
    }
}