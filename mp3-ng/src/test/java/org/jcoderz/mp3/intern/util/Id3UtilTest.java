package org.jcoderz.mp3.intern.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class Id3UtilTest
{
    private final String mGenreTestString; 
    private final String mGenreResultString; 
    
    public Id3UtilTest (String in, String expectedResult)
    {
        mGenreTestString = in;
        mGenreResultString = expectedResult;
    }
    
    @Test
    public void testFixGenre () throws KeyNotFoundException, FieldDataInvalidException
    {
        final ID3v23Tag tag = new ID3v23Tag();
        tag.setField(FieldKey.GENRE, mGenreTestString);
        final String fixGenre = Id3Util.fixGenre(tag);
        assertEquals("Expected different reult for input '" + mGenreTestString + "'.", 
            mGenreResultString, fixGenre);
    }
    
    @Parameters
    public static Collection<Object[]> data() 
    {
        final Object[][] data 
            = new Object[][]{
                {"", ""}, 
                {"Rock", "(17)Rock"},
                {"-", ""}, 
                {"unknown", ""}, 
                {"UNDEFINED", ""}, 
                {"genre", ""}, 
                {"Foo Bar", "(12)Foo Bar"}, 
                {"(8)Other", "(8)Other"}};
        return Arrays.asList(data);
    }    
    
}
