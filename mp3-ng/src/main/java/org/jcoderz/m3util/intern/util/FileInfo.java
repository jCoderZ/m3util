package org.jcoderz.mp3.intern.util;


import org.jcoderz.mb.type.TrackData;
import org.jcoderz.mp3.intern.FileLocation;
import org.jcoderz.mp3.intern.MusicBrainzMetadata;


/**
 * Maps a mp3 file {@link #getMbMetadata()} with a possible match
 * {@link #getTrackData()} and possible location
 * {@link #getFileLocation()}.
 * 
 * @author Andreas Mandel
 */
public final class FileInfo
    implements Cloneable
{
    final MusicBrainzMetadata mMbMetadata;

    private TrackData mTrackData;

    private FileLocation mFileLocation;

    public FileInfo (MusicBrainzMetadata metadata)
    {
        mMbMetadata = metadata;
    }

    public FileInfo clone ()
    {
        final FileInfo result;
        try
        {
            result = (FileInfo) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException("Object.clone not supported?", e);
        }
        return result;
    }
    
    public String toString ()
    {
        return "FileInfo: " + getMbMetadata().getFile().getName() + " ("
            + getMbMetadata() + ")";
    }

    public TrackData getTrackData ()
    {
        return mTrackData;
    }

    public FileLocation getFileLocation ()
    {
        return mFileLocation;
    }

    public void setFileLocation (FileLocation fileLocation)
    {
        mFileLocation = fileLocation;
    }

    public void setTrackData (TrackData trackData)
    {
        mTrackData = trackData;
    }

    public MusicBrainzMetadata getMbMetadata ()
    {
        return mMbMetadata;
    }
}
