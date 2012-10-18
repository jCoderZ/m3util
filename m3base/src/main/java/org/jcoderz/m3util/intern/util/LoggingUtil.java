package org.jcoderz.m3util.intern.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * This utility class provides helper methods for dealing with the logging
 * subsystem.
 *
 * @author amandel
 * @author mrumpf
 *
 */
public class LoggingUtil {

    private LoggingUtil() {
        // do not instantiate
    }

    /**
     * Initializes the logging subsystem by adding two file handlers to the root
     * logger. One handler creates a log-file with name
     * "LOGGER_NAME-TIMESTAMP.all.log which collects ALL log statements. The
     * other one with filename "LOGGER_NAME-TIMESTAMP.info.log" which collects
     * only INFO level statements.
     *
     * @param logger the logger to initialize
     * @throws FileNotFoundException when the log file could not be opened
     */
    public static void initLogging(Logger logger) throws FileNotFoundException {
        initLogging(logger, logger.getName(), org.jcoderz.commons.types.Date.now().toString()
                .replace(':', '.'));
    }

    /**
     * Initializes the logging subsystem by adding two file handlers to the root
     * logger. One handler creates a log-file with name
     * "PREFIX-TIMESTAMP.all.log which collects ALL log statements. The other
     * one with filename "PREFIX-TIMESTAMP.info.log" which collects only INFO
     * level statements.
     *
     * @param logger the logger to initialize
     * @param prefix the log file prefix
     * @throws FileNotFoundException when the log file could not be opened
     */
    public static void initLogging(Logger logger, String prefix) throws FileNotFoundException {
        initLogging(logger, prefix, org.jcoderz.commons.types.Date.now().toString()
                .replace(':', '.'));
    }

    /**
     * Initializes the logging subsystem by adding two file handlers to the root
     * logger. One handler creates a log-file with name "PREFIX-SUFFIX.all.log
     * which collects ALL log statements. The other one with filename
     * "PREFIX-SUFFIX.info.log" which collects only INFO level statements.
     *
     * @param logger the logger to initialize (level set to ALL)
     * @param prefix the log file prefix
     * @param suffix the log file suffix
     * @throws FileNotFoundException when the log file could not be opened
     */
    public static void initLogging(Logger logger, final String prefix, final String suffix)
            throws FileNotFoundException {
        final File logFile = new File(Environment.getLogFolder(),
                logger.getName() + "-" + suffix + ".all.log");
        final StreamHandler handler = new StreamHandler(new FileOutputStream(
                logFile, true), new SimpleFormatter());
        handler.setLevel(Level.ALL);
        Logger.getLogger("").addHandler(handler);
        final File logFileInfo = new File(Environment.getLogFolder(),
                logger.getName() + "-" + suffix + ".info.log");
        final StreamHandler handlerInfo = new StreamHandler(
                new FileOutputStream(logFileInfo, true), new SimpleFormatter());
        handlerInfo.setLevel(Level.INFO);
        Logger.getLogger("").addHandler(handlerInfo);
        setLogLevel();
        logger.setLevel(Level.ALL);
    }
    private static final List<Logger> LOGGER_LINK = new ArrayList<Logger>();

    private static void setLogLevel() {
        Logger.getLogger("").setLevel(Level.FINEST);
        LOGGER_LINK.add(Logger.getLogger("org.apache.commons"));
        Logger.getLogger("org.apache").setLevel(Level.WARNING);
        LOGGER_LINK.add(Logger.getLogger("com.sun"));
        Logger.getLogger("com.sun").setLevel(Level.WARNING);
        LOGGER_LINK.add(Logger.getLogger("javax.xml"));
        Logger.getLogger("javax.xml").setLevel(Level.WARNING);
        LOGGER_LINK.add(Logger.getLogger("org.jcoderz.mb.MbClient"));
        Logger.getLogger("org.jcoderz.mb.MbClient").setLevel(Level.INFO);
        LOGGER_LINK.add(Logger.getLogger("org.jaudiotagger"));
        Logger.getLogger("org.jaudiotagger").setLevel(Level.WARNING);
    }
}
