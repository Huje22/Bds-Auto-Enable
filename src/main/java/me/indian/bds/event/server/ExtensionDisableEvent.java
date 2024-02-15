package me.indian.bds.event.server;

import me.indian.bds.event.Event;
import me.indian.bds.extension.Extension;

public class ExtensionDisableEvent extends Event {

    private final Extension extension;

    public ExtensionDisableEvent(final Extension extension) {
        this.extension = extension;
    }

    public Extension getExtension() {
        return this.extension;
    }
}