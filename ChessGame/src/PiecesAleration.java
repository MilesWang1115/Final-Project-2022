public class PiecesAleration {
    Piece piece;
    PiecesLocation location;
    PiecesAction action;

    PiecesAleration(Piece piece, PiecesLocation location, PiecesAction action) {
        this.location = location;
        this.piece = piece;
        this.action = action;
    }
}
