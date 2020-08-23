package com.framnes.pgnj.stats;

import com.framnes.pgnj.evaluation.EvaluatedMove;
import com.github.bhlangonijr.chesslib.Side;

import java.time.Duration;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * Class representing the compiled analysis and statistics for the given PGN.
 */
public class Stats {

    private final int variations;
    private Duration duration;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // T-ANALYSIS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // The total number of moves that matched a given engine move (1st, 2nd, 3rd, etc); per evaluation range of position
    private int[][] tEvaluationCounts;

    // The total number of moves that matched a given engine move (1st, 2nd, 3rd, etc); regardless of evaluation
    private int[] totalEvaluationCounts;

    // The total number of moves that were found to be in each evaluation range
    private int[] rangeCounts;

    // The total number of moves
    private int totalMoves;

    // Moves made from winning / losing positions
    private int losingPositionMoves;
    private int winningPositionMoves;
    private int[] tCountsLosingPosition;
    private int[] tCountsWinningPosition;

    // State to track values for CP loss calculations
    private int[] rangeCpLoss;
    private int[] rangeCpCounts;
    private int totalCpLossLosingPositions;
    private int totalCpLossWinningPositions;
    private int totalCpMovesLosingPositions;
    private int totalCpMovesWinningPositions;
    private int totalCpLoss;
    private int totalCpMoves;

    public Stats(int variations) {

        this.variations = variations;

        tEvaluationCounts = new int[Range.values().length][variations];
        rangeCounts = new int[Range.values().length];
        rangeCpLoss = new int[Range.values().length];
        rangeCpCounts = new int[Range.values().length];

        losingPositionMoves = 0;
        winningPositionMoves = 0;
        tCountsLosingPosition = new int[variations];
        tCountsWinningPosition = new int[variations];

        totalEvaluationCounts = new int[variations];
        totalMoves = 0;
        totalCpLoss = 0;
        totalCpMoves = 0;

        totalCpLossLosingPositions = 0;
        totalCpLossWinningPositions = 0;
        totalCpMovesLosingPositions = 0;
        totalCpMovesWinningPositions = 0;

    }

    /**
     * Adds an entire games worth of {@code EvaluatedMove} to the stats.
     *
     * @param moves the moves to consider
     * @param side the side to analyze
     * @param bookMoves the number of moves to skip (as book moves)
     */
    public void addEvaluatedMoves(List<EvaluatedMove> moves, Side side, int bookMoves) {

        for (int i=bookMoves*2; i<moves.size(); i++) {

            if (side != null && Side.WHITE.equals(side) && i % 2 == 1) continue;
            if (side != null && Side.BLACK.equals(side) && i % 2 == 0) continue;

            EvaluatedMove evaluatedMove = moves.get(i);
            addEvaluatedMove(evaluatedMove);

        }

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
        System.out.println(String.format("%-35s %-10s %-10s %-10s %-10s %-10s ", "Eval", "N", "AvgCP-", "T1%", "T2%", "T3%"));
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

        System.out.println("---------------------------------------------------------------------------------------");
        System.out.println(String.format("%-35s %-10d %-10.2f %-10.2f %-10.2f %-10.2f ",
                "Losing",
                losingPositionMoves,
                totalCpMovesLosingPositions > 0 ? (double) totalCpLossLosingPositions / (double) totalCpMovesLosingPositions : 0.00,
                losingPositionMoves > 0 ? 100.0 * (double) tCountsLosingPosition[0] / (double) losingPositionMoves : 0.00,
                losingPositionMoves > 0 ? 100.0 * (double) tCountsLosingPosition[1] / (double) losingPositionMoves : 0.00,
                losingPositionMoves > 0 ? 100.0 * (double) tCountsLosingPosition[2] / (double) losingPositionMoves : 0.00
        ));
        System.out.println(String.format("%-35s %-10d %-10.2f %-10.2f %-10.2f %-10.2f ",
                "Winning",
                winningPositionMoves,
                totalCpMovesWinningPositions > 0 ? (double) totalCpLossWinningPositions / (double) totalCpMovesWinningPositions : 0.00,
                winningPositionMoves > 0 ? 100.0 * (double) tCountsWinningPosition[0] / (double) winningPositionMoves : 0.00,
                winningPositionMoves > 0 ? 100.0 * (double) tCountsWinningPosition[1] / (double) winningPositionMoves : 0.00,
                winningPositionMoves > 0 ? 100.0 * (double) tCountsWinningPosition[2] / (double) winningPositionMoves : 0.00
        ));

        System.out.println("---------------------------------------------------------------------------------------");
        System.out.println(String.format("%-35s %-10d %-10.2f %-10.2f %-10.2f %-10.2f ",
                "All Positions",
                totalMoves,
                totalCpMoves > 0 ? (double) totalCpLoss / (double) totalCpMoves : 0.00,
                totalMoves > 0 ? 100.0 * (double) totalEvaluationCounts[0] / (double) totalMoves : 0.00,
                totalMoves > 0 ? 100.0 * (double) totalEvaluationCounts[1] / (double) totalMoves : 0.00,
                totalMoves > 0 ? 100.0 * (double) totalEvaluationCounts[2] / (double) totalMoves : 0.00
        ));

        System.out.println();
        System.out.println(String.format("Analysis took %s", duration.toString()));

    }

    /**
     * Adds an {@code EvaluatedMove} to the stats.
     *
     * @param move the move to consider
     */
    synchronized private void addEvaluatedMove(EvaluatedMove move) {

        int rangeIndex = Range.getRangeIndex(move.getPositionEvaluation());
        if (rangeIndex >= 0) {

            // count for CP-
            if (move.includeForCpLoss()) {
                rangeCpLoss[rangeIndex] += move.getCpLoss();
                rangeCpCounts[rangeIndex]++;

                if (Range.isWinning(rangeIndex)) {
                    totalCpLossWinningPositions += move.getCpLoss();
                    totalCpMovesWinningPositions++;
                } else if (Range.isLosing(rangeIndex)) {
                    totalCpLossLosingPositions += move.getCpLoss();
                    totalCpMovesLosingPositions++;
                }

                totalCpLoss += move.getCpLoss();
                totalCpMoves++;

            }

            // count for T%
            rangeCounts[rangeIndex]++;
            if (Range.isWinning(rangeIndex)) {
                winningPositionMoves++;
            } else if (Range.isLosing(rangeIndex)) {
                losingPositionMoves++;
            }
            totalMoves++;

            int engineMoveIndex = move.getEngineMatchIndex();
            if (engineMoveIndex >= 0) {
                for (int eMove = engineMoveIndex; eMove < variations; eMove++) {
                    if (move.includeForTAnalysis(eMove)) {
                        tEvaluationCounts[rangeIndex][eMove]++;
                        if (Range.isWinning(rangeIndex)) {
                            tCountsWinningPosition[eMove]++;
                        } else if (Range.isLosing(rangeIndex)) {
                            tCountsLosingPosition[eMove]++;
                        }
                        totalEvaluationCounts[eMove]++;
                    }
                }
            }

        }

    }

    /**
     * Enum representing various evaluation ranges.
     */
    enum Range {

        MATED( eval -> eval <= -2000, "Opponent has forced mate" ),
        CRUSHED( eval -> -2000 < eval && eval <= -900 , "Losing by more than a Queen"),
        ROOK_DOWN( eval -> -900 < eval && eval <= -500, "Losing by a Rook" ),
        PIECE_DOWN( eval -> -500 < eval && eval <= -300, "Losing by a Piece" ),
        PAWNS_DOWN( eval -> -300 < eval && eval <= -100, "Losing by some pawns" ),
        EVEN( eval -> -100 <= eval && eval <= 100, "Even (+/- 100 CP)" ),
        PAWNS_UP( eval -> 100 <= eval && eval < 300, "Winning by some pawns" ),
        PIECE_UP( eval -> 300 <= eval && eval < 500, "Winning by a Piece" ),
        ROOK_UP( eval -> 500 <= eval && eval < 900, "Winning by a Rook" ),
        CRUSHING( eval -> 900 <= eval && eval < 2000, "Winning by more than a Queen" ),
        MATING( eval -> eval >= 2000,  "Player has forced mate"),
        ;

        IntPredicate predicate;
        String description;

        Range(IntPredicate predicate, String description) {
            this.predicate = predicate;
            this.description = description;
        }

        /**
         * Returns the matching range of an evaluation.
         *
         * @param evaluation the evaluation to match.
         * @return the index representing the matched range; otherwise -1
         */
        static int getRangeIndex(int evaluation) {
            for (int i=0; i<values().length; i++) {
                if (values()[i].predicate.test(evaluation)) return i;
            }
            return -1;
        }

        static boolean isWinning(int rangeIndex) {
            return rangeIndex > 5 && rangeIndex <= 10;
        }

        static boolean isLosing(int rangeIndex) {
            return rangeIndex >= 0 && rangeIndex < 5;
        }

    }

}
