/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.noding.MCIndexSegmentSetMutualIntersector;
import org.locationtech.jts.noding.SegmentString;


/**
 * Performs the following checks:
 * <ul>
 * <li>Exact duplicate polygons
 * <li>Misaligned segments
 * </ul>
 * 
 * @author mdavis
 *
 */
public class CoveragePolygonValidator {
  
  public static Geometry validate(Geometry base, Geometry adjPolygons) {
    return validate(base, adjPolygons, 0);
  }
  
  public static Geometry validate(Geometry base, Geometry adjPolygons, double distanceTolerance) {
    CoveragePolygonValidator v = new CoveragePolygonValidator(base);
    return v.validate(adjPolygons, distanceTolerance);
  }
  
  private Geometry targetGeom;
  private GeometryFactory geomFactory;
  private IndexedPointInAreaLocator[] locator;

  public CoveragePolygonValidator(Geometry geom) {
    this.targetGeom = geom;
    geomFactory = targetGeom.getFactory();
  }
  
  public Geometry validate(Geometry adjGeoms, double distanceTolerance) {
    List<Polygon> adjPolygons = PolygonExtracter.getPolygons(adjGeoms);
    locator = new IndexedPointInAreaLocator[adjPolygons.size()];
    
    //TODO: CANCEL skip non-touching polygons? (since adjacent one may be a legitimate MultiPolygon)
    //-- no, just use tol = 0 instead
    
    //TODO: DONE avoid flagging edges of spikes in coverage (perhaps: ignore matched edges in surrounding polygons?) 

    //TODO: DONE avoid flagging edges which match to test edges which are exact matches

    //TODO: DONE flag edges which are wholly inside target
    
    if (hasDuplicateGeom(targetGeom, adjGeoms)) {
      //TODO: convert to LineString copies
      return targetGeom.getBoundary();
    }
    
    List<CoverageEdge> targetSegStrings = extractSegmentStrings(targetGeom);
    List<CoverageEdge> adjSegStrings = extractSegmentStrings(adjGeoms);

    //System.out.println("# adj edges: " + adjSegStrings.size());
    Envelope targetEnv = targetGeom.getEnvelopeInternal().copy();
    targetEnv.expandBy(distanceTolerance);
    findMatchedSegments(targetSegStrings, adjSegStrings, targetEnv);

    //-- check if target is fully matched and thus forms a clean coverage 
    if (CoverageEdge.isAllValid(targetSegStrings))
      return createEmptyResult();
    
    findMisalignedSegments(targetSegStrings, adjSegStrings, distanceTolerance);
    findInteriorSegments(targetSegStrings, adjPolygons);
    
    return createChains(targetSegStrings);
  }

  private Geometry createEmptyResult() {
    return geomFactory.createLineString();
  }

  /**
   * Check if adjacent geoms contains a duplicate of the target.
   * This situation is not detected by segment alignment checking, since all segments are duplicate.

   * @param geom
   * @param adjGeoms 
   * @return
   */
  private boolean hasDuplicateGeom(Geometry geom, Geometry adjGeoms) {
    for (int i = 0; i < adjGeoms.getNumGeometries(); i++) {
      Geometry testGeom = adjGeoms.getGeometryN(i);
      if (! testGeom.getEnvelopeInternal().equals(geom.getEnvelopeInternal())) {
        continue;
      }
      return testGeom.equalsTopo(geom);
    }
    return false;
  }

  private void findMatchedSegments(List<CoverageEdge> targetSegStrings,
      List<CoverageEdge> adjSegStrings, Envelope targetEnv) {
    Map<Segment, Segment> segmentMap = new HashMap<Segment, Segment>();
    addMatchedSegments(targetSegStrings, targetEnv, segmentMap);
    addMatchedSegments(adjSegStrings, targetEnv, segmentMap);
  }
  
  private void addMatchedSegments(List<CoverageEdge> segStrings, Envelope envLimit, Map<Segment, Segment> segMap) {
    for (CoverageEdge ss : segStrings) {
      for (int i = 0; i < ss.size() - 1; i++) {
        Segment seg = Segment.create(ss, i);
        //-- skip segments which lie outside the limit envelope
        if (! envLimit.intersects(seg.p0, seg.p1)) {
          continue;
        }
        if (segMap.containsKey(seg)) {
          Segment segMatch = segMap.get(seg);
          segMatch.markValid();
          seg.markValid();
        }
        else {
          segMap.put(seg, seg);
        }
      }
    }
  }

  private static class Segment extends LineSegment {
    public static Segment create(CoverageEdge ss, int index) {
      Coordinate p0 = ss.getCoordinate(index);
      Coordinate p1 = ss.getCoordinate(index + 1);
      return new Segment(p0, p1, ss, index);
    }
    
    private CoverageEdge segString;
    private int index;

    public Segment(Coordinate p0, Coordinate p1, CoverageEdge ss, int index) {
      super(p0, p1);
      this.segString = ss;
      this.index = index;
      normalize();
    }
    
    public void markValid() {
      segString.markValid(index);
    }
  }
  
  private void findMisalignedSegments(List<CoverageEdge> targetSegStrings, List<CoverageEdge> adjSegStrings,
      double distanceTolerance) {
    InvalidSegmentDetector detector = new InvalidSegmentDetector(distanceTolerance);
    MCIndexSegmentSetMutualIntersector segSetMutInt = new MCIndexSegmentSetMutualIntersector(targetSegStrings, distanceTolerance);
    segSetMutInt.process(adjSegStrings, detector);
  }
  
  private void findInteriorSegments(List<CoverageEdge> targetSegStrings, List<Polygon> adjPolygons) {
    for (CoverageEdge ss : targetSegStrings) {
      for (int i = 0; i < ss.size() - 1; i++) {
        if (ss.isKnown(i))
          continue;
        
        //-- check if segment intersects interior of an adjacent polygon
        Coordinate p0 = ss.getCoordinate(i);
        if (isInterioSegment(p0, adjPolygons))
          ss.markInvalid(i);
      }
    }
  }
  
  private boolean isInterioSegment(Coordinate p, List<Polygon> adjPolygons) {
    for (int i = 0; i < adjPolygons.size(); i++) {
      Polygon adjPoly = adjPolygons.get(i);
      if (! adjPoly.getEnvelopeInternal().intersects(p))
        continue;
     
      if (isPointInPolygon(p, i, adjPoly))
        return true;
    }
    return false;
  }

  private boolean isPointInPolygon(Coordinate p, int i, Polygon poly) {
    PointOnGeometryLocator pia = getLocator(i, poly);
    return Location.INTERIOR == pia.locate(p);
  }

  private PointOnGeometryLocator getLocator(int i, Polygon poly) {
    IndexedPointInAreaLocator loc = locator[i];
    if (loc == null) {
      loc = new IndexedPointInAreaLocator(poly);
      locator[i] = loc;
    }
    return loc;
  }

  private Geometry createChains(List<CoverageEdge> segStrings) {
    List<SegmentString> chains = new ArrayList<SegmentString>();
    for (CoverageEdge ss : segStrings) {
      ss.createChains(chains);
    }
    
    if (chains.size() == 0) {
      return createEmptyResult();
    }
    
    LineString[] lines = new LineString[chains.size()];
    int i = 0;
    for (SegmentString ss : chains) {
      LineString line = geomFactory.createLineString(ss.getCoordinates());
      lines[i++] = line;
    }
    
    if (lines.length == 1) {
      return lines[0];
    }
    return geomFactory.createMultiLineString(lines);
  }  
  
  private static List<CoverageEdge> extractSegmentStrings(Geometry geom)
  {
    List<CoverageEdge> segStr = new ArrayList<CoverageEdge>();
    List<LineString> lines = LinearComponentExtracter.getLines(geom);
    for (LineString line : lines) {
      Coordinate[] pts = line.getCoordinates();
      segStr.add(new CoverageEdge(pts, geom));
    }
    return segStr;
  }
}
