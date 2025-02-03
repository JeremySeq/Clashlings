package com.jeremyseq.multiplayer_game.server;

import com.jeremyseq.multiplayer_game.common.AttackState;
import com.jeremyseq.multiplayer_game.common.Goblin;
import com.jeremyseq.multiplayer_game.common.Hitbox;
import com.jeremyseq.multiplayer_game.common.Vec2;
import com.jeremyseq.multiplayer_game.common.packets.S2C.EnemyHitS2CPacket;

import java.io.IOException;
import java.net.Socket;

public class ServerPlayer implements Hitbox {
    public final Socket socket;
    public final ServerGame serverGame;
    public String username;
    public Vec2 pos;
    public Vec2 deltaMovement = new Vec2(0, 0);

    public ServerPlayer(ServerGame serverGame, Socket socket, String username, Vec2 pos) {
        this.serverGame = serverGame;
        this.socket = socket;
        this.username = username;
        this.pos = pos;
    }



    public void attack(AttackState attackState) throws IOException {
        final int attackRange = 30;  // how far the attack reaches
        final int attackWidth = 20;  // width of the slash area
        final int enemyHitboxPadding = 10;  // extra margin around enemy hitbox

        final float playerWidth = this.getHitboxSize().x;
        final float playerHeight = this.getHitboxSize().y;

        // calculate player hitbox top left corner from center
        float playerX = this.pos.x - playerWidth / 2;
        float playerY = this.pos.y - playerHeight / 2;

        // attack hitbox (x, y, width, height)
        float hitboxX = playerX;
        float hitboxY = playerY;
        float hitboxWidth = attackWidth;
        float hitboxHeight = attackRange;

        switch (attackState) {
            case UP -> {
                hitboxX = this.pos.x - attackWidth / 2f;
                hitboxY = playerY - attackRange;
            }
            case DOWN -> {
                hitboxX = this.pos.x - attackWidth / 2f;
                hitboxY = playerY + playerHeight;
            }
            case LEFT -> {
                hitboxX = playerX - attackRange;
                hitboxY = this.pos.y - attackWidth / 2f;
                hitboxWidth = attackRange;
                hitboxHeight = attackWidth;
            }
            case RIGHT -> {
                hitboxX = playerX + playerWidth;
                hitboxY = this.pos.y - attackWidth / 2f;
                hitboxWidth = attackRange;
                hitboxHeight = attackWidth;
            }
        }

        for (Goblin enemy : this.serverGame.enemies.values()) {
            float enemyWidth = enemy.getHitboxSize().x;
            float enemyHeight = enemy.getHitboxSize().y;

            // convert enemy center position to top-left corner
            float enemyX = enemy.position.x - enemyWidth / 2 - enemyHitboxPadding;
            float enemyY = enemy.position.y - enemyHeight / 2 - enemyHitboxPadding;
            float adjustedEnemyWidth = enemyWidth + 2 * enemyHitboxPadding;
            float adjustedEnemyHeight = enemyHeight + 2 * enemyHitboxPadding;

            if (aabbCollision(hitboxX, hitboxY, hitboxWidth, hitboxHeight, enemyX, enemyY, adjustedEnemyWidth, adjustedEnemyHeight)) {
                enemy.hurt(2);

                // notify all players about the enemy hit and its updated health
                serverGame.server.sendToEachPlayer(new EnemyHitS2CPacket(enemy.id, enemy.health));
            }
        }
    }

    /** AABB collision */
    private boolean aabbCollision(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    @Override
    public Vec2 getHitboxSize() {
        return new Vec2(40, 40);
    }
}
