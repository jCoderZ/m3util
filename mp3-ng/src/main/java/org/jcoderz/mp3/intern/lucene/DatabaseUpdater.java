package org.jcoderz.mp3.intern.lucene;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.jcoderz.commons.util.DirTreeListener;
import org.jcoderz.commons.util.DirTreeWalker;
import org.jcoderz.mp3.intern.MusicBrainzMetadata;
import org.jcoderz.mp3.intern.TagQuality;
import org.jcoderz.mp3.intern.util.Environment;
import org.jcoderz.mp3.intern.util.LoggingUtil;

/**
 * The class DatabaseUpdater iterates through all folders of different quality
 * levels and updates or creates the Lucene index.
 * 
 * @author amandel
 * @author mrumpf
 */
public class DatabaseUpdater implements DirTreeListener {
	private static final String CLASSNAME = DatabaseUpdater.class.getName();
	private static final Logger logger = Logger.getLogger(CLASSNAME);

	final File mRepositoryBase;
	final File mLuceneBase;
	final LuceneIndex mLucene;

	/**
	 * This command line entry point can be called with either one or more than
	 * one parameters. The first parameter must be the root of the media
	 * library. The following parameters are TagQuality identifiers which
	 * determine the sub-folder to index.
	 * 
	 * <p>
	 * The following command will update all audio folders: DatabaseUpdater
	 * /media/usb
	 * </p>
	 * 
	 * <p>
	 * Indexing only the gold folder can be done like this: DatabaseUpdater
	 * /media/usb GOLDhandlerInfo
	 * </p>
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws IOException {
		LoggingUtil.initLogging(logger);

		DatabaseUpdater du = new DatabaseUpdater();
		if (args.length > 1) {
			for (int i = 1; i < args.length; i++) {
				du.refresh(TagQuality.valueOf(args[1]));
			}
		} else {
			du.refresh(TagQuality.GOLD);
			du.refresh(TagQuality.SILVER);
			du.refresh(TagQuality.BRONZE);
		}
	}

	/**
	 * Creates a database updater instance which updates the lucene index or
	 * creates a new one.
	 * 
	 * @param base
	 *            the base directory. The lucene index is located under
	 *            $base/tools/var/db/licene.
	 */
	public DatabaseUpdater() {
		mRepositoryBase = Environment.getLibraryHome();
		mLuceneBase = Environment.getLuceneFolder();
		mLuceneBase.mkdirs();
		mLucene = new LuceneIndex();
	}

	/**
	 * Refreshes the Lucene index.
	 * 
	 * @param quality
	 *            the quality tag which determines the sub-folder
	 */
	public void refresh(TagQuality quality) {
		final File root = new File(mRepositoryBase, "audio/"
				+ quality.getSubdir() + "/");
		final DirTreeWalker walker = new DirTreeWalker(root, this);
		mLucene.open(mLuceneBase);
		try {
			logger.info("Scanning tree under " + root);
			walker.start();
		} finally {
			mLucene.close();
		}
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
			final MusicBrainzMetadata mb = new MusicBrainzMetadata(file);
			if (mb.getUuid() != null) {
				try {
					logger.info("Adding file " + mb + "to the Lucene index");
					mLucene.updateDocument(DocumentUtil.create(mb));
				} catch (Exception ex) {
					logger.warning("Failure creating Lucene document for file "
							+ mb);
				}
			} else {
				logger.warning("File has no uuid and thus will not be added to the index: "
						+ mb);
			}
		}
	}
}
