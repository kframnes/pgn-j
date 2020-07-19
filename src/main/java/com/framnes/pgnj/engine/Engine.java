package com.framnes.pgnj.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of UCI protocol communicating with the chosen engine executable.
 */
public class Engine {

    final private static String BEST_MOVE_PATTERN = "info depth [0-9]+ seldepth [0-9]+ multipv ([0-9]+) " +
            "score cp (-?[0-9]+) nodes [0-9]+ nps [0-9]+ hashfull [0-9]+ tbhits [0-9]+ time [0-9]+ pv ([a-h1-8]+).*";
    final private static int THINK_TIME_MS = 15000;
    final private static int VARIATIONS = 3;

    final private Pattern bestMovePattern;
    final private BufferedReader input;
    final private BufferedWriter output;
    final private Process process;

    public Engine(String enginePath) {

        bestMovePattern = Pattern.compile(BEST_MOVE_PATTERN);

        try {
            process = Runtime.getRuntime().exec(enginePath);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            sendCommand("uci");
            sendCommand("setoption name MultiPV value " + VARIATIONS);
        } catch (IOException e) {
            throw new RuntimeException("Unable to start and bind engine process: ", e);
        }

    }

    /**
     * Tells the engine we're starting a new game.
     *
     * @return true if the engine is ready; otherwise false.
     */
    public boolean startNewGame() {
        sendCommand("ucinewgame");
        return isReady();
    }

    public String[] bestMoves(String fen) {
        sendCommand("position fen " + fen);
        sendCommand("go movetime " + THINK_TIME_MS);
        return readBestMoves();
    }

    /**
     * Confirms that we have a valid engine that is ready to interact with.
     *
     * @return true if the engine is ready; otherwise false.
     */
    public boolean isReady() {
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

    /**
     * Read output from engine until we
     *
     * @return an String[] of the best moves (best at 0, decreasing...)
     */
    private String[] readBestMoves() {

        String[] moves = new String[VARIATIONS];

        input.lines()
                .peek((line) -> System.out.println( " [ENGINE] <<< " + line))
                .peek((output) -> {
                    Matcher match = bestMovePattern.matcher(output);
                    if (match.matches()) {
                        moves[Integer.parseInt(match.group(1))-1] = match.group(3);
                    }
                })
                .anyMatch((line) -> line.contains("bestmove"));

        return moves;

    }

}
