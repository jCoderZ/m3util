package org.jcoderz.m3util.intern.lucene;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.commons.util.DirTreeListener;
import org.jcoderz.commons.util.DirTreeWalker;
import org.jcoderz.m3util.intern.MusicBrainzMetadata;
import org.jcoderz.m3util.intern.types.TagQuality;
import org.jcoderz.m3util.intern.util.Environment;
import org.jcoderz.m3util.intern.util.LoggingUtil;

/**
 * The class DatabaseUpdater iterates through all folders of different quality
 * levels and updates or creates the Lucene index.
 *
 * @author amandel
 * @author mrumpf
 */
public class LuceneUpdater implements DirTreeListener {

    private static final String CLASSNAME = LuceneUpdater.class.getName();
    private static final Logger logger = Logger.getLogger(CLASSNAME);
    private LuceneIndex mLucene;

    /**
     * This command line entry point can be called with either one or more than
     * one parameters. The first parameter must be the root of the media
     * library. The following parameters are TagQuality identifiers which
     * determine the sub-folder to index.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        LoggingUtil.initLogging(logger);

        LuceneUpdater du = new LuceneUpdater();
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                du.refresh(TagQuality.valueOf(args[1]));
            }
        } else {
            du.refresh();
        }
        du.close();
    }

    /**
     * Creates a database updater instance which updates the lucene index or
     * creates a new one.
     *
     * @param base the base directory. The lucene index is located under
     * $base/tools/var/db/licene.
     */
    public LuceneUpdater() {
        mLucene = new LuceneIndex();
        final File luceneFolder = Environment.getLuceneFolder();
        if (!luceneFolder.exists()) {
            luceneFolder.mkdirs();
        }
        mLucene.open(luceneFolder);
    }

    /**
     * Refreshes the Lucene index.
     *
     * @param quality the quality tag which determines the sub-folder
     */
    public void refresh(TagQuality quality) {
        final File root = new File(Environment.getLibraryHome(), "audio" + File.separatorChar
                + quality.getSubdir() + File.separatorChar);
        final DirTreeWalker walker = new DirTreeWalker(root, this);
        logger.info("Scanning tree under " + root);
        walker.start();
    }

    /**
     * Close the Lucene index.
     */
    public void close() {
        mLucene.close();
    }

    /**
     * Refreshes the Lucene index.
     */
    public void refresh() {
        refresh(TagQuality.GOLD);
        refresh(TagQuality.SILVER);
        refresh(TagQuality.BRONZE);
    }

    @Override
    public void enteringDir(File dir) {
        logger.finest("ENTERING: " + dir);
    }

    @Override
    public void exitingDir(File dir) {
        logger.finest("EXITING: " + dir);
    }

    @Override
    public void file(File file) {
        if (file.getName().endsWith(".mp3")) {
            try {
                final MusicBrainzMetadata mb = new MusicBrainzMetadata(file);
                // TODO: Use path as index!!!
                if (mb.getUuid() != null) {
                    logger.info("Adding file " + mb + "to the Lucene index");
                    mLucene.updateDocument(DocumentUtil.create(mb));
                } else {
                    logger.warning("File has no uuid and thus will not be added to the index: "
                            + mb);
                }
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Failure adding file " + file
                        + " to the Lucene index", ex);
            }
        }
    }
}
