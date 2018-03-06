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
package org.locationtech.jts.io;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.util.Assert;

/**
 * Writes the Well-Known Text representation of a {@link Geometry}.
 * The Well-Known Text format is defined in the
 * OGC <a href="http://www.opengis.org/techno/specs.htm">
 * <i>Simple Features Specification for SQL</i></a>.
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
public class WKTWriter
{
  /**
   * Generates the WKT for a <tt>POINT</tt>
   * specified by a {@link Coordinate}.
   *
   * @param p0 the point coordinate
   *
   * @return the WKT
   */
  public static String toPoint(Coordinate p0)
  {
    return "POINT ( " + p0.x + " " + p0.y  + " )";
  }

  /**
   * Generates the WKT for a <tt>LINESTRING</tt>
   * specified by a {@link CoordinateSequence}.
   *
   * @param seq the sequence to write
   *
   * @return the WKT string
   */
  public static String toLineString(CoordinateSequence seq)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("LINESTRING ");
    if (seq.size() == 0)
      buf.append(" EMPTY");
    else {
      buf.append("(");
      for (int i = 0; i < seq.size(); i++) {
        if (i > 0)
          buf.append(", ");
        buf.append(String.format(Locale.US, "%1$G %2$G", seq.getX(i), seq.getY(i)));
      }
      buf.append(")");
    }
    return buf.toString();
  }
  
  /**
   * Generates the WKT for a <tt>LINESTRING</tt>
   * specified by a {@link CoordinateSequence}.
   *
   * @param coord the sequence to write
   *
   * @return the WKT string
   */
  public static String toLineString(Coordinate[] coord)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("LINESTRING ");
    if (coord.length == 0)
      buf.append(" EMPTY");
    else {
      buf.append("(");
      for (int i = 0; i < coord.length; i++) {
        if (i > 0)
          buf.append(", ");
        buf.append(String.format(Locale.US, "%1$G %2$G", coord[i].x, coord[i].y));
      }
      buf.append(")");
    }
    return buf.toString();
  }

  /**
   * Generates the WKT for a <tt>LINESTRING</tt>
   * specified by two {@link Coordinate}s.
   *
   * @param p0 the first coordinate
   * @param p1 the second coordinate
   *
   * @return the WKT
   */
  public static String toLineString(Coordinate p0, Coordinate p1)
  {
    return "LINESTRING ( " + p0.x + " " + p0.y + ", " + p1.x + " " + p1.y + " )";
  }

  private static final int INDENT = 2;
  private static final int OUTPUT_DIMENSION = 2;

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
    StringBuilder buf = new StringBuilder(count);
    for (int i = 0; i < count; i++) {
      buf.append(ch);
    }
    return buf.toString();
  }

  private int outputDimension;
  private PrecisionModel precisionModel = null;
  private boolean isFormatted = false;
  private int coordsPerLine = -1;
  private String indentTabStr ;
  private boolean zIsMeasure = false;

  /**
   * Creates a new WKTWriter with default settings
   */
  public WKTWriter()
  {
    this(OUTPUT_DIMENSION);
  }

  /**
   * Creates a writer that writes {@link Geometry}s with
   * the given output dimension (2 to 4).
   * The output follows the following rules:
   * <ul>
   *   <li>If the specified <b>output dimension is 3</b> and the <b>z is measure flag
   *   is set to true</b>, the Z value of coordinates will be written if it is present
   * (i.e. if it is not <code>Double.NaN</code>)</li>
   *   <li>If the specified <b>output dimension is 3</b> and the <b>z is measure flag
   *   is set to false</b>, the Measure value of coordinates will be written if it is present
   * (i.e. if it is not <code>Double.NaN</code>)</li>
   *   <li>If the specified <b>output dimension is 4</b>, the Z value of coordinates will
   *   be written even if it is not present when the Measure value is present.The Measrue
   *   value of coordinates will be written if it is present
   * (i.e. if it is not <code>Double.NaN</code>)</li>
   * </ul>
   *
   * @param outputDimension the coordinate dimension to output (2 to 4)
   */
  public WKTWriter(int outputDimension) {

    setTab(INDENT);
    this.outputDimension = outputDimension;

    if (outputDimension < 2 || outputDimension > 4)
      throw new IllegalArgumentException("Invalid output dimension (must be 2 to 4)");
  }

  /**
   * Sets whether the output will be formatted.
   *
   * @param isFormatted true if the output is to be formatted
   */
  public void setFormatted(boolean isFormatted)
  {
    this.isFormatted = isFormatted;
  }

  /**
   * Sets the maximum number of coordinates per line
   * written in formatted output.
   * If the provided coordinate number is &lt;= 0,
   * coordinates will be written all on one line.
   *
   * @param coordsPerLine the number of coordinates per line to output.
   */
  public void setMaxCoordinatesPerLine(int coordsPerLine)
  {
    this.coordsPerLine = coordsPerLine;
  }

  /**
   * Sets the tab size to use for indenting.
   *
   * @param size the number of spaces to use as the tab string
   * @throws IllegalArgumentException if the size is non-positive
   */
  public void setTab(int size)
  {
    if(size <= 0)
      throw new IllegalArgumentException("Tab count must be positive");
    this.indentTabStr = stringOfChar(' ', size);
  }

  /**
   * Sets a flag indicating that the {@link Coordinate#z} value should be
   * interpreted as a measure value. This way {@code Geometry M} can be written.
   *
   * @param zIsMeasure the flag indicating if {@link Coordinate#z} is actually a
   *                   measure value.
   */
  public void setZIsMeasure(boolean zIsMeasure) {
    this.zIsMeasure = zIsMeasure;
  }

  /**
   * Sets a {@link PrecisionModel} that should be used on the ordinates written.
   * <p>If none/{@code null} is assigned, the precision model of the {@link Geometry#getFactory()}
   * is used.</p>
   * <p>Note: The precision model is applied to all ordinate values, not just x and y.</p>
   * @param precisionModel
   *    the flag indicating if {@link Coordinate#z}/{} is actually a measure value.
   */
  public void setPrecisionModel(PrecisionModel precisionModel) {
    this.precisionModel = precisionModel;
  }

  /**
   * Gets a value indicating if the z-ordinate value should be treated as a measure value
   * @return {@code true} if the z-ordinate value should be treated as a measrue value.
   */
  public boolean getZIsMeasure() {
    return this.zIsMeasure;
  }
  /**
   *  Converts a <code>Geometry</code> to its Well-known Text representation.
   *
   *@param  geometry  a <code>Geometry</code> to process
   *@return           a &lt;Geometry Tagged Text&gt; string (see the OpenGIS Simple
   *      Features Specification)
   */
  public String write(Geometry geometry)
  {
    Writer sw = new StringWriter();

    // determine the precision model
    PrecisionModel pm = this.precisionModel;
    if (pm == null) pm = geometry.getFactory().getPrecisionModel();

    try {
      writeFormatted(geometry, false, sw, pm);
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
    // determine the precision model
    PrecisionModel pm = this.precisionModel;
    if (pm == null) pm = geometry.getFactory().getPrecisionModel();

    // write the geometry
    writeFormatted(geometry, isFormatted, writer, pm);
  }

  /**
   *  Same as <code>write</code>, but with newlines and spaces to make the
   *  well-known text more readable.
   *
   *@param  geometry  a <code>Geometry</code> to process
   *@return           a &lt;Geometry Tagged Text&gt; string (see the OpenGIS Simple
   *      Features Specification), with newlines and spaces
   */
  public String writeFormatted(Geometry geometry)
  {
    Writer sw = new StringWriter();
    try {
      writeFormatted(geometry, true, sw, precisionModel);
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
    writeFormatted(geometry, true, writer, precisionModel);
  }
  /**
   *  Converts a <code>Geometry</code> to its Well-known Text representation.
   *
   *@param  geometry  a <code>Geometry</code> to process
   */
  private void writeFormatted(Geometry geometry, boolean useFormatting, Writer writer,
                              PrecisionModel precisionModel)
    throws IOException
  {
    // ensure we have a precision model
    if (precisionModel == null)
      precisionModel = geometry.getPrecisionModel();

    DecimalFormat formatter = createFormatter(precisionModel);
    appendGeometryTaggedText(geometry, useFormatting, 0, writer, formatter);
  }

  /**
   *  Converts a <code>Geometry</code> to &lt;Geometry Tagged Text&gt; format,
   *  then appends it to the writer.
   *
   * @param  geometry           the <code>Geometry</code> to process
   * @param  useFormatting      flag indicating that the output should be formatted
   * @param  level              the indentation level
   * @param  writer             the output writer to append to
   * @param  formatter       the <code>DecimalFormatter</code> to use to convert
   *      from a precise coordinate to an external coordinate
   */
  private void appendGeometryTaggedText(Geometry geometry, boolean useFormatting, int level, Writer writer,
                                        DecimalFormat formatter)
    throws IOException
  {
    indent(useFormatting, level, writer);

    if (geometry instanceof Point) {
      appendPointTaggedText((Point) geometry, useFormatting, level, writer, formatter);
    }
    else if (geometry instanceof LinearRing) {
      appendLinearRingTaggedText((LinearRing) geometry, useFormatting, level, writer, formatter);
    }
    else if (geometry instanceof LineString) {
      appendLineStringTaggedText((LineString) geometry, useFormatting, level, writer, formatter);
    }
    else if (geometry instanceof Polygon) {
      appendPolygonTaggedText((Polygon) geometry, useFormatting, level, writer, formatter);
    }
    else if (geometry instanceof MultiPoint) {
      appendMultiPointTaggedText((MultiPoint) geometry, useFormatting, level, writer, formatter);
    }
    else if (geometry instanceof MultiLineString) {
      appendMultiLineStringTaggedText((MultiLineString) geometry, useFormatting, level, writer, formatter);
    }
    else if (geometry instanceof MultiPolygon) {
      appendMultiPolygonTaggedText((MultiPolygon) geometry, useFormatting, level, writer, formatter);
    }
    else if (geometry instanceof GeometryCollection) {
      appendGeometryCollectionTaggedText((GeometryCollection) geometry, useFormatting, level, writer, formatter);
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
   * @param  point           the <code>Point</code> to process
   * @param  useFormatting      flag indicating that the output should be formatted
   * @param  level              the indentation level
   * @param  writer             the output writer to append to
   * @param  formatter          the formatter to use when writing numbers
   */
  private void appendPointTaggedText(Point point, boolean useFormatting, int level, Writer writer, DecimalFormat formatter)
    throws IOException
  {
    writer.write("POINT ");
    appendOrdinateText(point.getCoordinateSequence(), writer);
    appendSequenceText(point.getCoordinateSequence(), useFormatting, level, false, writer, formatter);
  }

  /**
   *  Converts a <code>LineString</code> to &lt;LineString Tagged Text&gt;
   *  format, then appends it to the writer.
   *
   * @param  lineString  the <code>LineString</code> to process
   * @param  useFormatting      flag indicating that the output should be formatted
   * @param  level              the indentation level
   * @param  writer             the output writer to append to
   * @param  formatter       the <code>DecimalFormatter</code> to use to convert
   *      from a precise coordinate to an external coordinate
   */
  private void appendLineStringTaggedText(LineString lineString, boolean useFormatting, int level, Writer writer,
                                          DecimalFormat formatter)
    throws IOException
  {
    writer.write("LINESTRING ");
    appendOrdinateText(lineString.getCoordinateSequence(), writer);
    appendSequenceText(lineString.getCoordinateSequence(), useFormatting, level, false, writer, formatter);
  }

  /**
   *  Converts a <code>LinearRing</code> to &lt;LinearRing Tagged Text&gt;
   *  format, then appends it to the writer.
   *
   * @param  linearRing  the <code>LinearRing</code> to process
   * @param  useFormatting      flag indicating that the output should be formatted
   * @param  level              the indentation level
   * @param  writer             the output writer to append to
   * @param  formatter       the <code>DecimalFormatter</code> to use to convert
   *      from a precise coordinate to an external coordinate
   */
  private void appendLinearRingTaggedText(LinearRing linearRing, boolean useFormatting, int level, Writer writer,
                                          DecimalFormat formatter)
    throws IOException
  {
    writer.write("LINEARRING ");
    appendOrdinateText(linearRing.getCoordinateSequence(), writer);
    appendSequenceText(linearRing.getCoordinateSequence(), useFormatting, level, false, writer, formatter);
  }

  /**
   *  Converts a <code>Polygon</code> to &lt;Polygon Tagged Text&gt; format,
   *  then appends it to the writer.
   *
   * @param  polygon  the <code>Polygon</code> to process
   * @param  useFormatting      flag indicating that the output should be formatted
   * @param  level              the indentation level
   * @param  writer             the output writer to append to
   * @param  formatter       the <code>DecimalFormatter</code> to use to convert
   *      from a precise coordinate to an external coordinate
   */
  private void appendPolygonTaggedText(Polygon polygon, boolean useFormatting, int level, Writer writer,
                                       DecimalFormat formatter)
    throws IOException
  {
    writer.write("POLYGON ");
    appendOrdinateText(polygon.getExteriorRing().getCoordinateSequence(), writer);
    appendPolygonText(polygon, useFormatting, level, false, writer, formatter);
  }

  /**
   *  Converts a <code>MultiPoint</code> to &lt;MultiPoint Tagged Text&gt;
   *  format, then appends it to the writer.
   *
   * @param  multipoint  the <code>MultiPoint</code> to process
   * @param  useFormatting      flag indicating that the output should be formatted
   * @param  level              the indentation level
   * @param  writer             the output writer to append to
   * @param  formatter       the <code>DecimalFormatter</code> to use to convert
   *      from a precise coordinate to an external coordinate
   */
  private void appendMultiPointTaggedText(MultiPoint multipoint, boolean useFormatting, int level, Writer writer,
                                          DecimalFormat formatter)
    throws IOException
  {
    writer.write("MULTIPOINT ");
    appendOrdinateText(findFirstSequence(multipoint), writer);
    appendMultiPointText(multipoint, useFormatting, level, writer, formatter);
  }

  /**
   *  Converts a <code>MultiLineString</code> to &lt;MultiLineString Tagged
   *  Text&gt; format, then appends it to the writer.
   *
   * @param  multiLineString  the <code>MultiLineString</code> to process
   * @param  useFormatting      flag indicating that the output should be formatted
   * @param  level              the indentation level
   * @param  writer             the output writer to append to
   * @param  formatter       the <code>DecimalFormatter</code> to use to convert
   *      from a precise coordinate to an external coordinate
   */
  private void appendMultiLineStringTaggedText(MultiLineString multiLineString, boolean useFormatting, int level,
      Writer writer, DecimalFormat formatter)
    throws IOException
  {
    writer.write("MULTILINESTRING ");
    appendOrdinateText(findFirstSequence(multiLineString), writer);
    appendMultiLineStringText(multiLineString, useFormatting, level, false, writer, formatter);
  }

  /**
   *  Converts a <code>MultiPolygon</code> to &lt;MultiPolygon Tagged Text&gt;
   *  format, then appends it to the writer.
   *
   * @param  multiPolygon  the <code>MultiPolygon</code> to process
   * @param  useFormatting      flag indicating that the output should be formatted
   * @param  level              the indentation level
   * @param  writer             the output writer to append to
   * @param  formatter       the <code>DecimalFormatter</code> to use to convert
   *      from a precise coordinate to an external coordinate
   */
  private void appendMultiPolygonTaggedText(MultiPolygon multiPolygon, boolean useFormatting, int level, Writer writer,
                                            DecimalFormat formatter)
    throws IOException
  {
    writer.write("MULTIPOLYGON ");
    appendOrdinateText(findFirstSequence(multiPolygon), writer);
    appendMultiPolygonText(multiPolygon, useFormatting, level, writer, formatter);
  }

  /**
   *  Converts a <code>GeometryCollection</code> to &lt;GeometryCollection
   *  Tagged Text&gt; format, then appends it to the writer.
   *
   * @param  geometryCollection  the <code>GeometryCollection</code> to process
   * @param  useFormatting      flag indicating that the output should be formatted
   * @param  level              the indentation level
   * @param  writer             the output writer to append to
   * @param  formatter       the <code>DecimalFormatter</code> to use to convert
   *      from a precise coordinate to an external coordinate
   */
  private void appendGeometryCollectionTaggedText(GeometryCollection geometryCollection, boolean useFormatting,
      int level, Writer writer, DecimalFormat formatter)
    throws IOException
  {
    writer.write("GEOMETRYCOLLECTION ");
    appendOrdinateText(findFirstSequence(geometryCollection), writer);
    appendGeometryCollectionText(geometryCollection, useFormatting, level, writer, formatter);
  }

  /**
   * Utility function to find the first {@link CoordinateSequence} that a geometry in {@code gc} has.
   *
   * @param gc a geometry collection
   * @return A coordinate sequence.
   */
  private CoordinateSequence findFirstSequence(GeometryCollection gc) {

    for (int i = 0; i < gc.getNumGeometries(); i++) {
      Geometry g = gc.getGeometryN(i);
      if (g instanceof Point)
        return ((Point)g).getCoordinateSequence();
      else if (g instanceof  LineString)
        return ((LineString)g).getCoordinateSequence();
      else if (g instanceof  Polygon)
      {
        if (!g.isEmpty())
          return ((Polygon)g).getExteriorRing().getCoordinateSequence();
      }
      else if (g instanceof  GeometryCollection)
      {
        CoordinateSequence res = findFirstSequence((GeometryCollection)g);
        if (res != null) return res;
      }
    }

    // Havn't found anything, return default
    CoordinateSequenceFactory csFactory = gc.getFactory().getCoordinateSequenceFactory();
    return csFactory.create(0, outputDimension);
  }

  /**
   * Appends the i'th coordinate from the sequence to the writer
   * <p>If the {@code seq} has coordinates that are {@link double.NAN}, these are not written, even though
   * {@link #outputDimension} suggests this.
   *
   * @param  seq        the <code>CoordinateSequence</code> to process
   * @param  i          the index of the coordinate to write
   * @param  writer     the output writer to append to
   * @param  formatter  the formatter to use for writing ordinate values
   */
  private void appendCoordinate(CoordinateSequence seq, int i, Writer writer, DecimalFormat formatter)
      throws IOException
  {
    writer.write(writeNumber(seq.getX(i), formatter) + " " + writeNumber(seq.getY(i), formatter));
    int dimension = outputDimension < seq.getDimension() ? outputDimension : seq.getDimension();
    if (dimension == 2) return;

    // we have three dimensions, either z- or measure-ordinate
    if (dimension == 3) {
      double val = (zIsMeasure & seq.getDimension() > 3)
              ? seq.getOrdinate(i, CoordinateSequence.M)
              : seq.getOrdinate(i, CoordinateSequence.Z);
      if (! Double.isNaN(val)) {
        writer.write(" ");
        writer.write(writeNumber(val, formatter));
      }
      return;
    }

    // we have 4 dimensions!
    double z = seq.getOrdinate(i, CoordinateSequence.Z);
    double m = seq.getOrdinate(i, CoordinateSequence.M);
    if (!Double.isNaN(z) | !Double.isNaN(m)) {
      writer.write(" ");
      writer.write(writeNumber(z, formatter));
    }

    if (!Double.isNaN(m)) {
      writer.write(" ");
      writer.write(writeNumber(z, formatter));
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
  private static String writeNumber(double d, DecimalFormat formatter) {
    return formatter.format(d);
  }

  /**
   * Appends additional ordinate information. This function may
   * <ul>
   *   <li>append 'Z' if the {@link #outputDimension} is 3,
   *   {@code seq} has a dimension of 3 and {@link #zIsMeasure}
   *   is {@code false}
   *   </li>
   *   <li>append 'M' if the {@link #outputDimension} is 3,
   *   {@code seq} has a dimension of 4 and {@link #zIsMeasure}
   *   is {@code true}
   *   </li>
   *   <li> append 'ZM' if {@link #outputDimension} and the
   *   dimension of {@code seq} are both 4</li>
   * </ul>
   *
   * @param seq        a {@code CoordinateSequence} to test the output dimensions.
   * @param writer     the output writer to append to.
   * @throws IOException if an error occurs while using the writer.
   */
  private void appendOrdinateText(CoordinateSequence seq, Writer writer) throws IOException {

    if (seq.size() > 0) {

      int dimensionToUse = seq.getDimension();
      if (outputDimension < dimensionToUse) dimensionToUse = outputDimension;

      switch (dimensionToUse) {
        case 2:
          break;
        case 3: // assume /
          writer.write(!(zIsMeasure & seq.getDimension()==3) ? "Z" : "M");
          break;
        case 4:
          writer.write("ZM");
          break;
      }
    }
  }
  /**
   *  Appends all members of a <code>CoordinateSequence</code> to the stream. Each {@code Coordinate} is separated from
   *  another using a colon, the ordinates of a {@code Coordinate} are separated by a space.
   *
   * @param  seq             the <code>CoordinateSequence</code> to process
   * @param  useFormatting   flag indicating that
   * @param  level           the indentation level
   * @param  indentFirst     flag indicating that the first {@code Coordinate} of the sequence should be indented for
   *                         better visibility
   * @param  writer          the output writer to append to
   * @param  formatter       the formatter to use for writing ordinate values.
   */
  private void appendSequenceText(CoordinateSequence seq, boolean useFormatting, int level, boolean indentFirst,
                                  Writer writer, DecimalFormat formatter)
    throws IOException
  {
    if (seq.size() == 0) {
      writer.write("EMPTY");
    }
    else {
      if (indentFirst) indent(useFormatting, level, writer);
      writer.write("(");
      for (int i = 0; i < seq.size(); i++) {
        if (i > 0) {
          writer.write(", ");
          if (coordsPerLine > 0
              && i % coordsPerLine == 0) {
            indent(useFormatting, level + 1, writer);
          }
        }
        appendCoordinate(seq, i, writer, formatter);
      }
      writer.write(")");
    }
  }

  /**
   *  Converts a <code>Polygon</code> to &lt;Polygon Text&gt; format, then
   *  appends it to the writer.
   *
   * @param  polygon         the <code>Polygon</code> to process
   * @param  useFormatting   flag indicating that
   * @param  level           the indentation level
   * @param  indentFirst     flag indicating that the first {@code Coordinate} of the sequence should be indented for
   *                         better visibility
   * @param  writer          the output writer to append to
   * @param  formatter       the formatter to use for writing ordinate values.
   */
  private void appendPolygonText(Polygon polygon, boolean useFormatting, int level, boolean indentFirst, Writer writer,
                                 DecimalFormat formatter)
    throws IOException
  {
    if (polygon.isEmpty()) {
      writer.write("EMPTY");
    }
    else {
      if (indentFirst) indent(useFormatting, level, writer);
      writer.write("(");
      appendSequenceText(polygon.getExteriorRing().getCoordinateSequence(),
              useFormatting, level, false, writer, formatter);
      for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
        writer.write(", ");
        appendSequenceText(polygon.getInteriorRingN(i).getCoordinateSequence(),
              useFormatting,level + 1,true, writer, formatter);
      }
      writer.write(")");
    }
  }

  /**
   *  Converts a <code>MultiPoint</code> to &lt;MultiPoint Text&gt; format, then
   *  appends it to the writer.
   *
   * @param  multiPoint      the <code>MultiPoint</code> to process
   * @param  useFormatting   flag indicating that
   * @param  level           the indentation level
   * @param  writer          the output writer to append to
   * @param  formatter       the formatter to use for writing ordinate values.
   */
  private void appendMultiPointText(MultiPoint multiPoint, boolean useFormatting, int level, Writer writer,
                                    DecimalFormat formatter)
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
          indentCoords(useFormatting, i, level + 1, writer);
        }
        appendSequenceText(((Point) multiPoint.getGeometryN(i)).getCoordinateSequence(),
                useFormatting, level, false, writer, formatter);
     }
      writer.write(")");
    }
  }

  /**
   *  Converts a <code>MultiLineString</code> to &lt;MultiLineString Text&gt;
   *  format, then appends it to the writer.
   *
   * @param  multiLineString  the <code>MultiLineString</code> to process
   * @param  useFormatting    flag indicating that
   * @param  level            the indentation level
   * @param  indentFirst      flag indicating that the first {@code Coordinate} of the sequence should be indented for
   *                          better visibility
   * @param  writer           the output writer to append to
   * @param  formatter        the formatter to use for writing ordinate values.
   */
  private void appendMultiLineStringText(MultiLineString multiLineString, boolean useFormatting, int level,
                                         boolean indentFirst, Writer writer, DecimalFormat formatter)
    throws IOException
  {
    if (multiLineString.isEmpty()) {
      writer.write("EMPTY");
    }
    else {
      int level2 = level;
      boolean doIndent = indentFirst;
      writer.write("(");
      for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
        if (i > 0) {
          writer.write(", ");
          level2 = level + 1;
          doIndent = true;
        }
        appendSequenceText(((LineString) multiLineString.getGeometryN(i)).getCoordinateSequence(),
                useFormatting, level2, doIndent, writer, formatter);
      }
      writer.write(")");
    }
  }

  /**
   *  Converts a <code>MultiPolygon</code> to &lt;MultiPolygon Text&gt; format,
   *  then appends it to the writer.
   *
   * @param  multiPolygon  the <code>MultiPolygon</code> to process
   * @param  useFormatting   flag indicating that
   * @param  level           the indentation level
   * @param  writer          the output writer to append to
   * @param  formatter       the formatter to use for writing ordinate values.
   */
  private void appendMultiPolygonText(MultiPolygon multiPolygon, boolean useFormatting, int level, Writer writer,
                                      DecimalFormat formatter)
    throws IOException
  {
    if (multiPolygon.isEmpty()) {
      writer.write("EMPTY");
    }
    else {
      int level2 = level;
      boolean doIndent = false;
      writer.write("(");
      for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
        if (i > 0) {
          writer.write(", ");
          level2 = level + 1;
          doIndent = true;
        }
        appendPolygonText((Polygon) multiPolygon.getGeometryN(i), useFormatting, level2, doIndent, writer, formatter);
      }
      writer.write(")");
    }
  }

  /**
   *  Converts a <code>GeometryCollection</code> to &lt;GeometryCollectionText&gt;
   *  format, then appends it to the writer.
   *
   * @param  geometryCollection  the <code>GeometryCollection</code> to process
   * @param  useFormatting   flag indicating that
   * @param  level           the indentation level
   * @param  writer          the output writer to append to
   * @param  formatter       the formatter to use for writing ordinate values.
   */
  private void appendGeometryCollectionText(GeometryCollection geometryCollection,
      boolean useFormatting, int level, Writer writer, DecimalFormat formatter)
    throws IOException
  {
    if (geometryCollection.isEmpty()) {
      writer.write("EMPTY");
    }
    else {
      int level2 = level;
      writer.write("(");
      for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
        if (i > 0) {
          writer.write(", ");
          level2 = level + 1;
        }
        appendGeometryTaggedText(geometryCollection.getGeometryN(i), useFormatting, level2, writer, formatter);
      }
      writer.write(")");
    }
  }

  private void indentCoords(boolean useFormatting, int coordIndex,  int level, Writer writer)
    throws IOException
  {
    if (coordsPerLine <= 0
        || coordIndex % coordsPerLine != 0)
      return;
    indent(useFormatting, level, writer);
  }

  private void indent(boolean useFormatting, int level, Writer writer)
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

