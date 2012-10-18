package org.jcoderz.m3util.intern;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.jcoderz.commons.util.DirTreeListener;
import org.jcoderz.commons.util.DirTreeWalker;
import org.jcoderz.commons.util.FileUtils;
import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.m3util.intern.util.Mp3Util;

/**
 * Simple silly sha1 dupe checker
 *
 * @author Andreas Mandel
 *
 */
public class DumpSha1Checker {

    private final HashMap<String, String> mRefSha1s = new HashMap<String, String>();
    private final boolean mSingleDirMode;
    private final File mReferenceDirectory;
    private final File mDeleteDirectory;
    private final boolean mDryRun;

    /**
     * Utility method to use this class as a main class. Should be dropped once
     * {@link org.jcoderz.mp3.M3Util} is ready for use. Takes up to 3 arguments:
     * <ol> <li>directory of reference mp3s</li> <li>Directory of mp3s to be
     * checked for dupes. Files here might be deleted! <li>If a 3rd argument is
     * passed to the class the {@link DumpSha1Checker} runns in a dry run mode.
     * </ol>
     *
     * @param args the up to 3 string arguments.
     */
    public static void main(String[] args) {
        // TODO: Must be smarter.
        DumpSha1Checker c = new DumpSha1Checker(new File(args[0]), new File(args[1]), args.length > 2);
        c.start();
    }

    /**
     * Delete sha1 dupes in delDir.
     *
     * @param refDir ref dir to collect existing sha1s.
     * @param delDir dir to delete dupes in.
     * @param dryRun if true, only report what would be done.
     */
    public DumpSha1Checker(File refDir, File delDir, boolean dryRun) {
        mSingleDirMode = refDir.equals(delDir);
        mReferenceDirectory = refDir;
        mDeleteDirectory = delDir;
        mDryRun = dryRun;
    }

    /**
     * Start the action.
     */
    private void start() {
        // pass 1 collect all ref sha1s.
        if (!mSingleDirMode) {
            fillRefData();
        }

        detectDelData();
    }

    private void detectDelData() {
        final DirTreeWalker delTreeWalker = new DirTreeWalker(mDeleteDirectory, new DirTreeListener() {
            public void file(File file) {
                if (file.getName().endsWith(".mp3") && file.length() > 1000) {
                    try {
                        final MusicBrainzMetadata mb = new MusicBrainzMetadata(file);
                        final String sha1;
                        if (!StringUtil.isBlankOrNull(mb.getSha1())) {
                            sha1 = mb.getSha1();
                        } else {
                            sha1 = Mp3Util.calcAudioFramesSha1(file);
                        }

                        if (mRefSha1s.containsKey(sha1)) {
                            if (mDryRun) {
                                System.out.println("rm \"" + file.getCanonicalPath() + "\"");
                                System.out.println("# dupe to " + mRefSha1s.get(sha1));
                            } else {
                                FileUtils.delete(file);
                            }
                        } else if (mSingleDirMode) {
                            mRefSha1s.put(sha1, file.getAbsolutePath());
                        }
                    } catch (IOException ex) {
                        System.err.println("Failed with '" + file + "' got " + ex + " will be ignored.");
                    } catch (RuntimeException ex) {
                        System.err.println("Failed with '" + file + "' got " + ex + " will be ignored.");
                    }
                }
            }

            public void exitingDir(File dir) {
                // TODO Auto-generated method stub
            }

            public void enteringDir(File dir) {
                // TODO Auto-generated method stub
            }
        });
        delTreeWalker.start();
    }

    private void fillRefData() {
        final DirTreeWalker refTreeWalker = new DirTreeWalker(mReferenceDirectory, new DirTreeListener() {
            public void file(File file) {
                if (file.getName().endsWith(".mp3") && file.length() > 1000) {
                    try {
                        final MusicBrainzMetadata mb = new MusicBrainzMetadata(file);
                        final String sha1;
                        if (!StringUtil.isBlankOrNull(mb.getSha1())) {
                            sha1 = mb.getSha1();
                        } else {
                            sha1 = Mp3Util.calcAudioFramesSha1(file);
                        }
                        // don't care for dupes here....
                        mRefSha1s.put(sha1, file.getAbsolutePath());
                    } catch (RuntimeException ex) {
                        System.err.println("Failed with '" + file + "' got " + ex + " will be ignored.");
                    }
                }
            }

            public void exitingDir(File dir) {
                // TODO Auto-generated method stub
            }

            public void enteringDir(File dir) {
                // TODO Auto-generated method stub
            }
        });
        refTreeWalker.start();
    }
}
