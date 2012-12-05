/*
 * Copyright 2012 Cameron Zemek <grom358@gmail.com>.
 */
package pokeraichallenge;

import java.util.List;
import poker.Card;

/**
 * A player in Texas Holdem
 *
 * @author Cameron Zemek <grom358@gmail.com>
 */
public class Player {
    private String name;
    private BotConnection conn;
    private int stack;
    private boolean allIn;
    private long timeBank;
    private List<Card> hand;

    public Player(Settings settings, String name, BotConnection conn) {
        this.name = name;
        this.stack = settings.getInitialStack();
        this.timeBank = settings.getTimeBank();
        this.allIn = false;
        this.conn = conn;
    }

    public String getName() {
        return name;
    }

    public BotConnection getConnection() {
        return conn;
    }

    public int getStack() {
        return stack;
    }

    public boolean isAllIn() {
        return allIn;
    }

    public void setCards(List<Card> hand) {
        this.hand = hand;
    }

    public List<Card> getCards() {
        return hand;
    }

    public long getTimeBank() {
        return timeBank;
    }

    public void addTimeBank(long time) {
        timeBank += time;
    }

    public int takeChips(int amount) {
        if (amount > stack) {
            amount = stack;
        }
        stack -= amount;
        if (stack == 0) {
            allIn = true;
        }
        return amount;
    }

    public void giveChips(int amount) {
        stack += amount;
        allIn = false;
    }

    public void println(String message) {
        conn.println(message);
    }

    public String go() {
        println("go " + timeBank);
        long start = System.currentTimeMillis();
        String line = conn.readLine(timeBank);
        timeBank -= System.currentTimeMillis() - start;
        if (timeBank < 0) {
            timeBank = 0;
        }
        return line;
    }
}
