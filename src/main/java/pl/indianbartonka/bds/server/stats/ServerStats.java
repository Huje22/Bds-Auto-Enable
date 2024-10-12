package pl.indianbartonka.bds.server.stats;

public class ServerStats {

    private long totalUpTime;

    public ServerStats(final long totalUpTime) {
        this.totalUpTime = totalUpTime;
    }

    public long getTotalUpTime() {
        return this.totalUpTime;
    }

    public void addOnlineTime(final long time) {
        this.totalUpTime += time;
    }
}