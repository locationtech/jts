package com.vividsolutions.jts.noding.snapround;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;
import com.vividsolutions.jts.noding.*;
import com.vividsolutions.jts.noding.snapround.*;

/**
 * Nodes the linework in a list of {@link Geometry}s using Snap-Rounding
 * to a given {@link PrecisionModel}.
 * <p>
 * The input coordinates are expected to be rounded
 * to the given precision model.
 * This class does not perform that function.
 * {@link GeometryPrecisionReducer} may be used to do this.
 * <p>
 * This class does <b>not</b> dissolve the output linework,
 * so there may be duplicate linestrings in the output.  
 * Subsequent processing (e.g. polygonization) may require
 * the linework to be unique.  Using {@link UnaryUnion} is one way
 * to do this (although this is an inefficient approach).
 * 
 * 
 */
public class GeometryNoder
{
  private GeometryFactory geomFact;
  private PrecisionModel pm;
  private boolean isValidityChecked = false;

  /**
   * Creates a new noder which snap-rounds to a grid specified
   * by the given
   * {@link PrecisionModel).
   * 
   * @param pm the precision model for the grid to snap-round to
   */
  public GeometryNoder(PrecisionModel pm) {
    this.pm = pm;
  }

  /**
   * Sets whether noding validity is checked after noding is performed.
   * 
   * @param isValidityChecked
   */
  public void setValidate(boolean isValidityChecked)
  {
  	this.isValidityChecked = isValidityChecked;
  }
  
  /**
   * Nodes the linework of a set of Geometrys using SnapRounding. 
   * 
   * @param geoms a Collection of Geometrys of any type
   * @return a List of LineStrings representing the noded linework of the input
   */
  public List node(Collection geoms)
  {
    // get geometry factory
    Geometry geom0 = (Geometry) geoms.iterator().next();
    geomFact = geom0.getFactory();

    List segStrings = toSegmentStrings(extractLines(geoms));
    //Noder sr = new SimpleSnapRounder(pm);
    Noder sr = new MCIndexSnapRounder(pm);
    sr.computeNodes(segStrings);
    Collection nodedLines = sr.getNodedSubstrings();

    //TODO: improve this to check for full snap-rounded correctness
    if (isValidityChecked) {
    	NodingValidator nv = new NodingValidator(nodedLines);
    	nv.checkValid();
    }

    return toLineStrings(nodedLines);
  }

  private List toLineStrings(Collection segStrings)
  {
    List lines = new ArrayList();
    for (Iterator it = segStrings.iterator(); it.hasNext(); ) {
      SegmentString ss = (SegmentString) it.next();
      // skip collapsed lines
      if (ss.size() < 2)
      	continue;
      lines.add(geomFact.createLineString(ss.getCoordinates()));
    }
    return lines;
  }

  private List extractLines(Collection geoms)
  {
    List lines = new ArrayList();
    LinearComponentExtracter lce = new LinearComponentExtracter(lines);
    for (Iterator it = geoms.iterator(); it.hasNext(); ) {
      Geometry geom = (Geometry) it.next();
      geom.apply(lce);
    }
    return lines;
  }

  private List toSegmentStrings(Collection lines)
  {
    List segStrings = new ArrayList();
    for (Iterator it = lines.iterator(); it.hasNext(); ) {
      LineString line = (LineString) it.next();
      segStrings.add(new NodedSegmentString(line.getCoordinates(), null));
    }
    return segStrings;
  }
}