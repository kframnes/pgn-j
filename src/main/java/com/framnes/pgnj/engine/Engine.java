package com.framnes.pgnj.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * An implementation of UCI protocol communicating with the chosen engine executable.
 */
public class Engine {

    final private BufferedReader input;
    final private BufferedWriter output;
    final private Process process;

    public Engine(String enginePath) {

        try {
            process = Runtime.getRuntime().exec(enginePath);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            sendCommand("uci");
        } catch (IOException e) {
            throw new RuntimeException("Unable to start and bind engine process: ", e);
        }

    }

    /**
     * Confirms that we have a valid engine that is ready to interact with.
     *
     * @return true if the engine is ready; otherwise false.
     */
    public boolean engineIsValid() {
        sendCommand("isready");
        return hasResponse("readyok");
    }

    /**
     * Sends a command to the engine.
     *
     * @param command the command to execute.
     */
    private void sendCommand(String command) {
        try {
            System.out.println(" [PGN-J] >>> " + command);
            output.write(command + "\n");
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Scans the output of the engine looking for expected value.
     *
     * @param expected the output for which we're looking.
     * @return true if the expected output was found; otherwise false.
     */
    private boolean hasResponse(String expected) {
        return input.lines()
                .peek((line) -> System.out.println( " [ENGINE] <<< " + line))
                .anyMatch(expected::equals);
    }

}
