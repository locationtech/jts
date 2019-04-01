/*
 * Copyright (c) 2016 Vivid Solutions.
 * Portions
 * Copyright (c) 2019 Felix Obermaier
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtslab.noding.anchorpoint;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.NodingValidator;
import org.locationtech.jts.noding.SegmentString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Nodes the linework in a list of {@link Geometry}s using anchor points
 * to a given {@link PrecisionModel}.
 * <p>
 * The input coordinates are expected to be rounded
 * to the given precision model.
 * This class does not perform that function.
 * <code>GeometryPrecisionReducer</code> may be used to do this.
 * <p>
 * This class does <b>not</b> dissolve the output linework,
 * so there may be duplicate linestrings in the output.  
 * Subsequent processing (e.g. polygonization) may require
 * the linework to be unique.  Using <code>UnaryUnion</code> is one way
 * to do this (although this is an inefficient approach).
 * 
 * @version 1.17
 * @author Martin Davis, Felix Obermaier
 */
public class GeometryNoder
{
  private final PrecisionModel pm;
  private boolean checkValidity = false;
  private AnchorPointNoder lastAnchorPointNoder;

  /**
   * Creates a new noder which snap-rounds to a grid specified
   * by the given {@link PrecisionModel}.
   * 
   * @param pm the precision model for the grid to snap-round to
   */
  public GeometryNoder(PrecisionModel pm) {
    this.pm = pm;
  }

  /**
   * Sets whether noding validity is checked after noding is performed.
   * 
   * @param checkValidity
   */
  void setValidate(boolean checkValidity)
  {
  	this.checkValidity = checkValidity;
  }
  
  /**
   * Nodes the linework of a set of Geometrys using SnapRounding. 
   * 
   * @param geoms a Collection of Geometrys of any type
   * @return a List of LineStrings representing the noded linework of the input
   */
  public List node(Collection geoms)
  {
    List segStrings = toSegmentStrings(extractLines(geoms));
    AnchorPointNoder apn = new AnchorPointNoder(pm);
    apn.computeNodes(segStrings);
    Collection nodedLines = apn.getNodedSubstrings();

    //TODO: improve this to check for full snap-rounded correctness
    if (this.checkValidity) {
      NodingValidator nv = new NodingValidator(nodedLines);
      nv.checkValid();
    }

    this.lastAnchorPointNoder = apn;

    // get geometry factory
    Geometry geom0 = (Geometry) geoms.iterator().next();
    return toLineStrings(nodedLines, geom0.getFactory());
  }

  private List toLineStrings(Collection segStrings, GeometryFactory factory)
  {
    List lines = new ArrayList();
    for (Iterator it = segStrings.iterator(); it.hasNext(); ) {
      SegmentString ss = (SegmentString) it.next();
      // skip collapsed lines
      if (ss.size() < 2)
      	continue;
      lines.add(factory.createLineString(ss.getCoordinates()));
    }
    return lines;
  }

  private static List extractLines(Collection geoms)
  {
    List lines = new ArrayList();
    LinearComponentExtracter lce = new LinearComponentExtracter(lines);
    for (Iterator it = geoms.iterator(); it.hasNext(); ) {
      Geometry geom = (Geometry) it.next();
      geom.apply(lce);
    }
    return lines;
  }

  private static List toSegmentStrings(Collection lines)
  {
    List segStrings = new ArrayList();
    for (Iterator it = lines.iterator(); it.hasNext(); ) {
      LineString line = (LineString) it.next();
      segStrings.add(new NodedSegmentString(line.getCoordinates(), null));
    }
    return segStrings;
  }

  /**
   *
   * @return
   */
  public List<AnchorPoint> getLastAnchorPoints(boolean onlyIntersection) {
    if (lastAnchorPointNoder == null)
      return new ArrayList<>();
    return this.lastAnchorPointNoder.getAnchorPoints(onlyIntersection);
  }
}
