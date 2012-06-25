package org.jcoderz.mp3.intern;


public enum TagQuality 
{
    GOLD("gold"), SILVER("silver"), BRONZE("bronze");
    private final String mSubdir;

    TagQuality (String subDir)
    {
        mSubdir = subDir;
    }

    public String getSubdir ()
    {
        return mSubdir;
    }
}
