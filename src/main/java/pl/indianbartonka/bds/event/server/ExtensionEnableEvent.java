package pl.indianbartonka.bds.event.server;

import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.bds.extension.Extension;

public class ExtensionEnableEvent extends Event {

    private final Extension extension;

    public ExtensionEnableEvent(final Extension extension) {
        this.extension = extension;
    }

    public Extension getExtension() {
        return this.extension;
    }
}