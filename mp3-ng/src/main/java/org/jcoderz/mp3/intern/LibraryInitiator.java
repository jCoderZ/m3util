package org.jcoderz.mp3.intern;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.mp3.intern.types.TagQuality;
import org.jcoderz.mp3.intern.util.Environment;

/**
 * 
 * @author mrumpf
 * 
 */
public class LibraryInitiator {
	private static final String CLASSNAME = LibraryInitiator.class.getName();
	private static final Logger logger = Logger.getLogger(CLASSNAME);

	public LibraryInitiator() {
		// ...
	}

	public void create() {
		File libHome = Environment.getLibraryHome();
		File audioFolder = createFolder(libHome, "audio");
		for (TagQuality tq : TagQuality.values()) {
			createFolder(audioFolder, tq.getSubdir());
		}
		createFolder(audioFolder, "playlist");
		File toolsFolder = createFolder(libHome, "tools");
		createFolder(toolsFolder, "bin");
		createFolder(toolsFolder, "etc");
		File libFolder = createFolder(toolsFolder, "lib");
		createFolder(libFolder, "jdk");
		createFolder(libFolder, "jboss-as");
		File varFolder = createFolder(toolsFolder, "var");
		File varLibFolder = createFolder(varFolder, "lib");
		createFolder(varLibFolder, "db");
		createFolder(varLibFolder, "lucene");
		createFolder(varFolder, "log");
		createFolder(libHome, "incoming");
	}

	private File createFolder(File base, String name) {
		File folder = new File(base, name);
		if (folder.exists()) {
			if (folder.isDirectory()) {

			} else {
				logger.log(Level.SEVERE, "Found file " + folder
						+ " but must be a folder!");
			}
		} else {
			folder.mkdir();
		}
		return folder;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
