package com.framnes.pgnj.stats;

import com.framnes.pgnj.evaluation.EvaluatedMove;
import com.github.bhlangonijr.chesslib.Side;

import java.util.List;
import java.util.function.IntPredicate;

/**
 * Class representing the compiled analysis and statistics for the given PGN.
 */
public class Stats {

    // A two-dimensional array measuring T1, T2, ..., Tn moves; from each score range
    private int[][] tEvaluationCounts;
    private int[] totalEvaluationCounts;

    // A total number of moves found for every range
    private int[] rangeCounts;
    private int totalMoves;

    // A total of lost CPs found for every range (used to create average)
    private int[] rangeCpLoss;
    private int totalCpLoss;

    public Stats(int variations) {
        tEvaluationCounts = new int[Range.values().length][variations];
        rangeCounts = new int[Range.values().length];
        rangeCpLoss = new int[Range.values().length];

        totalEvaluationCounts = new int[variations];
        totalMoves = 0;
        totalCpLoss = 0;

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

            if (Side.WHITE.equals(side) && i % 2 == 1) continue;
            if (Side.BLACK.equals(side) && i % 2 == 0) continue;

            EvaluatedMove evaluatedMove = moves.get(i);
            addEvaluatedMove(evaluatedMove);

        }

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
                    rangeCounts[i] > 0 ? (double) rangeCpLoss[i] / (double) rangeCounts[i] : 0.00,
                    rangeCounts[i] > 0 ? 100.0 * (double) tEvaluationCounts[i][0] / (double) rangeCounts[i] : 0.00,
                    rangeCounts[i] > 0 ? 100.0 * (double) tEvaluationCounts[i][1] / (double) rangeCounts[i] : 0.00,
                    rangeCounts[i] > 0 ? 100.0 * (double) tEvaluationCounts[i][2] / (double) rangeCounts[i] : 0.00
            ));
        }

        System.out.println("---------------------------------------------------------------------------------------");
        System.out.println(String.format("%-35s %-10d %-10.2f %-10.2f %-10.2f %-10.2f ",
                "All Positions",
                totalMoves,
                totalMoves > 0 ? (double) totalCpLoss / (double) totalMoves : 0.00,
                totalMoves > 0 ? 100.0 * (double) totalEvaluationCounts[0] / (double) totalMoves : 0.00,
                totalMoves > 0 ? 100.0 * (double) totalEvaluationCounts[1] / (double) totalMoves : 0.00,
                totalMoves > 0 ? 100.0 * (double) totalEvaluationCounts[2] / (double) totalMoves : 0.00
        ));


    }

    /**
     * Adds an {@code EvaluatedMove} to the stats.
     *
     * @param move the move to consider
     */
    synchronized private void addEvaluatedMove(EvaluatedMove move) {

        int rangeIndex = Range.getRangeIndex(move.getPositionEvaluation());
        if (rangeIndex > 0) {
            rangeCounts[rangeIndex]++;
            rangeCpLoss[rangeIndex] += move.getCpLoss();

            totalMoves++;
            totalCpLoss += move.getCpLoss();

            int engineMoveIndex = move.getEngineMatchIndex();
            if (engineMoveIndex >= 0) {
                tEvaluationCounts[rangeIndex][engineMoveIndex]++;
                totalEvaluationCounts[engineMoveIndex]++;
            }

        }

    }

    /**
     * Enum representing various evaluation ranges.
     */
    enum Range {

        CRUSHED( eval -> eval < -900 , "Losing by more than a Queen"),
        ROOK_DOWN( eval -> -900 < eval && eval < -500, "Losing by a Rook" ),
        PIECE_DOWN( eval -> -500 < eval && eval <= -300, "Losing by a Piece" ),
        PAWNS_DOWN( eval -> -300 < eval && eval <= -100, "Losing by some pawns" ),
        EVEN( eval -> -100 <= eval && eval <= 100, "Even (+/- 100 CP)" ),
        PAWNS_UP( eval -> 100 <= eval && eval < 300, "Winning by some pawns" ),
        PIECE_UP( eval -> 300 <= eval && eval < 500, "Winning by a Piece" ),
        ROOK_UP( eval -> 500 <= eval && eval < 900, "Winning by a Rook" ),
        CRUSHING( eval -> 900 < eval, "Winning by more than a Queen" )
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

    }

}
