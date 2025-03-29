package com.jeremyseq.multiplayer_game.common.packets.S2C;

import com.jeremyseq.multiplayer_game.client.Game;
import com.jeremyseq.multiplayer_game.client.LevelRenderer;
import com.jeremyseq.multiplayer_game.common.Packet;
import com.jeremyseq.multiplayer_game.common.Vec2;
import com.jeremyseq.multiplayer_game.common.level.LevelReader;
import com.jeremyseq.multiplayer_game.common.level.Tile;
import com.jeremyseq.multiplayer_game.server.ServerGame;

import java.io.Serial;
import java.net.Socket;
import java.util.List;

/**
 * Sends the Level to the client. Should only be used once on connection. (Or I guess in the future if switching levels.)
 * The client will set the player's position to the level's spawn position.
 */
public class SendLevelS2CPacket extends Packet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String level;

    public SendLevelS2CPacket(String level) {
        this.level = level;
    }

    @Override
    public void handle(ServerGame serverGame, Socket socket) {

    }

    @Override
    public void handle(Game game) {
        game.level = new LevelReader().readLevelString(level);
        game.levelRenderer.setLevel(game.level);

        // set the player position to the spawn point
        game.clientPlayer.position = game.level.getWorldPositionFromTilePosition(game.level.metadata.spawn, LevelRenderer.DRAW_SIZE);

        // set player layer based on spawn point, because it's not always going to be 1
        Vec2 spawnTilePos = game.level.metadata.spawn;
        int maxLayer = 0;
        for (int layer = 1; layer <= game.level.metadata.layers; layer++) {
            List<Tile> tiles = game.level.tiles.get(String.valueOf(layer));
            for (Tile tile : tiles) {
                if (tile.x == spawnTilePos.x && tile.y == spawnTilePos.y) {
                    maxLayer = layer;
                    break;
                }
            }
        }
        game.clientPlayer.currentLayer = maxLayer;
    }
}
