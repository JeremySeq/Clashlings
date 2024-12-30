package com.jeremyseq.multiplayer_game.level_editor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class LevelEditorMouseHandler extends MouseAdapter {

    private final LevelEditor levelEditor;

    public LevelEditorMouseHandler(LevelEditor levelEditor) {
        this.levelEditor = levelEditor;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            // up
            levelEditor.mouseWheelUp();
        }
        else if (e.getWheelRotation() > 0) {
            // down
            levelEditor.mouseWheelDown();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        levelEditor.mousePressed(e);
    }
}
