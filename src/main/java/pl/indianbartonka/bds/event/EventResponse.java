package pl.indianbartonka.bds.event;

public abstract class EventResponse {
    protected String eventName = this.getClass().getSimpleName();

    public final String getEventName() {
        return this.eventName;
    }
}
