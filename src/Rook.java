public class Rook extends Piece {
    Rook(Chess chess, PiecesColor color, PiecesLocation loc, boolean moved) {
        super(chess, color, PiecesName.Rook, loc, moved);
    }

    private boolean validTo(PiecesLocation newLoc) {
        int col_offset = Math.abs(newLoc.col-this.location.col);
        int row_offset = Math.abs(newLoc.row-this.location.row);

        //File only
        if(col_offset != 0 && row_offset != 0) {
            if(verbose)
                System.out.println("Rook:validTo(): File path only!");
            return false;
        }

        //Check pieces may be overed
        if(overPiece(newLoc)) {
            if(verbose)
                System.out.println("Rook:validTo(): There is a piece on the path!");
            return false;
        }
        return true;
    }

    @Override
    boolean tryMoveTo(PiecesLocation newLoc, boolean peek) {
        if(trace)
            System.out.println("Rook:tryMoveTo(): " + this.toString() + " -> " + newLoc.toString());

        if(!validTo(newLoc))
            return false;

        if(chess.pieceAt(newLoc) != null) {
            if(verbose)
                System.out.println("Rook:tryMoveTo(): Not an empty location!!");
            return false;
        }

        if(trace)
            System.out.println("Rook:tryMoveTo(): Moving to " + newLoc.toString());
        if(peek)
            return true;
        //Action!
        chess.movePiece(this, newLoc);
        return true;
    }

    @Override
    boolean tryEatAt(PiecesLocation newLoc, boolean peek) {
        if(trace)
            System.out.println("Rook:tryEatAt(): " + this.toString() + " -> " + newLoc.toString());

        if(!validTo(newLoc))
            return false;

        if(!isMyFood(newLoc)) {
            if(verbose)
                System.out.println("Rook:tryEatAt(): It's not my food!");
            return false;
        }

        if(trace)
            System.out.println("Rook:tryEatAt(): Eating and moving to " + newLoc.toString());
        if(peek)
            return true;
        //Action!
        chess.capturePiece(newLoc);
        chess.movePiece(this, newLoc);
        return true;
    }

}

