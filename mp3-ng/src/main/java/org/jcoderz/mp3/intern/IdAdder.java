package org.jcoderz.mp3.intern;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jcoderz.commons.util.DirTreeListener;
import org.jcoderz.commons.util.DirTreeWalker;
import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.mp3.intern.util.LoggingUtil;
import org.jcoderz.mp3.intern.util.Mp3Util;


/**
 * Adds uuid and sha1 as IDv2 Tag to the mp3 files found in the given
 * sirectory. Please note that the existing ID3 Tags might get converted
 * to ID3v2.3 Unicode tags is required.
 * 
 * @author Andreas Mandel
 */
public class IdAdder
{
    public static final String CLASSNAME = IdAdder.class.getName();

    public static final Logger LOGGER = Logger.getLogger(CLASSNAME);

    public final File mDir; 
    
    public static void main(String[] args) throws FileNotFoundException 
    {
    	LoggingUtil.initLogging(LOGGER, "ID-ADDER");
		new IdAdder(new File(args[0])).fillRefData();
	}
    
    public IdAdder (File dir)
    {
        mDir = dir;
    }
    
    public void fillRefData ()
    {
        final DirTreeWalker refTreeWalker 
        	= new DirTreeWalker(mDir, new DirTreeListener()
            {
                public void file (File file)
                {
                    if (file.getName().toLowerCase().endsWith(".mp3") && file.length() > 1000)
                    {
                        try
                        {
                            boolean changed = false;
                            final MusicBrainzMetadata mb 
                            	= new MusicBrainzMetadata(file);
                            final String oldData = mb.toString();
                            if (StringUtil.isBlankOrNull(mb.getSha1()))
                            {
                                mb.addSha1();
                                changed = true;
                            }
                            if (StringUtil.isBlankOrNull(mb.getUuid()))
                            {
                                mb.addUuid();
                                changed = true;
                            }
                            if (changed)
                            {
                                mb.sync(); // write changes to disk!
                                LOGGER.info("Updated " + file.getAbsolutePath());
                                if (oldData.equals(mb.toString()))
                                {
                                	LOGGER.info("!" + oldData);
                                }
                                else
                                {
                                	LOGGER.info("<" + oldData);
                                	LOGGER.info(">" + mb.toString());
                                }
                            }
                        }
                        catch (RuntimeException | IOException | TagException ex)
                        {
                            LOGGER.log(Level.WARNING, 
                            		"Failed with '" + file + "' got '" + ex + "' will be ignored.", ex);
                        }
                    }
                }

                public void exitingDir (File dir)
                {
                    LOGGER.fine("Entering: " + dir);
                }

                public void enteringDir (File dir)
                {
                    // NOOP
                }
            });
        refTreeWalker.start();
    }
}
