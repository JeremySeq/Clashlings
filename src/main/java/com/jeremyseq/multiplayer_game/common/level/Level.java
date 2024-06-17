package main.java.com.jeremyseq.multiplayer_game.common.level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;

public class Level {

    public LevelMetadata metadata;
    public HashMap<String, ArrayList<Tile>> tiles;

    public Level(LevelMetadata metadata, HashMap<String, ArrayList<Tile>> tiles) {
        this.metadata = metadata;
        this.tiles = tiles;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
