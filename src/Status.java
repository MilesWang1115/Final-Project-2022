import java.util.ArrayList;

public class Status {
    public boolean endGame;
    public boolean error;
    public String note;
    private ArrayList<PiecesAlteration> alterations;
    public Result result;

    Status(boolean endGame, boolean error, String note, Result result) {
        this.endGame = endGame;
        this.error = error;
        this.note = note;
        this.alterations = new ArrayList<>();
        this.result = result;
    }

    //Shortcut to get an ERROR status
    Status(String errorNote) {
        this.endGame = false;
        this.error = true;
        this.note = errorNote;
        this.alterations = null;
        this.result = null;
    }

    //Shortcut to get a Normal status
    Status() {
        this.endGame = false;
        this.error = false;
        this.note = "";
        alterations = new ArrayList<>();
        this.result = null;
    }

    public void add(PiecesAlteration alteration) {
        if(alterations != null)
            alterations.add(alteration);
    }

    public PiecesAlteration get(int index) {
        if(alterations != null)
            return alterations.get(index);
        return null;
    }

    public int size() {
        if(alterations != null)
            return alterations.size();
        return 0;
    }
}
