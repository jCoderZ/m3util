package org.jcoderz.mb;

import org.jcoderz.mb.type.ArtistCredit;
import org.jcoderz.mb.type.Medium.TrackList.Track;

/**
 * Helper class to allow access to effective track data.
 * This might be either stored with the recording, or in a 
 * individually for the track.
 * 
 * <p>There might be different titles in the track of the album vs in the recording. 
 * Eg: http://musicbrainz.org/release/58da2396-81a6-4e85-bab9-e623d42840bd
 * http://musicbrainz.org/recording/0ca12a54-b5b1-4029-b4eb-4824eb210845
 * <i>Will You Still Love Me Tomorrow</i>  ->  <i>Will You Love Me Tomorrow</i>.</p>
 * 
 * @author Andreas Mandel
 *
 */
public final class TrackHelper 
{
	public static String getTitle(Track track)
	{
		final String title;
		if (track.getTitle() == null)
		{
			title = track.getRecording().getTitle();
		}
		else
		{
			title = track.getTitle();
		}
		return title;
	}
	
	public static Long getLength(Track track)
	{
		final Long length;
		if (track.getLength() == null)
		{
			length = track.getRecording() == null 
					? null : track.getRecording().getLength() == null 
						? null : track.getRecording().getLength().longValue();
		}
		else
		{
			length = track.getLength().longValue();
		}
		return length;
	}
	
	public static ArtistCredit getArtistCredit(Track track)
	{
		final ArtistCredit artistCredit;
		if (track.getArtistCredit() == null)
		{
			artistCredit = track.getRecording().getArtistCredit();
		}
		else
		{
			artistCredit = track.getArtistCredit();
		}
		return artistCredit;
	}
	
}
