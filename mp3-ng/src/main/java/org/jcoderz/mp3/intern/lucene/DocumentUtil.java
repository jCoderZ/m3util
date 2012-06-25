package org.jcoderz.mp3.intern.lucene;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.jcoderz.commons.types.Date;
import org.jcoderz.commons.util.ObjectUtil;
import org.jcoderz.mp3.intern.MusicBrainzMetadata;
import org.jcoderz.mp3.intern.util.Mp3Util;

public class DocumentUtil
{
    /** Lucene field name for the song sha1. */
    public static final String SHA1 = "sha1";
    /** Lucene field name for the song uuid. */
    public static final String UUID = "uuid";
    /** Lucene field name for the song artist. */
    public static final String ARTIST = "artist";
    /** Lucene field name for the song title. */
    public static final String TITLE = "title";
    /** Lucene field name for the song album title. */
    public static final String ALBUM_TITLE = "album-title";
    /** Lucene field name for the song release year. */
    public static final String RELEASE_YEAR = "release-year";
    /** 
     * Lucene field name for the songs path and file name, 
     * relative to the repository base. 
     */
    public static final String PATH = "path";
    /** Lucene field name for the song tag quality. */
    public static final String TAG_QUALITY = "tag-quality";
    /** Lucene field name for the song length in milliseconds. */
    public static final String LENGTH_MILLIS 
        = "length-millis";
    /** Lucene field name for the songs story flag. */
    public static final String STORY = "story";
    /** Lucene field name for the songs cover image. */
    public static final String COVER_IMAGE = "cover-image";
    /** Lucene field name for the songs cover image. */
    public static final String COVER_IMAGE_TYPE = "cover-image-type";
    /** Lucene field name for the songs update time. */
    public static final String LAST_MODIFIED = "last-modified";
    
    
    public static Document create(MusicBrainzMetadata mb) 
    {
        final Document result = new Document();
        addField(result, UUID, mb.getUuid(), Field.Index.NOT_ANALYZED);
        addField(result, ARTIST, mb.getArtist());
        addField(result, TITLE, mb.getTitle());
        addField(result, ALBUM_TITLE, mb.getAlbum());
        // calculate???
        String sha1 = mb.getSha1(); 
        if (sha1 == null)
        {
            sha1 = Mp3Util.calcAudioFramesSha1(mb.getFile());
        }
        addField(result, SHA1, sha1, Field.Index.NOT_ANALYZED);
        final int year = mb.getYear();
        if (1800 < year)
        {
            addField(result, RELEASE_YEAR, Integer.toString(year));
        }
        addField(result, PATH, cutPath(mb.getFile()), Field.Index.NO);
        addField(result, LENGTH_MILLIS, Long.toString(mb.getLengthInMilliSeconds()));
        addField(result, STORY, mb.isStory() ? "1" : "0");
        addField(result, TAG_QUALITY, cutQuality(mb.getFile()));
        if (mb.getCoverImage() != null)
        {
            addField(result, COVER_IMAGE, 
                mb.getCoverImage().getBinaryData());
            addField(result, COVER_IMAGE_TYPE, 
                mb.getCoverImage().getMimeType(), Field.Index.NO);
        }
        addField(result, 
            LAST_MODIFIED, Date.now().toString(), Field.Index.NOT_ANALYZED);
        return result;
    }


    private static void addField (Document doc, String name,
        byte[] pictureData)
    {
        doc.add(new Field(name, pictureData));
    }


    public static String cutPath (File file)
    {
        final String fullName = file.getAbsolutePath().replace('\\', '/');
        Pattern pathCutter = Pattern.compile(".*/((mp3|xxx|upload|gold)/.*\\.mp3)");
        Matcher matcher = pathCutter.matcher(fullName);
        matcher.find();
        return matcher.group(1);
    }
    
    public static String cutQuality (File file)
    {
        final String fullName = file.getAbsolutePath().replace("\\", "/");
        Pattern pathCutter = Pattern.compile(".*/((mp3|xxx|upload|gold)/.*\\.mp3)");
        Matcher matcher = pathCutter.matcher(fullName);
        matcher.find();
        return matcher.group(2);
    }

    private static void addField (
        Document doc, String name, String value)
    {
        addField(doc, name, value, Field.Index.ANALYZED);
    }

    private static void addField (
        Document doc, String name, String value, Index index)
    {
        doc.add(
            new Field(name, ObjectUtil.toStringOrEmpty(value), 
                Field.Store.YES, index));
    }
}
