package pl.indianbartonka.bds.server.allowlist.component;

public record AllowlistPlayer(boolean ignoresPlayerLimit, String name, long xuid) {
}