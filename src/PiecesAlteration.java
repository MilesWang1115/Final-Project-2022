public class PiecesAlteration {
    Piece piece;
    PiecesLocation location;
    PiecesAction action;

    PiecesAlteration(Piece piece, PiecesLocation location, PiecesAction action) {
        this.location = location;
        this.piece = piece;
        this.action = action;
    }
}
