package org.jcoderz.m3util.intern.db;

import java.io.File;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.m3util.intern.MusicBrainzMetadata;
import org.jcoderz.m3util.intern.db.types.MediaFile;
import org.jcoderz.m3util.intern.util.Mp3Util;

public final class MediaFileUtil {

    public static MediaFile create(MusicBrainzMetadata mbm, Date updateRun) {
        final MediaFile result = new MediaFile();
        update(result, mbm, updateRun);
        return result;
    }

    private static String cutPath(File file) {
        final String fullName = file.getAbsolutePath().replace('\\', '/');
        // TODO: Use TagQuality class to get the names
        Pattern pathCutter = Pattern.compile(".*/((01-gold|02-silver|03-bronze)/.*\\.mp3)");
        Matcher matcher = pathCutter.matcher(fullName);
        matcher.find();
        return matcher.group(1);
    }

    public static void update(MediaFile mf, MusicBrainzMetadata mbm,
            Date updateRun) {
        if (StringUtil.isEmptyOrNull(mbm.getUuid())) {
            mf.setUuId(UUID.randomUUID().toString());
        } else {
            mf.setUuId(mbm.getUuid());
        }
        mf.setLastModified(new Date(mbm.getFile().lastModified()));
        mf.setLocation(cutPath(mbm.getFile())); // cut this
        mf.setMbId(mbm.getFileId());
        mf.setFileSize((int) mbm.getFile().length());
        mf.setUpdateRun(updateRun);
        String sha1 = mbm.getSha1();
        if (StringUtil.isEmptyOrNull(sha1)) {
            sha1 = Mp3Util.calcAudioFramesSha1(mbm.getFile());
        }
        mf.setSha1(sha1);
    }
}
