package pl.indianbartonka.bds.event;

public abstract class Event {

    protected String eventName = this.getClass().getSimpleName();

    public final String getEventName() {
        return this.eventName;
    }
}