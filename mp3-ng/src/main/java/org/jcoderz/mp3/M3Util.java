package org.jcoderz.mp3;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.jcoderz.mp3.intern.db.DatabaseUpdater;
import org.jcoderz.mp3.intern.lucene.LuceneUpdater;
import org.jcoderz.mp3.intern.types.TagQuality;

public class M3Util {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// TODO Auto-generated method stub
			System.out.println("Usage: ....");
		} else {
			String cmd = args[1].toLowerCase();
			if (args.length > 1) {
				String[] remaining = new String[args.length - 1];
				System.arraycopy(args, 1, remaining, 0, args.length - 1);
				switch (cmd) {
				case "create": {
					OptionParser parser = new OptionParser("h:");
					OptionSet options = parser.parse(remaining);

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
					break;
				}
			}
		}
	}
}
