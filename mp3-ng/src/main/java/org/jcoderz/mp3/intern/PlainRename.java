package org.jcoderz.mp3.intern;


import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.commons.util.DirTreeListener;
import org.jcoderz.commons.util.DirTreeWalker;
import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.mp3.intern.util.FileUtil;


/**
 * Plain stupid xxx renamer. Will move files to the repository directory
 * structure by trusting all available tag information. If a MB release
 * id is set this one will also be added to the output directory name.
 * 
 * TODO: Check IO, use logger.
 * TODO: Bind to main interface.
 * 
 * @author Andreas Mandel
 */
public class PlainRename
{
    private static final String CLASSNAME 
        = PlainRename.class.getName();
    private static final Logger logger 
        = Logger.getLogger(CLASSNAME);

    private final File mBasePath;

    private final File mTargetDir;

    private final boolean mDryRun;

    private final DirTreeWalker mDirTreeWalker;

    /**
     * @param args
     */
    public static void main (String[] args)
    {
        PlainRename checker = new PlainRename(new File(args[0]), false);
        checker.start();
    }

    public void start ()
    {
        mDirTreeWalker.start();
    }

    public PlainRename (File dir, boolean dryRun)
    {
        mDryRun = dryRun;
        mBasePath = dir;
        mTargetDir = new File(mBasePath.getParent(), "xxx/");
        mDirTreeWalker = new DirTreeWalker(dir, new DirTreeListener()
        {
            public void file (File file)
            {
                checkSingle(file);
            }

            public void exitingDir (File dir)
            {
                // System.out.println("-" + dir);
            }

            public void enteringDir (File dir)
            {
                // System.out.println("+" + dir);
            }
        });
    }

    private void checkSingle (File file)
    {
        if (file.getName().toLowerCase().endsWith(".mp3"))
        {
            try
            {
                final MusicBrainzMetadata mb = new MusicBrainzMetadata(file);
                mb.getBitrateString();
                if (!StringUtil.isBlankOrNull(mb.getArtist())
                    || !StringUtil.isBlankOrNull(mb.getTitle()))
                {
                    final FileLocation fl = new FileLocation(mb);
                    fl.setComplete(true);
                    fl.setSingle(false);
                    fl.setIsAlbumCollision(true);
                    fl.setIsFileCollision(true);
                    if (!mDryRun
                        && !FileUtil.moveFile(mb.getFile(),
                            fl.getPath(mTargetDir), fl.getFilename()))
                    {
                        System.out.println("**ERROR** '" + mb.getFile()
                            + "' -> '" + fl.getFile(mTargetDir) + "'");
                    }
                    else
                    {
                        System.out.println("  MOVE   '" + mb.getFile() + "' -> '"
                            + fl.getFile(mTargetDir) + "'");
                    }
                    FileUtil.deleteDirIfEmpty(mb.getFile());
                }
                else
                {
                    System.out.println(" NO DATA '" + mb.getFile());
                }
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Problems when checking '" + file
                    + "' got an Exception.", ex);
            }
        }
        else
        {
            System.out.println("Skip " + file);
        }
    }
}
