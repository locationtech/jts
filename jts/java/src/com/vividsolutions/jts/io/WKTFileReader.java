package com.vividsolutions.jts.io;

import java.io.*;
import java.util.*;
import com.vividsolutions.jts.geom.*;

/**
 * Reads a sequence of {@link Geometry}s in WKT format 
 * from a text file.
 * The geometries in the file may be separated by any amount
 * of whitespace and newlines.
 * 
 * @author Martin Davis
 *
 */
public class WKTFileReader 
{
	private File file = null;
  private Reader reader;
//  private Reader fileReader = new FileReader(file);
	private WKTReader wktReader;
	private int count = 0;
	private int limit = -1;
	private int offset = 0;
	
  /**
   * Creates a new <tt>WKTFileReader</tt> given the <tt>File</tt> to read from 
   * and a <tt>WKTReader</tt> to use to parse the geometries.
   * 
   * @param file the <tt>File</tt> to read from
   * @param wktReader the geometry reader to use
   */
	public WKTFileReader(File file, WKTReader wktReader)
	{
		this.file = file;
    this.wktReader = wktReader;
	}
	
  /**
   * Creates a new <tt>WKTFileReader</tt>, given the name of the file to read from.
   * 
   * @param filename the name of the file to read from
   * @param wktReader the geometry reader to use
   */
  public WKTFileReader(String filename, WKTReader wktReader)
  {
    this(new File(filename), wktReader);
  }
  
  /**
   * Creates a new <tt>WKTFileReader</tt>, given a {@link Reader} to read from.
   * 
   * @param reader the reader to read from
   * @param wktReader the geometry reader to use
   */
  public WKTFileReader(Reader reader, WKTReader wktReader)
  {
    this.reader = reader;
    this.wktReader = wktReader;
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
			Geometry g = wktReader.read(bufferedReader);
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
