public class Rules {
    private int minimumNeighboursToLive;
    private int maxNeighboursToLive;
    private int neighboursToBecomeAlive;

    public Rules(int minimumNeighboursToLive, int maxNeighboursToLive, int neighboursToBecomeAlive) {
        this.minimumNeighboursToLive = minimumNeighboursToLive;
        this.maxNeighboursToLive = maxNeighboursToLive;
        this.neighboursToBecomeAlive = neighboursToBecomeAlive;
    }

    public int getMinimumNeighboursToLive() {
        return minimumNeighboursToLive;
    }

    public int getMaxNeighboursToLive() {
        return maxNeighboursToLive;
    }

    public int getNeighboursToBecomeAlive() {
        return neighboursToBecomeAlive;
    }
}
