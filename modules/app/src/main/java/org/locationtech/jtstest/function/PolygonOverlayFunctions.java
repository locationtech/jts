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

package org.locationtech.jtstest.function;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jtstest.geomfunction.Metadata;

public class PolygonOverlayFunctions 
{

  public static Geometry overlaySR(Geometry g1, Geometry g2, 
      @Metadata(title="Scale factor")
      double scale)
  {
    PrecisionModel pm = new PrecisionModel(scale);
    return computeOverlay(g1, g2, new Noder() {
      public Geometry node(Geometry inputLines) {
       return OverlayNG.overlay(inputLines, null, OverlayNG.UNION, pm);
      }
    });
  }
  
  public static Geometry overlay(Geometry g1, Geometry g2)
  {
    return computeOverlay(g1, g2, new Noder( ) {
      public Geometry node(Geometry inputLines) {
        return OverlayNGRobust.overlay(inputLines, null, OverlayNG.UNION);
      }
    });
  }
  
  interface Noder {
    Geometry node(Geometry inputLines);
  }
  
  @Metadata(description="Nodes linework using Snapping iterated until noding is valid")
  public static Geometry overlayIterSnap(Geometry g1, Geometry g2, double snapTol)
  {
    Geometry result = computeOverlay(g1, g2, new IteratedSnappingNoder( snapTol ) );
    if (result == null) {
      throw new RuntimeException("Unable to compute valid noding using iterated snapping");
    }
    return result;
  }
  
  /**
   * Input geometry may be lines or polygons.
   * 
   * @param g1
   * @param g2 a geometry to overlay (may be null)
   * @param noder
   * @return Noded, polygonized dataset
   */
  private static Geometry computeOverlay(Geometry g1, Geometry g2, Noder noder)
  {
    GeometryFactory geomFact = g1.getFactory();

    List lines = LinearComponentExtracter.getLines(g1);
    // add second input's linework, if any
    if (g2 != null)
      LinearComponentExtracter.getLines(g2, lines);
    Geometry inputLines = g1.getFactory().buildGeometry(lines);
    
    Geometry nodedDedupedLinework = noder.node(inputLines);

    // polygonize the result
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(nodedDedupedLinework);
    List<Polygon> resultants = (List<Polygon>) polygonizer.getPolygons();

    /**
     * If the input contained polygons,
     * use PIP to find polygons which have a parent.
     * Otherwise just return all resultants
     * (to support providing just lines as input)
     */
    boolean hasPolys = g1.getDimension() >= 2;
    List<Polygon> polys = resultants;
    if (hasPolys) {
      polys = ParentFinder.findParents(g1, g2, resultants);
    }
    
    // convert to collection for return
    Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
    return geomFact.createGeometryCollection(polyArray);
  }
  
  private static Geometry node(Geometry inputLines, PrecisionModel pm) {
    if (pm == null) {
      return OverlayNGRobust.overlay(inputLines, null, OverlayNG.UNION);
    }
    return OverlayNG.overlay(inputLines, null, OverlayNG.UNION, pm);
  }
  
  static class IteratedSnappingNoder implements Noder {

    private double snapTol;

    public IteratedSnappingNoder(double snapTol) {
      this.snapTol = snapTol;
    }
    
    @Override
    public Geometry node(Geometry geom) {
      double snapDist = snapTol;
      int count = 0;
      while (count < 10) {
        Geometry noded = nodeSnapDedup(geom, snapDist);
        if (noded != null)
          return noded;
        // try increasing distance
        snapDist = 2 * snapDist;
        count++;
      }
      // FAIL!
      return null;
    }
    
    private Geometry nodeSnapDedup(Geometry geom, double snapDist) {
      Geometry noded = NodingFunctions.snappingNoder(geom, null, snapDist);
      Geometry dedup = DissolveFunctions.dissolve(noded);
      Geometry intNodes = NodingFunctions.findInteriorNodes(dedup);
      
      // not full noded at given snap distance
      if (! intNodes.isEmpty())
        return null;
      
      // success!
      return dedup;
    }
  }
  
  /**
   * Finds parentage of a set of overlay resultants.
   * Currently just finds set of resultants which have at least one parent .
   * This effectively removes holes from the result set.
   * 
   * @author mdavis
   *
   */
  static class ParentFinder {
    
    public static List<Polygon> findParents(Geometry source1, Geometry source2, List<Polygon> resultants) {
      ParentFinder hd = new ParentFinder();
      hd.addSourcePolygons(source1);
      hd.addSourcePolygons(source2);
      return hd.findParents(resultants);
    }
    
    /**
     * Spatial index containing source polygons
     */
    private STRtree sourceIndex = new STRtree();
    
    public ParentFinder() {
      
    }
    
    public void addSourcePolygons(Geometry source) {
      if (source == null || source.getDimension() < 2) return;
      for (int i = 0; i < source.getNumGeometries(); i++) {
        Geometry geom = source.getGeometryN(i);
        if (geom instanceof Polygonal) {
          sourceIndex.insert(geom.getEnvelopeInternal(), geom);
        }
      }
    }
    
    public List<Polygon> findParents(List<Polygon> resultants) {
      List<Polygon> polys = new ArrayList<Polygon>();
      for (Polygon res : resultants) {
        Point intPt = res.getInteriorPoint();
        Coordinate intCoord = intPt.getCoordinate();
        
        List<Geometry> candidates = sourceIndex.query(intPt.getEnvelopeInternal());
        for (Geometry cand : candidates) {
          
          boolean isParent = SimplePointInAreaLocator.isContained(intCoord, cand);
          if (isParent) {
            /**
             * For now, keep resultants which have at least one parent.
             * This could be enhanced to record all parents of a resultant.
             */
            polys.add(res);
            break;
          }
        }
      }
      return polys;
    }
  }
}
