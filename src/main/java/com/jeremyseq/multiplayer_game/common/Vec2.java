package main.java.com.jeremyseq.multiplayer_game.common;

import java.util.Objects;

public class Vec2 {
    public float x;
    public float y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 add(Vec2 toAdd) {
        return new Vec2(this.x + toAdd.x, this.y + toAdd.y);
    }

    public Vec2 subtract(Vec2 toSubtract) {
        return new Vec2(this.x - toSubtract.x, this.y - toSubtract.y);
    }

    public Vec2 normalize() {
        double length = Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
        if (length == 0) {
            return this;
        }
        float x = (float) (this.x / length);
        float y = (float) (this.y / length);
        return new Vec2(x, y);
    }

    public Vec2 multiply(float scalar) {
        return new Vec2(this.x * scalar, this.y * scalar);
    }


    public boolean equals(Vec2 vec2) {
        return this.x == vec2.x && this.y == vec2.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Vec2{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public String toPacketString() {
        return x + "," + y;
    }

    public static Vec2 fromString(String str) {
        String[] split = str.split(",");
        return new Vec2(Float.parseFloat(split[0]), Float.parseFloat(split[1]));
    }
}
