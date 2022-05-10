public class Queen extends Piece {
    Queen(Chess chess, PiecesColor color, PiecesLocation loc, boolean moved) {
        super(chess, color, PiecesName.Queen, loc, moved);
        //trace = true;
        //verbose = true;
    }

    private boolean validTo(PiecesLocation newLoc) {
        //Check path
        int col_offset = Math.abs(newLoc.col-this.location.col);
        int row_offset = Math.abs(newLoc.row-this.location.row);

        boolean valid_path = false;
        //File path
        if(col_offset == 0 && row_offset > 0)
            valid_path = true;
        //Rank path
        if(row_offset == 0 && col_offset > 0)
            valid_path = true;
        //Diagonal path
        if(col_offset > 0 && row_offset > 0 && col_offset == row_offset)
            valid_path = true;

        if(!valid_path) {
            if(verbose)
                System.out.println("Queen:validTo(): " + this.location + " -> " + newLoc +" is NOT a valid path!");
            return false;
        }

        //Check pieces may be overed
        if(overPiece(newLoc)) {
            if(verbose)
                System.out.println("Queen:validTo(): There is a piece on the path!");
            return false;
        }

        return true;
    }

    @Override
    boolean tryMoveTo(PiecesLocation newLoc, boolean peek) {
        if(trace)
            System.out.println("Queen:tryMoveTo(): " + this.toString() + " -> " + newLoc.toString());

        if(!validTo(newLoc))
            return false;

        if(chess.pieceAt(newLoc) != null) {
            if(verbose)
                System.out.println("Queen:tryMoveTo(): Not an empty location!" );
            return false;
        }

        if(trace)
            System.out.println("Queen:tryMoveTo(): Moving to " + newLoc.toString());
        if(peek)
            return true;
        //Action!
        chess.movePiece(this, newLoc);

        return true;
    }

    @Override
    boolean tryEatAt(PiecesLocation newLoc, boolean peek) {
        if(trace)
            System.out.println("Queen:tryEatAt(): " + this.toString() + " -> " + newLoc.toString());

        if(!validTo(newLoc))
            return false;

        if(!isMyFood(newLoc)) {
            if(verbose)
                System.out.println("Queen:tryEatAt(): There is no my food!" );
            return false;
        }

        if(trace)
            System.out.println("Queen:tryEatAt(): Eating and moving to " + newLoc.toString());
        if(peek)
            return true;
        //Action!
        chess.capturePiece(newLoc);
        chess.movePiece(this, newLoc);

        return true;

    }
}

