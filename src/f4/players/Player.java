package f4.players;


public interface Player {

    //public enum PlayerType {Human, Computer};

    //Metodi per il gioco
    public int getAction();
    public int getLastAction();
    public void setAction(int action);

    //Metodi per statistiche
    public int getActionNumber();

    public int getComputedNodes();
    public int getLastActionComputedNodes();
    public int getComputedNodesAverage();


    public float getElaborationTime();
    public float getLastActionElaborationTime();
    public float getElaborationTimeAverage();
}
