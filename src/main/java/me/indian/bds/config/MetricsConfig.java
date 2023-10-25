package me.indian.bds.config;

public class MetricsConfig {

    private final boolean enabled;
    private final boolean logFailedRequests;
    private final boolean logSentData;
    private final boolean logResponseStatusText;

    public MetricsConfig(final boolean enabled, final boolean logFailedRequests, final boolean logSentData, final boolean logResponseStatusText) {
        this.enabled = enabled;
        this.logFailedRequests = logFailedRequests;
        this.logSentData = logSentData;
        this.logResponseStatusText = logResponseStatusText;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isLogFailedRequests() {
        return this.logFailedRequests;
    }

    public boolean isLogSentData() {
        return this.logSentData;
    }

    public boolean isLogResponseStatusText() {
        return this.logResponseStatusText;
    }
}