/*
 * Copyright 2012 Cameron Zemek <grom358@gmail.com>.
 */
package pokeraichallenge;

/**
 * A poker move to perform
 *
 * @author Cameron Zemek <grom358@gmail.com>
 */
public class Move {
    static public enum Action {
        FOLD,
        CHECK,
        CALL,
        RAISE
    }

    static public final Move FOLD = new Move(Action.FOLD, 0);
    static public final Move CHECK = new Move(Action.CHECK, 0);
    static public final Move CALL = new Move(Action.CALL, 0);

    private Action action;
    private int amount;

    public Move(Action action, int amount) {
        this.action = action;
        this.amount = amount;
    }

    public Action getAction() {
        return action;
    }

    public int getAmount() {
        return amount;
    }

    static Move valueOf(String line) {
        String[] parts = line.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid move");
        }

        Action action = null;
        switch (parts[0]) {
            case "fold":
                action = Action.FOLD;
                break;
            case "check":
                action = Action.CHECK;
                break;
            case "call":
                action = Action.CALL;
                break;
            case "raise":
                action = Action.RAISE;
                break;
            default:
                throw new IllegalArgumentException("Invalid move - invalid action");
        }

        int amount = 0;
        try {
            amount = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid move - invalid amount");
        }

        return new Move(action, amount);
    }
}
