package com.jeremyseq.clashlings.common.level;

public class Tile {
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
