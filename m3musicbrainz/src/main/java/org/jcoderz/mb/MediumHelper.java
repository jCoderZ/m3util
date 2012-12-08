package org.jcoderz.mb;

import org.jcoderz.mb.type.Medium;
import org.jcoderz.mb.type.Release;

/**
 *
 * @author Andreas Mandel
 */
public class MediumHelper
{

    public static String buildAlbumTitle(Release album, Medium medium)
    {
        StringBuilder result = new StringBuilder();
        String albumTitle = album.getTitle();
        if (albumTitle == null || albumTitle.isEmpty())
        {
            albumTitle = medium.getTitle();
        }
        result.append(albumTitle);

        String mediumTitle = medium.getTitle();
        if (mediumTitle == null || mediumTitle.equals(albumTitle))
        {
            mediumTitle = "";
        }
        if (album.getMediumList().getCount().intValue() > 1)
        {
            result.append(" (disc ");
            result.append(medium.getPosition());
            if (!mediumTitle.isEmpty())
            {
                result.append(": ");
                result.append(mediumTitle);
            }
            result.append(')');
        }
        return result.toString();
    }
}
