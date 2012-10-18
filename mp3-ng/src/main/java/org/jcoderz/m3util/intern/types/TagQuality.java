package org.jcoderz.mp3.intern.types;

/**
 * This enumeration defines the different tag quality levels.
 * 
 * @author amandel
 * @author mrumpf
 * 
 */
public enum TagQuality {
	/**
	 * High-quality tags, tagged with data from MusicBrainz.
	 */
	GOLD("gold", "01"),
	/**
	 * Medium-quality tags, tagged via freedb or other sources.
	 */
	SILVER("silver", "02"),
	/**
	 * Low-quality tags or maybe not tagged at all.
	 */
	BRONZE("bronze", "03");

	public final static String REGEX_PATTERN;
	private final String mQuality;
	private final String mPrefix;

	static {
		StringBuilder sb = new StringBuilder();
		TagQuality[] tagQuality = TagQuality.values();
		for (int i = 0; i < tagQuality.length; i++) {
			sb.append(tagQuality[i].getSubdir());
			if (i < tagQuality.length - 1) {
				sb.append('|');
			}
		}
		REGEX_PATTERN = sb.toString();
	}

	TagQuality(String quality, String prefix) {
		mQuality = quality;
		mPrefix = prefix;
	}

	public String getSubdir() {
		return mPrefix + "-" + mQuality;
	}
}
