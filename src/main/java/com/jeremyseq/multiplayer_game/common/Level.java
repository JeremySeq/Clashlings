package main.java.com.jeremyseq.multiplayer_game.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;

public class Level {

    public HashMap<String, String> metadata;
    public List<Tile> tiles;

    public static class Tile {
        public int x;
        public int y;
        public String tilemap;
        public int i;
        public int j;
        public Tile(int x, int y, String tilemap, int i, int j) {
            this.x = x;
            this.y = y;
            this.tilemap = tilemap;
            this.i = i;
            this.j = j;
        }
    }

    public Level(HashMap<String, String> metadata, List<Tile> tiles) {
        this.metadata = metadata;
        this.tiles = tiles;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
