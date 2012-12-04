/*
 * Copyright 2012 Cameron Zemek <grom358@gmail.com>.
 */
package pokeraichallenge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Manages connection between the server and the bots. Threads are used
 * to deal with misbehaving bots.
 *
 * @author Cameron Zemek <grom358@gmail.com>
 */
public class BotConnection {
    private Process process;
    private InputHandler input;
    private OutputHandler output;
    private Thread inputThread;
    private Thread outputThread;

    public BotConnection(Process process) {
        this.process = process;
    }

    public void open() {
        input = new InputHandler(process.getInputStream());
        output = new OutputHandler(process.getOutputStream());

        // Start up handler threads
        inputThread = new Thread(input);
        inputThread.start();
        outputThread = new Thread(output);
        outputThread.start();
    }

    public void close() {
        input.isAlive = false;
        output.isAlive = false;

        // Wait for threads to die
        try {
            inputThread.join(1000);
            outputThread.join(1000);
        } catch (InterruptedException ex) {
            // TODO
        }

        // Cleanup resources
        try {
            process.getInputStream().close();
        } catch (IOException ex) {
            // TODO
        }

        try {
            process.getOutputStream().close();
        } catch (IOException ex) {
            // TODO
        }

        process.destroy(); // Kill the process
    }

    public void println(String message) {
        output.println(message);
    }

    public String readLine(long timeout) {
        return input.readLine(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Handles reading input from the bot
     */
    static private class InputHandler implements Runnable {
        private InputStream is;
        public volatile boolean isAlive = false;
        private BlockingQueue<String> inputQueue;

        public InputHandler(InputStream is) {
            this.is = is;
            this.inputQueue = new ArrayBlockingQueue<>(1); // Only keep 1 line
        }

        public String readLine(long timeout, TimeUnit unit) {
            if (!isAlive) {
                return "fold 0";
            }
            try {
                String item = inputQueue.poll(timeout, unit);
                if (item != null) {
                    return item;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "fold 0";
        }

        @Override
        public void run() {
            isAlive = true;
            try (InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr)) {
                while (isAlive) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    // We only ever expect to get a single line from a bot
                    // therefore only keep the last line received
                    inputQueue.clear();
                    inputQueue.offer(line);
                }
            } catch (IOException e) {
                // TODO: Log error
            } finally {
                isAlive = false;
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO: Log error
                }
            }
        }
    }

    /**
     * Handles writing output to the bot
     */
    static private class OutputHandler implements Runnable {
        private OutputStream os;
        public volatile boolean isAlive = false;
        private BlockingQueue<String> outputQueue;

        public OutputHandler(OutputStream os) {
            this.os = os;
            this.outputQueue = new ArrayBlockingQueue<>(10);
        }

        public void println(String message) {
            if (isAlive) {
                try {
                    outputQueue.offer(message, 1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public void run() {
            isAlive = true;
            try (OutputStreamWriter osr = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osr)) {
                while (isAlive) {
                    try {
                        String line = outputQueue.poll(1, TimeUnit.SECONDS);
                        if (line != null) {
                            bw.write(line);
                            bw.newLine();
                            bw.flush();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (IOException e) {
                // TODO: Log error
            } finally {
                isAlive = false;
                try {
                    os.close();
                } catch (IOException e) {
                    // TODO: Log error
                }
            }
        }
    }
}
