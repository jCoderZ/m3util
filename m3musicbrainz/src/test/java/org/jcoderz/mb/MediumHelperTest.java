/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jcoderz.mb;

import org.jcoderz.mb.type.Medium;
import org.jcoderz.mb.type.Release;
import org.jcoderz.mb.type.TrackData;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Andreas Mandel
 */
public class MediumHelperTest
{
    private MbClient mClient;

    public MediumHelperTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
//        mClient = new MbClient("http://mb-box:5000/");
//        mClient.setRecordDir(MbClientTest.getBasePath());
       mClient = new MbClient(
               "file:///" + MbClientTest.getBasePath().getAbsolutePath() + "/");
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of buildAlbumTitle method, of class MediumHelper.
     */
    @Test
    public void testBuildAlbumTitleWithMediumTitle()
    {
        final TrackData trackData =
                mClient.getTrackData(
                "d9eff86a-d4c3-4b1f-bb2a-f2ed473318c6", "8ebb47f5-d7d5-4c72-ba7d-cd09eb10ef94");
        String expResult = "Stadium Arcadium (disc 1: Jupiter)";
        final String result
                = MediumHelper.buildAlbumTitle(trackData.getRelease(), trackData.getMedium());
        assertEquals(expResult, result);
    }

    /**
     * Test of buildAlbumTitle method, of class MediumHelper.
     */
    @Test
    public void testBuildAlbumTitle()
    {
        TrackData trackData =
                mClient.getTrackData(
                "37b39f1f-b281-4e25-bb2b-1ee4e9d3cd4b", "c4dcf2e6-6c61-48a7-95a8-6567c8538beb");
        String expResult = "Vierzehn Lieder";
        final String result
                = MediumHelper.buildAlbumTitle(trackData.getRelease(), trackData.getMedium());
        assertEquals(expResult, result);
    }

    /**
     * Test of buildAlbumTitle method, of class MediumHelper.
     */
    @Test
    public void testBuildAlbumTitleMultibleMedium()
    {
        TrackData trackData =
                mClient.getTrackData(
                "79f8ddff-8578-4b46-b9e8-9be9f90154da", "41bb76a3-0c89-422b-932b-c56a02596435");
        String expResult = "The Hitchhiker's Guide to the Galaxy: The Complete 'Trilogy' of Five Volumes (disc 6)";
        final String result
                = MediumHelper.buildAlbumTitle(trackData.getRelease(), trackData.getMedium());
        assertEquals(expResult, result);
    }
}
