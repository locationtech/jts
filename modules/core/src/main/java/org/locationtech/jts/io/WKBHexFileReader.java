/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

/**
 * Reads a sequence of {@link Geometry}s in WKBHex format 
 * from a text file.
 * Each WKBHex geometry must be on a single line
 * The geometries in the file may be separated by any amount
 * of whitespace and newlines.
 * 
 * @author Martin Davis
 *
 */
public class WKBHexFileReader 
{
	private File file = null;
  private Reader reader;
	private WKBReader wkbReader;
	private int count = 0;
	private int limit = -1;
	private int offset = 0;
	
  /**
   * Creates a new <tt>WKBHexFileReader</tt> given the <tt>File</tt> to read from 
   * and a <tt>WKTReader</tt> to use to parse the geometries.
   * 
   * @param file the <tt>File</tt> to read from
   * @param wkbReader the geometry reader to use
   */
	public WKBHexFileReader(File file, WKBReader wkbReader)
	{
		this.file = file;
    this.wkbReader = wkbReader;
	}
	
  /**
   * Creates a new <tt>WKBHexFileReader</tt>, given the name of the file to read from.
   * 
   * @param filename the name of the file to read from
   * @param wkbReader the geometry reader to use
   */
  public WKBHexFileReader(String filename, WKBReader wkbReader)
  {
    this(new File(filename), wkbReader);
  }
  
  /**
   * Creates a new <tt>WKBHexFileReader</tt>, given a {@link Reader} to read from.
   * 
   * @param reader the reader to read from
   * @param wkbReader the geometry reader to use
   */
  public WKBHexFileReader(Reader reader, WKBReader wkbReader)
  {
    this.reader = reader;
    this.wkbReader = wkbReader;
  }
  
	/**
	 * Sets the maximum number of geometries to read.
   * 
	 * @param limit the maximum number of geometries to read
	 */
	public void setLimit(int limit)
	{
		this.limit = limit;
	}
	
	/**
	 * Sets the number of geometries to skip before storing.
   * 
	 * @param offset the number of geometries to skip
	 */
	public void setOffset(int offset)
	{
		this.offset = offset;
	}
	
	/**
	 * Reads a sequence of geometries.
	 * If an offset is specified, geometries read up to the offset count are skipped.
	 * If a limit is specified, no more than <tt>limit</tt> geometries are read.
	 * 
	 * @return the list of geometries read
	 * @throws IOException if an I/O exception was encountered
	 * @throws ParseException if an error occured reading a geometry
	 */
	public List read() 
	throws IOException, ParseException 
	{
    // do this here so that constructors don't throw exceptions
    if (file != null)
      reader = new FileReader(file);
    
		count = 0;
		try {
			BufferedReader bufferedReader = new BufferedReader(reader);
			try {
				return read(bufferedReader);
			} finally {
				bufferedReader.close();
			}
		} finally {
			reader.close();
		}
	}
	
	private List read(BufferedReader bufferedReader) throws IOException,
			ParseException {
		List geoms = new ArrayList();
		while (! isAtEndOfFile(bufferedReader) && ! isAtLimit(geoms)) {
		  String line = bufferedReader.readLine().trim();
		  if (line.length() == 0) 
		    continue;
			Geometry g = wkbReader.read(WKBReader.hexToBytes(line));
			if (count >= offset)
				geoms.add(g);
			count++;
		}
		return geoms;
	}
	
	private boolean isAtLimit(List geoms)
	{
		if (limit < 0) return false;
		if (geoms.size() < limit) return false;
		return true;
	}
	
  private static final int MAX_LOOKAHEAD = 1000;
  
  /**
	 * Tests if reader is at EOF.
	 */
	private boolean isAtEndOfFile(BufferedReader bufferedReader)
			throws IOException 
			{
		bufferedReader.mark(MAX_LOOKAHEAD);

		StreamTokenizer tokenizer = new StreamTokenizer(bufferedReader);
		int type = tokenizer.nextToken();

		if (type == StreamTokenizer.TT_EOF) {
			return true;
		}
		bufferedReader.reset();
		return false;
	}
}
