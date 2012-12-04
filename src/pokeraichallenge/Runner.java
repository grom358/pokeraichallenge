/*
 * Copyright 2012 Cameron Zemek <grom358@gmail.com>.
 */
package pokeraichallenge;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Play heads up Texas Holdem poker
 *
 * @author Cameron Zemek <grom358@gmail.com>
 */
public class Runner {
    private static String[] getCommand(String input) {
        StringTokenizer tokenizer = new StringTokenizer(input);
        String[] result = new String[tokenizer.countTokens()];
        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
            result[i] = tokenizer.nextToken();
        }
        return result;
    }

    /**
     * The command line arguments are as follows bot1-name: Name of the first
     * bot bot1-cmd: Command to execute the first bot bot2-name: Name of the
     * second bot bot2-cmd: Command to execute the second bot
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("USAGE: [cmd] [botName] [botCmd] [botName] [botCmd]");
            System.exit(1);
        }

        String bot1Name = args[0];
        String[] bot1Cmd = getCommand(args[1]);
        String bot2Name = args[2];
        String[] bot2Cmd = getCommand(args[3]);

        Settings settings = new Settings();
        List<Player> players = new ArrayList<>();

        ProcessBuilder pb;
        BotConnection conn;

        pb = new ProcessBuilder(bot1Cmd);
        pb.redirectError(new File(bot1Name + ".log"));
        conn = new BotConnection(pb.start());
        players.add(new Player(settings, bot1Name, conn));
        conn.open();

        pb = new ProcessBuilder(bot2Cmd);
        pb.redirectError(new File(bot2Name + ".log"));
        conn = new BotConnection(pb.start());
        players.add(new Player(settings, bot2Name, conn));
        conn.open();

        Match match = new Match(settings, players);
        Player winner = match.play();
        System.out.println("WINNER: " + winner.getName());

        for (Player player : players) {
            player.getConnection().close();
        }
    }
}
