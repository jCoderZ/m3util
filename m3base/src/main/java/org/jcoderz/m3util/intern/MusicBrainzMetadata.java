package org.jcoderz.m3util.intern;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.ID3v23Frame;
import org.jaudiotagger.tag.id3.ID3v23Frames;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;
import org.jaudiotagger.tag.id3.framebody.FrameBodyUFID;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;
import org.jaudiotagger.tag.reference.PictureTypes;
import org.jcoderz.commons.util.Assert;
import org.jcoderz.commons.util.ObjectUtil;
import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.mb.MbClient;
import org.jcoderz.mb.TrackHelper;
import org.jcoderz.mb.type.ArtistCredit;
import org.jcoderz.mb.type.Asin;
import org.jcoderz.mb.type.Medium;
import org.jcoderz.mb.type.Medium.TrackList.Track;
import org.jcoderz.mb.type.TrackData;
import org.jcoderz.m3util.intern.CoverArt.ImageData;
import org.jcoderz.m3util.intern.util.Id3Util;
import org.jcoderz.m3util.intern.util.MbUtil;
import org.jcoderz.m3util.intern.util.Mp3Util;
import org.jcoderz.mb.MediumHelper;

// TODO: Id3Lib fix MUSICBRAINZ_TRACK_ID handling
public class MusicBrainzMetadata {

    private static final String NO_ALBUM_TRACK_ALBUM_TYPE = "none";
    private static final String CLASSNAME = MusicBrainzMetadata.class.getName();
    private static final Logger logger = Logger.getLogger(CLASSNAME);
    /**
     * Part of uuids that is printed in to string.
     */
    private static final int UUID_SUBSTRING_LENGTH = 8;
    public static final Pattern FULL_PATH_PATTERN = Pattern.compile(".*/([^/]{2,})/([^/]{2,})/([0-9]{2}) - (.*)\\.mp3");
    public static final Pattern FILE_NUMBER_PATTERN = Pattern.compile("[0-9]{2,4}");
    public static final Pattern FILE_TAG_PATTERN = Pattern.compile("([0-9]{2,3}) - (.*) - (.*)\\.mp3");
    public static final Pattern FILE_TAG2_PATTERN = Pattern.compile("([0-9]{2,3})[\\. -](.*) - (.*)\\.mp3");
    public static final Pattern FILE_TAG3_PATTERN = Pattern.compile("([0-9]{2})-(.*) _ (.*)\\.mp3");
    public static final Pattern FILE_TAG4_PATTERN = Pattern.compile("([0-9]{1,2})\\.(.*) _ (.*)\\.mp3");
    public static final Pattern FILE_TAG5A_PATTERN = Pattern.compile("(.+) - ([0-9]{2}) - (.+)\\.mp3");
    public static final Pattern FILE_TAG5_PATTERN = Pattern.compile("(.+) ([0-9]{2}) (.+)\\.mp3");
    public static final Pattern AMAZON_ASIN_URL_REGEX = Pattern.compile("^http://(www.)?(.*)/.*/([0-9B][0-9A-Z]{9})$");
    public static final int AMAZON_ASIN_URL_REGEX_HOST_GROUP = 2;
    public static final int AMAZON_ASIN_URL_REGEX_ASIN_GROUP = 3;
    public static final String MB_ALBUM_ARTIST_ID_DESC = "MusicBrainz Album Artist Id";
    public static final String MB_ALBUM_ID_DESC = "MusicBrainz Album Id";
    public static final String MB_ALBUM_STATUS_DESC = "MusicBrainz Album Status";
    public static final String MB_ALBUM_TYPE_DESC = "MusicBrainz Album Type";
    public static final String MB_ARTIST_ID_DESC = "MusicBrainz Artist Id";
    public static final String MB_ARTIST_SORTNAME_DESC = "MusicBrainz Artist Sortname";
    public static final String MB_TRM_ID_DESC = "MusicBrainz TRM Id";
    // NEW
    public static final String MB_ALBUM_ARTIST_DESC = "MusicBrainz Album Artist";
    public static final String MB_ALBUM_ARTIST_SORTNAME_DESC_OLD = "MusicBrainz Album Artist Sortname";
    public static final String MB_ALBUM_ARTIST_SORTNAME_DESC = "ALBUMARTISTSORT";
    // NEW IS ALBUMARTISTSORT!!
    public static final String MB_ALBUM_RELEASE_COUNTRY_DESC = "MusicBrainz Album Release Country";
    public static final String MB_PUID_DESC = "MusicIP PUID";
    public static final String MB_ASIN_DESC = "ASIN";
    public static final String BARCODE_DESC = "BARCODE";
    public static final String CATALOGNUMBER_DESC = "CATALOGNUMBER";
    public static final String UUID_DESC = "UUID";
    public static final String SCRIPT_DESC = "SCRIPT";
    public static final String SHA1_DESC = "SHA1";
    public static final String SINGLE = "single";
    // allowed value is "single"
    public static final String FOO_SINGLE_DESC = "Foo Single";
    // allowed value is "MusicBrainz" or "Foo"
    // if not set the default is "MusicBrainz"
    public static final String FOO_TAG_AUTHORITY_DESC = "Foo TagAuthority";
    private static final String MB_UFI_OWNER = "http://musicbrainz.org";
    public static final Set<String> UNKOWN_FRAMES = new HashSet<>();
    private long mLengthInMilliSeconds = -2;
    private final MP3File mMediaFile;
    private FrameBodyTXXX mTagAuthorityFrame;
    private FrameBodyTXXX mSingleFrame;
    private FrameBodyTXXX mUuidFrame;
    private FrameBodyTXXX mSha1Frame;
    private File mFile;
    private String mBitrateString;
    private long mBitrate;
    private boolean mGotInfo = false;
    /**
     * If true the song is part of a fully available album.
     */
    private boolean mAlbumComplete = true;

    public MusicBrainzMetadata(File file) {
        this(file, false);
    }

    public MusicBrainzMetadata(Path path) {
        this(path.toFile(), false);
    }

    public MusicBrainzMetadata(File file, boolean readOnly) {
        mFile = file;
        try {
            mMediaFile = new MP3File(file, MP3File.LOAD_ALL, true);
        } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            throw new RuntimeException("TODO " + e, e);
        }
        scanMbFrames();
    }

    public File getFile() {
        return mFile;
    }

    public void sync()
            throws IOException, TagException {
        // copy idv2 information into v1
        if (mMediaFile.getID3v2Tag() != null) {
            setGenre(Id3Util.fixGenre(mMediaFile.getID3v2Tag()));
            // KEEP removeTxxxFrame("MusicIP PUID");
            removeTxxxFrame("MusicMagic Data");
            removeTxxxFrame("MusicMagic Fingerprint");
            removeTxxxFrame("MusicIP Data");
            removeTxxxFrame("MusicBrainz TRM Id");
            removeTxxxFrame("MusicBrainz Non-Album");


            mMediaFile.setID3v1Tag(Id3Util.toId3v11(mMediaFile.getID3v2Tag()));
            if (!(mMediaFile.getID3v2Tag() instanceof ID3v23Tag)) {
                mMediaFile.setID3v2Tag(new ID3v23Tag(mMediaFile.getID3v2Tag()));
            }
            // clean encodings used??
        }
        mMediaFile.save();
    }

    private void getInfo() {
        if (!mGotInfo) {
            mLengthInMilliSeconds = mMediaFile.getAudioHeader().getTrackLength() * 1000;
            mBitrate = mMediaFile.getAudioHeader().getBitRateAsNumber();
            mBitrateString = mMediaFile.getAudioHeader().getBitRate();
            mGotInfo = true;
        }
    }

    private AbstractID3v2Tag getId3V2Tag() {
        if (mMediaFile.hasID3v2Tag()) {
            // final ID3v2 v2 = mMediaFile.getId3v2Tag();
            // TODO merge information if only set in V1!?
        } else if (mMediaFile.hasID3v1Tag()) {
            final ID3v23Tag newTag = new ID3v23Tag(mMediaFile.getID3v1Tag());
            mMediaFile.setID3v2Tag(newTag);
        } else // no tag at all, use filename
        {
            mMediaFile.setID3v2Tag(new ID3v23Tag());
            guessByFilename();
        }
        return mMediaFile.getID3v2Tag();
    }

    private void guessByFilename() {
        logger.info("Try filename " + getFile().getName());
        AbstractID3v2Tag tag = mMediaFile.getID3v2Tag();
        String filename = getFile().getName();
        filename = filename.replace('_', ' ');
        Matcher matcher = FILE_TAG_PATTERN.matcher(filename);
        try {
            if (matcher.matches()) {
                int trackNumber = Integer.parseInt(matcher.group(1));
                setTrackNumber(trackNumber % 100);
                tag.setField(FieldKey.ARTIST, matcher.group(2).trim());
                tag.setField(FieldKey.TITLE, matcher.group(3).trim());
                tag.setField(FieldKey.ALBUM, getFile().getParentFile().getName());
                logger.info("Tag set by filename 1 " + toString());
                return;
            }
            matcher = FILE_TAG2_PATTERN.matcher(filename);
            if (matcher.matches()) {
                int trackNumber = Integer.parseInt(matcher.group(1));
                setTrackNumber(trackNumber % 100);
                String artist = matcher.group(2).trim();
                tag.setField(FieldKey.ARTIST, artist);
                tag.setField(FieldKey.TITLE, matcher.group(3).trim());
                String album = getFile().getParentFile().getName();
                matcher = Pattern.compile("(.*) - (.*)").matcher(album);
                if (matcher.matches()
                        && album.toLowerCase().indexOf(artist.toLowerCase()) != -1) {
                    if (matcher.group(2).trim().equalsIgnoreCase(artist)) {
                        album = matcher.group(1).trim();
                    } else {
                        album = matcher.group(2).trim();
                    }
                }
                tag.setField(FieldKey.ALBUM, album);
                logger.info("Tag set by filename 2 " + toString());
                return;
            }
            matcher = FILE_TAG3_PATTERN.matcher(getFile().getName());
            if (matcher.matches()) {
                int trackNumber = Integer.parseInt(matcher.group(1));
                setTrackNumber(trackNumber % 100);
                tag.setField(FieldKey.ARTIST, matcher.group(2).trim());
                tag.setField(FieldKey.TITLE, matcher.group(3).trim());
                tag.setField(FieldKey.ALBUM, getFile().getParentFile().getName());
                logger.info("Tag set by filename 3 " + toString());
                return;
            }
            matcher = FILE_TAG4_PATTERN.matcher(getFile().getName());
            if (matcher.matches()) {
                int trackNumber = Integer.parseInt(matcher.group(1));
                setTrackNumber(trackNumber % 100);
                tag.setField(FieldKey.ARTIST, matcher.group(2).trim());
                tag.setField(FieldKey.TITLE, matcher.group(3).trim());
                tag.setField(FieldKey.ALBUM, getFile().getParentFile().getName());
                logger.info("Tag set by filename 4 " + toString());
                return;
            }
            matcher = FILE_TAG5A_PATTERN.matcher(getFile().getName());
            if (matcher.matches()) {
                int trackNumber = Integer.parseInt(matcher.group(2));
                setTrackNumber(trackNumber % 100);
                tag.setField(FieldKey.ARTIST, matcher.group(1).trim());
                tag.setField(FieldKey.TITLE, matcher.group(3).trim());
                final String dir = getFile().getParentFile().getName();
                final String album;
                if (dir.matches("(.+) - (.+)")) {
                    album = dir.substring(3 + dir.indexOf(" - ", 0));
                } else {
                    album = dir;
                }
                tag.setField(FieldKey.ALBUM, album);
                logger.info("Tag set by filename 5a " + toString());
                return;
            }
            matcher = FILE_TAG5_PATTERN.matcher(getFile().getName());
            if (matcher.matches()) {
                int trackNumber = Integer.parseInt(matcher.group(2));
                setTrackNumber(trackNumber % 100);
                tag.setField(FieldKey.ARTIST, matcher.group(1).trim());
                tag.setField(FieldKey.TITLE, matcher.group(3).trim());
                tag.setField(FieldKey.ALBUM, getFile().getParentFile().getName());
                logger.info("Tag set by filename 5 " + toString());
                return;
            }
            final String fullPath = getFile().getPath().replace('\\', '/')
                    .replace('_', ' ');
            matcher = FULL_PATH_PATTERN.matcher(fullPath);
            if (matcher.matches() && !matcher.group(1).contains(" - ")
                    && !matcher.group(1).startsWith("[")
                    && !matcher.group(1).contains("mp3")
                    && !matcher.group(2).contains(" - ")
                    && !matcher.group(2).startsWith("[")
                    && !matcher.group(2).startsWith("mp3")) {
                int trackNumber = Integer.parseInt(matcher.group(3));
                setTrackNumber(trackNumber % 100);
                tag.setField(FieldKey.ARTIST, matcher.group(1).trim());
                tag.setField(FieldKey.TITLE, matcher.group(4).trim());
                tag.setField(FieldKey.ALBUM, matcher.group(2).trim());
                logger.info("Tag set by path 1 " + toString());
                return;
            }
        } catch (KeyNotFoundException ex) {
            logger.log(Level.WARNING, "File name detection failed " + ex, ex);
        } catch (FieldDataInvalidException ex) {
            logger.log(Level.WARNING, "File name detection failed " + ex, ex);
        }
        logger.info("No clue for " + getFile().getName());
    }

    public Object get(String tag) {
        logger.info("tag=" + tag);
        String name = null;
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getName().toLowerCase().substring(3).equals(tag.toLowerCase())) {
                name = method.getName();
            }
        }
        logger.info("name=" + name);

        Object result = null;
        if (name != null) {
            try {
                Method m = this.getClass().getMethod(name);
                logger.info("m=" + m);
                result = m.invoke(this);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: throw exception
            new Exception().printStackTrace();
        }
        return result;
    }

    public Artwork getCoverImage() {
        Artwork result = null;
        if (mMediaFile.hasID3v2Tag()) {
            for (Artwork art : mMediaFile.getID3v2Tag().getArtworkList()) {
                if (art.getPictureType() == PictureTypes.DEFAULT_ID) {
                    result = art;
                    break;
                } else if (art.getPictureType() == 0) {
                    result = art;
                }
            }
        } else {
            result = null;
        }
        return result;
    }

    public void setCoverImage(Artwork image) {
        try {
            image.setPictureType(PictureTypes.DEFAULT_ID); // Cover Front
            getId3V2Tag().setField(image);
        } catch (FieldDataInvalidException ex) {
            logger.log(
                    Level.WARNING, "Coud not set image got Exception " + ex + ".", ex);
        }
    }

    public String getAlbum() {
        return trim(getId3V2Tag().getFirst(FieldKey.ALBUM));
    }

    public String getArtist() {
        return trim(getId3V2Tag().getFirst(FieldKey.ARTIST));
    }

    public String getComment() {
        return trim(getId3V2Tag().getFirst(FieldKey.COMMENT));
    }

    public String getGenre() {
        return trim(getId3V2Tag().getFirst(FieldKey.GENRE));
    }

    public String getTitle() {
        return trim(getId3V2Tag().getFirst(FieldKey.TITLE));
    }

    public int getTotalTracks() {
        return integer(getId3V2Tag().getFirst(FieldKey.TRACK_TOTAL));
    }

    public int getTrackNumber() {
        int trackNumber;
        trackNumber = integer(getId3V2Tag().getFirst(FieldKey.TRACK));
        if (trackNumber == -1) {
            logger.log(
                    Level.FINEST, "Could not read track number from tag.");
            trackNumber = trackNumberFromFileName();
            if (trackNumber > 0) {
                logger.log(Level.FINEST, "Could be read from filename says "
                        + trackNumber);
            }
        }
        return trackNumber;
    }

    private int trackNumberFromFileName() {
        int trackNumber = -1;
        final String filename = mFile.getName();
        final Matcher matcher = FILE_NUMBER_PATTERN.matcher(filename);
        if (matcher.find()) {
            trackNumber = Integer.parseInt(matcher.group());
            setTrackNumber(trackNumber % 100);
        }
        return trackNumber;
    }

    public int getYear() {
        int result = integer(StringUtil.fitToLength(getId3V2Tag().getFirst(FieldKey.YEAR), 0, 4));
        if (result == -1) {
            result = -9999;
        }
        return result;
    }

    public boolean setAlbum(String album) {
        final boolean modified = checkModify("album", getAlbum(), album);
        if (album == null) {
            getId3V2Tag().removeFrame(ID3v23Frames.FRAME_ID_V3_ALBUM);
        } else {
            setField(FieldKey.ALBUM, album);
        }
        return modified;
    }

    public boolean setArtist(String sArtist) {
        return setField(FieldKey.ARTIST, sArtist);
    }

    public boolean setComment(String sComment) {
        return setField(FieldKey.COMMENT, sComment);
    }

    public boolean setGenre(String sGenre) {
        return setField(FieldKey.GENRE, sGenre);
    }

    public boolean setTitle(String sTitle) {
        return setField(FieldKey.TITLE, sTitle);
    }

    public boolean setTrackNumber(int iTrackNumber, int iTotalTracks) {
        int oldTrack = integer(getId3V2Tag().getFirst(FieldKey.TRACK));
        int oldTotalTracks = integer(getId3V2Tag().getFirst(FieldKey.TRACK_TOTAL));
        final boolean modified = (iTrackNumber != oldTrack || iTotalTracks != oldTotalTracks);
        if (iTrackNumber == -1) {
            getId3V2Tag().removeFrame(ID3v23Frames.FRAME_ID_V3_TRACK);
        } else {
            setField(FieldKey.TRACK, Integer.toString(iTrackNumber));
            setField(FieldKey.TRACK_TOTAL, Integer.toString(iTotalTracks));
        }
        return modified;
    }

    // TODO: Use ID2V3member directly!
    public boolean setTrackNumber(int iTrackNumber) {
        int oldTrack = integer(getId3V2Tag().getFirst(FieldKey.TRACK));
        final boolean modified = (iTrackNumber != oldTrack);
        if (iTrackNumber == -1) {
            getId3V2Tag().removeFrame(ID3v23Frames.FRAME_ID_V3_TRACK);
        } else {
            setField(FieldKey.TRACK, Integer.toString(iTrackNumber));
        }
        return modified;
    }

    public boolean setYear(int iYear) {
        return setField(FieldKey.YEAR, Integer.toString(iYear));
    }

    public String getAlbumArtistId() {
        return getId3V2Tag().getFirst(FieldKey.MUSICBRAINZ_RELEASEARTISTID);
    }

    public boolean setAlbumArtistId(String albumArtistId) {
        return setField(FieldKey.MUSICBRAINZ_RELEASEARTISTID, albumArtistId);
    }

    public String getSingle() {
        return readFrame(mSingleFrame);
    }

    private boolean setSingle(String fooSingle) {
        final SetFrameResult result = setMbTxxxFrame(mSingleFrame, FOO_SINGLE_DESC, fooSingle);
        mSingleFrame = result.getFrame();
        return result.isModified();
    }

    public boolean isSingle() {
        return SINGLE.equals(getSingle());
    }

    public boolean setSingle(boolean isSingle) {
        return setSingle(isSingle ? SINGLE : null);
    }

    public String getTagAuthority() {
        final String result;
        if (mTagAuthorityFrame == null) {
            result = "MusicBrainz";
        } else {
            result = readFrame(mTagAuthorityFrame);
        }
        return result;
    }

    public boolean setTagAuthority(String tagAuthority) {
        final SetFrameResult result = setMbTxxxFrame(
                mTagAuthorityFrame, FOO_TAG_AUTHORITY_DESC, tagAuthority);
        mTagAuthorityFrame = result.getFrame();
        return result.isModified();
    }

    public String getAlbumId() {
        return getId3V2Tag().getFirst(FieldKey.MUSICBRAINZ_RELEASEID);
    }

    public boolean setAlbumId(String albumId) {
        return setField(FieldKey.MUSICBRAINZ_RELEASEID, albumId);
    }

    public String getArtistId() {
        return getId3V2Tag().getFirst(FieldKey.MUSICBRAINZ_ARTISTID);
    }

    public boolean setArtistId(String artistId) {
        return setField(FieldKey.MUSICBRAINZ_ARTISTID, artistId);
    }

    public String getAlbumArtist() {
        return getId3V2Tag().getFirst(FieldKey.ALBUM_ARTIST);
    }

    public boolean setAlbumArtist(String albumArtist) {
        // Remove outdated field with same content
        removeTxxxFrame(MB_ALBUM_ARTIST_DESC);
        return setField(FieldKey.ALBUM_ARTIST, albumArtist);
    }

    public String getAlbumArtistSortname() {
        return getId3V2Tag().getFirst(FieldKey.ALBUM_ARTIST_SORT);
    }

    public boolean setAlbumArtistSortname(String albumArtistSortname) {
        // Remove outdated field with same content
        removeTxxxFrame(MB_ALBUM_ARTIST_SORTNAME_DESC_OLD);
        return setField(FieldKey.ALBUM_ARTIST_SORT, albumArtistSortname);
    }

    public String getAlbumReleaseCountry() {
        return getId3V2Tag().getFirst(FieldKey.MUSICBRAINZ_RELEASE_COUNTRY);
    }

    public boolean setAlbumReleaseCountry(String releaseCountry) {
        return setField(FieldKey.MUSICBRAINZ_RELEASE_COUNTRY, releaseCountry);
    }

    public String getPuid() {
        return getId3V2Tag().getFirst(FieldKey.MUSICIP_ID);
    }

    public boolean setPuid(String puid) {
        return setField(FieldKey.MUSICIP_ID, puid);
    }

    public String getAsin() {
        return getId3V2Tag().getFirst(FieldKey.AMAZON_ID);
    }

    public boolean setAsin(String asin) {
        return setField(FieldKey.AMAZON_ID, asin);
    }

    private boolean setLanguage(String value) {
        return setField(FieldKey.LANGUAGE, value);
    }

    private boolean setScript(String value) {
        return setField(FieldKey.SCRIPT, value);
    }

    public String getUuid() {
        return readFrame(mUuidFrame);
    }

    private boolean setUuid(String uuid) {
        final SetFrameResult result = setMbTxxxFrame(mUuidFrame, UUID_DESC, uuid);
        mUuidFrame = result.getFrame();
        return result.isModified();
    }

    public String getSha1() {
        return readFrame(mSha1Frame);
    }

    private boolean setSha1(String sha1) {
        final SetFrameResult result = setMbTxxxFrame(mSha1Frame, SHA1_DESC, sha1);
        mSha1Frame = result.getFrame();
        return result.isModified();
    }

    /** @return the musicbrainz track id */
    public String getFileId() {
        String trackId = null;
        if (StringUtil.isEmptyOrNull(getId3V2Tag().getFirst(FieldKey.MUSICBRAINZ_TRACK_ID))) {
            Object foo = getId3V2Tag().getFrame(ID3v23Frames.FRAME_ID_V3_UNIQUE_FILE_ID);
            List<AbstractID3v2Frame> frames;
            if (foo instanceof AbstractID3v2Frame) {
                frames = new ArrayList<AbstractID3v2Frame>();
                frames.add((AbstractID3v2Frame) foo);
            } else if (foo != null) {
                frames = (List<AbstractID3v2Frame>) foo;
            } else {
                frames = Collections.emptyList();
            }

            for (AbstractID3v2Frame frame : frames) {
//               logger.fine("UID FRAME:" + frame.getId() + " " + frame.getBody().getLongDescription());
                final FrameBodyUFID ufid = (FrameBodyUFID) frame.getBody();
                if (MB_UFI_OWNER.equals(ufid.getOwner())) {
                    trackId = new String(ufid.getUniqueIdentifier());
                    break;
                }
            }
        }
        return trackId; // getId3V2Tag().getFirst(FieldKey.MUSICBRAINZ_TRACK_ID);
    }

    public boolean setFileId(String fileId) {
        boolean changed;
        // LOOKS BUGGY
        if (ObjectUtil.equals(getFileId(), fileId)) {
            changed = false;
        } else {
            changed = setField(FieldKey.MUSICBRAINZ_TRACK_ID, fileId);
        }
        return changed;
    }

    public void scanMbFrames() {
        final Object theFrame = getId3V2Tag().getFrame(ID3v23Frames.FRAME_ID_V3_USER_DEFINED_INFO);
        final Iterator<AbstractID3v2Frame> frames;
        if (theFrame instanceof AbstractID3v2Frame) {
            frames = Collections.singletonList((AbstractID3v2Frame) theFrame).iterator();
        } else if (theFrame != null) {
            frames = ((List<AbstractID3v2Frame>) theFrame).iterator();
        } else {
            frames = Collections.EMPTY_LIST.iterator();
        }
        while (frames.hasNext()) {
            final AbstractID3v2Frame frame = frames.next();
            String desc = null;
            FrameBodyTXXX fb = null;
            if (frame.getBody() instanceof FrameBodyTXXX) {
                fb = (FrameBodyTXXX) frame.getBody();
                desc = fb.getDescription();
            } else {
                logger
                        .warning("TXXX frame found that holds no FrameBodyTXXX, but "
                        + frame.getBody().getClass() + " - " + frame);
                continue;
            }
            if (desc.equals(FOO_SINGLE_DESC)) {
                mSingleFrame = fb;
            } else if (desc.equals(FOO_TAG_AUTHORITY_DESC)) {
                mTagAuthorityFrame = fb;
            } else if (UUID_DESC.equals(desc)) {
                mUuidFrame = fb;
            } else if (SHA1_DESC.equals(desc)) {
                mSha1Frame = fb;
            } else {
                if (!UNKOWN_FRAMES.contains(desc)) {
                    UNKOWN_FRAMES.add(desc);
                    logger.info("Unknown TXXX text frame type '" + desc
                            + "' text: '" + fb.getText() + "'.");
                }
            }
        }
    }

    private SetFrameResult setMbTxxxFrame(
            FrameBodyTXXX txxxFrame,
            String desc, String value) {
        boolean modified = false;
        FrameBodyTXXX fb = null;
        if (value == null) {
            final FrameBodyTXXX oldFrame = getTxxxFrame(desc);
            if (oldFrame != null) {
                modified = true;
                removeTxxxFrame(desc);
                logger.fine("Delete frame '" + desc + "' old: '"
                        + oldFrame.getText() + "'.");
            }
        } else if (txxxFrame == null) {
            ID3v23Frame frame = new ID3v23Frame(ID3v23Frames.FRAME_ID_V3_USER_DEFINED_INFO);
            fb = new FrameBodyTXXX(TextEncoding.ISO_8859_1, desc, value);
            frame.setBody(fb);
            List<AbstractID3v2Frame> txxxFrames = (List<AbstractID3v2Frame>) getId3V2Tag().getFrame(ID3v23Frames.FRAME_ID_V3_USER_DEFINED_INFO);
            if (txxxFrames == null) {
                txxxFrames = new ArrayList<AbstractID3v2Frame>();
                getId3V2Tag().setFrame(ID3v23Frames.FRAME_ID_V3_USER_DEFINED_INFO, txxxFrames);
            }
            // DELETE NOT NEEDED.....
            txxxFrames.add(frame);
            logger.fine("New frame '" + desc + "' == '" + value + "'.");
            modified = true;
        } else {
            fb = txxxFrame;
            modified = checkModify(desc, txxxFrame.getText(), value);
            fb.setText(value);
        }
        return new SetFrameResult(modified, fb);
    }

    private FrameBodyTXXX getTxxxFrame(String id) {
        FrameBodyTXXX result = null;
        Iterator frames = getId3V2Tag().getFrameOfType(
                ID3v23Frames.FRAME_ID_V3_USER_DEFINED_INFO);
        while (frames.hasNext()) {
            final List<AbstractID3v2Frame> frameList = (List<AbstractID3v2Frame>) frames.next();
//           if (foo instanceof ID3v23Frame)
//           {
//               frames = new ArrayList<ID3v23Frame>();
//               frames.add((ID3v23Frame) foo);
//           }
//           else
//           {
//               frames =
//           }

            for (AbstractID3v2Frame frame : frameList) {
                if (frame.getBody() instanceof FrameBodyTXXX
                        && (((FrameBodyTXXX) frame.getBody()).getDescription().equals(id))) {
                    result = (FrameBodyTXXX) frame.getBody();
                    break;
                }
            }
        }
        return result;
    }

    private void removeTxxxFrame(String id) {
        Iterator<TagField> fields = getId3V2Tag().getFields();
        while (fields.hasNext()) {
            final AbstractTagFrameBody fieldBody = ((AbstractID3v2Frame) fields.next()).getBody();
            if (fieldBody instanceof FrameBodyTXXX) {
                FrameBodyTXXX fb = (FrameBodyTXXX) fieldBody;
                if (fb.getDescription().equals(id)) {
                    logger.fine(""
                            + "U delete " + id + " -> '" + fb.getText() + "'.");
                    fields.remove();
                    break;
                }
            }
        }
    }

    private static String readFrame(
            FrameBodyTXXX textInformationID3V2Frame) {
        final String result;
        if (textInformationID3V2Frame != null) {
            result = textInformationID3V2Frame.getText();
        } else {
            result = null;
        }
        return trim(result);
    }

    /**
     * @param albumComplete the albumComplete to set
     */
    public boolean setAlbumComplete(boolean albumComplete) {
        boolean modified = (isAlbumComplete() != albumComplete);
        if (albumComplete && isSingle()) {
            modified = setSingle(false) | modified;
        }
        mAlbumComplete = albumComplete;
        return modified;
    }

    /**
     * @return the albumComplete
     */
    public boolean isAlbumComplete() {
        return mAlbumComplete;
    }

    public String getAlbumType() {
        return getId3V2Tag().getFirst(FieldKey.MUSICBRAINZ_RELEASE_TYPE);
    }

    public boolean setAlbumType(String albumType) {
        return setField(FieldKey.MUSICBRAINZ_RELEASE_TYPE, albumType);
    }

    public boolean setAlbumStatus(String albumStatus) {
        return setField(
                FieldKey.MUSICBRAINZ_RELEASE_STATUS, albumStatus);
    }

    public String getAlbumStatus() {
        return getId3V2Tag().getFirst(FieldKey.MUSICBRAINZ_RELEASE_STATUS);
    }

    public boolean setArtistSortname(String sortname) {
        // Remove outdated field with same content
        removeTxxxFrame(MB_ARTIST_SORTNAME_DESC);
        return setField(FieldKey.ARTIST_SORT, sortname);
    }

    public String getArtistSortname() {
        return getId3V2Tag().getFirst(FieldKey.ARTIST_SORT);
    }

    /**
     * Returns the album artist sortname if available, or the artist sortname if
     * this is a single.
     */
    public String getEffectiveSortName() {
        final String albumArtistSortName = getId3V2Tag().getFirst(FieldKey.ALBUM_ARTIST_SORT);
        final String result;
        if (isSingle() || StringUtil.isEmptyOrNull(albumArtistSortName)) {
            result = getArtistSortname();
        } else {
            result = albumArtistSortName;
        }
        return result;
    }

    /**
     * Update all tags with the given {@link TrackData}.
     *
     * @param data the date to use for the update.
     * @return true, if data was changed.
     */
    public boolean update(TrackData data) {
        boolean changed = false;
        Assert.notNull(data, "TrackData");
        final Track track = data.getTrack();
        final Medium medium = data.getMedium();
        final org.jcoderz.mb.type.Release release = data.getRelease();
        Assert.notNull(track, "TrackData.getTrack()");


        boolean noAlbumTrack = medium == null
                || NO_ALBUM_TRACK_ALBUM_TYPE.equalsIgnoreCase(
                release.getReleaseGroup().getType());

        if (!noAlbumTrack) {
            Assert.notNull(medium, "TrackData.getMedium()");
            Assert.notNull(release, "TrackData.getRelease()");
        }

        final ArtistCredit artistCredit = TrackHelper.getArtistCredit(track);

        final org.jcoderz.mb.type.Artist albumArtist;
        // TODO: There might be multiple Ids....
        if (noAlbumTrack) {
            changed = setAlbumArtistId(null) || changed;
            changed = setAlbumArtist(null) || changed;
            changed = setAlbumArtistSortname(null) || changed;
            albumArtist = null;
        } else {
            final ArtistCredit albumArtistCredit = release.getArtistCredit();
            albumArtist = albumArtistCredit.getNameCredit().get(0).getArtist();
            changed = setAlbumArtistId(albumArtist.getId()) || changed;
            changed = setAlbumArtist(MbClient.getArtist(albumArtistCredit)) || changed;
            changed = setAlbumArtistSortname(MbClient.getArtistSortName(albumArtistCredit)) || changed; // BBBBBB
        }

        final org.jcoderz.mb.type.Artist trackArtist =
                artistCredit == null ? albumArtist
                : artistCredit.getNameCredit().get(0).getArtist();
        Assert.notNull(trackArtist, "track.getArtist()");
// FIXME: Id for multiple artists?
        changed = setArtistId(trackArtist.getId()) || changed;
        changed = setArtist(MbClient.getArtist(artistCredit)) || changed;
        changed = setArtistSortname(MbClient.getArtistSortName(artistCredit)) || changed;

        final String title = TrackHelper.getTitle(track);
        changed = setTitle(title) || changed;
        changed = setFileId(track.getRecording().getId()) || changed;

        if (noAlbumTrack) {
            changed = setTrackNumber(-1, -1) || changed;
            changed = setAlbum(null) || changed;
            changed = setAlbumId(null) || changed;
            changed = setField(FieldKey.DISC_NO, null) || changed;
            changed = setField(FieldKey.DISC_TOTAL, null) || changed;
            changed = setAlbumStatus(null) || changed;
            changed = setAsin(null) || changed;
        } else {
            changed = setTrackNumber(
                    track.getPosition().intValue(),
                    medium.getTrackList().getCount().intValue())
                    || changed;
            changed = setAlbum(buildAlbumTitle(data)) || changed;
            changed = setAlbumId(release.getId()) || changed;
            changed = setAlbumStatus(
                    release.getStatus() == null ? null : release.getStatus().getValue()) || changed;
            changed = setAsin(getAsin(data)) || changed;

            final int mediumCount = release.getMediumList().getCount().intValue();
            if (mediumCount > 1) {
                final int mediumPos = medium.getPosition().intValue();
                changed = setField(FieldKey.DISC_NO, Integer.toString(mediumPos)) || changed;
                changed = setField(FieldKey.DISC_TOTAL, Integer.toString(mediumCount)) || changed;
            }
        }

        final String type;
        if (release == null || release.getReleaseGroup() == null) {
            type = NO_ALBUM_TRACK_ALBUM_TYPE;
        } else if (release.getReleaseGroup().getType() == null) {
            type = "";
        } else {
            type = release.getReleaseGroup().getType().toLowerCase();
        }
        changed = setAlbumType(type) || changed;


        if (release != null
                && release.getReleaseGroup() != null
                && MbUtil.isSoundtrack(release.getReleaseGroup())) {
            changed = setGenre("(24)Soundtrack") || changed;
        } else if (release != null
                && release.getReleaseGroup() != null
                && MbUtil.isStory(release.getReleaseGroup())) {
            changed = setGenre("(28)Vocal") || changed;
        } else {
            final String genere = ("" + getGenre()).toLowerCase();
            if (genere.contains("soundtrack")
                    || genere.contains("vocal")) {
                changed = setGenre(null) || changed;
            }
        }

        // search for early release...
        if (release != null && release.getDate() != null
                && !StringUtil.isEmptyOrNull(release.getDate().getValue())) {
            final int year = Integer.parseInt(release.getDate().getValue().substring(0, 4));
            final int last = getYear();
            if ((last == -9999 || year < last) && setYear(year)) {
                changed = true;
                logger.info("U YEAR: " + last + " -> " + year);
            }
        }

        if (StringUtil.isNullOrEmpty(getUuid())) {
            changed = addUuid() || changed;
        }

        if (release != null && release.getTextRepresentation() != null) {
            if (release.getTextRepresentation().getLanguage() != null) {
                changed =
                        setLanguage(release.getTextRepresentation().getLanguage().getValue()) || changed;
            }
            if (release.getTextRepresentation().getScript() != null) {
                changed =
                        setScript(release.getTextRepresentation().getScript().getValue()) || changed;
            }
        }

        // sha1 handling
        final String sha1 = getSha1();
        if (!StringUtil.isNullOrEmpty(sha1) && changed) {   // no update if sha1 broken
            final String calcSha1 = Mp3Util.calcAudioFramesSha1(getFile());
            if (!sha1.equals(calcSha1)) {
                throw new RuntimeException("Will not Update sha1 mismatch!");
            }
        }
        if (StringUtil.isNullOrEmpty(sha1)) {
            changed = true;
            addSha1();
        }
        return changed;
    }

    /**
     * Create a uuid and add it to the file as Id3Tag. The Uuid will be newly
     * generated, existing Uuids will be overwritten.
     *
     * @return true, always.
     */
    public boolean addUuid() {
        return setUuid(UUID.randomUUID().toString());
    }

    /**
     * Calculate the sha1 value for the data stream of this file {
     *
     * @see Mp3Util#calcAudioFramesSha1(File)} and store the result in a Sha1
     * id3 tag.
     */
    public void addSha1() {
        setSha1(Mp3Util.calcAudioFramesSha1(getFile()));
    }

    private String buildAlbumTitle(TrackData data) {
        return MediumHelper.buildAlbumTitle(data.getRelease(), data.getMedium());
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        int dataCount = 0;
        String artist = getEffectiveSortName();
        if (artist == null || artist.length() == 0) {
            artist = getArtist();
        }
        if (StringUtil.isNullOrBlank(artist)) {
            buffer.append("???");
        } else {
            buffer.append(artist);
            dataCount++;
        }
        buffer.append('/');

        final String album = getAlbum();
        if (StringUtil.isNullOrBlank(album)) {
            buffer.append("???");
        } else {
            buffer.append(album);
            dataCount++;
        }
        if (!StringUtil.isNullOrBlank(getAlbumId())) {
            buffer.append(" [");
            buffer.append(getAlbumId().substring(0, UUID_SUBSTRING_LENGTH));
            buffer.append("\u2026]");
        }
        buffer.append('/');

        final String trackNumber;
        if (getTrackNumber() <= 0) {
            trackNumber = "XX";
        } else {
            trackNumber = new DecimalFormat("00").format(getTrackNumber());
        }

        buffer.append(trackNumber);
        buffer.append(" - ");
        final String title = getTitle();
        if (StringUtil.isNullOrBlank(title)) {
            buffer.append("???");
        } else {
            buffer.append(title);
            dataCount++;
        }

        if (dataCount == 0) {
            buffer.setLength(0);
            buffer.append(getFile().getAbsolutePath());
        }

        if (!StringUtil.isEmptyOrNull(getFileId())) {
            buffer.append(" [");
            buffer.append(getFileId().substring(0, UUID_SUBSTRING_LENGTH));
            buffer.append("\u2026]");
        }
        return buffer.toString();
    }

    public boolean isVa() {
        return MbClient.VARIOUS_ARTIST_ID.equals(getAlbumArtistId())
                || MbClient.VARIOUS_ARTIST_ID_2.equals(getAlbumArtistId());
    }

    public boolean isVaStyleName(boolean single) {
        // TODO: Should be put in MetaData class
        final boolean vaStyle;
        if (single) {
            vaStyle = false;
        } else if (isVa()) {
            vaStyle = true;
        } else if (isSoundtrack()) {
            vaStyle = true;
        } else if (isStory()) {
            vaStyle = true;
        } else {
            vaStyle = false;
        }
        return vaStyle;
    }

    public boolean isStory() {
        return getGenre().toLowerCase().contains("vocal");
    }

    public boolean isSoundtrack() {
        return getGenre().toLowerCase().contains("soundtrack");
    }

    private int integer(String val) {
        int result = -1;
        if (!StringUtil.isBlankOrNull(val) && !"null".equals(val)) {
            try {
                result = Integer.parseInt(val.trim());
            } catch (NumberFormatException ex) {
                logger.log(
                        Level.INFO, "Failed to parse int '" + val + "' " + ex, ex);
            }
        }
        return result;
    }

    private static String trim(String str) {
        return str == null ? null : str.trim();
    }

    public long getLengthInMilliSeconds() {
        getInfo();
        return mLengthInMilliSeconds;
    }

    public void setLengthInMilliSeconds(long lengthInMilliSeconds) {
        mLengthInMilliSeconds = lengthInMilliSeconds;
    }

    public String getBitrateString() {
        getInfo();
        return mBitrateString;
    }

    public long getBitrate() {
        getInfo();
        return mBitrate;
    }

    public String getLengthString() {
        return toString(getLengthInMilliSeconds());
    }

    public static String toString(long millies) {
        final String result;
        if (millies <= 0) {
            result = "";
        } else {
            final long minutes = millies / 60000;
            final double seconds = (millies % 60000) / 1000D;

            result = minutes + ":" + new DecimalFormat("00.000").format(seconds);
        }
        return result;
    }

    private boolean checkModify(String typeName, String oldValue,
            String newValue) {
        final boolean modified = !StringUtil.equals(oldValue, newValue);
        if (modified) {
            logger.fine("U " + typeName + " old: '" + oldValue + "' new: '"
                    + newValue + "'.");
        }
        return modified;
    }

    public void fetchCoverImage(CoverArt ca) {
        String id = getAsin();
        if (!StringUtil.isNullOrEmpty(id)) {
            if (getCoverImage() == null) {
                final ImageData image = ca.getImage(id);
                if (image != null) {
                    Artwork aw = new Artwork();
                    aw.setBinaryData(image.getImage());
                    aw.setMimeType(image.getMimeType());
                    setCoverImage(aw);
                    logger.info("U -> Added IMAGE " + this);
                }
            }
        }
        if (getCoverImage() == null) {
            id = getAlbumId();
            final ImageData image = ca.getImage(id);
            if (image != null) {
                Artwork aw = new Artwork();
                aw.setBinaryData(image.getImage());
                aw.setMimeType(image.getMimeType());
                setCoverImage(aw);
                logger.info("U -> Added IMAGE by mb-id " + this);
            }
        }

    }

    private String getAsin(TrackData data) { // TODO -> Relationships!
        final Asin asin = data.getRelease().getAsin();
        return ObjectUtil.toString(asin == null ? null : asin.getValue());
    }

    private boolean setField(FieldKey fieldKey, String newValue) {
        String oldValue = "";
        if (newValue == null) {
            newValue = "";
        }
        try {
            oldValue = getId3V2Tag().getFirst(fieldKey);
            if (oldValue == null) {
                oldValue = "";
            }
            if (StringUtil.isBlankOrNull(newValue) && StringUtil.isBlankOrNull(oldValue)) {
                // fine
            } else if (StringUtil.isBlankOrNull(newValue)) {
                getId3V2Tag().deleteField(fieldKey);
            } else {
                getId3V2Tag().setField(fieldKey, newValue);
            }
        } catch (KeyNotFoundException ex) {
            logger.log(Level.WARNING, "Coud not set " + fieldKey + " to "
                    + newValue + " got Exception " + ex + ".", ex);
        } catch (FieldDataInvalidException ex) {
            logger.log(Level.WARNING, "Coud not set " + fieldKey + " to "
                    + newValue + " got Exception " + ex + ".", ex);
        }
        final boolean modified = !StringUtil.equals(oldValue, newValue);
        if (modified) {
            logger.fine("U " + fieldKey + " old: '" + oldValue + "' new: '"
                    + newValue + "'.");
        }
        return modified;
    }

    private class SetFrameResult {

        private final boolean mModified;
        private final FrameBodyTXXX mFrame;

        public SetFrameResult(boolean modified, FrameBodyTXXX frame) {
            mFrame = frame;
            mModified = modified;
        }

        public boolean isModified() {
            return mModified;
        }

        public FrameBodyTXXX getFrame() {
            return mFrame;
        }
    }
}
