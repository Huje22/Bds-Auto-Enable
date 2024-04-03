package me.indian.bds.event;

public abstract class ResponsibleEvent {

    protected String eventName = this.getClass().getSimpleName();

    public final String getEventName() {
        return this.eventName;
    }
}