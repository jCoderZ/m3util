package org.jcoderz.mp3.intern;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jcoderz.commons.util.ArraysUtil;
import org.jcoderz.commons.util.DirTreeListener;
import org.jcoderz.commons.util.DirTreeWalker;
import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.mb.MbClient;
import org.jcoderz.mb.type.Includes;
import org.jcoderz.mb.type.Medium;
import org.jcoderz.mb.type.Recording;
import org.jcoderz.mb.type.Release;
import org.jcoderz.mb.type.TrackData;
import org.jcoderz.mp3.intern.util.FileUtil;
import org.jcoderz.mp3.intern.util.LoggingUtil;
import org.jcoderz.mp3.intern.util.MbUtil;

// Handle the situation that a incoming album will be moved to Singles!

// TODO:
//  - Support a way to tag files as singles
//  - Support a way to tag files as none mb files
//  - Support Play Lists
//  - Track changes in the DB


/**
 * This class implements the logic needed to update and freshen a existing
 * well maintained m3dditiez repository. 
 * It will update data changes coming from musicbrainz and can also be used 
 * to merge a set of clean mp3s into a existing repository.
 * It does not:
 * <ul>
 * <li>Clean up files by id guessing</li>
 * <li>...</li>
 * </ul>  
 *
 * @author Andreas Mandel
 */
public final class RefreshRepository
{
   private static final String CLASSNAME = RefreshRepository.class.getName();
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private final boolean mDryRun;
   private final int mPathOffset;
   private final DirTreeWalker mDirTreeWalker;
   private boolean mAlbumFailure = false;
   private boolean mAlbumModified = false;
   private Release mCurrentRelease = null;
   private Medium mCurrentMedium = null;
   
   
   // Could be a class knowing all the dirs and structure 
   private final File mRepositoryBasePath;
   private final File mRepositoryMp3Path;
   private final File mRepositoryDupePath;
   
   private final MbClient mMusicBrainz;
   private CoverArt mCoverArt;

   /**
    * TODO: Build command line interface.
    * @param args
    */
   public static void main (String[] args)
         throws IOException
   {
      //final String mbServerHostname = "http://192.168.56.101:3000";
      final String mbServerHostname = "http://mb-box:5000";
      // final String mbServerHostname = "file:///c:/tmp/mb/";
      
      LoggingUtil.initLogging(logger, "DRY-CLEANUP");
      logger.info("---- !START! ----");
      logger.info("----  RefreshRepository '" + args[0] + "'");

      final RefreshRepository checker 
            = new RefreshRepository(
                false /*dryRun*/,
//                new File(args[0]), 
//               new File(args[0] + "/mp3"), 
               new File(args[0] + "/upload/cleaned"), 
               new File(args[0]), 
               mbServerHostname);

      try
      {
          checker.start();
      }
      finally
      {
          if (MusicBrainzMetadata.UNKOWN_FRAMES.isEmpty())
          {
              logger.info("Hey, no unknown frames found, cool.");
          }
          else
          {
              logger.info("Found unknown frames: " +  MusicBrainzMetadata.UNKOWN_FRAMES);
          }
          logger.info("---- END ----");
      }
   }

    /**
     * Create a {@link RefreshRepository} instance, that can work on a
     * directory. Use {@link #start()} to start the actual work.
     * 
     * @param dryRun if true no actual files will be updated, only a log
     *        file will be written.
     * @param dir the directory to work on. Might be a sub-directory of
     *        the repos.
     * @param repositoryPath the root path of the clean repository.
     * @param mbServerHostname the name of the musicbrainz server to
     *        work with. You should use a local mirror because many
     *        requests will be sent to this server.
     */
   public RefreshRepository (boolean dryRun, File dir, File repositoryPath, String mbServerHostname)
   {
       mDryRun = dryRun;
      mMusicBrainz = new MbClient(mbServerHostname);
      // mMusicBrainz.setRecordDir(new File("c:/tmp/mb/"));
      mRepositoryBasePath = repositoryPath;
      mPathOffset = mRepositoryBasePath.getAbsolutePath().length();
      
      if (!mRepositoryBasePath.isDirectory())
      {
         throw new RuntimeException("Repository path does not exist '" 
               + mRepositoryBasePath + "'.");
      }
      mRepositoryMp3Path = new File(mRepositoryBasePath, "mp3");
      if (!mRepositoryMp3Path.isDirectory())
      {
         throw new RuntimeException(
               "Repository must contain a mp3 folder! '" 
               + mRepositoryBasePath + "'.");
      }
      if (!new File(mRepositoryBasePath, "playlist").isDirectory())
      {
         throw new RuntimeException(
               "Repository must contain a playlist folder! '" 
               + mRepositoryBasePath + "'.");
      }
      mRepositoryDupePath = new File(mRepositoryBasePath, "upload/dupes");
      mCoverArt = new CoverArt(new File(mRepositoryBasePath, "tools/var/cache/asin"));
      
      mDirTreeWalker = new DirTreeWalker(dir, new DirTreeListener()
      {
          private int mDirLevel;
          
         final List<MusicBrainzMetadata> mFiles 
               = new ArrayList<MusicBrainzMetadata>();
         
         public void file (File file)
         {
            final MusicBrainzMetadata mb = checkFile(file);
            if (mb != null)
            {
               mFiles.add(mb);
            }
         }

         public void exitingDir (File dir)
         {
             boolean handleDir = classifyType();
            
            if (!mFiles.isEmpty() && !mAlbumFailure && mCurrentRelease != null && handleDir)
            {
               checkAlbum(dir, mFiles);
            }
            else if (!mFiles.isEmpty() && !mAlbumFailure)
            {
                // some files in single dir.. might need movement?
                while (!mFiles.isEmpty())
                {
                    final MusicBrainzMetadata song = mFiles.remove(0);
                    final String currentAlbum = song.getAlbumId();
                    final List<MusicBrainzMetadata> songs = new ArrayList<MusicBrainzMetadata>();
                    songs.add(song);
                    Iterator<MusicBrainzMetadata> i = mFiles.iterator();
                    while (i.hasNext())
                    {
                        final MusicBrainzMetadata albumSong = i.next();
                        if (currentAlbum.equals(albumSong.getAlbumId()))
                        {
                            i.remove();
                            songs.add(albumSong);
                        }
                    }
                    // got all songs from same album in the set!
                    checkPartialAlbum(songs);
                }
            }
            else
            {
                if (mDirLevel <= 1)
                {
                    logger.info("DONE: " + dir.getName());
                }
            }
            mFiles.clear();
            FileUtil.deleteDirIfEmpty(dir);
            try
            {
               if (System.in.available() != 0)
               {
                  throw new RuntimeException("Exit requested");
               }
            }
            catch (IOException ex)
            {
               //
            }
            mDirLevel--;
         }

         /**
          * All files in mFiles are checked for complete album content...
          */
        private boolean classifyType ()
        {
            boolean result = true;
            if (!mFiles.isEmpty())
            {
                if (mCurrentMedium == null) // No disc....
                {
                    result = false;  // need detailed test....
                }
                else
                {
                    final int mediumSize = mCurrentMedium.getTrackList().getCount().intValue();
                    final boolean complete = (mFiles.size() == mediumSize);
                    final boolean single = mFiles.size() * 2 < mediumSize;
                    for (MusicBrainzMetadata song : mFiles)
                    {
                        mAlbumModified = mAlbumModified || song.setAlbumComplete(complete);
                        mAlbumModified = mAlbumModified || song.setSingle(single);
                    }
                    result = complete;
                }
            }
            return result;
        }

        public void enteringDir (File dir)
         {
             mDirLevel++;
            if (!mFiles.isEmpty())
            {
               logger.warning("Entering " + dir + " with no empty file list.");
               logger.warning(" Will discard files " + mFiles);
               mFiles.clear();
            }
            mAlbumFailure = false;
            mAlbumModified = false;
            mCurrentRelease = null;
         }
      });
   }
   
   private void start ()
   {
      mDirTreeWalker.start();
   }


   private void checkPartialAlbum (List<MusicBrainzMetadata> songs)
    {
        logger.fine("PartialAlbum @ " 
            + songs.get(0).getFile().getAbsolutePath().substring(mPathOffset) 
            + " [" + songs.size() + "]");
        
        try
        {
            // load the album....
            final String currentAlbum = songs.get(0).getAlbumId();
            final Release release 
                = StringUtil.isEmptyOrNull(currentAlbum) ? null : mMusicBrainz.getRelease(currentAlbum);
            final TrackData track 
                = MbUtil.getTrackDataWithIdUpdate(mMusicBrainz, songs.get(0), release);

            final int fullTrackCount 
                = track.getMedium() == null ? 0 : track.getMedium().getTrackList().getCount().intValue();
            final boolean complete = songs.size() >= fullTrackCount;
            final boolean single = songs.size() * 2 < fullTrackCount;
            for (MusicBrainzMetadata song : songs)
            {
                song.setAlbumComplete(complete);
                song.setSingle(single);
                final TrackData trackData 
                    = MbUtil.getTrackDataWithIdUpdate(mMusicBrainz, song, release);
                song.update(trackData);
                // now update file location!
                final File newFile = determinRepositoryPosition(songs, song);
                
                if (!mDryRun)
                {
                    song.fetchCoverImage(mCoverArt);
                    song.sync(); // write id3 tag info
                }
                if (newFile != null)
                {
                    moveToFile(newFile, song);
                }
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Problem with partialalbum in '" + 
                songs.get(0).getFile().getAbsolutePath().substring(mPathOffset) + "'." +
                " Songs :" + songs, ex);
        }
    }

   private MusicBrainzMetadata checkFile (File file)
   {
      MusicBrainzMetadata mbData = null;
      if (!mAlbumFailure 
            && ( file.getName().toLowerCase().endsWith(".mp3")
               || file.getName().toLowerCase().endsWith(".tmp")))
      {
         try
         {
            mbData = new MusicBrainzMetadata(file);
            
            if (file.getName().toLowerCase().endsWith(".tmp"))
            {
               logger.info("Picked up tmp file " + file);
            }
            
            // update data @ mb server...
            final String trackId = mbData.getFileId();

            if (StringUtil.isEmptyOrNull(trackId))
            {
               throw new Exception("TrackId not set in " + mbData
                   + " @ " + file.getAbsolutePath());
            }
            
            if (mbData.isSingle() 
                || mbData.getFile().getAbsolutePath().contains("[Singles]") 
                || mbData.getTrackNumber() == -1)
            {
               if (mCurrentRelease != null)
               {
                  throw new Exception(
                        "Single tagged file found in a album dir!");
               }
               refreshSingle(mbData);
            }
            else
            {
               if (mCurrentRelease == null)
               {
                   mCurrentRelease = mMusicBrainz.getRelease(mbData.getAlbumId());
                   // TODO: catch not found...
               }
                final TrackData track 
                    = MbUtil.getTrackDataWithIdUpdate(mMusicBrainz, mbData, mCurrentRelease);
                mCurrentMedium = track.getMedium();
               final String oldData = String.valueOf(mbData);
               // now check for updates + set data...
               boolean trackModified = mbData.update(track);
               if (mbData.getCoverImage() == null)
               {
                   mbData.fetchCoverImage(mCoverArt);
                   trackModified |= (mbData.getCoverImage() != null);
               }
               
               if (!mCurrentRelease.getId().equals(mbData.getAlbumId()))
               {
                  throw new Exception("Different album ids in single dir!");
               }
               
               if (!trackModified)
               {
                   // check for filename change...
                   final FileLocation fl = new FileLocation(mbData);
                   final boolean nameModified 
                       = !mbData.getFile().getName().equals(fl.getFilename());
                   trackModified = nameModified || trackModified;
               }
               
               mAlbumModified = trackModified || mAlbumModified;
               if (trackModified)
               {
                   if (oldData.equals(String.valueOf(mbData)))
                   {
                       logger.info("!" + oldData 
                           + "@" + mbData.getFile().getAbsolutePath());
                   }
                   else
                   {
                      logger.info("<" + oldData); 
                      logger.info(">" + mbData
                          + "@" + mbData.getFile().getAbsolutePath());
                   }
               }
            }
         }
         catch (Exception e)
         {
            logger.log(Level.WARNING, 
                  "Exception while checking '" + file 
                  + "', will not fix album: " + e.getMessage() 
                  + " STACK: " + ArraysUtil.toString(e.getStackTrace()), e);
            mAlbumFailure = true;
         }
      }
      return mbData;
   }


   private void refreshSingle (MusicBrainzMetadata mbData)
   {
      try
      {  // error within one single should not stop checking the
         // other singles.
         // TODO: No album tracks!!!
          final Recording track = 
              mMusicBrainz.getRecording(mbData.getFileId());
          if (track == null)
          {   
             throw new Exception(
                 "track " + mbData.getFileId() + " not found.");
          }
          
          // use 1st release in list of the one that matches
          // with earliest release date && smallest id...
          Release release = track.getReleaseList().getRelease().get(0);
          int date = 9999;
          Iterator<Release> i = track.getReleaseList().getRelease().iterator();
          boolean found = false;
          while (i.hasNext())
          {
              final Release rel = i.next();
              if (rel.getId().equals(mbData.getAlbumId()))
              {
                  if (date > MbClient.getReleaseYear(rel))
                  {
                      release = rel;
                      found = true;
                  }
                  else if (date == MbClient.getReleaseYear(rel)
                      && release.getId().compareTo(rel.getId()) > 0)
                  {
                      release = rel;
                      found = true;
                  }
              }
          }
          if (!found)
          {
              i = track.getReleaseList().getRelease().iterator();
              while (i.hasNext())
              {
                  final Release rel = i.next();
                  if (date > MbClient.getReleaseYear(rel))
                  {
                      release = rel;
                  }
                  else if (date == MbClient.getReleaseYear(rel)
                      && release.getId().compareTo(rel.getId()) > 0)
                  {
                      release = rel;
                  }
              }
          }

          /// TODO: Relationships???
          final TrackData data 
             = MbUtil.getTrackDataWithIdUpdate(
                 mMusicBrainz, mbData, mMusicBrainz.getRelease(release.getId()));
         final String oldData = String.valueOf(mbData);
         // now check for updates + set data...
         final boolean trackModified  = mbData.update(data);
         if (trackModified)
         {
                if (!mDryRun)
                {
                    mbData.fetchCoverImage(mCoverArt);
                    mbData.sync();
                }
             if (oldData.equals(String.valueOf(mbData)))
             {
                 logger.info("!" + oldData 
                     + "@" + mbData.getFile().getAbsolutePath());
             }
             else
             {
                logger.info("<" + oldData); 
                logger.info(">" + mbData
                    + "@" + mbData.getFile().getAbsolutePath());
             }
         }
         // Singles are moved with the dir! (TODO: check simple renames)
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, 
               "Exception while checking single '" + mbData.getFile() 
               + "'. (" + mbData +")", ex);
      }
   }

    private void checkAlbum (File dir, List<MusicBrainzMetadata> files)
    {
        logger.fine(
            "Album @ " + dir.getAbsolutePath().substring(mPathOffset) + " [" + files.size() + "]");
        try
        {
            if (files.size() 
                > mCurrentMedium.getTrackList().getCount().intValue())
            {
                throw new CouldNotFixAlbumException(
                    "Number of files unexpected. " + files.size() 
                    + " in dir and " 
                    + mCurrentMedium.getTrackList().getCount().intValue() 
                    + " in album. GiveUp");
            }
            
            // Check the album...
            final String albumId = files.get(0).getAlbumId();
            if (files.size() 
                != mCurrentMedium.getTrackList().getCount().intValue())
            {
                final boolean single 
                    = files.size() * 2 < mCurrentMedium.getTrackList().getCount().intValue();
                for (MusicBrainzMetadata song : files)
                {
                    song.setAlbumComplete(false);
                    mAlbumModified = song.setSingle(single) || mAlbumModified;
                }
            }
            final File newDir = determinRepositoryDirPosition(files);
            // check album for changes...
            mAlbumModified = mAlbumModified || (newDir != null);
            if (mAlbumModified)
            {
                Iterator<MusicBrainzMetadata> i = files.iterator();
                while (i.hasNext())
                {
                    if (!albumId.equals(i.next().getAlbumId()))
                    {
                        throw new CouldNotFixAlbumException(
                            "Different album ids in one dir!");
                    }
                }
                // fix the files
                i = files.iterator();
                while (i.hasNext())
                {
                    final MusicBrainzMetadata file = i.next();
                    if (!mDryRun)
                    {
                        file.fetchCoverImage(mCoverArt);
                        file.sync(); // write id3 tag info
                    }
                    final FileLocation fl = new FileLocation(file);
                    if (file.isSingle() && newDir != null)
                    {
                        moveToFile(new File(newDir, fl.getFilename()), file);
                    } 
                    else if (!file.getFile().getName().equals(fl.getFilename()))
                    {
                        moveToNewFilename(fl, file);
                    }
                }
                // now move the dir...
                if (newDir != null && !newDir.equals(dir))
                {
                    if (mDryRun)
                    {
                        logger.info(
                            "Would move: '" + dir + "' -> '" + newDir + "'.");
                    }
                    else if (!FileUtil.moveDir(dir, newDir))
                    {
                        // TODO: Retry -1- if dupe!?
                        throw new Exception(
                            "Failed to move directory '" + dir
                            + "' -> '" + newDir + "'.");
                    }
                    else
                    {
                        logger.info("moved '" + dir + "' '" + newDir + "'");
                    }
                    FileUtil.deleteDirIfEmpty(dir);
                }
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Problem with album in '"
                + dir.getAbsolutePath().substring(mPathOffset) + "'.", ex);
        }
    }
    
    private void moveToNewFilename (FileLocation fl, final MusicBrainzMetadata file)
        throws Exception
    {
        final String move 
            = file.getFile().getName() + " -> " + fl.getFilename() + " @ " 
                + file.getFile().getParent();
        if (mDryRun)
        {
            logger.info("Would rename: " + move);
        }
        else if (!FileUtil.moveFile(file.getFile(), file.getFile().getParentFile(), fl.getFilename()))
        {
            throw new Exception("Failed to rename file within dir '" + move);
        }
        else
        {
            logger.info("renamed: " + move);
        }
    }

    private void moveToFile (File target, MusicBrainzMetadata file)
        throws Exception
    {
        final String move = file.getFile().getAbsolutePath() + "' -> '"
            + target.getAbsolutePath() + "'.";
        if (target.isDirectory() || target.exists())
        {
            throw new Exception("Will not move, target exists. " + move);
        }
        
        if (mDryRun)
        {
            logger.info("Would move: " + move);
        }
        else if (!FileUtil.moveFile(
                file.getFile(), target.getParentFile(), target.getName()))
        {
            throw new Exception("Failed to rename file within dir '" + move);
        }
        else
        {
            logger.info("moved: " + move);
        }
    }

   /**
    * Check in which directory the given file should reside.
    * Returns null if the file is already at the right place. 
    */
    public File determinRepositoryDirPosition (List<MusicBrainzMetadata> album)
    {
        logger.entering(CLASSNAME,
            "determinRepositoryDirPosition(List<MusicBrainzMetadata>)", album);
        final MusicBrainzMetadata mb = album.get(0);
        final FileLocation fl = new FileLocation(mb);
        FilePos filePos = new FilePos(mRepositoryMp3Path, fl);
        // repository is NOT case sensitive (but the filesystem might be!)
        if (mb.getFile().getParent()
            .equalsIgnoreCase(filePos.getDir().getName()))
        {
            // file is at the right pos, case might be an issue (to be
            // checked!)
            if (!mb.getFile().getParent().equals(filePos.getDir().getName()))
            {
                filePos = null; // flag NO Change
            }
        }
        else if (mb.isSingle())
        {
            refineSinglePos(mb, filePos);
        }
        else
        // album
        {
            refineAlbumPos(album, filePos);
        }
        if (filePos != null
            && mb.getFile().getAbsolutePath()
                .equals(filePos.getFile().getAbsolutePath()))
        {
            filePos = null;
        }
        final File result = (filePos == null ? null : filePos.getDir());
        logger.exiting(CLASSNAME,
            "determinRepositoryPosition(MusicBrainzMetadata)", result);
        return result;
    }
   
   public File determinRepositoryPosition(List<MusicBrainzMetadata> songs, MusicBrainzMetadata mb)
   {
      logger.entering(CLASSNAME, 
            "determinRepositoryPosition(List<MusicBrainzMetadata>, MusicBrainzMetadata)", 
            new Object[] {songs, mb});
      
      final FileLocation fl = new FileLocation(mb);
      FilePos filePos 
            = new FilePos(mRepositoryMp3Path, fl);
      
      // repository is NOT case sensitive (but the filesystem might be!)
      if (mb.getFile().getAbsolutePath().equalsIgnoreCase(
            filePos.getFile().getAbsolutePath()))
      {
         // file is at the right pos, case might be an issue (to be checked!)
         if (!mb.getFile().getAbsolutePath().equals(
               filePos.getFile().getAbsolutePath()))
         {
            filePos = null;
         }
      }
      else if (mb.isSingle())
      {
         refineSinglePos(mb, filePos);
      }
      else // album
      {
         refineAlbumPos(songs, filePos);
      }
      if (filePos != null && mb.getFile().getAbsolutePath().equals(
            filePos.getFile().getAbsolutePath()))
      {
         filePos = null;
      }
      final File result = (filePos == null ? null : filePos.getFile());
//      if (result != null && result.toString().contains("Various Artists"))
//      {
//          Assert.fail(
//          "Path must not contain Various Artists" + result);
//      }
      logger.exiting(CLASSNAME, 
            "determinRepositoryPosition(MusicBrainzMetadata)", result);
      
      
      
      return result;
   }

    private void refineAlbumPos (
        List<MusicBrainzMetadata> album, FilePos filePos)
    {
        logger.entering(CLASSNAME,
            "refineAlbumPos(List<MusicBrainzMetadata>, FilePos)", 
            new Object[]{album, filePos});
        final MusicBrainzMetadata mb = album.get(0);

        // Same directory
        if (filePos.getFile().getParentFile().equals(mb.getFile().getParentFile())) 
        {
            // fine, already at album pos...
        }
        else if (!mb.isAlbumComplete() && existAsFullAlbum(mb))
        {
            setDupePos(filePos, mb);
        }
        else if (filePos.getDir().exists())
        {
            // there is already a album at the target position!
            // now check if the existing album is the same
            final List<MusicBrainzMetadata> existing 
                = readSongs(filePos.getDir());
            if (existing.isEmpty())
            {
                logger.log(Level.WARNING, 
                    "Empty dir in repository? '"
                    + filePos.getDir() + "'.");
                // just use it....
            }
            else
            {
                // Need to compare content of existing and current album
                // To detect similar albums.
                if (isRedundantAlbum(album, existing))
                {
                    logger.info("Redundant album detected. Album at " 
                        + album.get(0).getFile().getParent() 
                        + " is contained in album at "
                        + existing.get(0).getFile().getParent());
                }
                else
                { // is the not the same album,
                  // so try to make path more qualifying
                    filePos.getFileLocation().setIsAlbumCollision(true);
                }

                if (filePos.getFile().exists()
                    && !filePos.getFile().equals(mb.getFile()))
                { // this is a dupe
                    setDupePos(filePos, mb);
                }
            }
        }
        logger.exiting(CLASSNAME,
            "refineAlbumPos(List<MusicBrainzMetadata>, FilePos)", filePos);
    }

    private void setDupePos (FilePos filePos, final MusicBrainzMetadata mb)
    {
        filePos.setBaseDir(mRepositoryDupePath);
        filePos.getFileLocation().setDupe();
        int loop = 0;
        while (filePos.getFile().exists()
            && !filePos.getFile().equals(mb.getFile()))
        { // duplicate dupe.....
            filePos.setPathAdd("-" + loop++ + "-");
        }
    }

   private void refineSinglePos (MusicBrainzMetadata mb, FilePos filePos)
   {
       boolean dupe = isSingleDupe(mb);
       if (!dupe && filePos.getFile().exists())
       {
         // there is already a file at the target position!
         // now check if the existing file is the same song
         MusicBrainzMetadata existingMb 
               = new MusicBrainzMetadata(filePos.getFile());
         if (!existingMb.getFileId().equals(mb.getFileId()))
         {  // is the not the same song, so try to make filename more qualifying
             filePos.getFileLocation().setIsFileCollision(true);
         }
         dupe = filePos.getFile().exists() 
             && !filePos.getFile().equals(mb.getFile());
       }
       if (dupe)
       {
            setDupePos(filePos, mb);
       }
   }
   
   private boolean existAsFullAlbum(MusicBrainzMetadata mb) 
   {
       boolean result = false;
       
       final FileLocation completeAlbumFl = new FileLocation(mb);
       completeAlbumFl.setComplete(true);
       completeAlbumFl.setSingle(false);
       final File songFile = completeAlbumFl.getFile(mRepositoryMp3Path);
       if (songFile.exists())
       {
           final MusicBrainzMetadata existingMb 
               = new MusicBrainzMetadata(songFile);
           if (existingMb.getFileId().equals(mb.getFileId()))
           {
               logger.info("album dupe detected at: " + mb.getFile().getAbsolutePath() 
                   + " already in repos at: " + existingMb);
               result = true;
           }
           else
           {
               logger.fine("same name / different id single dupe detected at: " 
                   + mb.getFile().getAbsolutePath() + " already in repos at: " + existingMb.getFile().getAbsolutePath());
           }
       }
       return result;
   }
   
    public boolean isSingleDupe (MusicBrainzMetadata mb)
    {
        boolean result = false;
        final Recording recording = mMusicBrainz.getRecording(mb.getFileId(),
            Includes.RELEASES, Includes.MEDIA, Includes.ARTIST_CREDITS,
            Includes.RELEASE_GROUPS);
        // we have a bunch of dupe candidates now:
out: 
        for (Release rel : recording.getReleaseList().getRelease())
        {
            // test if the song is part of the repos...
            FileLocation fl = new FileLocation(rel, recording);
            Set<File> locations = fl.getPathVariations(mRepositoryMp3Path);
            for (File cand : locations)
            {
                if (cand.exists() && !cand.equals(mb.getFile()))
                {
                    final MusicBrainzMetadata existingMb 
                        = new MusicBrainzMetadata(cand);
                    if (existingMb.getFileId().equals(mb.getFileId()))
                    {
                        logger.info("single dupe: "
                            + mb.getFile().getAbsolutePath()
                            + " already in repos at: " + cand 
                            + " (" + existingMb + ")");
                        result = true;
                        break out;
                    }
                    else
                    {
                        logger
                            .fine("same name / different id single dupe detected at: "
                                + mb.getFile().getAbsolutePath()
                                + " already in repos at: "
                                + existingMb.getFile().getAbsolutePath());
                    }
                }
            }
        }
        return result;
    }   
   
    /**
     * Returns true, if the album already exists or is subset of an existing album. 
     * @param newAlbum the new album to check
     * @return true, if the album already exists or is subset of an existing album. 
     */
    public boolean isRedundantAlbum (List<MusicBrainzMetadata> newAlbum)
    {
        logger.entering(CLASSNAME,
            "isRedundantAlbum(List<MusicBrainzMetadata>)", 
            new Object[]{newAlbum});
        boolean result = false;
        
        // get candidates
        List<File> existingAlbumPoss = getExistingAlbumPos(newAlbum.get(0));
        for (File existingAlbumPos : existingAlbumPoss)
        {
            if (!existingAlbumPos.equals(newAlbum.get(0).getFile().getParentFile()))
            {
                result = isRedundantAlbum(newAlbum, readSongs(existingAlbumPos));
            }
            if (result)
            {
                break;
            }
        }
        
        
        logger.exiting(CLASSNAME, 
            "isRedundantAlbum(List<MusicBrainzMetadata>)",
            result);
        return result;
    }

    /**
     * Returns true, if the 2 album are the same or the existing album is a 
     * superset of the new album.
     * @param newAlbum the new album to check
     * @param existingAlbum the existing album to be checked.
     * @return true, if the 2 album are the same or the existing album is a 
     * superset of the new album.
     */
    public boolean isRedundantAlbum (
        List<MusicBrainzMetadata> newAlbum, List<MusicBrainzMetadata> existingAlbum)
    {
        logger.entering(CLASSNAME,
            "isRedundantAlbum(List<MusicBrainzMetadata>, List<MusicBrainzMetadata>)", 
            new Object[]{newAlbum, existingAlbum});
        final MusicBrainzMetadata newSong = newAlbum.get(0);
        final MusicBrainzMetadata existingSong = existingAlbum.get(0);
        boolean result;
        if (existingSong.getAlbumId().equals(newSong.getAlbumId())
            && !existingSong.getFile().equals(newSong.getFile())) 
        {
            logger.finer("Albums have the same id -> they are redundant.");
            // same Id...
            result = true;
        }
        else if (newAlbum.size() > existingAlbum.size())
        {
            logger.finer("New album has more songs than existing album -> Not redundant.");
            result = false;
        }
        else // iterate through songs...
        {
           // logger.finer("Need some code to be written to decide if album is redundant -> Not redundant.");
            // TEST!!!!!
            result = isRedundant(newAlbum, existingAlbum);;
        }
              
        logger.exiting(CLASSNAME,
            "isRedundantAlbum(List<MusicBrainzMetadata>, List<MusicBrainzMetadata>)",
            result);
        return result;
    }

    private boolean isRedundant (List<MusicBrainzMetadata> newAlbum,
        List<MusicBrainzMetadata> existingAlbum)
    {
        final List<MusicBrainzMetadata> newSongs = new ArrayList<MusicBrainzMetadata>();
        newSongs.addAll(newAlbum);
        
        for(MusicBrainzMetadata song : existingAlbum)
        {
            final Iterator<MusicBrainzMetadata> newSongsIterator 
                = newSongs.iterator();
            while (newSongsIterator.hasNext())
            {
                final MusicBrainzMetadata newSong = newSongsIterator.next();
                if (isSimiliar(song, newSong))
                {
                    newSongsIterator.remove();
                }
            }
        }
        if (newSongs.isEmpty())
        {
            logger.finer("All songs of are already there -> Album is redundant.");
        }
        else
        {
            logger.finer("Some songs are not already there. " + newSongs);
        }
        
        return newSongs.isEmpty();
    }
    
    private boolean isSimiliar (
        MusicBrainzMetadata song, MusicBrainzMetadata newSong)
    {
        final boolean result = song.getUuid().equals(newSong.getUuid()) 
            || (song.getTitle().equals(newSong.getTitle()) 
                && (Math.abs(song.getLengthInMilliSeconds() - newSong.getLengthInMilliSeconds()) < 5000));
        
        // logger.finest(song + (result ? "==" : "!=") + newSong);
        return result;
    }

    /**
     * Read all songs in the given directory and return the 
     * musicbrainz data.
     * @param file the directory to read.
     * @return the read metadata in a List.
     */
    private List<MusicBrainzMetadata> readSongs(File dir)
    {
        final List<MusicBrainzMetadata> result = new ArrayList<MusicBrainzMetadata>();
        final File[] files = dir.listFiles();
        Arrays.sort(files);
        for (File file : files)
        {
            if (file.getName().toLowerCase().endsWith(".mp3"))
            {
                result.add(new MusicBrainzMetadata(file));
            }
        }
        return result;
    }

    /**
     * Returns all possible existing directories for the album denoted in the 
     * given mb file.
     * @param mb
     * @return
     */
    private List<File> getExistingAlbumPos(MusicBrainzMetadata mb)
    {
        logger.entering(CLASSNAME, "getExistingAlbumPos(MusicBrainzMetadata)", mb);
        final List<File> albumPos = new ArrayList<File>();
        final FileLocation completeAlbumFl = new FileLocation(mb);
        completeAlbumFl.setComplete(true);
        completeAlbumFl.setSingle(false);
        final File album = completeAlbumFl.getPath(mRepositoryMp3Path);

        if (album.isDirectory())
        {
            albumPos.add(album);
        }
        
        if (album.getParentFile().isDirectory())
        {
            final Pattern pat = Pattern.compile(Pattern.quote(completeAlbumFl.getAlbum()) + " \\[.*\\]");
            final File[] listFiles 
                = album.getParentFile().listFiles(new FilenameFilter()
                {
                    public boolean accept (File dir, String name)
                    {
                        return pat.matcher(name).matches();
                    }
                });
            albumPos.addAll(Arrays.asList(listFiles));
        }
        
        Collections.sort(albumPos);
        logger.exiting(CLASSNAME, "getExistingAlbumPos(MusicBrainzMetadata)", albumPos);
        return albumPos;
    }

    static class FilePos
    {
        private File mBaseDir;

        private final FileLocation mFileLocation;

        // private String mFilename;
        // private String mPath;
        private String mPathAdd = "";

        public FilePos (File baseDir, FileLocation fl)
        {
            mBaseDir = baseDir;
            mFileLocation = fl;
        }

        public File getDir ()
        {
            return getFileLocation().getPath(new File(mBaseDir, mPathAdd));
        }

        public File getFile ()
        {
            return getFileLocation().getFile(new File(mBaseDir, mPathAdd));
        }

        public void setPathAdd (String pathAdd)
        {
            mPathAdd = pathAdd;
        }

        /**
         * @return Returns the baseDir.
         */
        public File getBaseDir ()
        {
            return mBaseDir;
        }

        /**
         * @param baseDir The baseDir to set.
         */
        public void setBaseDir (File baseDir)
        {
            mBaseDir = baseDir;
        }

        @Override
        public String toString ()
        {
            return "FilePos [getFile()=" + getFile() + "]";
        }

        public FileLocation getFileLocation ()
        {
            return mFileLocation;
        }
    }
}
