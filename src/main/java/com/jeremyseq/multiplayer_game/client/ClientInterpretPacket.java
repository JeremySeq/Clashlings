package main.java.com.jeremyseq.multiplayer_game.client;

import main.java.com.jeremyseq.multiplayer_game.common.AttackState;
import main.java.com.jeremyseq.multiplayer_game.common.level.LevelReader;
import main.java.com.jeremyseq.multiplayer_game.common.Vec2;

public class ClientInterpretPacket {
    public static void interpretPacket(Game game, String line) {
        if (line.startsWith("$init_connection:")) {
            String s = line.substring(17);
            if (s.equals("failed")) {
                System.out.println("Player initialization failed. Exiting...");
                System.exit(0);
            }
        } else if (line.startsWith("$pos:")) {
            String s = line.substring(5);
            String username = s.split("\\$")[0];
            String posStr = s.split("\\$")[1];
            username = username.substring(9);
            posStr = posStr.substring(4);

            Vec2 pos = Vec2.fromString(posStr);

            ClientPlayer respectivePlayer = game.getClientPlayerByUsername(username);
            if (respectivePlayer == null) {
                respectivePlayer = new ClientPlayer(game, username, pos);
                game.players.add(respectivePlayer);
            } else {
                respectivePlayer.position = pos;
            }
        } else if (line.startsWith("$movement.")) {
            String s = line.substring(10);
            String username = s.split(":")[0];
            String vec = s.split(":")[1];
            Vec2 deltaMovement = Vec2.fromString(vec);
            ClientPlayer player = game.getClientPlayerByUsername(username);
            player.deltaMovement = deltaMovement;
        } else if (line.startsWith("$attack.")) {
            String s = line.substring(8);
            String username = s.split(":")[0];
            ClientPlayer player = game.getClientPlayerByUsername(username);
            AttackState facing = AttackState.valueOf(s.split(":")[1]);
            player.attack(facing);
        } else if (line.startsWith("$level:")) {
            String s = line.substring(7);
            game.level = new LevelReader().readLevelString(s);
        } else {
            System.out.println(line);
        }
    }
}
