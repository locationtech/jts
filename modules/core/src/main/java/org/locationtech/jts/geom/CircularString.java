/*
 * Copyright (c) 2026 Jeroen Bloemscheer.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
/*
 * AI Disclosure: This file was largely AI-generated.
 * The AI-generated portions are made available under CC0-1.0 and not subject to the project's licence.
 * The human contributor has reviewed and verified that the code is correct.
 * SPDX-License-Identifier: EPL-2.0 OR EDL-1.0 and CC0-1.0
 * Assisted-by: Cursor Agent
 * Assisted-by: xAI Grok
 */
package org.locationtech.jts.geom;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.exactcurve.ExactCircularArc;

/**
 * A SQL/MM {@code CIRCULARSTRING}: a connected sequence of circular-arc
 * windows, each defined by three control points (start, mid, end).
 * The end of window {@code N} is the start of window {@code N+1}.
 * A CircularString is closed when the first control equals the last.
 * <p>
 * Control points <em>are</em> the stored {@link LineString} sequence.
 * {@link #getCoordinates()}, {@link #getNumPoints()},
 * {@link #getCoordinateN(int)} and {@link #getCoordinateSequence()}
 * speak those controls. Densify only via the named
 * {@link #toLinear(double)} path ({@link #linearize(double)} /
 * {@link #getLinearized(double)} are one-line aliases).
 * <p>
 * Alignment:
 * <ul>
 * <li>GeoTools {@code org.geotools.geometry.jts.CircularString} —
 *     subclass {@link LineString}; named {@code linearize(tolerance)}</li>
 * <li>GEOS {@code geos::geom::CircularString} — exact
 *     {@code getLength()} from composed arcs;
 *     named {@code getLinearized}</li>
 * <li>PostGIS {@code CIRCULARSTRING} WKT</li>
 * </ul>
 * Each window is a cached {@link ExactCircularArc}. Collinear windows
 * are exact chords ({@link ExactCircularArc#isExact()} stays true).
 * Length and envelope use those windows; they do not densify.
 * <p>
 * Full-circle convention: {@code CIRCULARSTRING(s, m, s)} is a circle
 * whose diameter is the segment {@code s–m} (center = midpoint of
 * {@code s} and {@code m}, radius = {@code |s-m|/2}).
 * <p>
 * {@code SegmentString} stays linear. This class is a geometry type,
 * not a noding type.
 *
 * @author Jeroen Bloemscheer
 * @see ExactCircularArc
 */
public class CircularString extends LineString {

  private static final long serialVersionUID = 20260916001L;

  /**
   * Minimum number of control points in a non-empty CircularString
   * (one 3-point window). Empty CircularStrings with 0 points are valid.
   */
  public static final int MINIMUM_VALID_SIZE = 3;

  /**
   * Default linearization tolerance: use
   * {@link ExactCircularArc#DEFAULT_SEGMENTS_PER_QUADRANT}.
   */
  public static final double DEFAULT_LINEARIZATION_TOLERANCE = Double.POSITIVE_INFINITY;

  private transient LineString linearized;
  private transient double linearizedTolerance = Double.NaN;
  private transient ExactCircularArc[] windows;

  /**
   * Constructs a CircularString from control points.
   * A null or empty sequence creates an empty CircularString.
   * The input sequence is copied.
   *
   * @param points control points, or {@code null} / empty for EMPTY
   * @param factory the geometry factory
   * @throws IllegalArgumentException if the point count is not 0 or an odd number &gt;= 3
   */
  public CircularString(CoordinateSequence points, GeometryFactory factory) {
    super(copyValidatedControls(points), factory);
  }

  private static CoordinateSequence copyValidatedControls(CoordinateSequence points) {
    if (points == null || points.size() == 0) {
      return points;
    }
    if (points.size() < MINIMUM_VALID_SIZE || (points.size() % 2) == 0) {
      throw new IllegalArgumentException("Invalid number of points in CircularString (found "
          + points.size() + " - must be 0 or an odd number >= " + MINIMUM_VALID_SIZE + ")");
    }
    return points.copy();
  }

  /**
   * Returns a copy of the SQL/MM control points (not linearized vertices).
   *
   * @return a deep copy of the control-point array
   */
  public Coordinate[] getControlPoints() {
    return CoordinateArrays.copyDeep(points.toCoordinateArray());
  }

  /**
   * Returns a copy of the control-point sequence used for WKT and windowing.
   *
   * @return a copy of the control-point sequence
   */
  public CoordinateSequence getControlPointSequence() {
    return points.copy();
  }

  /**
   * Gets the number of control points (same as {@link #getNumPoints()}).
   *
   * @return the control-point count
   */
  public int getNumControlPoints() {
    return getNumPoints();
  }

  /**
   * Gets the number of 3-point circular windows.
   *
   * @return {@code 0} if empty, otherwise {@code (nControl - 1) / 2}
   */
  public int getNumArcs() {
    if (isEmpty()) {
      return 0;
    }
    return (points.size() - 1) / 2;
  }

  /**
   * Returns the composed {@link ExactCircularArc} window at {@code arcIndex}.
   * Windows are cached; this does not allocate a new arc per call.
   *
   * @param arcIndex 0-based window index
   * @return the composed {@link ExactCircularArc}
   */
  public ExactCircularArc getArcN(int arcIndex) {
    ExactCircularArc[] arcs = windows();
    if (arcIndex < 0 || arcIndex >= arcs.length) {
      throw new IllegalArgumentException("arcIndex out of range: " + arcIndex);
    }
    return arcs[arcIndex];
  }

  private ExactCircularArc[] windows() {
    if (windows != null) {
      return windows;
    }
    int n = getNumArcs();
    ExactCircularArc[] arcs = new ExactCircularArc[n];
    for (int i = 0; i < n; i++) {
      int k = i * 2;
      arcs[i] = new ExactCircularArc(
          points.getCoordinate(k),
          points.getCoordinate(k + 1),
          points.getCoordinate(k + 2));
    }
    windows = arcs;
    return arcs;
  }

  /**
   * GeoTools-aligned alias of {@link #toLinear()}.
   *
   * @return a newly created LineString
   */
  public LineString linearize() {
    return toLinear();
  }

  /**
   * GeoTools-aligned alias of {@link #toLinear(double)}.
   *
   * @param tolerance max distance from the true arcs
   * @return a newly created LineString
   */
  public LineString linearize(double tolerance) {
    return toLinear(tolerance);
  }

  /**
   * GEOS-aligned alias of {@link #toLinear()}.
   *
   * @return a newly created LineString
   */
  public LineString getLinearized() {
    return toLinear();
  }

  /**
   * GEOS-aligned alias of {@link #toLinear(double)}.
   *
   * @param tolerance max distance from the true arcs
   * @return a newly created LineString
   */
  public LineString getLinearized(double tolerance) {
    return toLinear(tolerance);
  }

  /**
   * Densifies this CircularString to a {@link LineString}
   * at {@link #DEFAULT_LINEARIZATION_TOLERANCE}.
   *
   * @return a newly created LineString
   */
  public LineString toLinear() {
    return toLinear(DEFAULT_LINEARIZATION_TOLERANCE);
  }

  /**
   * Named Year-1 densify path (primary name). GeoTools {@code linearize}
   * and GEOS {@code getLinearized} delegate here.
   *
   * @param tolerance max distance from the true arcs; {@code 0} uses the
   *        maximum segment count; non-finite uses the default quadrant count
   * @return a newly created LineString
   */
  public LineString toLinear(double tolerance) {
    if (isEmpty()) {
      return getFactory().createLineString();
    }
    if (linearized != null && doubleEquals(linearizedTolerance, tolerance)) {
      return (LineString) linearized.copy();
    }
    CoordinateSequence seq = createLinearizedSequence(tolerance);
    LineString result = getFactory().createLineString(seq);
    linearized = result;
    linearizedTolerance = tolerance;
    return (LineString) result.copy();
  }

  /**
   * Returns a freshly copied linearized coordinate sequence
   * (caller mutation cannot affect this geometry).
   *
   * @param tolerance linearization tolerance
   * @return a new coordinate sequence
   */
  public CoordinateSequence getLinearizedCoordinateSequence(double tolerance) {
    return createLinearizedSequence(tolerance);
  }

  private CoordinateSequence createLinearizedSequence(double tolerance) {
    if (isEmpty()) {
      return getFactory().getCoordinateSequenceFactory().create(new Coordinate[] {});
    }
    List<Coordinate> coords = new ArrayList<Coordinate>();
    ExactCircularArc[] arcs = windows();
    for (int i = 0; i < arcs.length; i++) {
      arcs[i].appendLinearized(coords, tolerance, i == 0);
    }
    return getFactory().getCoordinateSequenceFactory().create(
        coords.toArray(new Coordinate[coords.size()]));
  }

  public String getGeometryType() {
    return Geometry.TYPENAME_CIRCULARSTRING;
  }

  protected int getTypeCode() {
    return Geometry.TYPECODE_CIRCULARSTRING;
  }

  protected boolean isEquivalentClass(Geometry other) {
    return other instanceof CircularString;
  }

  public boolean isClosed() {
    if (isEmpty()) {
      return false;
    }
    return points.getCoordinate(0).equals2D(points.getCoordinate(points.size() - 1));
  }

  public boolean isEmpty() {
    return points.size() == 0;
  }

  public Point getStartPoint() {
    if (isEmpty()) {
      return null;
    }
    return getFactory().createPoint(points.getCoordinate(0));
  }

  public Point getEndPoint() {
    if (isEmpty()) {
      return null;
    }
    return getFactory().createPoint(points.getCoordinate(points.size() - 1));
  }

  /**
   * Exact circular length: sum of composed {@link ExactCircularArc#length()}.
   * Collinear windows contribute the chord path. Does not densify.
   */
  public double getLength() {
    if (isEmpty()) {
      return 0.0;
    }
    double tot = 0.0;
    ExactCircularArc[] arcs = windows();
    for (int i = 0; i < arcs.length; i++) {
      tot += arcs[i].length();
    }
    return tot;
  }

  /**
   * Control points (deep copy). Does not linearize.
   */
  public Coordinate[] getCoordinates() {
    return CoordinateArrays.copyDeep(points.toCoordinateArray());
  }

  /**
   * Control-point sequence (copy). Does not linearize.
   */
  public CoordinateSequence getCoordinateSequence() {
    return points.copy();
  }

  /**
   * Control point at index {@code n} (copy). Does not linearize.
   */
  public Coordinate getCoordinateN(int n) {
    return points.getCoordinate(n).copy();
  }

  public Coordinate getCoordinate() {
    if (isEmpty()) {
      return null;
    }
    return points.getCoordinate(0).copy();
  }

  /**
   * Number of control points. Does not linearize.
   */
  public int getNumPoints() {
    return points.size();
  }

  public Point getPointN(int n) {
    return getFactory().createPoint(getCoordinateN(n));
  }

  public boolean isCoordinate(Coordinate pt) {
    for (int i = 0; i < points.size(); i++) {
      if (points.getCoordinate(i).equals(pt)) {
        return true;
      }
    }
    return false;
  }

  protected Envelope computeEnvelopeInternal() {
    Envelope env = new Envelope();
    ExactCircularArc[] arcs = windows();
    for (int i = 0; i < arcs.length; i++) {
      arcs[i].expandEnvelope(env);
    }
    return env;
  }

  public boolean equalsExact(Geometry other, double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    CircularString cs = (CircularString) other;
    if (points.size() != cs.points.size()) {
      return false;
    }
    for (int i = 0; i < points.size(); i++) {
      if (!equal(points.getCoordinate(i), cs.points.getCoordinate(i), tolerance)) {
        return false;
      }
    }
    return true;
  }

  public void apply(CoordinateFilter filter) {
    for (int i = 0; i < points.size(); i++) {
      filter.filter(points.getCoordinate(i));
    }
  }

  public void apply(CoordinateSequenceFilter filter) {
    if (points.size() == 0) {
      return;
    }
    for (int i = 0; i < points.size(); i++) {
      filter.filter(points, i);
      if (filter.isDone()) {
        break;
      }
    }
    if (filter.isGeometryChanged()) {
      geometryChanged();
    }
  }

  protected void geometryChangedAction() {
    super.geometryChangedAction();
    linearized = null;
    linearizedTolerance = Double.NaN;
    windows = null;
  }

  public CircularString reverse() {
    return (CircularString) super.reverse();
  }

  protected CircularString reverseInternal() {
    CoordinateSequence seq = points.copy();
    CoordinateSequences.reverse(seq);
    return getFactory().createCircularString(seq);
  }

  protected CircularString copyInternal() {
    return new CircularString(points.copy(), factory);
  }

  public void normalize() {
    if (isEmpty()) {
      return;
    }
    for (int i = 0; i < points.size() / 2; i++) {
      int j = points.size() - 1 - i;
      if (!points.getCoordinate(i).equals(points.getCoordinate(j))) {
        if (points.getCoordinate(i).compareTo(points.getCoordinate(j)) > 0) {
          CoordinateSequence copy = points.copy();
          CoordinateSequences.reverse(copy);
          points = copy;
          geometryChanged();
        }
        return;
      }
    }
  }

  protected int compareToSameClass(Object o) {
    CircularString other = (CircularString) o;
    int i = 0;
    int j = 0;
    while (i < points.size() && j < other.points.size()) {
      int comparison = points.getCoordinate(i).compareTo(other.points.getCoordinate(j));
      if (comparison != 0) {
        return comparison;
      }
      i++;
      j++;
    }
    if (i < points.size()) {
      return 1;
    }
    if (j < other.points.size()) {
      return -1;
    }
    return 0;
  }

  protected int compareToSameClass(Object o, CoordinateSequenceComparator comp) {
    CircularString other = (CircularString) o;
    return comp.compare(this.points, other.points);
  }

  private static boolean doubleEquals(double a, double b) {
    if (Double.isNaN(a) || Double.isNaN(b)) {
      return false;
    }
    if (a == b) {
      return true;
    }
    return Double.isInfinite(a) && Double.isInfinite(b) && Math.signum(a) == Math.signum(b);
  }
}
