package org.jcoderz.m3util.intern.util;

import org.jcoderz.m3util.intern.util.MbUtil;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.jcoderz.mb.MbClient;
import org.jcoderz.mb.type.Release;
import org.jcoderz.mb.type.TrackData;
import org.jcoderz.m3util.intern.MusicBrainzMetadata;
import org.jcoderz.m3util.intern.TestUtil;
import org.junit.Test;

public class MbUtilTest {

    @Test
    public void test() {
        final MbClient client = TestUtil.getMbClient();
        final MusicBrainzMetadata mb = new MusicBrainzMetadata(
                new File(TestUtil.getMp3BasePath(),
                "unsorted/17 - Ville Valo & Natalia Avelon - Summer Wine (film version).mp3"));
        final Release rel = client.getRelease(mb.getAlbumId()); // "2269a07a-5896-4dc0-a622-98a8e143a6cc");
        assertEquals("Starting file Id unexpected.", "fe42b741-f994-493e-ae36-fcedc42f1fc7", mb.getFileId());
        final TrackData td = MbUtil.getTrackDataWithIdUpdate(client, mb, rel);
        assertEquals("New file Id unexpected.",
                "b4a0c0f1-f89d-4ea0-8a7e-0934917c6eea", td.getTrack().getRecording().getId());
    }
}
