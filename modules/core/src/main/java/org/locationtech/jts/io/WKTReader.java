/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io;


import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.AssertionFailedException;

/**
 * Converts a geometry in Well-Known Text format to a {@link Geometry}.
 * <p>
 * <code>WKTReader</code> supports
 * extracting <code>Geometry</code> objects from either {@link Reader}s or
 *  {@link String}s. This allows it to function as a parser to read <code>Geometry</code>
 *  objects from text blocks embedded in other data formats (e.g. XML). <P>
 * <p>
 *  A <code>WKTReader</code> is parameterized by a <code>GeometryFactory</code>,
 *  to allow it to create <code>Geometry</code> objects of the appropriate
 *  implementation. In particular, the <code>GeometryFactory</code>
 *  determines the <code>PrecisionModel</code> and <code>SRID</code> that is
 *  used. <P>
 *
 *  The <code>WKTReader</code> converts all input numbers to the precise
 *  internal representation.
 *
 * <h3>Notes:</h3>
 * <ul>
 * <li>Keywords are case-insensitive.
 * <li>The reader supports non-standard "LINEARRING" tags.
 * <li>The reader uses <tt>Double.parseDouble</tt> to perform the conversion of ASCII
 * numbers to floating point.  This means it supports the Java
 * syntax for floating point literals (including scientific notation).
 * </ul>
 *
 * <h3>Syntax</h3>
 * The following syntax specification describes the version of Well-Known Text
 * supported by JTS.
 * (The specification uses a syntax language similar to that used in
 * the C and Java language specifications.)
 * <p>
 * As of version 1.15, JTS can read (but not write) WKT Strings including Z, M or ZM
 * in the name of the geometry type (ex. POINT Z, LINESTRINGZM).
 * Note that it only makes the reader more flexible, but JTS could already read
 * 3D coordinates from WKT String and still can't read 4D coordinates.
 *
 * <blockquote><pre>
 * <i>WKTGeometry:</i> one of<i>
 *
 *       WKTPoint  WKTLineString  WKTLinearRing  WKTPolygon
 *       WKTMultiPoint  WKTMultiLineString  WKTMultiPolygon
 *       WKTGeometryCollection</i>
 *
 * <i>WKTPoint:</i> <b>POINT</b><i>[Dimension]</i> <b>( </b><i>Coordinate</i> <b>)</b>
 *
 * <i>WKTLineString:</i> <b>LINESTRING</b><i>[Dimension]</i> <i>CoordinateSequence</i>
 *
 * <i>WKTLinearRing:</i> <b>LINEARRING</b><i>[Dimension]</i> <i>CoordinateSequence</i>
 *
 * <i>WKTPolygon:</i> <b>POLYGON</b><i>[Dimension]</i> <i>CoordinateSequenceList</i>
 *
 * <i>WKTMultiPoint:</i> <b>MULTIPOINT</b><i>[Dimension]</i> <i>CoordinateSingletonList</i>
 *
 * <i>WKTMultiLineString:</i> <b>MULTILINESTRING</b><i>[Dimension]</i> <i>CoordinateSequenceList</i>
 *
 * <i>WKTMultiPolygon:</i>
 *         <b>MULTIPOLYGON</b><i>[Dimension]</i> <b>(</b> <i>CoordinateSequenceList {</i> , <i>CoordinateSequenceList }</i> <b>)</b>
 *
 * <i>WKTGeometryCollection: </i>
 *         <b>GEOMETRYCOLLECTION</b><i>[Dimension]</i> <b> (</b> <i>WKTGeometry {</i> , <i>WKTGeometry }</i> <b>)</b>
 *
 * <i>CoordinateSingletonList:</i>
 *         <b>(</b> <i>CoordinateSingleton {</i> <b>,</b> <i>CoordinateSingleton }</i> <b>)</b>
 *         | <b>EMPTY</b>
 *         
 * <i>CoordinateSingleton:</i>
 *         <b>(</b> <i>Coordinate</i> <b>)</b>
 *         | <b>EMPTY</b>
 *
 * <i>CoordinateSequenceList:</i>
 *         <b>(</b> <i>CoordinateSequence {</i> <b>,</b> <i>CoordinateSequence }</i> <b>)</b>
 *         | <b>EMPTY</b>
 *
 * <i>CoordinateSequence:</i>
 *         <b>(</b> <i>Coordinate {</i> , <i>Coordinate }</i> <b>)</b>
 *         | <b>EMPTY</b>
 *
 * <i>Coordinate:
 *         Number Number Number<sub>opt</sub></i>
 *
 * <i>Number:</i> A Java-style floating-point number (including <tt>NaN</tt>, with arbitrary case)
 *
 * <i>Dimension:</i>
 *         <b>Z</b>|<b> Z</b>|<b>M</b>|<b> M</b>|<b>ZM</b>|<b> ZM</b>
 *
 * </pre></blockquote>
 *
 *
 *@version 1.7
 * @see WKTWriter
 */
public class WKTReader
{
  private static final String COMMA = ",";
  private static final String L_PAREN = "(";
  private static final String R_PAREN = ")";
  private static final String NAN_SYMBOL = "NaN";

  private GeometryFactory geometryFactory;
  private CoordinateSequenceFactory csFactory;
  private static CoordinateSequenceFactory csFactoryXYZM = CoordinateArraySequenceFactory.instance();
  private PrecisionModel precisionModel;

  /**
   * Flag indicating that the old notation of coordinates in JTS
   * is supported.
   */
  private static final boolean ALLOW_OLD_JTS_COORDINATE_SYNTAX = true;
  private boolean isAllowOldJtsCoordinateSyntax = ALLOW_OLD_JTS_COORDINATE_SYNTAX;

  /**
   * Flag indicating that the old notation of MultiPoint coordinates in JTS
   * is supported.
   */
  private static final boolean ALLOW_OLD_JTS_MULTIPOINT_SYNTAX = true;
  private boolean isAllowOldJtsMultipointSyntax = ALLOW_OLD_JTS_MULTIPOINT_SYNTAX;

  /**
   * Creates a reader that creates objects using the default {@link GeometryFactory}.
   */
  public WKTReader() {
    this(new GeometryFactory());
  }

  /**
   *  Creates a reader that creates objects using the given
   *  {@link GeometryFactory}.
   *
   *@param  geometryFactory  the factory used to create <code>Geometry</code>s.
   */
  public WKTReader(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.csFactory = geometryFactory.getCoordinateSequenceFactory();
    this.precisionModel = geometryFactory.getPrecisionModel();
  }

  /**
   * Sets a flag indicating, that coordinates may have 3 ordinate values even though no Z or M ordinate indicator
   * is present. The default value is {@link #ALLOW_OLD_JTS_COORDINATE_SYNTAX}.
   *
   * @param value a boolean value
   */
  public void setIsOldJtsCoordinateSyntaxAllowed(boolean value) {
    isAllowOldJtsCoordinateSyntax = value;
  }

  /**
   * Sets a flag indicating, that point coordinates in a MultiPoint geometry must not be enclosed in paren.
   * The default value is {@link #ALLOW_OLD_JTS_MULTIPOINT_SYNTAX}
   * @param value a boolean value
   */
  public void setIsOldJtsMultiPointSyntaxAllowed(boolean value) {
    isAllowOldJtsMultipointSyntax = value;
  }

  /**
   * Reads a Well-Known Text representation of a {@link Geometry}
   * from a {@link String}.
   *
   * @param wellKnownText
   *            one or more &lt;Geometry Tagged Text&gt; strings (see the OpenGIS
   *            Simple Features Specification) separated by whitespace
   * @return a <code>Geometry</code> specified by <code>wellKnownText</code>
   * @throws ParseException
   *             if a parsing problem occurs
   */
  public Geometry read(String wellKnownText) throws ParseException {
    StringReader reader = new StringReader(wellKnownText);
    try {
      return read(reader);
    }
    finally {
      reader.close();
    }
  }

  /**
   * Reads a Well-Known Text representation of a {@link Geometry}
   * from a {@link Reader}.
   *
   *@param  reader           a Reader which will return a &lt;Geometry Tagged Text&gt;
   *      string (see the OpenGIS Simple Features Specification)
   *@return                  a <code>Geometry</code> read from <code>reader</code>
   *@throws  ParseException  if a parsing problem occurs
   */
  public Geometry read(Reader reader) throws ParseException {
    StreamTokenizer tokenizer = createTokenizer(reader);
    try {
      return readGeometryTaggedText(tokenizer);
    }
    catch (IOException e) {
      throw new ParseException(e.toString());
    }
  }

  /**
   * Utility function to create the tokenizer
   * @param reader a reader
   *
   * @return a WKT Tokenizer.
   */
  private static StreamTokenizer createTokenizer(Reader reader) {
    StreamTokenizer tokenizer = new StreamTokenizer(reader);
    // set tokenizer to NOT parse numbers
    tokenizer.resetSyntax();
    tokenizer.wordChars('a', 'z');
    tokenizer.wordChars('A', 'Z');
    tokenizer.wordChars(128 + 32, 255);
    tokenizer.wordChars('0', '9');
    tokenizer.wordChars('-', '-');
    tokenizer.wordChars('+', '+');
    tokenizer.wordChars('.', '.');
    tokenizer.whitespaceChars(0, ' ');
    tokenizer.commentChar('#');

    return tokenizer;
  }

  /**
   * Reads a <code>Coordinate</Code> from a stream using the given {@link StreamTokenizer}.
   * <p>
   *   All ordinate values are read, but -depending on the {@link CoordinateSequenceFactory} of the
   *   underlying {@link GeometryFactory}- not necessarily all can be handled. Those are silently dropped.
   * </p>
   * @param tokenizer the tokenizer to use
   * @param ordinateFlags a bit-mask defining the ordinates to read.
   * @param tryParen a value indicating if a starting {@link #L_PAREN} should be probed.
   * @return a {@link CoordinateSequence} of length 1 containing the read ordinate values
   *
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private CoordinateSequence getCoordinate(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags, boolean tryParen)
          throws IOException, ParseException
  {

    boolean opened = false;
    if (tryParen && isOpenerNext(tokenizer) ) {
      tokenizer.nextToken();
      opened = true;
    }

    // create a sequence for one coordinate
    int offsetM = ordinateFlags.contains(Ordinate.Z) ? 1 : 0;
    CoordinateSequence sequence = csFactory.create(1, toDimension(ordinateFlags), ordinateFlags.contains(Ordinate.M) ? 1 : 0);
    sequence.setOrdinate(0, CoordinateSequence.X, precisionModel.makePrecise(getNextNumber(tokenizer)));
    sequence.setOrdinate(0, CoordinateSequence.Y, precisionModel.makePrecise(getNextNumber(tokenizer)));

    // additionally read other vertices
    if (ordinateFlags.contains(Ordinate.Z))
      sequence.setOrdinate(0, CoordinateSequence.Z, getNextNumber(tokenizer));
    if (ordinateFlags.contains(Ordinate.M))
      sequence.setOrdinate(0, CoordinateSequence.Z + offsetM, getNextNumber(tokenizer));

    if (ordinateFlags.size() == 2 && this.isAllowOldJtsCoordinateSyntax && isNumberNext(tokenizer)) {
      sequence.setOrdinate(0, CoordinateSequence.Z, getNextNumber(tokenizer));
    }

    // read close token if it was opened here
    if (opened) {
      getNextCloser(tokenizer);
    }

    return sequence;
  }

  /**
   * Reads a <code>Coordinate</Code> from a stream using the given {@link StreamTokenizer}.
   * <p>
   *   All ordinate values are read, but -depending on the {@link CoordinateSequenceFactory} of the
   *   underlying {@link GeometryFactory}- not necessarily all can be handled. Those are silently dropped.
   * </p>
   * <p>
   *
   * </p>
   * @param tokenizer the tokenizer to use
   * @param ordinateFlags a bit-mask defining the ordinates to read.
   * @return a {@link CoordinateSequence} of length 1 containing the read ordinate values
   *
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private CoordinateSequence getCoordinateSequence(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
          throws IOException, ParseException {
    if (getNextEmptyOrOpener(tokenizer).equals(WKTConstants.EMPTY))
      return this.csFactory.create(0, toDimension(ordinateFlags), ordinateFlags.contains(Ordinate.M) ? 1 : 0);
    
    ArrayList coordinates = new ArrayList();
    do {
      coordinates.add(getCoordinate(tokenizer, ordinateFlags, false));
    } while (getNextCloserOrComma(tokenizer).equals(COMMA));

    return mergeSequences(coordinates, ordinateFlags);  }

  /**
   * Reads a <code>CoordinateSequence</Code> from a stream using the given {@link StreamTokenizer}
   * for an old-style JTS MultiPoint (Point coordinates not enclosed in parentheses).
   * <p>
   * All ordinate values are read, but -depending on the {@link CoordinateSequenceFactory} of the
   * underlying {@link GeometryFactory}- not necessarily all can be handled. Those are silently dropped.
   * </p>
   * @param tokenizer the tokenizer to use
   * @param ordinateFlags a bit-mask defining the ordinates to read.
   * @param tryParen a value indicating if a starting {@link #L_PAREN} should be probed for each coordinate.
   * @param isReadEmptyOrOpener indicates if an opening paren or EMPTY should be scanned for
   * @return a {@link CoordinateSequence} of length 1 containing the read ordinate values
   *
   * @throws  IOException     if an I/O error occurs
   * @throws  ParseException  if an unexpected token was encountered
S  */
  private CoordinateSequence getCoordinateSequenceOldMultiPoint(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
          throws IOException, ParseException {

    ArrayList coordinates = new ArrayList();
    do {
      coordinates.add(getCoordinate(tokenizer, ordinateFlags, true));
    } while (getNextCloserOrComma(tokenizer).equals(COMMA));

    return mergeSequences(coordinates, ordinateFlags);
  }

  /**
   * Computes the required dimension based on the given ordinate values.
   * It is assumed that {@link Ordinate#X} and {@link Ordinate#Y} are included.
   *
   * @param ordinateFlags the ordinate bit-mask
   * @return the number of dimensions required to store ordinates for the given bit-mask.
   */
  private int toDimension(EnumSet<Ordinate> ordinateFlags) {
    int dimension = 2;
    if (ordinateFlags.contains(Ordinate.Z))
      dimension++;
    if (ordinateFlags.contains(Ordinate.M))
      dimension++;

    if (dimension == 2 && this.isAllowOldJtsCoordinateSyntax)
      dimension++;

    return dimension;
  }

  /**
   * Merges an array of one-coordinate-{@link CoordinateSequence}s into one {@link CoordinateSequence}.
   *
   * @param sequences an array of coordinate sequences. Each sequence contains <b>exactly one</b> coordinate.
   * @param ordinateFlags a bit-mask of required ordinates.
   * @return a coordinate sequence containing all coordinate
   */
  private CoordinateSequence mergeSequences(ArrayList sequences, EnumSet<Ordinate> ordinateFlags) {

    // if the sequences array is empty or null create an empty sequence
    if (sequences == null || sequences.size() == 0)
      return csFactory.create(0, toDimension(ordinateFlags));

    if (sequences.size() == 1)
      return (CoordinateSequence) sequences.get(0);

    EnumSet<Ordinate> mergeOrdinates;
    if (this.isAllowOldJtsCoordinateSyntax && ordinateFlags.size() == 2) {
      mergeOrdinates = ordinateFlags.clone();
      for (int i = 0; i < sequences.size(); i++) {
        if (((CoordinateSequence)sequences.get(i)).hasZ()) {
          mergeOrdinates.add(Ordinate.Z);
          break;
        }
      }
    }
    else
      mergeOrdinates = ordinateFlags;

    // create and fill the result sequence
    CoordinateSequence sequence = this.csFactory.create(sequences.size(), toDimension(mergeOrdinates),
            mergeOrdinates.contains(Ordinate.M) ? 1 : 0);

    int offsetM = CoordinateSequence.Z + (mergeOrdinates.contains(Ordinate.Z) ? 1 : 0);
    for (int i = 0; i < sequences.size(); i++) {
      CoordinateSequence item = (CoordinateSequence)sequences.get(i);
      sequence.setOrdinate(i, CoordinateSequence.X, item.getOrdinate(0, CoordinateSequence.X));
      sequence.setOrdinate(i, CoordinateSequence.Y, item.getOrdinate(0, CoordinateSequence.Y));
      if (mergeOrdinates.contains(Ordinate.Z))
        sequence.setOrdinate(i, CoordinateSequence.Z, item.getOrdinate(0, CoordinateSequence.Z));
      if (mergeOrdinates.contains(Ordinate.M))
        sequence.setOrdinate(i, offsetM, item.getOrdinate(0, offsetM));
    }

    // return it
    return sequence;
  }

  /**
   * Returns the next array of <code>Coordinate</code>s in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next element returned by the stream should be L_PAREN (the
   *      beginning of "(x1 y1, x2 y2, ..., xn yn)") or EMPTY.
   *@return                  the next array of <code>Coordinate</code>s in the
   *      stream, or an empty array if EMPTY is the next element returned by
   *      the stream.
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   *
   *@deprecated in favor of functions returning {@link CoordinateSequence}s
   */
  private Coordinate[] getCoordinates(StreamTokenizer tokenizer) throws IOException, ParseException {
    String nextToken = getNextEmptyOrOpener(tokenizer);
    if (nextToken.equals(WKTConstants.EMPTY)) {
      return new Coordinate[] {};
    }
    ArrayList coordinates = new ArrayList();
    coordinates.add(getPreciseCoordinate(tokenizer));
    nextToken = getNextCloserOrComma(tokenizer);
    while (nextToken.equals(COMMA)) {
      coordinates.add(getPreciseCoordinate(tokenizer));
      nextToken = getNextCloserOrComma(tokenizer);
    }
    Coordinate[] array = new Coordinate[coordinates.size()];
    return (Coordinate[]) coordinates.toArray(array);
  }

  /**
   * Returns the next array of <code>Coordinate</code>s in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next element returned by the stream should be a number.
   *@return                  the next array of <code>Coordinate</code>s in the
   *      stream.
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   *
   *@deprecated in favor of functions returning {@link CoordinateSequence}s
   */
  private Coordinate[] getCoordinatesNoLeftParen(StreamTokenizer tokenizer) throws IOException, ParseException {
    String nextToken = null;
    ArrayList coordinates = new ArrayList();
    coordinates.add(getPreciseCoordinate(tokenizer));
    nextToken = getNextCloserOrComma(tokenizer);
    while (nextToken.equals(COMMA)) {
      coordinates.add(getPreciseCoordinate(tokenizer));
      nextToken = getNextCloserOrComma(tokenizer);
    }
    Coordinate[] array = new Coordinate[coordinates.size()];
    return (Coordinate[]) coordinates.toArray(array);
  }

  /**
   * Returns the next precise <code>Coordinate</code> in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next element returned by the stream should be a number.
   *@return                  the next array of <code>Coordinate</code>s in the
   *      stream.
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   *
   *@deprecated in favor of functions returning {@link CoordinateSequence}s
   */
  private Coordinate getPreciseCoordinate(StreamTokenizer tokenizer)
      throws IOException, ParseException
  {
    Coordinate coord = new Coordinate();
    coord.x = getNextNumber(tokenizer);
    coord.y = getNextNumber(tokenizer);
    if (isNumberNext(tokenizer)) {
        coord.setZ(getNextNumber(tokenizer));
    }
    if (isNumberNext(tokenizer)) {
      getNextNumber(tokenizer); // ignore M value
    }
    precisionModel.makePrecise(coord);
    return coord;
  }

  /**
   * Tests if the next token in the stream is a number
   *
   * @param tokenizer the tokenizer
   * @return {@code true} if the next token is a number, otherwise {@code false}
   * @throws  IOException     if an I/O error occurs
   */
  private static boolean isNumberNext(StreamTokenizer tokenizer) throws IOException {
    int type = tokenizer.nextToken();
    tokenizer.pushBack();
    return type == StreamTokenizer.TT_WORD;
  }

  /**
   * Tests if the next token in the stream is a left opener ({@link #L_PAREN})
   *
   * @param tokenizer the tokenizer
   * @return {@code true} if the next token is a {@link #L_PAREN}, otherwise {@code false}
   * @throws  IOException     if an I/O error occurs
   */
  private static boolean isOpenerNext(StreamTokenizer tokenizer) throws IOException {
    int type = tokenizer.nextToken();
    tokenizer.pushBack();
    return type == '(';
  }

  /**
   * Parses the next number in the stream.
   * Numbers with exponents are handled.
   * <tt>NaN</tt> values are handled correctly, and
   * the case of the "NaN" symbol is not significant. 
   *
   * @param  tokenizer        tokenizer over a stream of text in Well-known Text
   * @return                  the next number in the stream
   * @throws  ParseException  if the next token is not a valid number
   * @throws  IOException     if an I/O error occurs
   */
  private double getNextNumber(StreamTokenizer tokenizer) throws IOException,
      ParseException {
    int type = tokenizer.nextToken();
    switch (type) {
      case StreamTokenizer.TT_WORD:
      {
        if (tokenizer.sval.equalsIgnoreCase(NAN_SYMBOL)) {
          return Double.NaN;
        }
        else {
          try {
            return Double.parseDouble(tokenizer.sval);
          }
          catch (NumberFormatException ex) {
            throw parseErrorWithLine(tokenizer, "Invalid number: " + tokenizer.sval);
          }
        }
      }
    }
    throw parseErrorExpected(tokenizer, "number");
  }

  /**
   *  Returns the next EMPTY or L_PAREN in the stream as uppercase text.
   *
   *@return                  the next EMPTY or L_PAREN in the stream as uppercase
   *      text.
   *@throws  ParseException  if the next token is not EMPTY or L_PAREN
   *@throws  IOException     if an I/O error occurs
   * @param  tokenizer        tokenizer over a stream of text in Well-known Text
   */
  private static String getNextEmptyOrOpener(StreamTokenizer tokenizer) throws IOException, ParseException {
    String nextWord = getNextWord(tokenizer);
    if (nextWord.equalsIgnoreCase(WKTConstants.Z)) {
      //z = true;
      nextWord = getNextWord(tokenizer);
    }
    else if (nextWord.equalsIgnoreCase(WKTConstants.M)) {
      //m = true;
      nextWord = getNextWord(tokenizer);
    }
    else if (nextWord.equalsIgnoreCase(WKTConstants.ZM)) {
      //z = true;
      //m = true;
      nextWord = getNextWord(tokenizer);
    }
    if (nextWord.equals(WKTConstants.EMPTY) || nextWord.equals(L_PAREN)) {
      return nextWord;
    }
    throw parseErrorExpected(tokenizer, WKTConstants.EMPTY + " or " + L_PAREN);
  }

  /**
   *  Returns the next ordinate flag information in the stream as uppercase text.
   *  This can be Z, M or ZM.
   *
   *@return                  the next EMPTY or L_PAREN in the stream as uppercase
   *      text.
   *@throws  ParseException  if the next token is not EMPTY or L_PAREN
   *@throws  IOException     if an I/O error occurs
   * @param  tokenizer        tokenizer over a stream of text in Well-known Text
   */
  private static EnumSet<Ordinate> getNextOrdinateFlags(StreamTokenizer tokenizer) throws IOException, ParseException {

    EnumSet<Ordinate> result = EnumSet.of(Ordinate.X, Ordinate.Y);

    String nextWord = lookAheadWord(tokenizer).toUpperCase(Locale.ROOT);
    if (nextWord.equalsIgnoreCase(WKTConstants.Z)) {
      tokenizer.nextToken();
      result.add(Ordinate.Z);
    }
    else if (nextWord.equalsIgnoreCase(WKTConstants.M)) {
      tokenizer.nextToken();
      result.add(Ordinate.M);
    }
    else if (nextWord.equalsIgnoreCase(WKTConstants.ZM)) {
      tokenizer.nextToken();
      result.add(Ordinate.Z);
      result.add(Ordinate.M);
    }
    return result;
  }

  /**
   *  Returns the next word in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next token must be a word.
   *@return                  the next word in the stream as uppercase text
   *@throws  ParseException  if the next token is not a word
   *@throws  IOException     if an I/O error occurs
   */
  private static String lookAheadWord(StreamTokenizer tokenizer) throws IOException, ParseException {
    String nextWord = getNextWord(tokenizer);
    tokenizer.pushBack();
    return nextWord;
  }

  /**
   *  Returns the next {@link #R_PAREN} or {@link #COMMA} in the stream.
   *
   *@return                  the next R_PAREN or COMMA in the stream
   *@throws  ParseException  if the next token is not R_PAREN or COMMA
   *@throws  IOException     if an I/O error occurs
   * @param  tokenizer        tokenizer over a stream of text in Well-known Text
   */
  private static String getNextCloserOrComma(StreamTokenizer tokenizer) throws IOException, ParseException {
    String nextWord = getNextWord(tokenizer);
    if (nextWord.equals(COMMA) || nextWord.equals(R_PAREN)) {
      return nextWord;
    }
    throw parseErrorExpected(tokenizer, COMMA + " or " + R_PAREN);
  }

  /**
   *  Returns the next {@link #R_PAREN} in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next token must be R_PAREN.
   *@return                  the next R_PAREN in the stream
   *@throws  ParseException  if the next token is not R_PAREN
   *@throws  IOException     if an I/O error occurs
   */
  private String getNextCloser(StreamTokenizer tokenizer) throws IOException, ParseException {
    String nextWord = getNextWord(tokenizer);
    if (nextWord.equals(R_PAREN)) {
      return nextWord;
    }
    throw parseErrorExpected(tokenizer, R_PAREN);
  }

  /**
   *  Returns the next word in the stream.
   *
   *@return                  the next word in the stream as uppercase text
   *@throws  ParseException  if the next token is not a word
   *@throws  IOException     if an I/O error occurs
   * @param  tokenizer        tokenizer over a stream of text in Well-known Text
   */
  private static String getNextWord(StreamTokenizer tokenizer) throws IOException, ParseException {
    int type = tokenizer.nextToken();
    switch (type) {
    case StreamTokenizer.TT_WORD:

      String word = tokenizer.sval;
      if (word.equalsIgnoreCase(WKTConstants.EMPTY))
          return WKTConstants.EMPTY;
      return word;

    case '(': return L_PAREN;
    case ')': return R_PAREN;
    case ',': return COMMA;
    }
    throw parseErrorExpected(tokenizer, "word");
  }

  /**
   * Creates a formatted ParseException reporting that the current token
   * was unexpected.
   *
   * @param expected a description of what was expected
   * @throws AssertionFailedException if an invalid token is encountered
   */
  private static ParseException parseErrorExpected(StreamTokenizer tokenizer, String expected)
  {
    // throws Asserts for tokens that should never be seen
    if (tokenizer.ttype == StreamTokenizer.TT_NUMBER)
      Assert.shouldNeverReachHere("Unexpected NUMBER token");
    if (tokenizer.ttype == StreamTokenizer.TT_EOL)
      Assert.shouldNeverReachHere("Unexpected EOL token");

    String tokenStr = tokenString(tokenizer);
    return parseErrorWithLine(tokenizer, "Expected " + expected + " but found " + tokenStr);
  }

  /**
   * Creates a formatted ParseException reporting that the current token
   * was unexpected.
   *
   * @param msg a description of what was expected
   * @throws AssertionFailedException if an invalid token is encountered
   */
  private static ParseException parseErrorWithLine(StreamTokenizer tokenizer, String msg)
  {
    return new ParseException(msg + " (line " + tokenizer.lineno() + ")");
  }
  
  /**
   * Gets a description of the current token type
   * @param tokenizer the tokenizer
   * @return a description of the current token
   */
  private static String tokenString(StreamTokenizer tokenizer)
  {
    switch (tokenizer.ttype) {
      case StreamTokenizer.TT_NUMBER:
        return "<NUMBER>";
      case StreamTokenizer.TT_EOL:
        return "End-of-Line";
      case StreamTokenizer.TT_EOF: return "End-of-Stream";
      case StreamTokenizer.TT_WORD: return "'" + tokenizer.sval + "'";
    }
    return "'" + (char) tokenizer.ttype + "'";
  }

  /**
   *  Creates a <code>Geometry</code> using the next token in the stream.
   *
   *@return                  a <code>Geometry</code> specified by the next token
   *      in the stream
   *@throws  ParseException  if the coordinates used to create a <code>Polygon</code>
   *      shell and holes do not form closed linestrings, or if an unexpected
   *      token was encountered
   *@throws  IOException     if an I/O error occurs
   * @param  tokenizer        tokenizer over a stream of text in Well-known Text
   */
  private Geometry readGeometryTaggedText(StreamTokenizer tokenizer) throws IOException, ParseException {
    String type;

    EnumSet<Ordinate> ordinateFlags = EnumSet.of(Ordinate.X, Ordinate.Y);
    try {
      type = getNextWord(tokenizer).toUpperCase(Locale.ROOT);
      if (type.endsWith(WKTConstants.ZM)) {
        ordinateFlags.add(Ordinate.Z);
        ordinateFlags.add(Ordinate.M);
      } else if (type.endsWith(WKTConstants.Z)) {
        ordinateFlags.add(Ordinate.Z);
      } else if (type.endsWith(WKTConstants.M)) {
        ordinateFlags.add(Ordinate.M);
      }
    } catch (IOException e) {
      return null;
    } catch (ParseException e) {
      return null;
    }

    return readGeometryTaggedText(tokenizer, type, ordinateFlags);
  }

  private Geometry readGeometryTaggedText(StreamTokenizer tokenizer, String type, EnumSet<Ordinate> ordinateFlags)
          throws IOException, ParseException {

    if (ordinateFlags.size() == 2) {
      ordinateFlags = getNextOrdinateFlags(tokenizer);
    }

    // if we can create a sequence with the required dimension everything is ok, otherwise
    // we need to take a different coordinate sequence factory.
    // It would be good to not have to try/catch this but if the CoordinateSequenceFactory
    // exposed a value indicating which min/max dimension it can handle or even an
    // ordinate bit-flag.
    try {
      csFactory.create(0, toDimension(ordinateFlags), ordinateFlags.contains(Ordinate.M) ? 1 : 0);
    } catch (Exception e)
    {
      geometryFactory = new GeometryFactory(geometryFactory.getPrecisionModel(),
              geometryFactory.getSRID(), csFactoryXYZM);
    }

    if (type.startsWith(WKTConstants.POINT)) {
      return readPointText(tokenizer, ordinateFlags);
    }
    else if (type.startsWith(WKTConstants.LINESTRING)) {
      return readLineStringText(tokenizer, ordinateFlags);
    }
    else if (type.startsWith(WKTConstants.LINEARRING)) {
      return readLinearRingText(tokenizer, ordinateFlags);
    }
    else if (type.startsWith(WKTConstants.POLYGON)) {
      return readPolygonText(tokenizer, ordinateFlags);
    }
    else if (type.startsWith(WKTConstants.MULTIPOINT)) {
      return readMultiPointText(tokenizer, ordinateFlags);
    }
    else if (type.startsWith(WKTConstants.MULTILINESTRING)) {
      return readMultiLineStringText(tokenizer, ordinateFlags);
    }
    else if (type.startsWith(WKTConstants.MULTIPOLYGON)) {
      return readMultiPolygonText(tokenizer, ordinateFlags);
    }
    else if (type.startsWith(WKTConstants.GEOMETRYCOLLECTION)) {
      return readGeometryCollectionText(tokenizer, ordinateFlags);
    }
    throw parseErrorWithLine(tokenizer, "Unknown geometry type: " + type);
  }

  /**
   *  Creates a <code>Point</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;Point Text&gt;.
   *@return                  a <code>Point</code> specified by the next token in
   *      the stream
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private Point readPointText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags) throws IOException, ParseException {
    Point point = geometryFactory.createPoint(getCoordinateSequence(tokenizer, ordinateFlags));
    return point;
  }

  /**
   *  Creates a <code>LineString</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;LineString Text&gt;.
   *@return                  a <code>LineString</code> specified by the next
   *      token in the stream
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private LineString readLineStringText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags) throws IOException, ParseException {
    return geometryFactory.createLineString(getCoordinateSequence(tokenizer, ordinateFlags));
  }

  /**
   *  Creates a <code>LinearRing</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;LineString Text&gt;.
   *@return                  a <code>LinearRing</code> specified by the next
   *      token in the stream
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if the coordinates used to create the <code>LinearRing</code>
   *      do not form a closed linestring, or if an unexpected token was
   *      encountered
   */
  private LinearRing readLinearRingText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
    throws IOException, ParseException
  {
    return geometryFactory.createLinearRing(getCoordinateSequence(tokenizer, ordinateFlags));
  }

  /**
   *  Creates a <code>MultiPoint</code> using the next tokens in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;MultiPoint Text&gt;.
   *@return                  a <code>MultiPoint</code> specified by the next
   *      token in the stream
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private MultiPoint readMultiPointText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags) throws IOException, ParseException
  {
    String nextToken = getNextEmptyOrOpener(tokenizer);
    if (nextToken.equals(WKTConstants.EMPTY)) {
      return geometryFactory.createMultiPoint(new Point[0]);
    }
    
    // check for old-style JTS syntax (no parentheses surrounding Point coordinates) and parse it if present
    // MD 2009-02-21 - this is only provided for backwards compatibility for a few versions
    if (isAllowOldJtsMultipointSyntax) {
      String nextWord = lookAheadWord(tokenizer);
      if (nextWord != L_PAREN && nextWord != WKTConstants.EMPTY) {
        return geometryFactory.createMultiPoint(
            getCoordinateSequenceOldMultiPoint(tokenizer, ordinateFlags));
      }
    }
    
    ArrayList points = new ArrayList();
    Point point = readPointText(tokenizer, ordinateFlags);
    points.add(point);
    nextToken = getNextCloserOrComma(tokenizer);
    while (nextToken.equals(COMMA)) {
      point = readPointText(tokenizer, ordinateFlags);
      points.add(point);
      nextToken = getNextCloserOrComma(tokenizer);
    }
    Point[] array = new Point[points.size()];
    return geometryFactory.createMultiPoint((Point[]) points.toArray(array));
  }


  /**
   *  Creates a <code>Polygon</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;Polygon Text&gt;.
   *@return                  a <code>Polygon</code> specified by the next token
   *      in the stream
   *@throws  ParseException  if the coordinates used to create the <code>Polygon</code>
   *      shell and holes do not form closed linestrings, or if an unexpected
   *      token was encountered.
   *@throws  IOException     if an I/O error occurs
   */
  private Polygon readPolygonText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags) throws IOException, ParseException {
    String nextToken = getNextEmptyOrOpener(tokenizer);
    if (nextToken.equals(WKTConstants.EMPTY)) {
        return geometryFactory.createPolygon();
    }
    ArrayList holes = new ArrayList();
    LinearRing shell = readLinearRingText(tokenizer, ordinateFlags);
    nextToken = getNextCloserOrComma(tokenizer);
    while (nextToken.equals(COMMA)) {
      LinearRing hole = readLinearRingText(tokenizer, ordinateFlags);
      holes.add(hole);
      nextToken = getNextCloserOrComma(tokenizer);
    }
    LinearRing[] array = new LinearRing[holes.size()];
    return geometryFactory.createPolygon(shell, (LinearRing[]) holes.toArray(array));
  }

  /**
   *  Creates a <code>MultiLineString</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;MultiLineString Text&gt;.
   *@return                  a <code>MultiLineString</code> specified by the
   *      next token in the stream
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private MultiLineString readMultiLineStringText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
          throws IOException, ParseException {
    String nextToken = getNextEmptyOrOpener(tokenizer);
    if (nextToken.equals(WKTConstants.EMPTY)) {
      return geometryFactory.createMultiLineString();
    }

    ArrayList lineStrings = new ArrayList();
    do {
      LineString lineString = readLineStringText(tokenizer, ordinateFlags);
      lineStrings.add(lineString);
      nextToken = getNextCloserOrComma(tokenizer);
    } while (nextToken.equals(COMMA));

    LineString[] array = new LineString[lineStrings.size()];
    return geometryFactory.createMultiLineString((LineString[]) lineStrings.toArray(array));
  }

  /**
   *  Creates a <code>MultiPolygon</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;MultiPolygon Text&gt;.
   *@return                  a <code>MultiPolygon</code> specified by the next
   *      token in the stream, or if if the coordinates used to create the
   *      <code>Polygon</code> shells and holes do not form closed linestrings.
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private MultiPolygon readMultiPolygonText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags) throws IOException, ParseException {
    String nextToken = getNextEmptyOrOpener(tokenizer);
    if (nextToken.equals(WKTConstants.EMPTY)) {
      return geometryFactory.createMultiPolygon();
    }
    ArrayList polygons = new ArrayList();
    do {
      Polygon polygon = readPolygonText(tokenizer, ordinateFlags);
      polygons.add(polygon);
      nextToken = getNextCloserOrComma(tokenizer);
    } while (nextToken.equals(COMMA));
    Polygon[] array = new Polygon[polygons.size()];
    return geometryFactory.createMultiPolygon((Polygon[]) polygons.toArray(array));
  }

  /**
   *  Creates a <code>GeometryCollection</code> using the next token in the
   *  stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;GeometryCollection Text&gt;.
   *@return                  a <code>GeometryCollection</code> specified by the
   *      next token in the stream
   *@throws  ParseException  if the coordinates used to create a <code>Polygon</code>
   *      shell and holes do not form closed linestrings, or if an unexpected
   *      token was encountered
   *@throws  IOException     if an I/O error occurs
   */
  private GeometryCollection readGeometryCollectionText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags) throws IOException, ParseException {
    String nextToken = getNextEmptyOrOpener(tokenizer);
    if (nextToken.equals(WKTConstants.EMPTY)) {
      return geometryFactory.createGeometryCollection();
    }
    ArrayList geometries = new ArrayList();
    do {
      Geometry geometry = readGeometryTaggedText(tokenizer);
      geometries.add(geometry);
      nextToken = getNextCloserOrComma(tokenizer);
    } while (nextToken.equals(COMMA));

    Geometry[] array = new Geometry[geometries.size()];
    return geometryFactory.createGeometryCollection((Geometry[]) geometries.toArray(array));
  }

}

