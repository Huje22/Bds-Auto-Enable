package pl.indianbartonka.bds.event.server;

import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.bds.extension.Extension;

public class ExtensionDisableEvent extends Event {

    private final Extension extension;

    public ExtensionDisableEvent(final Extension extension) {
        this.extension = extension;
    }

    public Extension getExtension() {
        return this.extension;
    }
}