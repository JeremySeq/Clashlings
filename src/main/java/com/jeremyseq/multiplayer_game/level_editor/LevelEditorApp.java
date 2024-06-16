package main.java.com.jeremyseq.multiplayer_game.level_editor;

import javax.swing.*;

public class LevelEditorApp {
    private static void initWindow() {
        // create a window frame and set the title in the toolbar
        JFrame window = new JFrame("Level Editor");
        // when we close the window, stop the app
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create the jpanel to draw on.
        // this also initializes the levelEditor loop
        LevelEditor levelEditor = new LevelEditor();
        // add the jpanel to the window
        window.add(levelEditor);
        // pass keyboard inputs to the jpanel

        // don't allow the user to resize the window
        window.setResizable(false);
        // fit the window size around the components (just our jpanel).
        // pack() should be called after setResizable() to avoid issues on some platforms
        window.pack();
        // open window in the center of the screen
        window.setLocationRelativeTo(null);
        // display the window
        window.setVisible(true);
        System.setProperty("awt.useSystemAAFontSettings","on");
    }

    public static void main(String[] args) {
        // invokeLater() is used here to prevent our graphics processing from
        // blocking the GUI. https://stackoverflow.com/a/22534931/4655368
        // this is a lot of boilerplate code that you shouldn't be too concerned about.
        // just know that when main runs it will call initWindow() once.
        SwingUtilities.invokeLater(() -> initWindow());
    }
}
