package com.framnes.pgnj.stats;

import com.framnes.pgnj.evaluation.EvaluatedMove;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.game.GameResult;

import java.time.Duration;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class representing the compiled analysis and statistics for the given PGN.
 */
public class Stats {

    private final int variations;
    private Duration duration;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // T-ANALYSIS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private int[][] tEvaluationCounts;
    private int[][] tEvaluationCountsWonGames;
    private int[][] tEvaluationCountsLostGames;

    private int[] rangeCounts;
    private int[] rangeCountsWonGames;
    private int[] rangeCountsLostGames;

    private int[] rangeCpLoss;
    private int[] rangeCpCounts;

    public Stats(int variations) {

        this.variations = variations;

        tEvaluationCounts = new int[Range.values().length][variations];
        rangeCounts = new int[Range.values().length];
        rangeCpLoss = new int[Range.values().length];
        rangeCpCounts = new int[Range.values().length];

        tEvaluationCountsWonGames = new int[Range.values().length][variations];
        rangeCountsWonGames = new int[Range.values().length];

        tEvaluationCountsLostGames = new int[Range.values().length][variations];
        rangeCountsLostGames = new int[Range.values().length];

    }

    /**
     * Adds an entire games worth of {@code EvaluatedMove} to the stats.
     *
     * @param moves the moves to consider
     * @param side the side to analyze
     * @param bookMoves the number of moves to skip (as book moves)
     * @param playerWon (optional) true if the analyzed player won, false if they lost lost; null for a draw.
     */
    public void addEvaluatedMoves(List<EvaluatedMove> moves, Side side, int bookMoves, Boolean playerWon) {

        for (int i=bookMoves*2; i<moves.size(); i++) {

            if (side != null && Side.WHITE.equals(side) && i % 2 == 1) continue;
            if (side != null && Side.BLACK.equals(side) && i % 2 == 0) continue;

            EvaluatedMove evaluatedMove = moves.get(i);
            addEvaluatedMove(evaluatedMove, playerWon);

        }

    }

    public void addEvaluatedMoves(List<EvaluatedMove> moves, Side side, int bookMoves) {
        addEvaluatedMoves(moves, side, bookMoves, null);
    }

    public void addTiming(long elapsedSeconds) {
        this.duration = Duration.ofMillis(elapsedSeconds);
    }

    /**
     * Prints results of the PGN evaluation.
     */
    public void outputEvaluation() {

        System.out.println();
        System.out.println("=======================================================================================");
        System.out.println(String.format("%-35s %-10s %-10s %-10s %-10s %-10s ", "Position Eval", "N", "AvgCP-", "T1%", "T2%", "T3%"));
        System.out.println("=======================================================================================");
        Range[] ranges = Range.values();
        for (int i = 0; i < ranges.length; i++) {
            System.out.println(String.format("%-35s %-10d %-10.2f %-10.2f %-10.2f %-10.2f ",
                    ranges[i].description,
                    rangeCounts[i],
                    rangeCpCounts[i] > 0 ? (double) rangeCpLoss[i] / (double) rangeCpCounts[i] : 0.00,
                    rangeCounts[i] > 0 ? 100.0 * (double) tEvaluationCounts[i][0] / (double) rangeCounts[i] : 0.00,
                    rangeCounts[i] > 0 ? 100.0 * (double) tEvaluationCounts[i][1] / (double) rangeCounts[i] : 0.00,
                    rangeCounts[i] > 0 ? 100.0 * (double) tEvaluationCounts[i][2] / (double) rangeCounts[i] : 0.00
            ));
        }

        System.out.println();
        System.out.println("---------------------------------------------------------------------------------------");
        System.out.println("  ONLY WINS");
        System.out.println("---------------------------------------------------------------------------------------");
        for (int i = 0; i < ranges.length; i++) {
            System.out.println(String.format("%-35s %-10d %-10s %-10.2f %-10.2f %-10.2f ",
                    ranges[i].description,
                    rangeCountsWonGames[i],
                    "N/A",
                    rangeCountsWonGames[i] > 0 ? 100.0 * (double) tEvaluationCountsWonGames[i][0] / (double) rangeCountsWonGames[i] : 0.00,
                    rangeCountsWonGames[i] > 0 ? 100.0 * (double) tEvaluationCountsWonGames[i][1] / (double) rangeCountsWonGames[i] : 0.00,
                    rangeCountsWonGames[i] > 0 ? 100.0 * (double) tEvaluationCountsWonGames[i][2] / (double) rangeCountsWonGames[i] : 0.00
            ));
        }

        System.out.println();
        System.out.println("---------------------------------------------------------------------------------------");
        System.out.println("  ONLY LOSES");
        System.out.println("---------------------------------------------------------------------------------------");
        for (int i = 0; i < ranges.length; i++) {
            System.out.println(String.format("%-35s %-10d %-10s %-10.2f %-10.2f %-10.2f ",
                    ranges[i].description,
                    rangeCountsLostGames[i],
                    "N/A",
                    rangeCountsLostGames[i] > 0 ? 100.0 * (double) tEvaluationCountsLostGames[i][0] / (double) rangeCountsLostGames[i] : 0.00,
                    rangeCountsLostGames[i] > 0 ? 100.0 * (double) tEvaluationCountsLostGames[i][1] / (double) rangeCountsLostGames[i] : 0.00,
                    rangeCountsLostGames[i] > 0 ? 100.0 * (double) tEvaluationCountsLostGames[i][2] / (double) rangeCountsLostGames[i] : 0.00
            ));
        }

        System.out.println();
        System.out.println(String.format("Analysis took %s", duration.toString()));

    }

    /**
     * Adds an {@code EvaluatedMove} to the stats.
     *
     * @param move the move to consider
     * @param playerWon (optional) true if the analyzed player won, false if they lost lost; null for a draw.
     */
    synchronized private void addEvaluatedMove(EvaluatedMove move, Boolean playerWon) {

        int[][] resultBasedEvaluationCounts = getResultBasedEvaluationCounts(playerWon);
        int[] resultBasedRangeCounts = getResultBasedRangeCounts(playerWon);

        List<Integer> rangeIndices = Range.getRangeIndices(move.getPositionEvaluation());
        if (!rangeIndices.isEmpty()) {
            for (Integer rangeIndex : rangeIndices) {

                if (move.includeForCpLoss()) {
                    rangeCpLoss[rangeIndex] += move.getCpLoss();
                    rangeCpCounts[rangeIndex]++;
                }

                rangeCounts[rangeIndex]++;
                resultBasedRangeCounts[rangeIndex]++;

                int engineMoveIndex = move.getEngineMatchIndex();
                if (engineMoveIndex >= 0) {
                    for (int eMove = engineMoveIndex; eMove < variations; eMove++) {
                        if (move.includeForTAnalysis(eMove)) {
                            tEvaluationCounts[rangeIndex][eMove]++;
                            resultBasedEvaluationCounts[rangeIndex][eMove]++;
                        }
                    }
                }
            }
        }

    }

    int[][] getResultBasedEvaluationCounts(Boolean playerWon) {
        if (playerWon != null) {
            return playerWon ? tEvaluationCountsWonGames : tEvaluationCountsLostGames;
        }
        return new int[Range.values().length][variations];
    }

    int[] getResultBasedRangeCounts(Boolean playerWon) {
        if (playerWon != null) {
            return playerWon ? rangeCountsWonGames : rangeCountsLostGames;
        }
        return new int[Range.values().length];
    }

    /**
     * Enum representing various evaluation ranges.
     */
    enum Range {

        MATED( eval -> eval <= -2000, "Opponent has forced mate" ),
        CRUSHED( eval -> -2000 < eval && eval <= -900 , "Losing by more than a Queen"),
        ROOK_DOWN( eval -> -900 < eval && eval <= -500, "Losing by a Rook" ),
        PIECE_DOWN( eval -> -500 < eval && eval <= -300, "Losing by a Piece" ),
        PAWNS_DOWN( eval -> -300 < eval && eval < -100, "Losing by some pawns" ),
        EVEN( eval -> -100 <= eval && eval <= 100, "Even (+/- 100 CP)" ),
        PAWNS_UP( eval -> 100 < eval && eval < 300, "Winning by some pawns" ),
        PIECE_UP( eval -> 300 <= eval && eval < 500, "Winning by a Piece" ),
        ROOK_UP( eval -> 500 <= eval && eval < 900, "Winning by a Rook" ),
        CRUSHING( eval -> 900 <= eval && eval < 2000, "Winning by more than a Queen" ),
        MATING( eval -> eval >= 2000,  "Player has forced mate"),

        LOSING(eval -> eval < -100, "Losing"),
        WINNING(eval -> eval > 100, "Winning"),

        TOTAl(eval -> true, "Total")

        ;

        IntPredicate predicate;
        String description;

        Range(IntPredicate predicate, String description) {
            this.predicate = predicate;
            this.description = description;
        }

        /**
         * Returns the matching ranges of an evaluation.
         *
         * @param evaluation the evaluation to match.
         * @return the indeces representing the matched range; otherwise -1
         */
        static List<Integer> getRangeIndices(int evaluation) {
            return IntStream.range(0, values().length)
                    .filter( i -> values()[i].predicate.test(evaluation) )
                    .boxed()
                    .collect(Collectors.toList());
        }

    }

}
