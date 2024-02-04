package me.indian.bds.event.server;

import me.indian.bds.event.Event;

public class TPSChangeEvent extends Event {

    private final int tps,lastTps;

    public TPSChangeEvent(final int tps , final  int lastTps){
        this.tps = tps;
        this.lastTps = lastTps;

    }

    public int getTps() {
        return this.tps;
    }

    public int getLastTps() {
        return this.lastTps;
    }
}