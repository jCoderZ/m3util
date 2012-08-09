package org.jcoderz.mp3.intern.db;

import java.io.File;
import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jcoderz.commons.util.DirTreeListener;
import org.jcoderz.commons.util.DirTreeWalker;
import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.mp3.intern.MusicBrainzMetadata;
import org.jcoderz.mp3.intern.TagQuality;
import org.jcoderz.mp3.intern.db.types.MediaFile;

public class DatabaseUpdater
    implements DirTreeListener
{
    private static final String CLASSNAME = DatabaseUpdater.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASSNAME);

    final File mRepositoryBase;
    final Date mUpdateRun = new Date();

    public static void main (String[] args)
    {
        DatabaseUpdater du = new DatabaseUpdater(new File(args[0]));
        du.refresh(TagQuality.GOLD);
    }
    
    public DatabaseUpdater(File base)
    {
        RepositoryDb.startup(base);
        mRepositoryBase = base;
    }
    
    public void refresh(TagQuality qual)
    {
        final DirTreeWalker walker 
            = new DirTreeWalker(
                new File(mRepositoryBase, qual.getSubdir() + "/"), this); // FIXME DIRECTORY
        try
        {
            walker.start();
        }
        finally
        {
            RepositoryDb.close();
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
            EntityManager em = RepositoryDb.getEntityManager();
            
            
            
            try
            {
                final MusicBrainzMetadata mbm = new MusicBrainzMetadata(file);
                EntityTransaction tx = em.getTransaction();
                tx.begin();
               
                MediaFile mf;
                if (!StringUtil.isEmptyOrNull(mbm.getUuid()))
                {
                     mf = em.find(MediaFile.class, mbm.getUuid());
                }
                else
                {
                    mf = null;
                }
                
                if (mf == null) 
                {
                    // no uuid - need to use path as Id???
                    MediaFile create = MediaFileUtil.create(mbm, mUpdateRun);
                    em.persist(create);
                }
                else if (file.length() != mf.getFileSize()
                    || file.lastModified() != (mf.getLastModified() == null ? 0 : mf.getLastModified().getTime()))
                {
                    MediaFileUtil.update(mf, mbm, mUpdateRun);
                }
                tx.commit();
            }
            catch (Exception ex)
            {
                System.out.println("Could not add " + file + " got " + ex);
                ex.printStackTrace();
            }
            finally
            {
                em.close();
            }
        }
    }

    
}
