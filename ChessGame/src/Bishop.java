public class Bishop extends Piece {

    Bishop(Chess chess,PiecesColor color, PiecesLocation loc, boolean moved) {
        super(chess, color, PiecesName.Bishop, loc, moved);
    }

    private boolean validTo(PiecesLocation newLoc) {
        int col_offset = Math.abs(newLoc.col-this.location.col);
        int row_offset = Math.abs(newLoc.row-this.location.row);

        //Diagonal only
        if(col_offset == 0 || row_offset == 0) {
            if(verbose)
                System.out.println("Bishop:validTo(): Diagonal path only!");
            return false;
        }
        if(col_offset != row_offset) {
            if(verbose)
                System.out.println("Bishop:validTo(): Not a 45 degree direction!");
            return false;
        }

        //Check pieces may be overed
        if(overPiece(newLoc)) {
            if(verbose)
                System.out.println("Bishop:validTo(): There is a piece on the path!");
            return false;
        }
        return true;
    }

    @Override
    boolean tryMoveTo(PiecesLocation newLoc, boolean peek) {
        if(trace)
            System.out.println("Bishop:tryMoveTo(): " + this.toString() + " -> " + newLoc.toString());

        if(chess.pieceAt(newLoc) != null) {
            if(verbose)
                System.out.println("Bishop:tryMoveTo(): Not an empty location!");
            return false;
        }

        if(!validTo(newLoc))
            return false;

        if(trace)
            System.out.println("Bishop:tryMoveTo(): Moving to " + newLoc.toString());
        if(peek)
            return true;
        //Action!
        chess.movePiece(this, newLoc);
        return true;
    }

    @Override
    boolean tryEatAt(PiecesLocation newLoc, boolean peek) {
        if(trace)
            System.out.println("Bishop:tryEatAt(): " + this.toString() + " -> " + newLoc.toString());

        if(!isMyFood(newLoc)) {
            if(verbose)
                System.out.println("Bishop:tryEatAt(): It's not my food!");
            return false;
        }

        if(!validTo(newLoc))
            return false;

        if(trace)
            System.out.println("Bishop:tryEatAt(): Eating and Moving to " + newLoc.toString());
        if(peek)
            return true;
        //Action!
        chess.capturePiece(newLoc);
        chess.movePiece(this, newLoc);
        return true;
    }
}
