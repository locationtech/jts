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

package org.locationtech.jtstest.util.io;


import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.*;

/**
 * Writes the SVG representation of a {@link Geometry}.
 *
 * @version 1.7
 * @see WKTReader
 */
public class SVGWriter
{
  /**
   *  Creates the <code>DecimalFormat</code> used to write <code>double</code>s
   *  with a sufficient number of decimal places.
   *
   *@param  precisionModel  the <code>PrecisionModel</code> used to determine
   *      the number of decimal places to write.
   *@return                 a <code>DecimalFormat</code> that write <code>double</code>
   *      s without scientific notation.
   */
  private static DecimalFormat createFormatter(PrecisionModel precisionModel) {
    // the default number of decimal places is 16, which is sufficient
    // to accommodate the maximum precision of a double.
    int decimalPlaces = precisionModel.getMaximumSignificantDigits();
    // specify decimal separator explicitly to avoid problems in other locales
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setDecimalSeparator('.');
    String fmtString = "0" + (decimalPlaces > 0 ? "." : "")
                 +  stringOfChar('#', decimalPlaces);
    return new DecimalFormat(fmtString, symbols);
  }

  /**
   *  Returns a <code>String</code> of repeated characters.
   *
   *@param  ch     the character to repeat
   *@param  count  the number of times to repeat the character
   *@return        a <code>String</code> of characters
   */
  public static String stringOfChar(char ch, int count) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < count; i++) {
      buf.append(ch);
    }
    return buf.toString();
  }

  private DecimalFormat formatter;
  private boolean isFormatted = false;
  private boolean useFormatting = false;
  private int coordsPerLine = -1;
  private String indentTabStr = "  ";

  /**
   * Creates a new SVGWriter with default settings
   */
  public SVGWriter()
  {
  }

  /**
   *  Converts a <code>Geometry</code> to its Well-known Text representation.
   *
   *@param  geometry  a <code>Geometry</code> to process
   *@return           a <Geometry Tagged Text> string (see the OpenGIS Simple
   *      Features Specification)
   */
  public String write(Geometry geometry)
  {
    Writer sw = new StringWriter();
    try {
      writeFormatted(geometry, isFormatted, sw);
    }
    catch (IOException ex) {
      Assert.shouldNeverReachHere();
    }
    return sw.toString();
  }

  /**
   *  Converts a <code>Geometry</code> to its Well-known Text representation.
   *
   *@param  geometry  a <code>Geometry</code> to process
   */
  public void write(Geometry geometry, Writer writer)
    throws IOException
  {
    writeFormatted(geometry, false, writer);
  }

  /**
   *  Same as <code>write</code>, but with newlines and spaces to make the
   *  well-known text more readable.
   *
   *@param  geometry  a <code>Geometry</code> to process
   *@return           a <Geometry Tagged Text> string (see the OpenGIS Simple
   *      Features Specification), with newlines and spaces
   */
  public String writeFormatted(Geometry geometry)
  {
    Writer sw = new StringWriter();
    try {
      writeFormatted(geometry, true, sw);
    }
    catch (IOException ex) {
      Assert.shouldNeverReachHere();
    }
    return sw.toString();
  }
  /**
   *  Same as <code>write</code>, but with newlines and spaces to make the
   *  well-known text more readable.
   *
   *@param  geometry  a <code>Geometry</code> to process
   */
  public void writeFormatted(Geometry geometry, Writer writer)
    throws IOException
  {
    writeFormatted(geometry, true, writer);
  }
  /**
   *  Converts a <code>Geometry</code> to its Well-known Text representation.
   *
   *@param  geometry  a <code>Geometry</code> to process
   */
  private void writeFormatted(Geometry geometry, boolean useFormatting, Writer writer)
    throws IOException
  {
    this.useFormatting = useFormatting;
    formatter = createFormatter(geometry.getPrecisionModel());
    //writer.write("<g>\n");
    appendGeometry(geometry, 0, writer);
    //writer.write("</g>\n");
  }


  /**
   *  Converts a <code>Geometry</code> to &lt;Geometry Tagged Text&gt; format,
   *  then appends it to the writer.
   *
   *@param  geometry  the <code>Geometry</code> to process
   *@param  writer    the output writer to append to
   */
  private void appendGeometry(Geometry geometry, int level, Writer writer)
    throws IOException
  {
    indent(level, writer);

    if (geometry instanceof Point) {
      Point point = (Point) geometry;
      appendPoint(point.getCoordinate(), level, writer, point.getPrecisionModel());
    }
    else if ((geometry instanceof LinearRing) 
      || (geometry instanceof LineString)) {
      appendLineString((LineString) geometry, level, false, writer);
    }
    else if (geometry instanceof Polygon) {
      appendPolygon((Polygon) geometry, level, writer);
    }
    else if (geometry instanceof MultiPoint) {
      appendMultiPoint((MultiPoint) geometry, level, writer);
    }
    else if (geometry instanceof MultiLineString) {
      appendMultiLineString((MultiLineString) geometry, level, false, writer);
    }
    else if (geometry instanceof MultiPolygon) {
      appendMultiPolygon((MultiPolygon) geometry, level, writer);
    }
    else if (geometry instanceof GeometryCollection) {
      appendGeometryCollection((GeometryCollection) geometry, level, writer);
    }
    else {
      Assert.shouldNeverReachHere("Unsupported Geometry implementation:"
           + geometry.getClass());
    }
  }

  /**
   *  Converts a <code>Polygon</code> to &lt;Polygon Tagged Text&gt; format,
   *  then appends it to the writer.
   *
   *@param  polygon  the <code>Polygon</code> to process
   *@param  writer   the output writer to append to
   */
  private void appendPolygon(Polygon polygon, int level, Writer writer)
    throws IOException
  {
    if (polygon.getNumInteriorRing() == 0) {
      appendPolygonPolygon(polygon, level, false, writer);
    }
    else {
      appendPolygonPath(polygon, level, false, writer);
    }
  }

  /**
   *  Converts a <code>Coordinate</code> to &lt;Point Text&gt; format, then
   *  appends it to the writer.
   *
   *@param  coordinate      the <code>Coordinate</code> to process
   *@param  writer          the output writer to append to
   *@param  precisionModel  the <code>PrecisionModel</code> to use to convert
   *      from a precise coordinate to an external coordinate
   */
  private void appendPoint(Coordinate coordinate, int level, Writer writer,
      PrecisionModel precisionModel)
    throws IOException
  {
    writer.write("<circle cx='" + coordinate.x + "' cy='" + coordinate.y + "' r='1' />\n");
  }

  /**
   * Appends the i'th coordinate from the sequence to the writer
   *
   * @param  seq  the <code>CoordinateSequence</code> to process
   * @param i     the index of the coordinate to write
   * @param  writer the output writer to append to
   */
  private void appendCoordinate(CoordinateSequence seq, int i, Writer writer)
      throws IOException
  {
    writer.write(writeNumber(seq.getX(i)) + "," + writeNumber(seq.getY(i)));
  }

  /**
   *  Converts a <code>double</code> to a <code>String</code>, not in scientific
   *  notation.
   *
   *@param  d  the <code>double</code> to convert
   *@return    the <code>double</code> as a <code>String</code>, not in
   *      scientific notation
   */
  private String writeNumber(double d) {
    return formatter.format(d);
  }

  /**
   *  Converts a <code>LineString</code> to &lt;LineString Text&gt; format, then
   *  appends it to the writer.
   *
   *@param  lineString  the <code>LineString</code> to process
   *@param  writer      the output writer to append to
   */
  private void appendSequencePath(CoordinateSequence seq, int level, boolean doIndent, Writer writer)
      throws IOException
    {
      if (seq.size() == 0) {
        //writer.write("EMPTY");
      }
      else {
        if (doIndent) indent(level, writer);
        for (int i = 0; i < seq.size(); i++) {
          writer.write(" " + ((i == 0) ? "M" : "L"));
          if (i > 0) {
            if (coordsPerLine > 0
                && i % coordsPerLine == 0) {
              indent(level + 1, writer);
            }
          }
          appendCoordinate(seq, i, writer);
        }
      }
    }
  private void appendSequencePoints(CoordinateSequence seq, int level, boolean doIndent, Writer writer)
      throws IOException
    {
      if (seq.size() == 0) {
        //writer.write("EMPTY");
      }
      else {
        if (doIndent) indent(level, writer);
        for (int i = 0; i < seq.size(); i++) {
          writer.write(" ");
          if (i > 0) {
            if (coordsPerLine > 0
                && i % coordsPerLine == 0) {
              indent(level + 1, writer);
            }
          }
          appendCoordinate(seq, i, writer);
        }
      }
    }

  /**
   *  Converts a <code>LineString</code> to &lt;LineString Text&gt; format, then
   *  appends it to the writer.
   *
   *@param  lineString  the <code>LineString</code> to process
   *@param  writer      the output writer to append to
   */
  private void appendLineString(LineString lineString, int level, boolean doIndent, Writer writer)
    throws IOException
  {
      if (doIndent) indent(level, writer);
      writer.write("<polyline fill='none' points='");
      appendSequencePoints(lineString.getCoordinateSequence(), level, doIndent, writer);
      writer.write("'/>\n");
  }

  /**
   *  Converts a <code>Polygon</code> to &lt;Polygon Text&gt; format, then
   *  appends it to the writer.
   *
   *@param  polygon  the <code>Polygon</code> to process
   *@param  writer   the output writer to append to
   */
  private void appendPolygonPolygon(Polygon polygon, int level, boolean indentFirst, Writer writer)
      throws IOException
    {
        if (indentFirst) indent(level, writer);
        writer.write("<polygon points='");
        appendSequencePoints(polygon.getExteriorRing().getCoordinateSequence(), level, false, writer);
        writer.write("' />\n");
    }

  private void appendPolygonPath(Polygon polygon, int level, boolean indentFirst, Writer writer)
      throws IOException
    {
        if (indentFirst) indent(level, writer);
        writer.write("<path fill-rule='evenodd' d='");
        appendSequencePath(polygon.getExteriorRing().getCoordinateSequence(), level, false, writer);
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
          writer.write(" ");
          appendSequencePath(polygon.getInteriorRingN(i).getCoordinateSequence(), level + 1, true, writer);
        }
        writer.write("' />\n");
    }


  /**
   *  Converts a <code>MultiPoint</code> to &lt;MultiPoint Text&gt; format, then
   *  appends it to the writer.
   *
   *@param  multiPoint  the <code>MultiPoint</code> to process
   *@param  writer      the output writer to append to
   */
  private void appendMultiPoint(MultiPoint multiPoint, int level, Writer writer)
    throws IOException
  {
    if (multiPoint.isEmpty()) {
      writer.write(" ");
    }
    else {
      int level2 = level;
      for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
        if (i > 0) {
          level2 = level + 1;
        }
        appendPoint(multiPoint.getGeometryN(i).getCoordinate(), level2, writer, multiPoint.getPrecisionModel());
      }
    }
  }

  /**
   *  Converts a <code>MultiLineString</code> to &lt;MultiLineString Text&gt;
   *  format, then appends it to the writer.
   *
   *@param  multiLineString  the <code>MultiLineString</code> to process
   *@param  writer           the output writer to append to
   */
  private void appendMultiLineString(MultiLineString multiLineString, int level, boolean indentFirst,
      Writer writer)
    throws IOException
  {
      int level2 = level;
      boolean doIndent = indentFirst;
      for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
        if (i > 0) {
          level2 = level + 1;
          doIndent = true;
        }
        appendLineString((LineString) multiLineString.getGeometryN(i), level2, doIndent, writer);
      }
  }

  /**
   *  Converts a <code>MultiPolygon</code> to &lt;MultiPolygon Text&gt; format,
   *  then appends it to the writer.
   *
   *@param  multiPolygon  the <code>MultiPolygon</code> to process
   *@param  writer        the output writer to append to
   */
  private void appendMultiPolygon(MultiPolygon multiPolygon, int level, Writer writer)
    throws IOException
  {
      int level2 = level;
      for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
        if (i > 0) {
          level2 = level + 1;
        }
        appendPolygon((Polygon) multiPolygon.getGeometryN(i), level2, writer);
      }
  }

  /**
   *  Converts a <code>GeometryCollection</code> to &lt;GeometryCollectionText&gt;
   *  format, then appends it to the writer.
   *
   *@param  geometryCollection  the <code>GeometryCollection</code> to process
   *@param  writer              the output writer to append to
   */
  private void appendGeometryCollection(GeometryCollection geometryCollection, int level,
      Writer writer)
    throws IOException
  {
      int level2 = level;
      for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
        if (i > 0) {
          level2 = level + 1;
        }
        appendGeometry(geometryCollection.getGeometryN(i), level2, writer);
      }
  }

  private void indent(int level, Writer writer)
    throws IOException
  {
    if (! useFormatting || level <= 0)
      return;
    writer.write("\n");
    for (int i = 0; i < level; i++) {
      writer.write(indentTabStr);
    }
  }


}

