package org.jcoderz.mp3.intern.lucene;

import java.io.File;
import java.util.logging.Logger;

import org.jcoderz.commons.util.DirTreeListener;
import org.jcoderz.commons.util.DirTreeWalker;
import org.jcoderz.mp3.intern.MusicBrainzMetadata;
import org.jcoderz.mp3.intern.TagQuality;

public class DatabaseUpdater
    implements DirTreeListener
{
    private static final String CLASSNAME = DatabaseUpdater.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASSNAME);

    final File mRepositoryBase;
    final File mLuceneBase;
    final LuceneIndex mLucene;

    public static void main (String[] args)
    {
        DatabaseUpdater du = new DatabaseUpdater(new File(args[0]));
        du.refresh(TagQuality.GOLD);
    }
    
    public DatabaseUpdater(File base)
    {
        mRepositoryBase = base;
        mLuceneBase = new File(base, "tools/var/db/lucene"); // FIXME DIRECTORY
        mLuceneBase.mkdirs();
        mLucene = new LuceneIndex();
    }
    
    public void refresh(TagQuality qual)
    {
        final DirTreeWalker walker 
            = new DirTreeWalker(
                new File(mRepositoryBase, qual.getSubdir() + "/"), this); // FIXME DIRECTORY
        mLucene.open(mLuceneBase);
        try
        {
            walker.start();
        }
        finally
        {
            mLucene.close();
        }
    }
    
    public void enteringDir (File dir)
    {
        LOGGER.info("ENTERING: " + dir);
        
    }

    public void exitingDir (File dir)
    {
        // TODO Auto-generated method stub
        
    }

    public void file (File file)
    {
        if (file.getName().endsWith(".mp3"))
        {
            final MusicBrainzMetadata mb 
                = new MusicBrainzMetadata(file);
            if (mb.getUuid() != null)
            {
                mLucene.updateDocument(DocumentUtil.create(mb));
            }
        }
    }

    
}
