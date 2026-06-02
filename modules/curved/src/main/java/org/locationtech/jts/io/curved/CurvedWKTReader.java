/*
 * Copyright (c) 2026 grootstebozewolf
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io.curved;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.curved.CircularString;
import org.locationtech.jts.geom.curved.CompoundCurve;
import org.locationtech.jts.geom.curved.CurvePolygon;
import org.locationtech.jts.geom.curved.MultiCurve;
import org.locationtech.jts.geom.curved.MultiSurface;
import org.locationtech.jts.geom.curved.PolyhedralSurface;
import org.locationtech.jts.geom.curved.Tin;
import org.locationtech.jts.geom.curved.Triangle;
import org.locationtech.jts.io.Ordinate;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTConstants;
import org.locationtech.jts.io.WKTReader;

/**
 * A {@link WKTReader} subclass that recognises the OGC SFA / ISO 19125-2
 * extended geometry types via the {@code readOtherGeometryText} extension
 * point in core:
 * <ul>
 *   <li>{@code CIRCULARSTRING}</li>
 *   <li>{@code COMPOUNDCURVE}</li>
 *   <li>{@code CURVEPOLYGON}</li>
 *   <li>{@code MULTICURVE}</li>
 *   <li>{@code MULTISURFACE}</li>
 *   <li>{@code TRIANGLE}</li>
 *   <li>{@code POLYHEDRALSURFACE}</li>
 *   <li>{@code TIN}</li>
 * </ul>
 * <p>
 * This is a phase-1 implementation: composite types (CompoundCurve,
 * CurvePolygon, MultiCurve, MultiSurface) collapse member structure to
 * concatenated coordinates / linearised rings on read. The classes are
 * structurally simple wrappers over their parent geometry types so the
 * existing JTS algorithm suite continues to work, treating curves as
 * polylines and curve-bounded surfaces as polygons.
 */
public class CurvedWKTReader extends WKTReader {

  private static final String L_PAREN = "(";

  public CurvedWKTReader() {
    super();
  }

  public CurvedWKTReader(GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  @Override
  protected Geometry readOtherGeometryText(StreamTokenizer tokenizer, String type, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    if (isTypeName(tokenizer, type, WKTConstants.TRIANGLE)) {
      return readTriangleText(tokenizer, ordinateFlags);
    }
    if (isTypeName(tokenizer, type, WKTConstants.POLYHEDRALSURFACE)) {
      return readPolyhedralSurfaceText(tokenizer, ordinateFlags);
    }
    if (isTypeName(tokenizer, type, WKTConstants.TIN)) {
      return readTinText(tokenizer, ordinateFlags);
    }
    if (isTypeName(tokenizer, type, WKTConstants.CIRCULARSTRING)) {
      return readCircularStringText(tokenizer, ordinateFlags);
    }
    if (isTypeName(tokenizer, type, WKTConstants.COMPOUNDCURVE)) {
      return readCompoundCurveText(tokenizer, ordinateFlags);
    }
    if (isTypeName(tokenizer, type, WKTConstants.CURVEPOLYGON)) {
      return readCurvePolygonText(tokenizer, ordinateFlags);
    }
    if (isTypeName(tokenizer, type, WKTConstants.MULTICURVE)) {
      return readMultiCurveText(tokenizer, ordinateFlags);
    }
    if (isTypeName(tokenizer, type, WKTConstants.MULTISURFACE)) {
      return readMultiSurfaceText(tokenizer, ordinateFlags);
    }
    return super.readOtherGeometryText(tokenizer, type, ordinateFlags);
  }

  private Triangle readTriangleText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    Polygon p = readPolygonText(tokenizer, ordinateFlags);
    if (p.isEmpty()) return new Triangle(geometryFactory);
    return new Triangle((LinearRing) p.getExteriorRing(), geometryFactory);
  }

  private PolyhedralSurface readPolyhedralSurfaceText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    return new PolyhedralSurface(readPolygonArray(tokenizer, ordinateFlags), geometryFactory);
  }

  private Tin readTinText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    return new Tin(readPolygonArray(tokenizer, ordinateFlags), geometryFactory);
  }

  private Polygon[] readPolygonArray(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    String tok = getNextEmptyOrOpener(tokenizer);
    if (tok.equals(WKTConstants.EMPTY)) return new Polygon[0];
    List<Polygon> polygons = new ArrayList<Polygon>();
    do {
      polygons.add(readPolygonText(tokenizer, ordinateFlags));
      tok = getNextCloserOrComma(tokenizer);
    } while (tok.equals(","));
    return polygons.toArray(new Polygon[0]);
  }

  private CircularString readCircularStringText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    LineString ls = readLineStringText(tokenizer, ordinateFlags);
    return new CircularString(ls.getCoordinateSequence(), geometryFactory);
  }

  private CompoundCurve readCompoundCurveText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    String tok = getNextEmptyOrOpener(tokenizer);
    if (tok.equals(WKTConstants.EMPTY)) {
      return new CompoundCurve(createCoordinateSequenceEmpty(ordinateFlags), geometryFactory);
    }
    // Choose between SFA-structured form `((...), CIRCULARSTRING(...), ...)`
    // and legacy flat forms. CurvedWKTWriter now emits the standard member-structured form.
    String w = lookAheadWord(tokenizer);
    if (!w.equals(L_PAREN) && !isCurveMemberTag(w)) {
      List<Coordinate> coords = new ArrayList<Coordinate>();
      do {
        coords.add(getCoordinate(tokenizer, ordinateFlags, false));
      } while (getNextCloserOrComma(tokenizer).equals(","));
      return new CompoundCurve(csFactory.create(coords.toArray(new Coordinate[0])), geometryFactory);
    }
    List<Coordinate> all = new ArrayList<Coordinate>();
    do {
      Coordinate[] cc = readCurveMember(tokenizer, ordinateFlags).getCoordinates();
      int start = all.isEmpty() ? 0 : 1;
      for (int i = start; i < cc.length; i++) all.add(cc[i]);
      tok = getNextCloserOrComma(tokenizer);
    } while (tok.equals(","));
    CoordinateSequence seq = csFactory.create(all.toArray(new Coordinate[0]));
    return new CompoundCurve(seq, geometryFactory);
  }

  private CurvePolygon readCurvePolygonText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    String tok = getNextEmptyOrOpener(tokenizer);
    if (tok.equals(WKTConstants.EMPTY)) return new CurvePolygon(geometryFactory);
    // F-CP: collect every ring member structurally (LineString / Circular / Compound)
    // so CurvePolygon can expose them via getExteriorCurve / getInteriorCurveN.
    List<LineString> rings = new ArrayList<LineString>();
    do {
      rings.add(readCurveMember(tokenizer, ordinateFlags));
      tok = getNextCloserOrComma(tokenizer);
    } while (tok.equals(","));
    LineString shell = rings.remove(0);
    return new CurvePolygon(shell, rings.toArray(new LineString[0]), geometryFactory);
  }

  private MultiCurve readMultiCurveText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    String tok = getNextEmptyOrOpener(tokenizer);
    if (tok.equals(WKTConstants.EMPTY)) return new MultiCurve(new LineString[0], geometryFactory);
    List<LineString> members = new ArrayList<LineString>();
    do {
      members.add(readCurveMember(tokenizer, ordinateFlags));
      tok = getNextCloserOrComma(tokenizer);
    } while (tok.equals(","));
    return new MultiCurve(members.toArray(new LineString[0]), geometryFactory);
  }

  private MultiSurface readMultiSurfaceText(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    String tok = getNextEmptyOrOpener(tokenizer);
    if (tok.equals(WKTConstants.EMPTY)) return new MultiSurface(new Polygon[0], geometryFactory);
    List<Polygon> members = new ArrayList<Polygon>();
    do {
      members.add(readSurfaceMember(tokenizer, ordinateFlags));
      tok = getNextCloserOrComma(tokenizer);
    } while (tok.equals(","));
    return new MultiSurface(members.toArray(new Polygon[0]), geometryFactory);
  }

  /** Reads a curve aggregate member: untagged {@code (...)}, tagged
   *  CIRCULARSTRING / COMPOUNDCURVE, or EMPTY. Returns a LineString. */
  private LineString readCurveMember(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    String w = lookAheadWord(tokenizer);
    if (w.equals(L_PAREN)) return readLineStringText(tokenizer, ordinateFlags);
    if (w.equals(WKTConstants.EMPTY)) {
      getNextWord(tokenizer);
      return geometryFactory.createLineString(createCoordinateSequenceEmpty(ordinateFlags));
    }
    String type = getNextWord(tokenizer).toUpperCase(Locale.ROOT);
    Geometry g = readGeometryTaggedText(tokenizer, type, ordinateFlags);
    if (g instanceof LineString) return (LineString) g;
    throw parseErrorWithLine(tokenizer, "Expected curve member but got " + type);
  }

  /** Reads a surface aggregate member: untagged polygon body or tagged CURVEPOLYGON. */
  private Polygon readSurfaceMember(StreamTokenizer tokenizer, EnumSet<Ordinate> ordinateFlags)
      throws IOException, ParseException {
    String w = lookAheadWord(tokenizer);
    if (w.equals(L_PAREN)) return readPolygonText(tokenizer, ordinateFlags);
    String type = getNextWord(tokenizer).toUpperCase(Locale.ROOT);
    Geometry g = readGeometryTaggedText(tokenizer, type, ordinateFlags);
    if (g instanceof Polygon) return (Polygon) g;
    throw parseErrorWithLine(tokenizer, "Expected surface member but got " + type);
  }

  private static boolean isCurveMemberTag(String w) {
    return w.equalsIgnoreCase(WKTConstants.CIRCULARSTRING)
        || w.equalsIgnoreCase(WKTConstants.COMPOUNDCURVE);
  }
}
