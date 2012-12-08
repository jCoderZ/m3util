package org.jcoderz.mb;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jcoderz.mb.type.Includes;
import org.jcoderz.mb.type.Recording;
import org.jcoderz.mb.type.RecordingList;
import org.jcoderz.mb.type.Release;
import org.jcoderz.mb.type.TrackData;
import org.junit.Before;
import org.junit.Test;

public class MbClientTest
{

    MbClient mClient;

    @Before
    public void setUp()
    {
        // mClient = new MbClient();
        // mClient.setRecordDir(getBasePath());
        mClient =
                new MbClient("file:///" + getBasePath().getAbsolutePath() + "/");
    }

    @Test
    public void testGetRelease() throws InterruptedException
    {
        Release releaseFull =
                mClient.getReleaseFull("37b39f1f-b281-4e25-bb2b-1ee4e9d3cd4b");
        assertEquals("Unexpected title.", "Vierzehn Lieder", releaseFull.getTitle());
    }

    @Test
    public void testGetReleaseSmall() throws InterruptedException
    {
        final Set<Includes> includes = new HashSet<>();
        includes.add(Includes.RECORDINGS);
        includes.add(Includes.ARTISTS);
        final Release release =
                mClient.getRelease("37b39f1f-b281-4e25-bb2b-1ee4e9d3cd4b", includes);
        assertEquals("Unexpected title.", "Vierzehn Lieder", release.getTitle());
    }

    @Test
    public void testGetRecordingsByPuid() throws InterruptedException
    {
        final Set<Includes> includes = new HashSet<>();
        includes.add(Includes.RELEASES);
        includes.add(Includes.ARTISTS);
        final RecordingList recordingsByPuid =
                mClient.getRecordingsByPuid("b949df48-9240-c357-0cf8-941e10c7a3b6", includes);
        final List<Recording> recordings = recordingsByPuid.getRecording();
        for (Recording recording : recordings)
        {
            System.out.println(recording.getTitle());
        }

    }

    @Test
    public void testGetRecordingsByPuidDefault() throws InterruptedException
    {
        final RecordingList recordingsByPuid =
                mClient.getRecordingsByPuid("b949df48-9240-c357-0cf8-941e10c7a3b6");
        final List<Recording> recordings = recordingsByPuid.getRecording();
        for (Recording recording : recordings)
        {
            System.out.println(recording.getTitle());
        }

    }

    @Test
    public void testGetTitle()
    {
        TrackData trackData =
                mClient.getTrackData(
                "58da2396-81a6-4e85-bab9-e623d42840bd", "0ca12a54-b5b1-4029-b4eb-4824eb210845");
        assertEquals("Will You Still Love Me Tomorrow", TrackHelper.getTitle(trackData.getTrack()));
        assertEquals("Will You Still Love Me Tomorrow", trackData.getTrack().getTitle());
        assertEquals("Will You Love Me Tomorrow", trackData.getTrack().getRecording().getTitle());
    }

    public static File getBasePath()
    {
        String root = System.getProperty("basedir"); // maven 2 property
        if (root == null)
        {
            root = new File(".").getAbsolutePath(); // current working dir, valid in eclipse
        }

        return new File(
                root + File.separatorChar + "src"
                + File.separatorChar + "test" + File.separatorChar + "resources"
                + File.separatorChar + "mbServer" + File.separatorChar);
    }
}
