/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.util;

import java.io.*;
import java.util.*;

/**
 * Useful file utilities.
 *
 * @version 1.7
 */
public class FileUtil 
{
  public static final String EXTENSION_SEPARATOR = ".";

  public static String name(String path)
  {
    File file = new File(path);
    return file.getName();
  }

  public static String extension(String path)
  {
    String name = name(path);
    int extIndex = name.lastIndexOf(EXTENSION_SEPARATOR.charAt(0));
    if (extIndex < 0) return "";
    return name.substring(extIndex, name.length());
  }

    /**
     * Deletes the files in the directory, but does not remove the directory.
     */
    public static void deleteFiles(String directoryName) {
        File dir = new File(directoryName);
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
    }

    /**
     * Returns true if the given directory exists.
     */
    public static boolean directoryExists(String directoryName) {
        File directory = new File(directoryName);
        return directory.exists();
    }

    /**
     * Returns a List of the String's in the text file, one per line.
     */
    public static List getContents(String textFileName) throws FileNotFoundException, IOException {
        List contents = new Vector();
        FileReader fileReader = new FileReader(textFileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = bufferedReader.readLine();
        while (line != null) {
            contents.add(line);
            line = bufferedReader.readLine();
        }
        return contents;
    }
 
    public static String readText(String filename) 
    throws IOException 
    {
      return readText(new File(filename));
    }
    
    /**
     * Gets the contents of a text file as a single String
     * @param file
     * @return text file contents
     * @throws IOException
     */
 public static String readText(File file) 
  	throws IOException 
  	{
		String thisLine;
		StringBuffer strb = new StringBuffer("");

		FileInputStream fin = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fin));
		while ((thisLine = br.readLine()) != null) {
			strb.append(thisLine + "\r\n");
		}
		String result = strb.toString();
		return result;
	}

    /**
		 * Saves the String with the given filename
		 */
    public static void setContents(String textFileName, String contents) throws IOException {
        FileWriter fileWriter = new FileWriter(textFileName, false);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(contents);
        bufferedWriter.flush();
        bufferedWriter.close();
        fileWriter.close();
    }

    /**
     * Copies the source file to the destination filename.
     * Posted by Mark Thornton <mthorn@cix.compulink.co.uk> on Usenet.
     */
    public static void copyFile(File source, File destination) throws IOException {
        RandomAccessFile out = new RandomAccessFile(destination, "rw");
        //Tell the OS in advance how big the file will be. This may reduce fragmentation
        out.setLength(source.length());
        //copy the content
        FileInputStream in = new FileInputStream(source);
        byte[] buffer = new byte[16384];
        while (true) {
            int n = in.read(buffer);
            if (n == -1)
                break;
            out.write(buffer, 0, n);
        }
        in.close();
        out.close();
    }
}
