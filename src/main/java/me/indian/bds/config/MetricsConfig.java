package me.indian.bds.config;

public record MetricsConfig(boolean enabled, boolean logFailedRequests, boolean logSentData,
                            boolean logResponseStatusText) {

}