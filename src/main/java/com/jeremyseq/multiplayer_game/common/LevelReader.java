package main.java.com.jeremyseq.multiplayer_game.common;


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;

public class LevelReader {
    public String readLevelJSONFile(String levelName) {
        StringBuilder data;
        try {
            InputStream inputStream = (Objects.requireNonNull(getClass().getResourceAsStream("/levels/" + levelName + ".json")));
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            data = new StringBuilder();
            for (String line; (line = reader.readLine()) != null; ) {
                // Process line
                data.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return data.toString();
    }

    public Level readLevelString(String data) {
        Gson gson = new Gson();
        if (gson.fromJson(data, Level.class) == null) {
            return new Level(new HashMap<>(), new HashMap<>());
        }
        return gson.fromJson(data, Level.class);
    }

    public Level readLevel(String levelName) {
        String data = readLevelJSONFile(levelName);
        return readLevelString(data);
    }

}
