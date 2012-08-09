package org.jcoderz.mp3.intern.util;

import java.io.File;

public class Environment {

	public static final File M3_LIBRARY_HOME = new File(System.getProperty("M3_LIBRARY_HOME"));

	public static File getLibraryHome() {
		return M3_LIBRARY_HOME;
	}

	public static File getLogFolder() {
		return new File(getLibraryHome(), "tools/var/log");
	}

	public static File getLuceneFolder() {
		return new File(getLibraryHome(), "tools/var/lib/lucene");
	}

	public static File getDbFolder() {
		return new File(getLibraryHome(), "tools/var/lib/db");
	}
}
