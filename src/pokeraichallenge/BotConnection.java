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
    private BlockingQueue<String> inputQueue = new ArrayBlockingQueue<>(100);
    private BlockingQueue<String> outputQueue = new ArrayBlockingQueue<>(100);

    public BotConnection(Process process) {
        this.process = process;
    }

    public void open() {
        InputHandler input = new InputHandler(process.getInputStream(), inputQueue);
        OutputHandler output = new OutputHandler(process.getOutputStream(), outputQueue);

        // Start up handler threads
        (new Thread(input)).start();
        (new Thread(output)).start();
    }

    public void close() {
        outputQueue.offer("STOP"); // Poison value to stop OutputHandler consumer
        (new Thread(new CloseHandler(process))).start();
    }

    public void println(String message) {
        outputQueue.offer(message);
    }

    public String readLine(long timeout) {
        try {
            String line = inputQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (line != null) {
                return line;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return "fold 0";
    }

    /**
     * Handles reading input from the bot
     */
    static private class InputHandler implements Runnable {
        private InputStream is;
        private BlockingQueue<String> inputQueue;

        public InputHandler(InputStream is, BlockingQueue<String> inputQueue) {
            this.is = is;
            this.inputQueue = inputQueue;
        }

        @Override
        public void run() {
            try (InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    inputQueue.offer(line);
                }
            } catch (IOException e) {
                // TODO: Log error
            } finally {
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
        private BlockingQueue<String> outputQueue;

        public OutputHandler(OutputStream os, BlockingQueue<String> outputQueue) {
            this.os = os;
            this.outputQueue = outputQueue;
        }

        @Override
        public void run() {
            try (OutputStreamWriter osr = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osr)) {
                while (true) {
                    try {
                        String line = outputQueue.take();
                        if (line.equals("STOP")) {
                           break;
                        }
                        bw.write(line);
                        bw.newLine();
                        bw.flush();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (IOException e) {
                // TODO: Log error
            } finally {
                try {
                    os.close();
                } catch (IOException e) {
                    // TODO: Log error
                }
            }
        }
    }

    /**
     * Handles process cleanup
     */
    static private class CloseHandler implements Runnable {
        private Process process;

        public CloseHandler(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
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
    }
}
