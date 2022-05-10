public class Knight extends Piece {

    Knight(Chess chess, PiecesColor color, PiecesLocation loc, boolean moved) {
        super(chess, color, PiecesName.Knight, loc, moved);
    }

    private boolean validTo(PiecesLocation newLoc) {
        int col_offset = Math.abs(newLoc.col-this.location.col);
        int row_offset = Math.abs(newLoc.row-this.location.row);

        /*
         *  +-+-+
         *  | | |
         *  +-+-+
         */
        if(col_offset == 2 && row_offset == 1)
            return true;

        /*
         * +-+
         * | |
         * +-+
         * | |
         * +-+
         */
        if(col_offset == 1 && row_offset == 2)
            return true;

        if(verbose)
            System.out.println("Knight:validTo(): Invalid path patten!");
        return false;
    }

    @Override
    public String shortName() {
        return "N";
    }

    @Override
    boolean tryMoveTo(PiecesLocation newLoc, boolean peek) {
        if(trace)
            System.out.println("Knight:tryMoveTo(): " + this.toString() + " -> " + newLoc.toString());

        if(!validTo(newLoc))
            return false;

        if(chess.pieceAt(newLoc) != null) {
            if(verbose)
                System.out.println("Knight:tryMoveTo(): Not an empty location!");
            return false;
        }

        if(trace)
            System.out.println("Knight:tryMoveTo(): Moving to " + newLoc.toString());
        if(peek)
            return true;
        //Action!
        chess.movePiece(this, newLoc);
        return true;
    }

    @Override
    boolean tryEatAt(PiecesLocation newLoc, boolean peek) {
        if(trace)
            System.out.println("Knight:tryEatAt(): " + this.toString() + " -> " + newLoc.toString());

        if(!validTo(newLoc))
            return false;

        if(!isMyFood(newLoc)) {
            if(verbose)
                System.out.println("Knight:tryEatAt(): It's not my food!");
            return false;
        }

        if(trace)
            System.out.println("Knight:tryEatAt(): Eating and moving to " + newLoc.toString());
        if(peek)
            return true;
        //Action!
        chess.capturePiece(newLoc);
        chess.movePiece(this, newLoc);
        return true;
    }

}

