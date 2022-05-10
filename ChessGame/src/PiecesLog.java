
public class PiecesLog {
    PiecesColor	color;
    PiecesAction action;
    String note;

    //Standard constructor
    PiecesLog(PiecesColor turns, PiecesAction action, String note) {
        this.color = turns;
        this.action = action;
        this.note = note;
    }

    //Constructor for 'Check' action
    PiecesLog(Piece piece, Piece king) {
        this.color = piece.color();
        this.action = PiecesAction.Check;
        this.note = piece + " " + king;
    }

    public String toString() {
        return color + " " + action + " " + note;
    }

}
