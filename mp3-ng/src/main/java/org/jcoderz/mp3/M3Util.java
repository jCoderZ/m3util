package org.jcoderz.mp3;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import joptsimple.OptionException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.jcoderz.mp3.intern.IdAdder;
import org.jcoderz.mp3.intern.LibraryInitiator;
import org.jcoderz.mp3.intern.db.DatabaseUpdater;
import org.jcoderz.mp3.intern.lucene.LuceneUpdater;
import org.jcoderz.mp3.intern.types.TagQuality;

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
        sb.append("        rename\n");
        System.out.println(sb.toString());
    }

    private static void usageCreate() {
        StringBuilder sb = new StringBuilder();
        sb.append("create: Create a new media library folder structure\n");
        sb.append("usage: ");
        sb.append(M3Util.class.getSimpleName().toLowerCase());
        sb.append("\n create [options] [args]\n");
        sb.append("	 -r|--root <ROOT_FOLDER>\n");
        System.out.println(sb.toString());
    }

    private static void usageIndex() {
        StringBuilder sb = new StringBuilder();
        sb.append("index: ...\n");
        sb.append("usage: ");
        sb.append(M3Util.class.getSimpleName().toLowerCase());
        sb.append("\n index [options] [args]\n");
        sb.append("	 ???\n");
        System.out.println(sb.toString());
    }

    private static void usageId() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: ...\n");
        sb.append("usage: ");
        sb.append(M3Util.class.getSimpleName().toLowerCase());
        sb.append("\n id [options] [args]\n");
        sb.append("	 ???\n");
        System.out.println(sb.toString());
    }

    private static void usageRefresh() {
        StringBuilder sb = new StringBuilder();
        sb.append("refresh: ...\n");
        sb.append("usage: ");
        sb.append(M3Util.class.getSimpleName().toLowerCase());
        sb.append("\n refresh [options] [args]\n");
        sb.append("	 ???\n");
        System.out.println(sb.toString());
    }

    private static void usageRename() {
        StringBuilder sb = new StringBuilder();
        sb.append("rename: ...\n");
        sb.append("usage: ");
        sb.append(M3Util.class.getSimpleName().toLowerCase());
        sb.append("\n rename [options] [args]\n");
        sb.append("	 ???\n");
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
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
            if (options.has("help") || options.has("h") || !options.hasOptions()) {
                usageCreate();
            } else {
                LibraryInitiator li = new LibraryInitiator();
                // TODO: parse the home folder or use the environment
                li.create(rootFile.value(options));
            }
        } catch (OptionException ex) {
            usageCreate();
        }
    }

    private static void handleIndex(String[] args) {
        // TODO: call refresh depending on parameters
        // OptionParser parser = new OptionParser("h:");
        // OptionSet options = parser.parse(remaining);
        LuceneUpdater updateLucene = new LuceneUpdater();
        updateLucene.refresh(TagQuality.GOLD);
        updateLucene.refresh(TagQuality.SILVER);
        updateLucene.refresh(TagQuality.BRONZE);

        DatabaseUpdater updateDb = new DatabaseUpdater();
        updateDb.refresh(TagQuality.GOLD);
        updateDb.refresh(TagQuality.SILVER);
        updateDb.refresh(TagQuality.BRONZE);
    }

    private static void handleId(String[] args) {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(HELP_OPTIONS);
        try {
            OptionSet options = parser.parse(args);
            if (options.has("help") || options.has("h") || !options.hasOptions()) {
                usageRefresh();
            } else {
                IdAdder ia = new IdAdder();
                ia.fillRefData(new File(args[0]));
            }
        } catch (OptionException ex) {
            usageRefresh();
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
