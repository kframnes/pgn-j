package com.framnes.pgnj.engine;

import com.framnes.pgnj.evaluation.EngineMove;
import com.framnes.pgnj.evaluation.EvaluatedMove;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of UCI protocol communicating with the chosen engine executable.
 */
public class Engine {

    final private static String BEST_MOVE_PATTERN = ".*?multipv ([0-9]+) score cp (-?[0-9]+).*?pv ([a-h1-8]+[bnrq]?).*";
    final private static String BEST_MATE_PATTERN = ".*?multipv ([0-9]+) score mate (-?[0-9]+).*?pv ([a-h1-8]+[bnrq]?).*";
    final private static String CHECKMATE_PATTERN = "info depth 0 score mate 0";
    //final private static int THINK_TIME_MS = 10000;
    final private static int DEPTH = 10;
    final private static int VARIATIONS = 3;

    final private Pattern bestMovePattern;
    final private Pattern bestMatePattern;
    final private BufferedReader input;
    final private BufferedWriter output;
    final private Process process;

    public Engine(String enginePath) {

        bestMovePattern = Pattern.compile(BEST_MOVE_PATTERN);
        bestMatePattern = Pattern.compile(BEST_MATE_PATTERN);

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

    public EvaluatedMove bestMoves(String fen) {
        sendCommand("position fen " + fen);
        sendCommand("go depth " + DEPTH);
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
            //System.out.println(" [PGN-J] >>> " + command);
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
                //.peek((line) -> System.out.println( " [ENGINE] <<< " + line))
                .anyMatch(expected::equals);
    }

    /**
     * Read output from engine until we see the phrase "bestmove" indicating the evaluation has ended.
     *
     * @return an {@code EvaluatedMove} of the best moves (best at 0, decreasing...)
     */
    private EvaluatedMove readBestMoves() {

        EngineMove[] moves = new EngineMove[VARIATIONS];

        input.lines()
                //.peek((line) -> System.out.println( " [ENGINE] <<< " + line))
                .peek((output) -> {

                    if (output.equalsIgnoreCase(CHECKMATE_PATTERN)) {
                        moves[0] = new EngineMove("###", Integer.MAX_VALUE);
                        return;
                    }

                    Matcher match = bestMovePattern.matcher(output);
                    if (match.matches()) {
                        moves[Integer.parseInt(match.group(1)) - 1] = new EngineMove(match.group(3), Integer.parseInt(match.group(2)));
                    } else {

                        Matcher mate = bestMatePattern.matcher(output);
                        if (mate.matches()) {
                            int mateIn = Integer.parseInt(mate.group(2));
                            int mateEvaluation = 32767 - 1000 * Math.abs(mateIn);
                            if (mateIn < 0) mateEvaluation *= -1;
                            moves[Integer.parseInt(mate.group(1)) - 1] = new EngineMove(mate.group(3), mateEvaluation);
                        }

                    }

                })
                .anyMatch((line) -> line.contains("bestmove"));

        return new EvaluatedMove(moves);

    }

}
