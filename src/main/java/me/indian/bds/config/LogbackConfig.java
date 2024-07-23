package me.indian.bds.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import me.indian.bds.util.DefaultsVariables;

public final class LogbackConfig {

    private static boolean INITED = false;

    private LogbackConfig() {
    }

    public static void init() {
        if (INITED) return;

        final String logbackDir = DefaultsVariables.getAppDir() + File.separator + "config" + File.separator + "logback.xml";

        final File file = new File(logbackDir);
        if (!file.exists()) createLogbackConfig(file);

        System.setProperty("logback.configurationFile", logbackDir);
        INITED = true;
    }

    private static void createLogbackConfig(final File logbackConfig) {
        try {
            if (logbackConfig.createNewFile()) {
                try (final FileWriter writer = new FileWriter(logbackConfig)) {
                    writer.write(defaultConfig());
                }
            } else {
                throw new RuntimeException("Nie udało się utworzyć konfiguracji logback");
            }
        } catch (final IOException exception) {
            throw new RuntimeException("Nie udało się utworzyć konfiguracji logback", exception);
        }
    }

    private static String defaultConfig() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <configuration>
                    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
                        <encoder>
                            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %blue(LogBack) %magenta(Logger) [%thread] %highlight(%-5level){TRACE=blue, DEBUG=green, INFO=white, WARN=yellow, ERROR=red} %logger{36} - %msg%n
                             </pattern>
                        </encoder>
                    </appender>
                                
                    <logger name="net.dv8tion.jda" level="ERROR"/>
                    <logger name="net.dv8tion.jda" level="WARN"/>
                                
                    <logger name="org.eclipse.jetty" level="WARN"/>
                    <logger name="org.eclipse.jetty" level="ERROR"/>
                    <logger name="io.javalin" level="ERROR"/>
                    <logger name="io.javalin" level="WARN"/>
                                
                    <root level="INFO">
                        <appender-ref ref="STDOUT"/>
                    </root>
                </configuration>
                """;
    }
}