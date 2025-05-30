package pl.indianbartonka.bds.event.server;

import pl.indianbartonka.bds.event.Event;

public class TPSChangeEvent extends Event {

    private final double tps, lastTps;

    public TPSChangeEvent(final double tps, final double lastTps) {
        this.tps = tps;
        this.lastTps = lastTps;

    }

    public double getTps() {
        return this.tps;
    }

    public double getLastTps() {
        return this.lastTps;
    }
}
