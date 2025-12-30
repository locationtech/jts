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
package org.locationtech.jts.geom.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryComponentFilter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;

/**
 * Extracts all the 1-dimensional ({@link LineString}) components from a {@link Geometry}.
 * For polygonal geometries, this will extract all the component {@link LinearRing}s.
 * <p>
 * If desired, {@link LinearRing}s can be forced to be returned as {@link LineString}s.
 *
 * @version 1.7
 */
public class LinearComponentExtracter implements GeometryComponentFilter
{
  /**
   * Extracts the linear components from a collection of {@link Geometry}s
   * and adds them to the provided {@link Collection}.
   *
   * @param geoms the collection of geometries from which to extract linear components
   * @param out   the collection to add the extracted linear components to
   * @return the {@code out} collection of linear components (LineStrings or LinearRings)
   */
  public static Collection<? super LineString> getLines(
      Collection<? extends Geometry> geoms,
      Collection<? super LineString> out)
  {
    return getLines(geoms, out, false);
  }

  /**
   * Extracts the linear components from a collection of {@link Geometry}s
   * and adds them to the provided {@link Collection}.
   *
   * @param geoms             the collection of geometries from which to extract linear components
   * @param out               the collection to add the extracted linear components to
   * @param forceToLineString true if {@link LinearRing}s should be converted to {@link LineString}s
   * @return the {@code out} collection of linear components (LineStrings or LinearRings)
   */
  public static Collection<? super LineString> getLines(
      Collection<? extends Geometry> geoms,
      Collection<? super LineString> out,
      boolean forceToLineString)
  {
    for (Geometry g : geoms) {
      getLines(g, out, forceToLineString);
    }
    return out;
  }

  /**
   * Extracts the linear components from a single {@link Geometry}
   * and adds them to the provided {@link Collection}.
   *
   * @param geom the geometry from which to extract linear components
   * @param out  the collection to add the extracted linear components to
   * @return the {@code out} collection of linear components (LineStrings or LinearRings)
   */
  public static Collection<? super LineString> getLines(
      Geometry geom,
      Collection<? super LineString> out)
  {
    return getLines(geom, out, false);
  }

  /**
   * Extracts the linear components from a single {@link Geometry}
   * and adds them to the provided {@link Collection}.
   *
   * @param geom              the geometry from which to extract linear components
   * @param out               the collection to add the extracted linear components to
   * @param forceToLineString true if {@link LinearRing}s should be converted to {@link LineString}s
   * @return the {@code out} collection of linear components (LineStrings or LinearRings)
   */
  public static Collection<? super LineString> getLines(
      Geometry geom,
      Collection<? super LineString> out,
      boolean forceToLineString)
  {
    if (geom == null) return out;

    // Fast-path for single LineString inputs when not forcing ring conversion.
    // (If forcing, we still run the filter so LinearRing gets converted.)
    if (!forceToLineString && geom instanceof LineString) {
      out.add((LineString) geom);
      return out;
    }

    geom.apply(new LinearComponentExtracter(out, forceToLineString));
    return out;
  }

  /**
   * Extracts the linear components from a single geometry.
   * If more than one geometry is to be processed, it is more efficient to create a single
   * {@link LinearComponentExtracter} instance and pass it to multiple geometries.
   *
   * @param geom the geometry from which to extract linear components
   * @return a new modifiable list of linear components
   */
  public static List<LineString> getLines(Geometry geom)
  {
    return getLines(geom, false);
  }

  /**
   * Extracts the linear components from a single geometry.
   * If more than one geometry is to be processed, it is more efficient to create a single
   * {@link LinearComponentExtracter} instance and pass it to multiple geometries.
   *
   * @param geom              the geometry from which to extract linear components
   * @param forceToLineString true if {@link LinearRing}s should be converted to {@link LineString}s
   * @return a new modifiable list of linear components
   */
  public static List<LineString> getLines(Geometry geom, boolean forceToLineString)
  {
    List<LineString> lines = new ArrayList<LineString>();
    getLines(geom, lines, forceToLineString);
    return lines;
  }

  /**
   * Extracts the linear components from a single {@link Geometry}
   * and returns them as either a {@link LineString} or {@link MultiLineString}
   * (or an empty geometry if none are present).
   *
   * @param geom the geometry from which to extract
   * @return a linear geometry built from the extracted components
   */
  public static Geometry getGeometry(Geometry geom)
  {
    return geom.getFactory().buildGeometry(getLines(geom));
  }

  /**
   * Extracts the linear components from a single {@link Geometry}
   * and returns them as either a {@link LineString} or {@link MultiLineString}
   * (or an empty geometry if none are present).
   *
   * @param geom              the geometry from which to extract
   * @param forceToLineString true if {@link LinearRing}s should be converted to {@link LineString}s
   * @return a linear geometry built from the extracted components
   */
  public static Geometry getGeometry(Geometry geom, boolean forceToLineString)
  {
    return geom.getFactory().buildGeometry(getLines(geom, forceToLineString));
  }

  private final Collection<? super LineString> lines;
  private boolean isForcedToLineString = false;

  /**
   * Constructs a filter with a collection in which to store linear components found.
   *
   * @param lines the collection in which to store linear components found
   */
  public LinearComponentExtracter(Collection<? super LineString> lines)
  {
    this(lines, false);
  }

  /**
   * Constructs a filter with a collection in which to store linear components found.
   *
   * @param lines                the collection in which to store linear components found
   * @param isForcedToLineString true if {@link LinearRing}s should be converted to {@link LineString}s
   */
  public LinearComponentExtracter(Collection<? super LineString> lines, boolean isForcedToLineString)
  {
    this.lines = lines;
    this.isForcedToLineString = isForcedToLineString;
  }

  /**
   * Indicates that {@link LinearRing} components should be converted to pure {@link LineString}s.
   *
   * @param isForcedToLineString true if {@link LinearRing}s should be converted to {@link LineString}s
   */
  public void setForceToLineString(boolean isForcedToLineString)
  {
    this.isForcedToLineString = isForcedToLineString;
  }

  @Override
  public void filter(Geometry geom)
  {
    if (geom == null) return;

    if (isForcedToLineString && geom instanceof LinearRing) {
      LinearRing ring = (LinearRing) geom;
      LineString line = geom.getFactory().createLineString(ring.getCoordinateSequence());
      lines.add(line);
      return;
    }

    // If not being forced, and this is a linear component (includes LinearRing)
    if (geom instanceof LineString) {
      lines.add((LineString) geom);
    }

    // else: not a linear component, so skip it
  }
}