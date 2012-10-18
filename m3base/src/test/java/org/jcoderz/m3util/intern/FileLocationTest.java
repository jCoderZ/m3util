package org.jcoderz.m3util.intern;

import org.jcoderz.m3util.intern.FileLocation;
import static org.junit.Assert.assertTrue;

import org.jcoderz.mb.MbClient;
import org.jcoderz.mb.type.TrackData;
import org.junit.Test;

/**
 * Test the file location determination.
 */
public class FileLocationTest {

    @Test
    public void test() {
        final MbClient client = TestUtil.getMbClient();
        final TrackData trackData = client.getTrackData("0a57ecb7-ede4-4031-ba59-fa11bc93e9e1", "f2deb319-cce3-4c2f-90eb-47f59192fbd8");
        final FileLocation fl = new FileLocation(trackData.getRelease(), trackData.getTrack().getRecording());
        assertTrue("File loation of audio book must contain 'story' " + fl, fl.toString().contains("/[Story]/"));
    }
}
