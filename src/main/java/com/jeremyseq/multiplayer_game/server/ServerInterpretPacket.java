package main.java.com.jeremyseq.multiplayer_game.server;

import main.java.com.jeremyseq.multiplayer_game.common.AttackState;
import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerInterpretPacket {
    public static void interpretPacket(Socket socket, ServerGame serverGame, DataOutputStream out, String line) throws IOException {
        if (line.startsWith("$pos:")) {
            // player position was sent, decoding
            String s = line.substring(5);
            Vec2 vec2 = Vec2.fromString(s);
            ServerPlayer player = serverGame.getPlayerBySocket(socket);
            player.pos = vec2;
        } else if (line.startsWith("$init_connection:")) {
            String s = line.substring(17);
            String username = s.substring(9);
            if (serverGame.isValidUsername(username)) {
                serverGame.connectPlayer(username, socket);
                out.writeUTF("$init_connection:success");
                System.out.println("Player initialized: " + username);
            } else {
                out.writeUTF("$init_connection:failed");
                System.out.println("Player initialization failed: " + username);
            }
        } else if (line.startsWith("$movement:")) {
            String s = line.substring(10);
            ServerPlayer player = serverGame.getPlayerBySocket(socket);
            // set delta movement as given in the packet
            player.deltaMovement = Vec2.fromString(s);
        } else if (line.startsWith("$attack:")) {
            String s = line.substring(8);
            ServerPlayer player = serverGame.getPlayerBySocket(socket);
            AttackState attackState = AttackState.valueOf(s);
            player.attack(attackState);

            for (ServerPlayer otherPlayer : serverGame.players) {
                if (player == otherPlayer) {
                    continue;
                }
                DataOutputStream serverPlayerOut = new DataOutputStream(otherPlayer.socket.getOutputStream());
                serverPlayerOut.writeUTF("$attack." + player.username + ":" + attackState.name());
            }
        } else {
            System.out.println(line);
        }
    }
}
