public class PiecesLocation {
    int col;
    int row;
    private static String locationAlpha = "abcdefgh";

    boolean validCol(int col) {
        if(col < 0 || col > 7) {
            System.out.println("PiecesLocationi: col=" + String.valueOf(col) + " is out of range! (0~7)");
            return false;
        }
        return true;
    }

    boolean validRow(int row) {
        if(row < 0 || row > 7) {
            System.out.println("PiecesLocationi: row=" + String.valueOf(row) + " is out of range! (0~7)");
            return false;
        }
        return true;
    }

    //Location String Format : "a1" -> "h8"
    PiecesLocation(String strLoc) {
        col = locationAlpha.indexOf(strLoc.substring(0, 1));
        if(!validCol(col))
            throw new NumberFormatException();
        row = Integer.parseInt(strLoc.substring(1)) - 1;
        if(!validRow(row))
            throw new NumberFormatException();
    }

    PiecesLocation(int col, int row) {
        this.col = col;
        if(!validCol(col))
            throw new NumberFormatException();
        this.row = row;
        if(!validRow(row))
            throw new NumberFormatException();
    }

    public String toString() {
        String res = locationAlpha.substring(col, col+1) + String.valueOf(row+1) ;
        return res;
    }

    public boolean equals(PiecesLocation loc) {
        if(this == loc)
            return true;
        if(this.row != loc.row)
            return false;
        if(this.col != loc.col)
            return false;
        return true;
    }
}
