package org.jcoderz.mb;

import java.math.BigInteger;
import org.jcoderz.mb.type.ArtistCredit;
import org.jcoderz.mb.type.Medium.TrackList.Track;
import org.jcoderz.mb.type.Recording;
import static org.junit.Assert.*;
import org.junit.Test;

public class TrackHelperTest
{

    @Test
    public void testGetTitle()
    {
        final Track testTrack = new Track();
        testTrack.setTitle("Track Title");
        final Recording rec = new Recording();
        testTrack.setRecording(rec);
        rec.setTitle("Rec Title");
        assertEquals("Track Title", TrackHelper.getTitle(testTrack));
        testTrack.setTitle(null);
        assertEquals("Rec Title", TrackHelper.getTitle(testTrack));
    }

    @Test
    public void testGetLength()
    {
        final Track testTrack = new Track();
        testTrack.setLength(BigInteger.ONE);
        final Recording rec = new Recording();
        testTrack.setRecording(rec);
        rec.setLength(BigInteger.ZERO);
        assertEquals(Long.valueOf(1), TrackHelper.getLength(testTrack));
        testTrack.setLength(null);
        assertEquals(Long.valueOf(0), TrackHelper.getLength(testTrack));
    }

    @Test
    public void testGetArtistCredit()
    {
        final Track testTrack = new Track();
        final ArtistCredit trackAc = new ArtistCredit();
        testTrack.setArtistCredit(trackAc);
        final Recording rec = new Recording();
        testTrack.setRecording(rec);
        final ArtistCredit recAc = new ArtistCredit();
        rec.setArtistCredit(recAc);
        assertSame(trackAc, TrackHelper.getArtistCredit(testTrack));
        testTrack.setArtistCredit(null);
        assertEquals(recAc, TrackHelper.getArtistCredit(testTrack));
    }
}
