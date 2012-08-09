/**
 * 
 */
package org.jcoderz.mp3.intern.util;

import java.text.Collator;
import java.util.Collections;
import java.util.Locale;

import org.jcoderz.commons.util.Assert;
import org.jcoderz.mb.MbClient;
import org.jcoderz.mb.type.Includes;
import org.jcoderz.mb.type.Recording;
import org.jcoderz.mb.type.Release;
import org.jcoderz.mb.type.TrackData;
import org.jcoderz.mp3.intern.MusicBrainzMetadata;

/**
 * Utility class for new musicbrainz interface.
 * 
 * @author Andreas Mandel
 *
 */
public final class MbUtil
{
    /** No instances for utility class. */
    private MbUtil()
    {
        // No instances.
    }
    
    /**
     * This method tries to handle the situation where the track id changed within 
     * musicbrainz database. This can happen if releases are merged and happens quite
     * frequently in the ngs schema of musicbrainz, where the same song in different
     * albums appears with the same track id.
     *  
     * @param mbClient the connection to the musicbrainz server used if lookups are needed.
     * @param mbData the metadata of the song we take care for 
     * @param album the album in which the song (mbData) is expected to be 
     * @return the TrackData object containing Release, Medium and Track information uniquely 
     *  identifying the song. 
     */
    public static TrackData getTrackDataWithIdUpdate (MbClient mbClient,
        MusicBrainzMetadata mbData, Release album)
    {
        final String trackId = mbData.getFileId();
        Assert.notNull(trackId, "mbData.getFileId()");
        TrackData track = mbClient.getTrackData(album, trackId);
        if (track.getMedium() == null)
        {
            // Try update ....
            final Recording recording 
                = mbClient.getRecording(trackId, Collections.<Includes> emptySet());
            
            if (!trackId.equals(recording.getId())
                && ((recording.getLength() == null || 
                        (Math.abs(mbData.getLengthInMilliSeconds() - recording.getLength().longValue()) > 5000))
                && ((recording.getTitle() == null || 
                        !compare(mbData.getTitle(), recording.getTitle())))))
            {
                Assert.fail(
                    "Length diff to high! Will not use new id (" + trackId
                        + "->" + recording.getId() + " ---- " + mbData + " ->"
                        + recording.getTitle() + " len:"
                        + mbData.getLengthInMilliSeconds() + " -> "
                        + (recording.getLength() != null ? recording.getLength().longValue() : ""));
            }
            
            
            track = mbClient.getTrackData(album, recording.getId());
        }
        if (track.getMedium() == null && album != null)
        {
            Assert.fail("Cold not find track " + trackId + " in Release " + album.getId());
        }
        return track;
    }

    private static boolean compare (String a, String b)
    {
       if (a == null)
       {
          a =  "";
       }
       if (b == null)
       {
          b = "";
       }
       a = a.replaceAll("\\(.*\\)", "");
       b = b.replaceAll("\\(.*\\)", "");
       a = a.replaceAll("[^A-Za-z0-9]", "");
       b = b.replaceAll("[^A-Za-z0-9]", "");
       final int len = Math.min(a.length(), b.length()); 
       if (a.length() > 29)
       {
          a = a.substring(0, Math.max(29, len));
       }
       if (b.length() > 29)
       {
          b = b.substring(0, Math.max(29, len));
       }
       final Collator collator = Collator.getInstance(new Locale("en", "US"));
       collator.setStrength(Collator.PRIMARY);
       return collator.compare(a, b) == 0;
    }

}
