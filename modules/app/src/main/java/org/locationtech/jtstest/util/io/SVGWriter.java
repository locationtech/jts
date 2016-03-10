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

package org.locationtech.jtstest.util.io;


import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.*;

/**
 * Writes the Well-Known Text representation of a {@link Geometry}.
 * The Well-Known Text format is defined in the
 * OGC <A HREF="http://www.opengis.org/techno/specs.htm">
 * <i>Simple Features Specification for SQL</i></A>.
 * See {@link WKTReader} for a formal specification of the format syntax.
 * <p>
 * The <code>WKTWriter</code> outputs coordinates rounded to the precision
 * model. Only the maximum number of decimal places 
 * necessary to represent the ordinates to the required precision will be
 * output.
 * <p>
 * The SFS WKT spec does not define a special tag for {@link LinearRing}s.
 * Under the spec, rings are output as <code>LINESTRING</code>s.
 * In order to allow precisely specifying constructed geometries, 
 * JTS also supports a non-standard <code>LINEARRING</code> tag which is used 
 * to output LinearRings.
 *
 * @version 1.7
 * @see WKTReader
 */
public class SVGWriter
{

  private static final int INDENT = 2;

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
    // to accomodate the maximum precision of a double.
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

  private int outputDimension = 2;
  private DecimalFormat formatter;
  private boolean isFormatted = false;
  private boolean useFormatting = false;
  private int level = 0;
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
    appendGeometryTaggedText(geometry, 0, writer);
    //writer.write("</g>\n");
  }


  /**
   *  Converts a <code>Geometry</code> to &lt;Geometry Tagged Text&gt; format,
   *  then appends it to the writer.
   *
   *@param  geometry  the <code>Geometry</code> to process
   *@param  writer    the output writer to append to
   */
  private void appendGeometryTaggedText(Geometry geometry, int level, Writer writer)
    throws IOException
  {
    indent(level, writer);

    if (geometry instanceof Point) {
      Point point = (Point) geometry;
      appendPointTaggedText(point.getCoordinate(), level, writer, point.getPrecisionModel());
    }
    else if (geometry instanceof LinearRing) {
      appendLinearRingTaggedText((LinearRing) geometry, level, writer);
    }
    else if (geometry instanceof LineString) {
      appendLineStringTaggedText((LineString) geometry, level, writer);
    }
    else if (geometry instanceof Polygon) {
      appendPolygon((Polygon) geometry, level, writer);
    }
    else if (geometry instanceof MultiPoint) {
      appendMultiPointTaggedText((MultiPoint) geometry, level, writer);
    }
    else if (geometry instanceof MultiLineString) {
      appendMultiLineStringTaggedText((MultiLineString) geometry, level, writer);
    }
    else if (geometry instanceof MultiPolygon) {
      appendMultiPolygonTaggedText((MultiPolygon) geometry, level, writer);
    }
    else if (geometry instanceof GeometryCollection) {
      appendGeometryCollectionTaggedText((GeometryCollection) geometry, level, writer);
    }
    else {
      Assert.shouldNeverReachHere("Unsupported Geometry implementation:"
           + geometry.getClass());
    }


  }

  /**
   *  Converts a <code>Coordinate</code> to &lt;Point Tagged Text&gt; format,
   *  then appends it to the writer.
   *
   *@param  coordinate      the <code>Coordinate</code> to process
   *@param  writer          the output writer to append to
   *@param  precisionModel  the <code>PrecisionModel</code> to use to convert
   *      from a precise coordinate to an external coordinate
   */
  private void appendPointTaggedText(Coordinate coordinate, int level, Writer writer,
      PrecisionModel precisionModel)
    throws IOException
  {
    appendPoint(coordinate, level, writer, precisionModel);
  }

  /**
   *  Converts a <code>LineString</code> to &lt;LineString Tagged Text&gt;
   *  format, then appends it to the writer.
   *
   *@param  lineString  the <code>LineString</code> to process
   *@param  writer      the output writer to append to
   */
  private void appendLineStringTaggedText(LineString lineString, int level, Writer writer)
    throws IOException
  {
    appendLineString(lineString, level, false, writer);
  }

  /**
   *  Converts a <code>LinearRing</code> to &lt;LinearRing Tagged Text&gt;
   *  format, then appends it to the writer.
   *
   *@param  linearRing  the <code>LinearRing</code> to process
   *@param  writer      the output writer to append to
   */
  private void appendLinearRingTaggedText(LinearRing linearRing, int level, Writer writer)
    throws IOException
  {
    appendLineString(linearRing, level, false, writer);
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
   *  Converts a <code>MultiPoint</code> to &lt;MultiPoint Tagged Text&gt;
   *  format, then appends it to the writer.
   *
   *@param  multipoint  the <code>MultiPoint</code> to process
   *@param  writer      the output writer to append to
   */
  private void appendMultiPointTaggedText(MultiPoint multipoint, int level, Writer writer)
    throws IOException
  {
    appendMultiPointText(multipoint, level, writer);
  }

  /**
   *  Converts a <code>MultiLineString</code> to &lt;MultiLineString Tagged
   *  Text&gt; format, then appends it to the writer.
   *
   *@param  multiLineString  the <code>MultiLineString</code> to process
   *@param  writer           the output writer to append to
   */
  private void appendMultiLineStringTaggedText(MultiLineString multiLineString, int level,
      Writer writer)
    throws IOException
  {
    appendMultiLineStringText(multiLineString, level, false, writer);
  }

  /**
   *  Converts a <code>MultiPolygon</code> to &lt;MultiPolygon Tagged Text&gt;
   *  format, then appends it to the writer.
   *
   *@param  multiPolygon  the <code>MultiPolygon</code> to process
   *@param  writer        the output writer to append to
   */
  private void appendMultiPolygonTaggedText(MultiPolygon multiPolygon, int level, Writer writer)
    throws IOException
  {
    appendMultiPolygonText(multiPolygon, level, writer);
  }

  /**
   *  Converts a <code>GeometryCollection</code> to &lt;GeometryCollection
   *  Tagged Text&gt; format, then appends it to the writer.
   *
   *@param  geometryCollection  the <code>GeometryCollection</code> to process
   *@param  writer              the output writer to append to
   */
  private void appendGeometryCollectionTaggedText(GeometryCollection geometryCollection, int level,
      Writer writer)
    throws IOException
  {
    appendGeometryCollectionText(geometryCollection, level, writer);
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
      writer.write("(");
      appendCoordinate(coordinate, writer);
      writer.write(")");
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
   *  Converts a <code>Coordinate</code> to <code>&lt;Point&gt;</code> format,
   *  then appends it to the writer.
   *
   *@param  coordinate      the <code>Coordinate</code> to process
   *@param  writer          the output writer to append to
   */
  private void appendCoordinate(Coordinate coordinate, Writer writer)
    throws IOException
  {
    writer.write(writeNumber(coordinate.x) + " " + writeNumber(coordinate.y));
    if (outputDimension >= 3 && ! Double.isNaN(coordinate.z)) {
      writer.write(" ");
      writer.write(writeNumber(coordinate.z));
    }
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

  private void appendPathStart(boolean useFillRule, Writer writer) throws IOException {
    String fillRule = useFillRule ? "fill-rule='evenodd' " :  "";
    writer.write("<path " + fillRule + "d='");
  }
  private void appendPathEnd(Writer writer) throws IOException {
    writer.write("' />\n");
  }


  /**
   *  Converts a <code>MultiPoint</code> to &lt;MultiPoint Text&gt; format, then
   *  appends it to the writer.
   *
   *@param  multiPoint  the <code>MultiPoint</code> to process
   *@param  writer      the output writer to append to
   */
  private void appendMultiPointText(MultiPoint multiPoint, int level, Writer writer)
    throws IOException
  {
    if (multiPoint.isEmpty()) {
      writer.write("EMPTY");
    }
    else {
      writer.write("(");
      for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
        if (i > 0) {
          writer.write(", ");
          indentCoords(i, level + 1, writer);
        }
        writer.write("(");
        appendCoordinate(((Point) multiPoint.getGeometryN(i)).getCoordinate(), writer);
        writer.write(")");
     }
      writer.write(")");
    }
  }

  /**
   *  Converts a <code>MultiLineString</code> to &lt;MultiLineString Text&gt;
   *  format, then appends it to the writer.
   *
   *@param  multiLineString  the <code>MultiLineString</code> to process
   *@param  writer           the output writer to append to
   */
  private void appendMultiLineStringText(MultiLineString multiLineString, int level, boolean indentFirst,
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
  private void appendMultiPolygonText(MultiPolygon multiPolygon, int level, Writer writer)
    throws IOException
  {
      int level2 = level;
      boolean doIndent = false;
      for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
        if (i > 0) {
          level2 = level + 1;
          doIndent = true;
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
  private void appendGeometryCollectionText(GeometryCollection geometryCollection, int level,
      Writer writer)
    throws IOException
  {
      int level2 = level;
      for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
        if (i > 0) {
          level2 = level + 1;
        }
        appendGeometryTaggedText(geometryCollection.getGeometryN(i), level2, writer);
      }
  }

  private void indentCoords(int coordIndex,  int level, Writer writer)
    throws IOException
  {
    if (coordsPerLine <= 0
        || coordIndex % coordsPerLine != 0)
      return;
    indent(level, writer);
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

