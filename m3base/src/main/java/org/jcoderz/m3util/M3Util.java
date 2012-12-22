package org.jcoderz.m3util;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.jcoderz.m3util.intern.IdAdder;
import org.jcoderz.m3util.intern.LibraryInitiator;
import org.jcoderz.m3util.intern.Sha1DupeChecker;
import org.jcoderz.m3util.intern.db.DatabaseUpdater;
import org.jcoderz.m3util.intern.lucene.LuceneUpdater;
import org.jcoderz.m3util.intern.types.TagQuality;

/**
 * This is the m3util command line tool which integrates all other command line
 * tools from this project.
 *
 * The <a
 * href="http://pholser.github.com/jopt-simple/examples.html">jopt-simple</a>
 * parsing library is used for parameter parsing.
 *
 * @author mrumpf
 *
 */
public class M3Util {

    private static final String LOGGING_PROPERTIES = "/org/jcoderz/mp3/logging.properties";

    static {
        try {
            final InputStream inputStream = M3Util.class.getResourceAsStream(LOGGING_PROPERTIES);
            if (inputStream == null) {
                throw new RuntimeException("Cannot find '" + LOGGING_PROPERTIES + "'.");
            }
            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private static final Logger logger = Logger.getLogger(M3Util.class.getName());
    private static final List<String> HELP_OPTIONS = Arrays.asList("help", "h");

    private static void usage() {
        StringBuilder sb = new StringBuilder();
        sb.append("usage: " + M3Util.class.getSimpleName().toLowerCase()
                + " <subcommand> [options] [args]\n");
        sb.append("    " + M3Util.class.getSimpleName().toLowerCase()
                + " command-line client\n");
        sb.append("    Type '" + M3Util.class.getSimpleName().toLowerCase()
                + " <subcommand> --help' for help on a specific subcommand.\n");
        sb.append("\n");
        sb.append("    Available subcommands:\n");
        sb.append("        create\n");
        sb.append("        index\n");
        sb.append("        id\n");
        sb.append("        refresh\n");
        sb.append("        dupecheck\n");
        sb.append("        rename\n");
        System.out.println(sb.toString());
    }

    private static void usageCreate() {
        StringBuilder sb = new StringBuilder();
        sb.append("create: Creates a new media library folder structure.\n");
        sb.append("        If the root option is not specified, the library\n");
        sb.append("        will be created in the current working directory.\n");
        sb.append("usage: ");
        sb.append(M3Util.class.getSimpleName().toLowerCase());
        sb.append("\n create [options]\n");
        sb.append("	 -r|--root <ROOT_FOLDER>\n");
        System.out.println(sb.toString());
    }

    private static void usageIndex() {
        StringBuilder sb = new StringBuilder();
        sb.append("index: Creates the database and the lucene index.\n");
        sb.append("       If the quality option is not specified, the index\n");
        sb.append("       will be created for all three quality levels.\n");
        sb.append("usage: ");
        sb.append(M3Util.class.getSimpleName().toLowerCase());
        sb.append("\n index [options]\n");
        sb.append("	 -q|--quality <GOLD|SILVER|BRONZE>  --  the tag quality to index\n");
        sb.append("	 -l|--lucene  --  create Lucene index only\n");
        sb.append("	 -d|--database  --  create database index only\n");
        System.out.println(sb.toString());
    }

    private static void usageId() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: ...\n");
        sb.append("usage: ");
        sb.append(M3Util.class.getSimpleName().toLowerCase());
        sb.append("\n id [options] [args]\n");
        sb.append("	 TODO\n");
        System.out.println(sb.toString());
    }

    private static void usageRefresh() {
        StringBuilder sb = new StringBuilder();
        sb.append("refresh: ...\n");
        sb.append("usage: ");
        sb.append(M3Util.class.getSimpleName().toLowerCase());
        sb.append("\n refresh [options] [args]\n");
        sb.append("	 TODO\n");
        System.out.println(sb.toString());
    }

    private static void usageDupeCheck() {
        StringBuilder sb = new StringBuilder();
        sb.append("dupecheck: Checks a folder tree of files against the library\n");
        sb.append("           by comparing each file of the source folder against\n");
        sb.append("           the SHA1 tag that is stored in all library files.\n");
        sb.append("usage: ");
        sb.append(M3Util.class.getSimpleName().toLowerCase());
        sb.append("\n dupecheck [options]\n");
        // TODO: Use the Environment.getAudioFolder() instead
        sb.append("	 -l|--library - the library folder\n");
        sb.append("	 -d|--dupes - the folder to check for duplicates\n");
        sb.append("	 -y|--dryrun - run the command without deleting the duplicate files\n");
        System.out.println(sb.toString());
    }

    private static void usageRename() {
        StringBuilder sb = new StringBuilder();
        sb.append("rename: ...\n");
        sb.append("usage: ");
        sb.append(M3Util.class.getSimpleName().toLowerCase());
        sb.append("\n rename [options] [args]\n");
        sb.append("	 TODO\n");
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
        logger.info("M3Util");
        if (args.length == 0) {
            usage();
        } else {
            if (args.length >= 1) {
                String cmd = args[0].toLowerCase();
                String[] remaining = new String[args.length - 1];
                System.arraycopy(args, 1, remaining, 0, args.length - 1);
                switch (cmd) {
                    case "create": {
                        handleCreate(remaining);
                    }
                    break;

                    case "index": {
                        handleIndex(remaining);
                    }
                    break;

                    case "id": {
                        handleId(remaining);
                    }
                    break;

                    case "refresh": {
                        handleRefresh(remaining);
                    }
                    break;

                    case "dupecheck": {
                        handleDupeCheck(remaining);
                    }
                    break;

                    case "rename": {
                        handleRename(remaining);
                    }
                    break;

                    default:
                        usage();
                        break;
                }
            }
        }
    }

    private static void handleCreate(String[] args) {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(HELP_OPTIONS);
        try {
            List<String> root = Arrays.asList("root", "r");
            OptionSpec<File> rootFile = parser.acceptsAll(root).withRequiredArg().ofType(File.class);
            OptionSet options = parser.parse(args);
            if (options.has("help") || options.has("h")) {
                usageCreate();
            } else {
                LibraryInitiator li = new LibraryInitiator();
                li.create(rootFile.value(options));
            }
        } catch (OptionException ex) {
            usageCreate();
        }
    }

    private static void handleIndex(String[] args) {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(HELP_OPTIONS);
        parser.acceptsAll(Arrays.asList("lucene", "l"));
        parser.acceptsAll(Arrays.asList("database", "d"));
        try {
            List<String> quality = Arrays.asList("quality", "q");
            OptionSpec<TagQuality> tagQuality = parser.acceptsAll(quality).withRequiredArg().ofType(TagQuality.class);
            OptionSet options = parser.parse(args);
            if (options.has("help") || options.has("h")) {
                usageIndex();
            } else {
                boolean lucene = options.has("lucene") || options.has("l");
                boolean database = options.has("database") || options.has("d");
                if (lucene || (!lucene && !database)) {
                    LuceneUpdater updateLucene = new LuceneUpdater();
                    try {
                        if (!options.has(tagQuality)) {
                            updateLucene.refresh();
                        } else {
                            updateLucene.refresh(tagQuality.value(options));
                        }
                    } finally {
                        updateLucene.close();
                    }
                }
                if (database || (!lucene && !database)) {
                    DatabaseUpdater updateDb = new DatabaseUpdater();
                    try {
                        if (!options.has(tagQuality)) {
                            updateDb.refresh();
                        } else {
                            updateDb.refresh(tagQuality.value(options));
                        }
                    } finally {
                        updateDb.close();
                    }
                }
            }
        } catch (OptionException ex) {
            usageIndex();
        }
    }

    private static void handleId(String[] args) {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(HELP_OPTIONS);
        try {
            OptionSet options = parser.parse(args);
            if (options.has("help") || options.has("h") || !options.hasOptions()) {
                usageId();
            } else {
                IdAdder ia = new IdAdder();
                ia.fillRefData(new File(args[0]));
            }
        } catch (OptionException ex) {
            usageId();
        }
    }

    private static void handleDupeCheck(String[] args) {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(HELP_OPTIONS);
        try {
            List<String> dryrunOptions = Arrays.asList("dryrun", "y");
            parser.acceptsAll(dryrunOptions);
            List<String> libraryOptions = Arrays.asList("library", "l");
            OptionSpec<File> libraryRootFolder = parser.acceptsAll(libraryOptions).withRequiredArg().ofType(File.class);
            List<String> dupesOptions = Arrays.asList("dupes", "d");
            OptionSpec<File> dupesRootFolder = parser.acceptsAll(dupesOptions).withRequiredArg().ofType(File.class);
            OptionSet options = parser.parse(args);
            if (options.has("help") || options.has("h") || !options.hasOptions()) {
                usageDupeCheck();
            } else {
                boolean dryrun = (options.has("dryrun") || options.has("y"));
                File src = libraryRootFolder.value(options);
                File dst = dupesRootFolder.value(options);
                Sha1DupeChecker dc = new Sha1DupeChecker(src, dst, dryrun);
                dc.start();
            }
        } catch (OptionException ex) {
            usageDupeCheck();
        }
    }

    private static void handleRefresh(String[] args) {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(HELP_OPTIONS);
        try {
            OptionSet options = parser.parse(args);
            if (options.has("help") || options.has("h") || !options.hasOptions()) {
                usageRefresh();
            } else {
                // TODO
            }
        } catch (OptionException ex) {
            usageRefresh();
        }
    }

    private static void handleRename(String[] args) {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(HELP_OPTIONS);
        try {
            OptionSet options = parser.parse(args);
            if (options.has("help") || options.has("h") || !options.hasOptions()) {
                usageRename();
            } else {
                // TODO
            }
        } catch (OptionException ex) {
            usageRename();
        }
    }
}
