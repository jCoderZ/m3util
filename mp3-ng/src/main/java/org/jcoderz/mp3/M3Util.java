package org.jcoderz.mp3;

import java.io.File;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

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
		sb.append("create: Create a new media library folder structure");
		sb.append("usage: " + M3Util.class.getSimpleName().toLowerCase()
				+ " create [options] [args]");
		sb.append("	 TODO: Detailed Description");
		sb.append("Valid options:");
		sb.append("	 ...");
		System.out.println(sb.toString());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			usage();
		} else {
			String cmd = args[1].toLowerCase();
			if (args.length > 1) {
				String[] remaining = new String[args.length - 1];
				System.arraycopy(args, 1, remaining, 0, args.length - 1);
				switch (cmd) {
				case "create": {
					OptionParser parser = new OptionParser();
					parser.accepts("help");
					parser.accepts("h");
					OptionSet options = parser.parse(remaining);
					if (options.has("help") || options.has("h")) {
						usageCreate();
					}
					else {
						LibraryInitiator li = new LibraryInitiator();
						// TODO: parse the home folder or use the environment
						li.create();
					}
				}
					break;
				case "index": {
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
					break;

				case "id": {
					OptionParser parser = new OptionParser("h:");
					OptionSet options = parser.parse(remaining);
					IdAdder ia = new IdAdder();
					ia.fillRefData(new File(remaining[0]));
				}
					break;

				case "refresh": {
					// TODO RefreshRepository
				}
					break;

				case "rename": {
					// TODO PlainRename
				}
					break;

				default:
					usage();
					break;
				}
			}
		}
	}
}
