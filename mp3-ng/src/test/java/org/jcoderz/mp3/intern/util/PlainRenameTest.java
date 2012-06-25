package org.jcoderz.mp3.intern.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.jcoderz.commons.util.FileUtils;
import org.jcoderz.mp3.intern.PlainRename;
import org.jcoderz.mp3.intern.TestUtil;
import org.junit.Test;

public class PlainRenameTest
{
    @Test
    public void test () throws IOException
    {
        final File targetDir 
            = new File(TestUtil.getTargetBasePath(), "plain-rename-test").getCanonicalFile();
        if (targetDir.exists() && targetDir.isDirectory())
        {
            FileUtils.rmdir(targetDir);
        }
        assertTrue(targetDir.mkdirs());

        File inDir = new File(targetDir, "unsorted");
        assertTrue(inDir.mkdirs());
        
        FileUtils.copy(TestUtil.getMp3BasePath(), inDir);
        
        final PlainRename pr = new PlainRename(inDir, false);
        pr.start();

        assertFile(
            targetDir, 
            "xxx/[Various]/Das wilde Leben [2269a07a-5896-4dc0-a622-98a8e143a6cc]/" 
            + "17 - Ville Valo & Natalia Avelon - Summer Wine (film version) [fe42b741-f994-493e-ae36-fcedc42f1fc7].mp3");
        assertFile(
            targetDir, 
            "xxx/P/PeterLicht/Lieder vom Ende des Kapitalismus/08 - Hallo hallo (Dies ist der Tag).mp3");
        assertFile(
            targetDir, 
            "xxx/P/PeterLicht/Lieder vom Ende des Kapitalismus [e012e8f6-9c55-4783-9ce1-2b42bb8380cd]/" 
            + "09 - Wir werden siegen [3a26348f-c24a-4820-871e-36d864b9d120].mp3");
        // File is a dupe and should remain untouched
        assertFile(
            targetDir, 
            "unsorted/mp3metadata/unsorted/PeterLicht/Lieder vom Ende des Kapitalismus/foo2-dupe.mp3");
    }

    private void assertFile (File targetDir, String fileName)
    {
        final File file = new File(targetDir, fileName.replace('/', File.separatorChar));
        assertTrue("File does not exist " + file, file.exists());
    }
}
