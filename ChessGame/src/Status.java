import java.util.ArrayList;

public class Status {
    public boolean endGame;
    public boolean error;
    public String note;
    private ArrayList<PiecesAleration> alerations;
    public Result result;

    Status(boolean endGame, boolean error, String note, Result result) {
        this.endGame = endGame;
        this.error = error;
        this.note = note;
        this.alerations = new ArrayList<>();
        this.result = result;
    }

    //Shortcut to get an ERROR status
    Status(String errorNote) {
        this.endGame = false;
        this.error = true;
        this.note = errorNote;
        this.alerations = null;
        this.result = null;
    }

    //Shortcut to get a Normal status
    Status() {
        this.endGame = false;
        this.error = false;
        this.note = "";
        alerations = new ArrayList<>();
        this.result = null;
    }

    public void add(PiecesAleration aleration) {
        if(alerations != null)
            alerations.add(aleration);
    }

    public PiecesAleration get(int index) {
        if(alerations != null)
            return alerations.get(index);
        return null;
    }

    public int size() {
        if(alerations != null)
            return alerations.size();
        return 0;
    }
}
