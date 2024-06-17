package main.java.com.jeremyseq.multiplayer_game.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;

public class Level {

    public HashMap<String, String> metadata;
    public HashMap<String, ArrayList<Tile>> tiles;

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

        @Override
        public String toString() {
            return "Tile{" +
                    "x=" + x +
                    ", y=" + y +
                    ", tilemap='" + tilemap + '\'' +
                    ", i=" + i +
                    ", j=" + j +
                    '}';
        }
    }

    public Level(HashMap<String, String> metadata, HashMap<String, ArrayList<Tile>> tiles) {
        this.metadata = metadata;
        this.tiles = tiles;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
