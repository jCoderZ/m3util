package org.jcoderz.mp3.intern.util;

import static org.junit.Assert.assertEquals;

import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.id3.ID3v11Tag;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.junit.Test;

public class Id3UtilTest2
{
    @Test
    public void testFixGenre () throws KeyNotFoundException, FieldDataInvalidException
    {
        final ID3v23Tag tag = new ID3v23Tag();
        
        
        tag.setField(FieldKey.ARTIST, FieldKey.ARTIST.toString());
        tag.setField(FieldKey.ALBUM, FieldKey.ALBUM.toString());
        tag.setField(FieldKey.GENRE, "Rave");
        tag.setField(FieldKey.TITLE, FieldKey.TITLE.toString());
        tag.setField(FieldKey.YEAR, "1234");
        tag.setField(FieldKey.TRACK, "22");
        tag.setField(FieldKey.COMMENT, FieldKey.COMMENT.toString());
        
        ID3v11Tag id3v11 = Id3Util.toId3v11(tag);
        
        assertEquals(
            FieldKey.ARTIST.toString(), id3v11.getFirst(FieldKey.ARTIST));
        assertEquals(
            FieldKey.ALBUM.toString(), id3v11.getFirst(FieldKey.ALBUM));
        assertEquals("Rave", id3v11.getFirst(FieldKey.GENRE));
        assertEquals(
            FieldKey.TITLE.toString(), id3v11.getFirst(FieldKey.TITLE));
        assertEquals("1234", id3v11.getFirst(FieldKey.YEAR));
        assertEquals("22", id3v11.getFirst(FieldKey.TRACK));
        assertEquals(
            FieldKey.COMMENT.toString(), id3v11.getFirst(FieldKey.COMMENT));
                
        tag.setField(FieldKey.GENRE, "");
        id3v11 = Id3Util.toId3v11(tag);
        assertEquals("", id3v11.getFirst(FieldKey.GENRE));
        
    }
    
}
