package me.indian.bds.event;

public abstract class ResponsibleEvent {

    protected String eventName = null;

    public final String getEventName() {
        return this.eventName == null ? this.getClass().getSimpleName() : this.eventName;
    }

}