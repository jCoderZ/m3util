package org.jcoderz.mp3.intern.util;

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
 * 
 * @author amandel
 *
 */
public class LoggingUtil {

	public static void initLogging(Logger logger) throws FileNotFoundException {
		initLogging(logger, org.jcoderz.commons.types.Date.now().toString().replace(':', '.'));
	}
	public static void initLogging(Logger logger,
			final String timestamp) throws FileNotFoundException {
		final File logFile = new File(Environment.getLogFolder(), "DRY_CLEANUP-"
					+ timestamp + ".log");
		final StreamHandler handler = new StreamHandler(new FileOutputStream(
				logFile, true), new SimpleFormatter());
		handler.setLevel(Level.ALL);
		Logger.getLogger("").addHandler(handler);
		final File 	logFileInfo = new File(Environment.getLogFolder(), "DRY_CLEANUP-INFO-" + timestamp);
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
