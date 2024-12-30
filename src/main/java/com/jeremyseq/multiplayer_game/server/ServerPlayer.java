package com.jeremyseq.multiplayer_game.server;

import com.jeremyseq.multiplayer_game.common.AttackState;
import com.jeremyseq.multiplayer_game.common.Goblin;
import com.jeremyseq.multiplayer_game.common.Vec2;
import com.jeremyseq.multiplayer_game.common.packets.S2C.EnemyHitS2CPacket;

import java.io.IOException;
import java.net.Socket;

public class ServerPlayer {
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
        Vec2 hitpoint_pos = this.pos;
        int attackRange = 30;  // how far the attack reaches

        hitpoint_pos = switch (attackState) {
            case UP -> hitpoint_pos.add(new Vec2(0, -attackRange));
            case DOWN -> hitpoint_pos.add(new Vec2(0, attackRange));
            case LEFT -> hitpoint_pos.add(new Vec2(-attackRange, 0));
            case RIGHT -> hitpoint_pos.add(new Vec2(attackRange, 0));
            default -> hitpoint_pos;
        };

        int enemyHitboxRadius = 50;  // radius for a circular hitbox

        for (Goblin enemy : this.serverGame.enemies.values()) {
            // calculate the distance between the hitpoint and the enemy
            float distanceX = hitpoint_pos.x - enemy.position.x;
            float distanceY = hitpoint_pos.y - enemy.position.y;

            // check if the hitpoint is within the enemy's hitbox
            if (Math.abs(distanceX) <= enemyHitboxRadius && Math.abs(distanceY) <= enemyHitboxRadius) {
                enemy.hurt(2);  // do damage

                // notify all players about the enemy hit and its updated health
                serverGame.server.sendToEachPlayer(new EnemyHitS2CPacket(enemy.id, enemy.health));
            }
        }
    }
}
