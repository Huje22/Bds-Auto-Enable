package me.indian.bds.bstats;

public class BstatsConfig {

    public final boolean enabled;
    public final String serverUuid;
    public final boolean logFailedRequests;
    public final boolean logSentData;
    public final boolean logResponseStatusText;

    public BstatsConfig(final boolean enabled, final String serverUuid, final boolean logFailedRequests, final boolean logSentData, final boolean logResponseStatusText) {
        this.enabled = enabled;
        this.serverUuid = serverUuid;
        this.logFailedRequests = logFailedRequests;
        this.logSentData = logSentData;
        this.logResponseStatusText = logResponseStatusText;
    }
}
