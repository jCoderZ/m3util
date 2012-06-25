/**
 * 
 */
package org.jcoderz.mp3.intern;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andreas Mandel
 *
 */
public class MusicBrainzMetadataTest
{
    private final static char FS = File.separatorChar;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp ()
        throws Exception
    {
    }

    /**
     * Test method for {@link org.jcoderz.mp3.intern.MusicBrainzMetadata#getCoverImageOrNull()}.
     * @throws ID3Exception ID3 reading fails fatal-
     */
    @Test
    public void testPlainFileInfo ()
    {
        final MusicBrainzMetadata mb 
            = new MusicBrainzMetadata(
                new File(TestUtil.getMp3BasePath(), FS + "unsorted" + FS 
                + "PeterLicht" + FS + "Lieder vom Ende des Kapitalismus" + FS + "08 - Hallo hallo (Dies ist der Tag).mp3"));
        
        assertEquals("Artist from path not read.", "PeterLicht", mb.getArtist());
        assertEquals("Title from path not read.", "Hallo hallo (Dies ist der Tag)", mb.getTitle());
        assertEquals("Album from path not read.", "Lieder vom Ende des Kapitalismus", mb.getAlbum());
        assertEquals("Track number nor read.", 8, mb.getTrackNumber());
    }
    
    /**
     * Test method for {@link org.jcoderz.mp3.intern.MusicBrainzMetadata#getCoverImageOrNull()}.
     */
    @Test
    public void testTaggedFileInfo ()
    {
        final MusicBrainzMetadata mb 
            = new MusicBrainzMetadata(
                new File(TestUtil.getMp3BasePath(), FS + "unsorted" + FS 
                + "PeterLicht" + FS + "Lieder vom Ende des Kapitalismus" + FS + "foo.mp3"));
        
        assertEquals("Artist from path not read.", "PeterLicht", mb.getArtist());
        assertEquals("Title from path not read.", "Wir werden siegen", mb.getTitle());
        assertEquals("Album from path not read.", "Lieder vom Ende des Kapitalismus", mb.getAlbum());
        assertEquals("Track number nor read.", 9, mb.getTrackNumber());
    }
}
