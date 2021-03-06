package org.jcoderz.m3util.intern.db;

import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jcoderz.commons.util.DirTreeListener;
import org.jcoderz.commons.util.DirTreeWalker;
import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.m3util.intern.MusicBrainzMetadata;
import org.jcoderz.m3util.intern.db.types.MediaFile;
import org.jcoderz.m3util.intern.types.TagQuality;
import org.jcoderz.m3util.intern.util.Environment;

/**
 * The class DatabaseUpdater iterates through all folders of different quality
 * levels and updates or creates the database index.
 *
 * @author amandel
 * @author mrumpf
 */
public class DatabaseUpdater implements DirTreeListener {

    private static final String CLASSNAME = DatabaseUpdater.class.getName();
    private static final Logger logger = Logger.getLogger(CLASSNAME);
    final Date mUpdateRun = new Date();

    /**
     * This command line entry point can be called with either one or more than
     * one parameters. The first parameter must be the root of the media
     * library. The following parameters are TagQuality identifiers which
     * determine the sub-folder to index.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DatabaseUpdater du = new DatabaseUpdater();
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
     * Creates a database updater instance which updates the database or creates
     * a new one.
     *
     * @param base the base directory. The database files are located under
     * $base/tools/var/lib/db.
     */
    public DatabaseUpdater() {
        RepositoryDb.startup(Environment.getDbFolder());
    }

    /**
     * Close the EntityManagerFactory.
     */
    public void close() {
        RepositoryDb.close();
    }

    /**
     * Refreshes the database.
     *
     * @param quality the quality tag which determines the sub-folder
     */
    public void refresh(TagQuality quality) {
        final File root = new File(Environment.getAudioFolder(), quality.getSubdir());
        final DirTreeWalker walker = new DirTreeWalker(root, this);
        logger.info("Scanning tree under " + root);
        walker.start();
    }

    /**
     * Refreshes the database.
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
                logger.info("Adding file " + mb + "to the database index");
                update(file, mb);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Failure adding file " + file
                        + " to the database index", ex);
            }
        }
    }

    private void update(File file, final MusicBrainzMetadata mbm) {
        EntityManager em = RepositoryDb.getEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            MediaFile mf;
            if (!StringUtil.isEmptyOrNull(mbm.getUuid())) {
                mf = em.find(MediaFile.class, mbm.getUuid());
            } else {
                mf = null;
            }

            if (mf == null) {
                // no uuid - need to use path as Id???
                MediaFile create = MediaFileUtil.create(mbm, mUpdateRun);
                em.persist(create);
            } else if (file.length() != mf.getFileSize()
                    || file.lastModified() != (mf.getLastModified() == null ? 0
                    : mf.getLastModified().getTime())) {
                MediaFileUtil.update(mf, mbm, mUpdateRun);
            }
            tx.commit();
        } finally {
            em.close();
        }
    }
}
