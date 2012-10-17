package org.jcoderz.mp3.intern;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.mp3.intern.types.TagQuality;
import org.jcoderz.mp3.intern.util.Environment;

/**
 * This class creates the media library folder structure and populates the
 * folders with the libraries necessary to run all tools which are part of the
 * library.
 *
 * <p>It seems to be possible to download the Oracle JREs directly: <a
 * href="http://ivan-site.com/2012/05/download-oracle-java-jre-jdk-using-a-script/">
 * Download Oracle Java JRE & JDK using a script</a><p>
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

    /**
     * Create a media library folder structure at the M3_LIBRARY_HOME location
     * or at the current working directory.
     */
    public void create() {
        File libHome = Environment.getLibraryHome();
        if (libHome == null) {
            libHome = new File(new File(System.getProperty("user.dir")), "m3");
            logger.warning("Using m3 folder in current working directory as library root");
        }

        if (!libHome.exists()) {
            boolean success = libHome.mkdirs();
            if (!success) {
                throw new RuntimeException("Could not create M3_LIBRARY_HOME="
                        + libHome);
            }
        } else {
            if (libHome.isFile()) {
                // TODO: Use own exceptions
                throw new RuntimeException("M3_LIBRARY_HOME=" + libHome
                        + " is not a directory!");
            }
        }

        create(libHome);
    }

    /**
     * Creates a media library folder structure at the specified location.
     *
     * @param libHome the root folder where to create the media library
     * directory tree
     */
    public void create(File libHome) {
        File audioFolder = createFolder(libHome, "audio");
        for (TagQuality tq : TagQuality.values()) {
            createFolder(audioFolder, tq.getSubdir());
        }
        createFolder(audioFolder, "playlist");
        File toolsFolder = createFolder(libHome, "tools");
        createFolder(toolsFolder, "bin");
        createFolder(toolsFolder, "etc");
        File libFolder = createFolder(toolsFolder, "lib");
        File jdkFolder = createFolder(libFolder, "jre");
        createFolder(jdkFolder, "win");
        createFolder(jdkFolder, "lnx");
        createFolder(jdkFolder, "mac");
        createFolder(libFolder, "m3util");
        createFolder(libFolder, "m3server");
        File varFolder = createFolder(toolsFolder, "var");
        File varLibFolder = createFolder(varFolder, "lib");
        createFolder(varLibFolder, "db");
        createFolder(varLibFolder, "lucene");
        createFolder(varFolder, "log");
        createFolder(varFolder, "tmp");
        File cacheFolder = createFolder(varFolder, "cache");
        createFolder(cacheFolder, "images");
        // TODO: where to put the folder incoming???
        File incFolder = createFolder(varFolder, "incoming");
        createFolder(incFolder, "audio");
    }

    private void downloadJre() {
        // TODO:
        // download to var/tmp
        // extract to tools/lib/jre/...
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
            boolean success = folder.mkdirs();
            if (!success) {
                throw new RuntimeException("Could not create folder " + folder);
            }
        }
        return folder;
    }
}
