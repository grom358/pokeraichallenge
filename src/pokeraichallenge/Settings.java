/*
 * Copyright 2012 Cameron Zemek <grom358@gmail.com>.
 */
package pokeraichallenge;

/**
 * Game settings
 *
 * @author Cameron Zemek <grom358@gmail.com>
 */
public class Settings {
    public String getGameType() {
        return "NLHE";
    }

    public String getGameMode() {
        return "tournament";
    }

    public int getTimeBank() {
        return 5000;
    }

    public int getTimePerMove() {
        return 500;
    }

    public int getHandsPerLevel() {
        return 10;
    }

    public int getInitialStack() {
        return 1500;
    }
}
