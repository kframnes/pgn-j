package com.framnes.pgnj.evaluation;

/**
 * An engine suggested move.
 */
public class EngineMove {

    final private String move;
    final private int evaluation;

    public EngineMove(String move, int evaluation) {
        this.move = move;
        this.evaluation = evaluation;
    }

    public String getMove() {
        return move;
    }

    public int getEvaluation() {
        return evaluation;
    }

}
