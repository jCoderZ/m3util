package org.jcoderz.m3util.intern;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.tag.TagException;
import org.jcoderz.commons.util.DirTreeListener;
import org.jcoderz.commons.util.DirTreeWalker;
import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.m3util.intern.util.LoggingUtil;

/**
 * Adds uuid and sha1 as IDv2 Tag to the mp3 files found in the given
 * sub-directory. Please note that the existing ID3 Tags might get converted to
 * ID3v2.3 Unicode tags. This conversion takes place when ids are added by this
 * class.
 *
 * @author amandel
 */
public class IdAdder {

    public static final String CLASSNAME = IdAdder.class.getName();
    public static final Logger logger = Logger.getLogger(CLASSNAME);

    public static void main(String[] args) throws FileNotFoundException {
        LoggingUtil.initLogging(logger);
        IdAdder ia = new IdAdder();
        ia.fillRefData(new File(args[0]));
    }

    public void fillRefData(File dir) {
        final DirTreeWalker refTreeWalker = new DirTreeWalker(dir,
                new DirTreeListener() {
                    public void file(File file) {
                        if (file.getName().toLowerCase().endsWith(".mp3")
                                && file.length() > 1000) {
                            try {
                                boolean changed = false;
                                final MusicBrainzMetadata mb = new MusicBrainzMetadata(
                                        file);
                                final String oldData = mb.toString();
                                if (StringUtil.isBlankOrNull(mb.getSha1())) {
                                    mb.addSha1();
                                    changed = true;
                                }
                                if (StringUtil.isBlankOrNull(mb.getUuid())) {
                                    mb.addUuid();
                                    changed = true;
                                }
                                if (changed) {
                                    mb.sync(); // write changes to disk!
                                    logger.info("Updated "
                                            + file.getAbsolutePath());
                                    if (oldData.equals(mb.toString())) {
                                        logger.info("!" + oldData);
                                    } else {
                                        logger.info("<" + oldData);
                                        logger.info(">" + mb.toString());
                                    }
                                }
                            } catch (RuntimeException | IOException | TagException ex) {
                                logger.log(Level.WARNING, "Failed with '"
                                        + file + "' got '" + ex
                                        + "' will be ignored.", ex);
                            }
                        }
                    }

                    public void exitingDir(File dir) {
                        logger.fine("Entering: " + dir);
                    }

                    public void enteringDir(File dir) {
                        // NOOP
                    }
                });
        refTreeWalker.start();
    }
}
