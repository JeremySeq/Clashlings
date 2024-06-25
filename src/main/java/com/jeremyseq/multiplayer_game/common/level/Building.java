package main.java.com.jeremyseq.multiplayer_game.common.level;

public class Building {
    public BuildingType type;
    public int x;
    public int y;
    public BuildingState state;

    public Building(BuildingType type, int x, int y, BuildingState state) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.state = state;
    }
}
