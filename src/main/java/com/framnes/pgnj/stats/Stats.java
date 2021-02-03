package com.framnes.pgnj.stats;

import com.framnes.pgnj.evaluation.EvaluatedMove;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.game.GameResult;

import java.time.Duration;
import java.util.List;
import java.util.function.BiPredicate;
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

    private int winningToLosing = 0;
    private int winningToLosingWonGames = 0;
    private int winningToLosingLostGames = 0;

    private int losingToWinning = 0;
    private int losingToWinningWonGames = 0;
    private int losingToWinningLostGames = 0;

    public Stats(int variations) {

        this.variations = variations;

        tEvaluationCounts = new int[Range.values().length][variations];
        rangeCounts = new int[Range.values().length];

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
     * @param elo (optional) opponents elo; 0 if not given.
     */
    public void addEvaluatedMoves(List<EvaluatedMove> moves, Side side, int bookMoves, Boolean playerWon, int elo) {

        for (int i=bookMoves*2; i<moves.size(); i++) {

            if (side != null && Side.WHITE.equals(side) && i % 2 == 1) continue;
            if (side != null && Side.BLACK.equals(side) && i % 2 == 0) continue;

            EvaluatedMove evaluatedMove = moves.get(i);
            addEvaluatedMove(evaluatedMove, playerWon, elo);

            // Assuming we have a previous move, grab it to figure out if this move transitioned us from winning to
            // losing, or the other way around.
            if (i > 0) {

                EvaluatedMove previousMove = moves.get(i-1);
                if (previousMove.getPositionEvaluation() < 0 && evaluatedMove.getPositionEvaluation() > 0) {
                    losingToWinning++;
                    if (playerWon != null && playerWon) {
                        losingToWinningWonGames++;
                    } else if (playerWon != null && !playerWon) {
                        losingToWinningLostGames++;
                    }
                } else if (previousMove.getPositionEvaluation() > 0 && evaluatedMove.getPositionEvaluation() < 0) {
                    winningToLosing++;
                    if (playerWon != null && playerWon) {
                        winningToLosingWonGames++;
                    } else if (playerWon != null && !playerWon) {
                        winningToLosingLostGames++;
                    }
                }

            }

        }

    }

    public void addEvaluatedMoves(List<EvaluatedMove> moves, Side side, int bookMoves) {
        addEvaluatedMoves(moves, side, bookMoves, null, 0);
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
            System.out.println(String.format("%-35s %-10d %-10s %-10.2f %-10.2f %-10.2f ",
                    ranges[i].description,
                    rangeCounts[i],
                    "N/A",
                    rangeCounts[i] > 0 ? 100.0 * (double) tEvaluationCounts[i][0] / (double) rangeCounts[i] : 0.00,
                    rangeCounts[i] > 0 ? 100.0 * (double) tEvaluationCounts[i][1] / (double) rangeCounts[i] : 0.00,
                    rangeCounts[i] > 0 ? 100.0 * (double) tEvaluationCounts[i][2] / (double) rangeCounts[i] : 0.00
            ));
        }
        System.out.println();
        System.out.println(String.format("Losing to Winning: %d moves", losingToWinning));
        System.out.println(String.format("Winning to Losing: %d moves", winningToLosing));

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
        System.out.println(String.format("Losing to Winning: %d moves", losingToWinningWonGames));
        System.out.println(String.format("Winning to Losing: %d moves", winningToLosingWonGames));

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
        System.out.println(String.format("Losing to Winning: %d moves", losingToWinningLostGames));
        System.out.println(String.format("Winning to Losing: %d moves", winningToLosingLostGames));

        System.out.println();
        System.out.println(String.format("Analysis took %s", duration.toString()));

    }

    /**
     * Adds an {@code EvaluatedMove} to the stats.
     *
     * @param move the move to consider
     * @param playerWon (optional) true if the analyzed player won, false if they lost lost; null for a draw.
     * @param elo (optional) opponents elo; 0 if not given.
     */
    synchronized private void addEvaluatedMove(EvaluatedMove move, Boolean playerWon, int elo) {

        int[][] resultBasedEvaluationCounts = getResultBasedEvaluationCounts(playerWon);
        int[] resultBasedRangeCounts = getResultBasedRangeCounts(playerWon);

        List<Integer> rangeIndices = Range.getRangeIndices(move.getPositionEvaluation(), elo);
        if (!rangeIndices.isEmpty()) {
            for (Integer rangeIndex : rangeIndices) {

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

        MATED( (eval, elo) -> eval <= -2000, "Opponent has forced mate" ),
        CRUSHED( (eval, elo) -> -2000 < eval && eval <= -900 , "Losing by more than a Queen"),
        ROOK_DOWN( (eval, elo) -> -900 < eval && eval <= -500, "Losing by a Rook" ),
        PIECE_DOWN( (eval, elo) -> -500 < eval && eval <= -300, "Losing by a Piece" ),
        PAWNS_DOWN( (eval, elo) -> -300 < eval && eval < -100, "Losing by some pawns" ),
        EVEN( (eval, elo) -> -100 <= eval && eval <= 100, "Even (+/- 100 CP)" ),
        PAWNS_UP( (eval, elo) -> 100 < eval && eval < 300, "Winning by some pawns" ),
        PIECE_UP( (eval, elo) -> 300 <= eval && eval < 500, "Winning by a Piece" ),
        ROOK_UP( (eval, elo) -> 500 <= eval && eval < 900, "Winning by a Rook" ),
        CRUSHING( (eval, elo) -> 900 <= eval && eval < 2000, "Winning by more than a Queen" ),
        MATING( (eval, elo) -> eval >= 2000,  "Player has forced mate"),

        LOSING( (eval, elo) -> eval < -100, "Losing"),
        WINNING( (eval, elo) -> eval > 100, "Winning"),

        ELO_UNKNOWN( (eval, elo) -> elo == 0, "Unknown Elo"),
        ELO_LESS_1000( (eval, elo) -> elo < 1000, "Elo < 1000"),
        ELO_1000_1099( (eval, elo) -> elo >= 1000 && elo < 1100, "Elo 1000-1099"),
        ELO_1100_1199( (eval, elo) -> elo >= 1100 && elo < 1200, "Elo 1100-1199"),
        ELO_1200_1299( (eval, elo) -> elo >= 1200 && elo < 1300, "Elo 1200-1299"),
        ELO_1300_1399( (eval, elo) -> elo >= 1300 && elo < 1400, "Elo 1300-1399"),
        ELO_1400_1499( (eval, elo) -> elo >= 1400 && elo < 1500, "Elo 1400-1499"),
        ELO_1500_1599( (eval, elo) -> elo >= 1500 && elo < 1600, "Elo 1500-1599"),
        ELO_1600_1699( (eval, elo) -> elo >= 1600 && elo < 1700, "Elo 1600-1699"),
        ELO_1700_1799( (eval, elo) -> elo >= 1700 && elo < 1800, "Elo 1700-1799"),
        ELO_1800_1899( (eval, elo) -> elo >= 1800 && elo < 1900, "Elo 1800-1899"),
        ELO_1900_1999( (eval, elo) -> elo >= 1900 && elo < 2000, "Elo 1900-1999"),
        ELO_OVER_2000( (eval, elo) -> elo >= 2000, "Elo >= 2000"),

        TOTAl((eval, elo) -> true, "Total")

        ;

        BiPredicate<Integer, Integer> predicate;
        String description;

        Range(BiPredicate<Integer, Integer> predicate, String description) {
            this.predicate = predicate;
            this.description = description;
        }

        /**
         * Returns the matching ranges of an evaluation.
         *
         * @param evaluation the evaluation to match.
         * @return the indeces representing the matched range; otherwise -1
         */
        static List<Integer> getRangeIndices(int evaluation, int elo) {
            return IntStream.range(0, values().length)
                    .filter( i -> values()[i].predicate.test(evaluation, elo) )
                    .boxed()
                    .collect(Collectors.toList());
        }

    }

}
