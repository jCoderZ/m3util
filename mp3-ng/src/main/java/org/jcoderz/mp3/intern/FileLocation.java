package org.jcoderz.mp3.intern;


import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import org.jcoderz.commons.util.Assert;
import org.jcoderz.commons.util.ObjectUtil;
import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.mb.MbClient;
import org.jcoderz.mb.type.Medium;
import org.jcoderz.mb.type.Medium.TrackList.Track;
import org.jcoderz.mb.type.Recording;
import org.jcoderz.mb.type.Release;
import org.jcoderz.mb.type.Type;
import org.jcoderz.mp3.intern.util.FileUtil;
import org.jcoderz.mp3.intern.util.MbUtil;

/**
 * Class is capable to determine the file location based on the available metadata.
 * 
 * TODO: New Musicbrainz handling of feat. etc.
 * eg: renamed: 08 - Linda Perry - Knock Me Out (feat. Grace Slick).mp3 -> 08 - Linda Perry feat. Grace Slick - Knock Me Out (feat. Grace Slick).mp3.mp3 @ f:\mp3\[Soundtrack]\The Crow~ City of Angels
 *     renamed: 16 - Heather Nova - Believe in Angels (feat. Graeme Revel).mp3 -> 16 - Heather Nova feat. Graeme Revell - Believe in Angels.mp3.mp3 @ f:\mp3\[Soundtrack]\The Crow~ City of Angels
 *     renamed: 08 - Evenstar (feat. Isabel Bayrakdarian).mp3 -> 08 - Evenstar.mp3.mp3 @ f:\mp3\[Soundtrack]\The Lord of the Rings~ The Two Towers
 *     renamed: 01 - Stefan Raab - Space Taxi (feat. Spucky, Kork & Schrotty) (Radio Version).mp3 -> 01 - Stefan Raab feat. Spucky, Kork & Schrotty - Space Taxi (Radio Version).mp3.mp3 @ f:\mp3\[Soundtrack]\(T)Raumschiff Surprise Periode 1~ Die Songs
 * 
 * @author Andreas Mandel
 *
 */
public final class FileLocation
{
    private String mPath;

    private String mFilename;

    private boolean mIsUpToDate = false;

    private boolean mIsComplete;

    private boolean mIsSingle;

    private boolean mIsVaStyle;

    private boolean mIsStory;

    private boolean mIsSoundtrack;

    private boolean mIsAlbumCollision;

    private boolean mIsFileCollision;

    private int mTrackNumber;

    private String mArtist;

    private String mArtistSortName;

    private String mAlbumArtist;

    private String mAlbumArtistSortName;

    private String mAlbum;

    private String mAlbumId;

    private String mTitle;

    private String mTitleId;

    public FileLocation(MusicBrainzMetadata md)
    {
        set(md);
    }
    
    public FileLocation (Release rel, Recording recording)
    {
        mIsUpToDate = false;
        setComplete(true);
        setSingle(false);
        mIsVaStyle = MbClient.containsVa(rel.getArtistCredit());
        
        final String type = ObjectUtil.toStringOrEmpty(rel.getReleaseGroup().getType());
        
        mIsStory = MbUtil.isStory(rel.getReleaseGroup());
        mIsSoundtrack = Type.SOUNDTRACK.toString().equalsIgnoreCase(type);
        mTrackNumber = -1;
        if (rel.getMediumList().getMedium().get(0).getTrackList().getDefTrack().size() == 1)
        {
            Assert.assertEquals("Only one Medium expected in release.", 1, 
                rel.getMediumList().getMedium().size());
            Assert.assertEquals("Only one Track expected in medium.", 1, 
                rel.getMediumList().getMedium().get(0).getTrackList().getDefTrack().size());
            mTrackNumber = rel.getMediumList().getMedium().get(0)
                .getTrackList().getDefTrack().get(0).getPosition().intValue();
            mAlbum = rel.getTitle(); 
            mAlbumId = rel.getId(); 
            mTitle = recording.getTitle();             
        }
        else
        {
out:
            for (Medium m : rel.getMediumList().getMedium())
            {
                for (Track t : m.getTrackList().getDefTrack())
                {
                    Assert.notNull(t , "medium.track");
                    Assert.notNull(t.getRecording() , "medium.track.recording");
                    Assert.notNull(t.getRecording().getId() , "medium.track.recording.id");
                    Assert.notNull(recording , "recording");
                    Assert.notNull(recording.getId() , "recording.id");
                    if (t.getRecording().getId().equals(recording.getId()))
                    {
                        mTrackNumber = t.getPosition().intValue();
                        if (StringUtil.isEmptyOrNull(t.getTitle()))
                        {
                            mTitle = recording.getTitle();
                        }
                        else
                        {
                            mTitle = t.getTitle();
                        }
                        if (StringUtil.isEmptyOrNull(m.getTitle()))
                        {
                            mAlbum = rel.getTitle(); // TODO: OK??  Medium??
                        }
                        else
                        {
                            mAlbum = m.getTitle(); // TODO: OK??  Medium??
                        }
                        break out;
                    }
                }
            }
            Assert.assertTrue("Failed to find recording and medium.", mTrackNumber != -1);
        }

        mArtist = MbClient.getArtist(recording.getArtistCredit());
        mArtistSortName = MbClient.getArtistSortName(recording.getArtistCredit());
        mAlbumArtist = MbClient.getArtist(rel.getArtistCredit());
        mAlbumArtistSortName = MbClient.getArtistSortName(rel.getArtistCredit());
        mAlbumId = rel.getId();
        mTitleId = recording.getId();
    }

    public void set(MusicBrainzMetadata md)
    {
        mIsUpToDate = false;
        setComplete(md.isAlbumComplete());
        setSingle(md.isSingle());
        mIsVaStyle = md.isVa();
        mIsStory = md.isStory();
        mIsSoundtrack = md.isSoundtrack();
        mTrackNumber = md.getTrackNumber();
        mArtist = md.getArtist();
        mArtistSortName = md.getArtistSortname();
        mAlbumArtist = md.getAlbumArtist();
        mAlbumArtistSortName = md.getAlbumArtistSortname();
        mAlbum = md.getAlbum();
        mAlbumId = md.getAlbumId();
        mTitle = md.getTitle();
        mTitleId = md.getFileId();
        if (StringUtil.isBlankOrNull(mTitle)) 
        {
            mTitle = md.getFile().getName();
        }
    }

    public boolean isIsAlbumCollision ()
    {
        return mIsAlbumCollision;
    }

    public void setIsAlbumCollision (boolean isAlbumCollision)
    {
        mIsUpToDate = false;
        mIsAlbumCollision = isAlbumCollision;
    }

    public boolean isIsFileCollision ()
    {
        return mIsFileCollision;
    }

    public void setIsFileCollision (boolean isFileCollision)
    {
        mIsUpToDate = false;
        mIsFileCollision = isFileCollision;
    }

    public String getFilename()
    {
        update();
        return mFilename;
    }
    
    public String getPath()
    {
        update();
        return mPath;
    }

    public File getFile(File baseDir)
    {
        update();
        return new File(baseDir, mPath + mFilename);
    }
    
    public File getPath(File baseDir)
    {
        update();
        return new File(baseDir, mPath);
    }

    /**
     * Returns the album name part of the file location.
     * @return the album name part of the file location.
     */
    public String getAlbum()
    {
        final StringBuilder sb = new StringBuilder();
        final String album = FileUtil.fixPath(mAlbum);
        sb.append(album);
        if (mIsAlbumCollision && !StringUtil.isBlankOrNull(mAlbumId))
        {
            sb.append(" [");
            sb.append(mAlbumId);
            sb.append(']');
        }
        if (!isComplete())
        {
            sb.append(" {-}");
        }
        return sb.toString();
    }
    
    private void update ()
    {
        if (!mIsUpToDate)
        {
            updatePath();
            updateFileName();
        }
        mIsUpToDate = true;
    }

    private void updateFileName ()
    {
        final StringBuilder sb = new StringBuilder();
        final String trackNumber;
        if (mTrackNumber <= 0)
        {
            trackNumber = "XX";
        }
        else
        {
            trackNumber = new DecimalFormat("00").format(mTrackNumber);
        }
        if (!isSingle() && mIsVaStyle)
        {
            sb.append(trackNumber);
            sb.append(" - ");
            sb.append(mArtist);
            sb.append(" - ");
            sb.append(mTitle);
        }
        else if (isSingle() || StringUtil.isBlankOrNull(mAlbum))
        {
            sb.append(mTitle);
        }
        else
        {
            sb.append(trackNumber);
            sb.append(" - ");
            sb.append(mTitle);
        }
        if (mIsFileCollision && !StringUtil.isBlankOrNull(mTitleId))
        {
            sb.append(" [");
            sb.append(mTitleId);
            sb.append(']');
        }
       mFilename = FileUtil.fixFilename(sb.toString()) + ".mp3";
    }

    private void updatePath ()
    {
        final StringBuilder sb = new StringBuilder();

        String artist;
        if (isSingle())
        {
            artist = mArtistSortName;
            if (StringUtil.isBlankOrNull(artist))
            {
                artist = mArtist;
            }
        }
        else
        {
            artist = mAlbumArtistSortName;
            if (StringUtil.isBlankOrNull(artist))
            {
                artist = mAlbumArtist;
                if (StringUtil.isBlankOrNull(artist))
                {
                    artist = mArtistSortName;
                    if (StringUtil.isBlankOrNull(artist))
                    {
                        artist = mArtist;
                    }
                }
            }
        }
        artist = FileUtil.fixPath(artist);
        if (isSingle() || (!mIsSoundtrack && !mIsStory && !mIsVaStyle))
        {
            sb.append('/');
            sb.append(getPathLetter(artist));
            sb.append('/');
            sb.append(artist);
        }
        else if (mIsSoundtrack)
        {
            sb.append("/[Soundtrack]");
        }
        else if (mIsStory)
        {
            sb.append("/[Story]/");
            if (mIsVaStyle)
            {
                sb.append("[Various]");
            }
            else
            {
                sb.append(artist);
            }
        }
        else if (mIsVaStyle)
        {
            sb.append("/[Various]");
        }
        else
        {
            throw new RuntimeException("Unexpected code reached.");
        }
        sb.append('/');
        if (isSingle() || StringUtil.isBlankOrNull(mAlbum)) 
        {
            sb.append('/');
            sb.append("[Singles]");
            sb.append('/');
        }
        else
        {
            sb.append(getAlbum());
            sb.append('/');
        }
        mPath = sb.toString();
    }

    public static String getPathLetter (String name)
    {
        final String result;
        if (name != null && name.length() != 0)
        {
            final char sortChar = Character.toUpperCase(name.charAt(0));
            if (sortChar >= 'A' && sortChar <= 'Z')
            {
                result = Character.toString(sortChar);
            }
            else if (sortChar >= '0' && sortChar <= '9')
            {
                result = "#";
            }
            else
            {
                result = "+";
            }
        }
        else
        {
            result = "+";
        }
        return result;
    }

    /**
     * Create different possible locations of the file.
     * @param repositoryMp3Path
     * @return
     */
    public Set<File> getPathVariations (File baseDir)
    {
        setComplete(true);
        setSingle(false);
        mIsAlbumCollision = false;
        mIsFileCollision = false;
        File[] result = new File[12];
        int pos = 0;
        pos = buildCollisionVariations(baseDir, result, pos);
        setComplete(false);
        pos = buildCollisionVariations(baseDir, result, pos);
        setSingle(true);
        pos = buildCollisionVariations(baseDir, result, pos);
        final Set<File> uniqueSet = new HashSet<File>();
        for (File file : result)
        {
            try
            {
                uniqueSet.add(file.getCanonicalFile());
            }
            catch (IOException ex)
            {
                //
            }
        }
        return uniqueSet;
    }

    private int buildCollisionVariations (File baseDir, File[] result, int pos)
    {
        mIsAlbumCollision = false;
        mIsFileCollision = false;
        mIsUpToDate = false;
        result[pos++] = getFile(baseDir);
        mIsAlbumCollision = true;
        mIsUpToDate = false;
        result[pos++] = getFile(baseDir);
        mIsFileCollision = true;
        mIsUpToDate = false;
        result[pos++] = getFile(baseDir);
        mIsAlbumCollision = false;
        mIsUpToDate = false;
        result[pos++] = getFile(baseDir);
        return pos;
    }

    public boolean isComplete ()
    {
        return mIsComplete;
    }

    public void setComplete (boolean isComplete)
    {
        mIsComplete = isComplete;
    }

    public boolean isSingle ()
    {
        return mIsSingle;
    }

    public void setSingle (boolean isSingle)
    {
        mIsSingle = isSingle;
    }

    /** Set flags how to be stored in dupe dir. */
    public void setDupe ()
    {
        setIsAlbumCollision(true);
        setIsFileCollision(false);
        setComplete(true);
        setSingle(false);
    }
    
    public String toString()
    {
    	return getPath() + '/' + getFilename();
    }
}
