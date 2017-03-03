/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipHelper
{

   /**
    * Size of the buffer to read/write data
    */
   private static final int BUFFER_SIZE = 8192;

   /**
    * Extracts a zip file specified by the zipFilePath to a directory specified by
    * destDirectory (will be created if does not exists)
    */
   public static void unzip(File file, File destDir) throws IOException
   {
      if (!destDir.exists())
      {
         destDir.mkdir();
      }
      ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file));
      ZipEntry entry = zipIn.getNextEntry();
      // iterates over entries in the zip file
      while (entry != null)
      {
         File entryFile = new File(destDir, entry.getName());
         if (!entry.isDirectory())
         {
            // if the entry is a file, extracts it
            extractFile(zipIn, entryFile);
         }
         else
         {
            // if the entry is a directory, make the directory
            entryFile.mkdir();
         }
         zipIn.closeEntry();
         entry = zipIn.getNextEntry();
      }
      zipIn.close();
   }

   private static void extractFile(ZipInputStream zipIn, File file) throws IOException
   {
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
      byte[] bytesIn = new byte[BUFFER_SIZE];
      int read;
      while ((read = zipIn.read(bytesIn)) != -1)
      {
         bos.write(bytesIn, 0, read);
      }
      bos.close();
   }

}
