package edu.merrimack.simplechat.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

/**
 * Programmatic logback configuration so the log file can be chosen from the server config.
 */
public final class LogUtil {

    /** Utility class; do not instantiate. */
    private LogUtil() {
    }

    /**
     * Configures logback to write INFO+ logs to the given file.
     */
    public static void configureLogging(String logFile) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setContext(context);
        ple.setPattern("%d{ISO8601} %-5level [%thread] %logger - %msg%n");
        ple.start();

        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(context);
        fileAppender.setFile(logFile);
        fileAppender.setEncoder(ple);
        fileAppender.start();

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(fileAppender);
        root.setLevel(Level.INFO);
    }
}
