package com.jeremyseq.multiplayer_game.common;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record Logger(String identifier) {

    public void warning(String message) {
        System.err.println(this.create("WARNING", message));
    }

    public void debug(String message) {
        System.out.println(this.create("DEBUG", message));
    }

    public void info(String message) {
        System.out.println(this.create("INFO", message));
    }

    private String create(String type, String message) {
        return "[" + getTime() + "] [" + this.identifier + "] [" + type + "]: " + message;
    }

    private static String getTime() {
        LocalTime time = LocalTime.now();
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
