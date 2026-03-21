package com.rogueforge.game.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.rogueforge.game.core.RogueForgeGame;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("ROGUE FORGE");
        configuration.setWindowedMode(1280, 720);
        new Lwjgl3Application(new RogueForgeGame(), configuration);
    }
}
