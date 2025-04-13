package com.jeremyseq.multiplayer_game.common;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    public final String identifier;

    public boolean DEBUG_MODE = false;
    public boolean INCLUDE_THREAD_NAME = false;

    public Logger(String identifier) {
        this.identifier = identifier;
    }

    public void warning(String message) {
        System.err.println(this.create("WARNING", message));
    }

    public void error(String message) {
        System.err.println(this.create("ERROR", message));
    }

    public void debug(String message) {
        if (DEBUG_MODE) System.out.println(this.create("DEBUG", message));
    }

    public void info(String message) {
        System.out.println(this.create("INFO", message));
    }

    private String create(String type, String message) {
        if (INCLUDE_THREAD_NAME) {
            String threadName = Thread.currentThread().getName();
            return "[" + getTime() + "] [" + this.identifier + "/" + threadName + "] [" + type + "]: " + message;
        }
        return "[" + getTime() + "] [" + this.identifier + "] [" + type + "]: " + message;
    }

    private static String getTime() {
        LocalTime time = LocalTime.now();
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
