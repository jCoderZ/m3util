package org.jcoderz.m3util.intern;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.jcoderz.mb.MbClient;
import org.junit.Test;

public class TestUtil {

    /**
     * Used to switch between stored test data and live mb system.
     *
     * @return the {@link MbClient} to be used in testcases.
     */
    public static MbClient getMbClient() {
        //final MbClient client = new MbClient();
        //client.setRecordDir(TestUtil.getMbServerBasePath());
        final MbClient client = new MbClient("file:///" + TestUtil.getMbServerBasePath().getAbsolutePath());
        return client;
    }

    public static File getMp3BasePath() {
        return new File(getBasePath(), "mp3metadata");
    }

    public static File getMbServerBasePath() {
        return new File(getBasePath(), "mbServer");
    }

    public static File getTargetBasePath() {
        return new File(getPrjRoot(), "target");
    }

    public static File getBasePath() {
        return new File(
                getPrjRoot() + File.separatorChar + "src"
                + File.separatorChar + "test" + File.separatorChar
                + "resources" + File.separatorChar);
    }

    private static String getPrjRoot() {
        String root = System.getProperty("basedir"); // maven 2 property
        if (root == null) {
            root = new File(".").getAbsolutePath(); // current working dir, valid in eclipse
        }
        return root;
    }

    @Test
    public void testAvailibility() {
        assertTrue("Base Path does not exist " + getBasePath(), getBasePath().exists());
        assertTrue("Base Path is not a directory" + getBasePath(), getBasePath().isDirectory());
        assertTrue("Mp3 base Path does not exist " + getMp3BasePath(), getBasePath().exists());
        assertTrue("Mp3 base Path is not a directory" + getMp3BasePath(), getBasePath().isDirectory());
    }
}
