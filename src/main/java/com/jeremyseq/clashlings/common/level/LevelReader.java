package com.jeremyseq.clashlings.common.level;


import com.google.gson.Gson;
import com.jeremyseq.clashlings.common.Vec2;
import com.jeremyseq.clashlings.common.level.buildings.GoblinHut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
        Level level = gson.fromJson(data, Level.class);
        if (level == null) {
            return new Level(new LevelMetadata(1, new Vec2(0, 0)), new HashMap<>(), new HashMap<>());
        }
        // parse buildings into subclasses
        level.buildings = parseBuildings(level.buildings);
        return level;
    }

    public Level readLevel(String levelName) {
        String data = readLevelJSONFile(levelName);
        return readLevelString(data);
    }

    /**
     * Parses the given map of buildings and converts them into their specific subclasses based on their type.
     * @param buildings A map where the key is the layer name and the value is a list of buildings in that layer.
     * @return A new map with the same structure, but with buildings converted to their specific subclasses.
     */
    public static HashMap<String, ArrayList<Building>> parseBuildings(HashMap<String, ArrayList<Building>> buildings) {
        HashMap<String, ArrayList<Building>> parsedBuildings = new HashMap<>();

        for (Map.Entry<String, ArrayList<Building>> entry : buildings.entrySet()) {
            String layer = entry.getKey();
            ArrayList<Building> buildingList = entry.getValue();
            ArrayList<Building> convertedBuildings = new ArrayList<>();

            for (Building building : buildingList) {
                switch (building.type) {
                    case GOBLIN_HUT:
                        convertedBuildings.add(new GoblinHut(building.type, building.x, building.y, building.state));
                        break;
                    default:
                        convertedBuildings.add(building); // Fallback to the base Building object
                        break;
                }
            }

            parsedBuildings.put(layer, convertedBuildings);
        }

        return parsedBuildings;
    }
}
