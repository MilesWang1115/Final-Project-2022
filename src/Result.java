
public class Result {
    boolean draw;
    PiecesColor winner;
    PiecesAction action;

    Result(PiecesColor winner, PiecesAction action) {
        this.draw = false;
        this.winner = winner;
        this.action = action;
    }

    Result(PiecesAction action) {
        this.draw = true;
        this.winner = PiecesColor.White;
        this.action = action;
    }
}