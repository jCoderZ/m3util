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

    @Test
    public void testGet ()
    {
        final MusicBrainzMetadata mb 
        = new MusicBrainzMetadata(
            new File(TestUtil.getMp3BasePath(), FS + "unsorted" + FS 
            + "PeterLicht" + FS + "Lieder vom Ende des Kapitalismus" + FS + "foo.mp3"));

        assertEquals("Album differs", mb.getAlbum(), mb.get("Album"));
        assertEquals("Artist differs", mb.getArtist(), mb.get("Artist"));
        assertEquals("Comment differs", mb.getComment(), mb.get("Comment"));
        assertEquals("Genre differs", mb.getGenre(), mb.get("Genre"));
        assertEquals("Title differs", mb.getTitle(), mb.get("Title"));
        assertEquals("TotalTracks differs", mb.getTotalTracks(), mb.get("TotalTracks"));
        assertEquals("TrackNumber differs", mb.getTrackNumber(), mb.get("TrackNumber"));
        assertEquals("Year differs", mb.getYear(), mb.get("Year"));
        assertEquals("AlbumArtistId differs", mb.getAlbumArtistId(), mb.get("AlbumArtistId"));
        assertEquals("Single differs", mb.getSingle(), mb.get("Single"));
        assertEquals("TagAuthority differs", mb.getTagAuthority(), mb.get("TagAuthority"));
        assertEquals("AlbumId differs", mb.getAlbumId(), mb.get("AlbumId"));
        assertEquals("ArtistId differs", mb.getArtistId(), mb.get("ArtistId"));
        assertEquals("AlbumArtist differs", mb.getAlbumArtist(), mb.get("AlbumArtist"));
        assertEquals("AlbumArtistSortname differs", mb.getAlbumArtistSortname(), mb.get("AlbumArtistSortname"));
        assertEquals("AlbumReleaseCountry differs", mb.getAlbumReleaseCountry(), mb.get("AlbumReleaseCountry"));
        assertEquals("Puid differs", mb.getPuid(), mb.get("Puid"));
        assertEquals("Asin differs", mb.getAsin(), mb.get("Asin"));
        assertEquals("Uuid differs", mb.getUuid(), mb.get("Uuid"));
        assertEquals("Sha1 differs", mb.getSha1(), mb.get("Sha1"));
        assertEquals("FileId differs", mb.getFileId(), mb.get("FileId"));
        assertEquals("AlbumType differs", mb.getAlbumType(), mb.get("AlbumType"));
        assertEquals("AlbumStatus differs", mb.getAlbumStatus(), mb.get("AlbumStatus"));
        assertEquals("ArtistSortname differs", mb.getArtistSortname(), mb.get("ArtistSortname"));
        assertEquals("EffectiveSortName differs", mb.getEffectiveSortName(), mb.get("EffectiveSortName"));
        assertEquals("LengthInMilliSeconds differs", mb.getLengthInMilliSeconds(), mb.get("LengthInMilliSeconds"));
        assertEquals("BitrateString differs", mb.getBitrateString(), mb.get("BitrateString"));
        assertEquals("LengthString differs", mb.getLengthString(), mb.get("LengthString"));
    }
}
