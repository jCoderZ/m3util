
package org.jcoderz.mp3.intern.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jcoderz.commons.util.IoUtil;
import org.jcoderz.commons.util.StringUtil;

/**
 * File utility methods, could partly be moved to fawkez
 * 
 * @author Andreas Mandel
 *
 */
public final class FileUtil
{
   /** Max length of a filename component */
   public static final int MAX_FILENAME_LENGTH = 100;
   
   private static final String CLASSNAME = FileUtil.class.getName();
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private FileUtil ()
   {
      // no instances
   }

   public static String fixPath (String filename)
   {
      String resultStr = fixFilename(filename);
      while (resultStr.endsWith("."))
      {
         resultStr = resultStr.substring(0, resultStr.length() - 1);
         resultStr = resultStr.trim();
      }
      while (resultStr.startsWith("."))
      {
         resultStr = resultStr.substring(1);
         resultStr = resultStr.trim();
      }
      return resultStr;
   }

   public static String fixFilename (String filename)
   {
      final StringBuffer result = new StringBuffer();
      if (filename != null)
      {
         for (int i = 0; i < filename.length(); i++)
         {
            char c = filename.charAt(i);
            switch (c)
            {
               case '<':
                  result.append('(');
                  break;
               case '>':
                  result.append(')');
                  break;
               case '\\':
               case '/':
               case '|':
               case '=':
               case '*':
                  result.append('-');
                  break;
               case '"':
                  result.append('\'');
                  break;
               case ':':
                  result.append('~');
                  break;
               case ';':
               case '?':
                  break;
               default:
                  result.append(c);
            }
         }
      }
      String resultStr = result.toString().trim();
      
      resultStr = resultStr.replaceAll(" [ ]+", " ");
      
      
      if (resultStr.length() > MAX_FILENAME_LENGTH)
      {
         // save Volume / disc info...
         // pattern is:
         // xxx, Volume 7 The Plants (disc 1: Trees)
         // 
         
         // we might be more strategic in a upcoming version..
         String start = resultStr.toString();
         final StringBuffer end = new StringBuffer();
         if (resultStr.matches("(.*)(, Volume [0-9]+)(.*)"))
         {
            Pattern pattern = Pattern.compile(
                  "(.*)(, Volume [0-9]+)( .*)?");
            Matcher matcher = pattern.matcher(resultStr);
            if (matcher.matches())
            {
               end.append(matcher.group(2));
               start = matcher.group(1);
            }
         }
         if (resultStr.matches("(.*)( \\(disc [0-9]+)(.*)"))
         {
            Pattern pattern = Pattern.compile("(.*)( \\(disc [0-9]+)(.*)");
            Matcher matcher = pattern.matcher(resultStr);
            if (matcher.matches())
            {
               String start2 = matcher.group(1);
               if (start2.length() < start.length())
               {
                  start = start2;
               }
               
               if ((start.length() + 10) < MAX_FILENAME_LENGTH - end.length()
                     && matcher.group(3) != null)
               {  // space for the disc text!
                  end.append(matcher.group(2));
                  final String discDescription = matcher.group(3).trim();
                  final int remainingSpace 
                        = MAX_FILENAME_LENGTH - end.length() - start.length();
                  
                  if (discDescription.length() > remainingSpace)
                  {
                     end.append(
                           StringUtil.trimLength(
                              discDescription, remainingSpace - 2));
                     end.append("\u2026)");
                  }
                  else
                  {
                     end.append(discDescription);
                  }
               }
               else
               {
                  end.append(matcher.group(2));
                  end.append(")");
               }

            }
         }
         int remainder = MAX_FILENAME_LENGTH - end.length();
         if (start.length() > remainder)
         {
            start = start.substring(0, remainder - 1).trim() 
                  + "\u2026";
         }
         resultStr = start + end;
      }
      return resultStr;
   }

   public static void deleteDirIfEmpty (File file)
   {
      if (file.getParentFile().isDirectory()
            && file.getParentFile().listFiles().length == 0)
      {
         if (!file.getParentFile().delete())
         {
            logger.warning("Failed to delete empty dir "
                  + file.getParentFile() + ".");
         }
         else
         {
            deleteDirIfEmpty(file.getParentFile());
         }
      }
   }

   public static boolean moveFile (File file, File targetDir, String filename)
   {
      boolean result = false;
      final File target = new File(targetDir, filename);
      
      final long lastModified = file.lastModified();
      
      if (target.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath())
            && !target.getAbsolutePath().equals(file.getAbsolutePath()))
      {
         // case change on a case insensitive fs...
         final File tmpTarget = new File(targetDir, "tmp." + filename);
         result = file.renameTo(tmpTarget);
         if (result)
         {
            result = tmpTarget.renameTo(target);
            if (!result)
            {
               tmpTarget.renameTo(file); // try to rename back...
            }
            if (lastModified > 0)
            {
                file.setLastModified(lastModified);
            }
         }
         if (!result)
         {
            logger.log(Level.WARNING,
                  "Failed to (case) move '" + file.getAbsolutePath()
                  + "' to '" + target.getAbsolutePath());
         }
      }
      else if (!target.exists())
      {
         if (!targetDir.exists() && !targetDir.mkdirs())
         {
             logger.log(Level.WARNING,
                 "Failed to create dir '" + targetDir.getAbsolutePath()
                 + "'.");
           result = false;
           return result;
         }
         result = file.renameTo(target);
         if (!result)
         {
            try
            {
               // try a copy...
               IoUtil.copy(file, target);
               result = true;
               target.setLastModified(lastModified);
               
            }
            catch (IOException ex)
            {
               logger.log(Level.WARNING,
                     "Failed to copy '" + file.getAbsolutePath()
                     + "' to '" + target.getAbsolutePath(), ex);
               result = false;
            }
            if (result)
            {
               if (!file.delete())
               {
                  logger.warning("Failed to delete source after copy " + file);
               }
            }
         }
         else
         {
             if (lastModified > 0)
             {
                 target.setLastModified(lastModified);
             }
         }
      }
      else
      {
         logger.fine(
               "Target file already exists: " + target.getAbsolutePath());
      }
      return result;
   }

   /**
    * @param dir
    * @param file
    * @return
    */
   public static boolean moveDir (File dir, File target)
   {
      target.getParentFile().mkdirs(); // create dirs up to the target dir;
      return dir.renameTo(target);
   }

}
