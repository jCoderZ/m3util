package org.jcoderz.mp3;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.jcoderz.mp3.intern.LibraryInitiator;
import org.jcoderz.mp3.intern.db.DatabaseUpdater;
import org.jcoderz.mp3.intern.lucene.LuceneUpdater;
import org.jcoderz.mp3.intern.types.TagQuality;

/**
 * 
 * <ul>
 * <li>http://pholser.github.com/jopt-simple/examples.html</li>
 * </ul>
 * 
 * @author mrumpf
 * 
 */
public class M3Util {

	private static void usage() {
		StringBuilder sb = new StringBuilder();
		sb.append("usage: " + M3Util.class.getName().toLowerCase()
				+ " <subcommand> [options] [args]");
		sb.append("		" + M3Util.class.getName().toLowerCase()
				+ " command-line client");
		sb.append("		Type '" + M3Util.class.getName().toLowerCase()
				+ " help <subcommand>' for help on a specific subcommand.");
		sb.append("		  or '" + M3Util.class.getName().toLowerCase()
				+ " --version' to see the version number.");
		sb.append("");
		sb.append("		Available subcommands:");
		sb.append("			create");
		sb.append("			index");
		System.out.println(sb.toString());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO: Add OptionParser for --help, -h, -?, -
		if (args.length == 0) {
			usage();
		} else {
			String cmd = args[1].toLowerCase();
			if (args.length > 1) {
				String[] remaining = new String[args.length - 1];
				System.arraycopy(args, 1, remaining, 0, args.length - 1);
				switch (cmd) {
				case "create": {
					OptionParser parser = new OptionParser("h:");
					OptionSet options = parser.parse(remaining);
					LibraryInitiator li = new LibraryInitiator();
					// TODO: parse the home folder or use the environemtn
					li.create();
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

				default:
					usage();
					break;
				}
			}
		}
	}
}
