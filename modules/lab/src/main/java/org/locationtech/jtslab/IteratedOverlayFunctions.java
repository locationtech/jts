/*
 * Copyright (c) 2020 Martin Davis, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtslab;


import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

public class IteratedOverlayFunctions {
  
  public static Geometry overlayOld(Geometry coll) {
    return overlay(coll, false, null);
  }

  public static Geometry overlayNG(Geometry coll) {
    return overlay(coll, true, null);
  }
  
  public static Geometry overlaySR(Geometry coll, double scale) {
    PrecisionModel pm = new PrecisionModel(scale);
    return overlay(coll, true, pm);
  }
  
  private static Geometry overlay(Geometry coll, boolean useNG, PrecisionModel pm) {
    List<Geometry> result = new ArrayList<Geometry>();
    for (int i = 0; i < coll.getNumGeometries(); i++) {
      Geometry inGeom = coll.getGeometryN(i);
      
      int size = result.size();
      for (int j = 0; j < size; j++) {
        Geometry resGeom = result.get(j);
        if (resGeom.isEmpty()) continue;
        
        Geometry intGeom = extractPolygons(overlayIntersection(resGeom, inGeom, useNG, pm));
        if (! intGeom.isEmpty()) {
          result.add(intGeom);
          
          Geometry resDiff = extractPolygons(overlayDifference(resGeom, intGeom, useNG, pm));
          result.set( j, resDiff );
          
          inGeom = extractPolygons(overlayDifference(inGeom, intGeom, useNG, pm));
        }
      }
      // keep remainder of input (non-overlapped part)
      if (! inGeom.isEmpty()) {
        result.addAll( PolygonExtracter.getPolygons( inGeom ) );
        //result.add( inGeom );
      }
    }
    // TODO: return only non-empty polygons
    List<Polygon> resultPolys = extractPolygonsNonEmpty(result);
    return coll.getFactory().buildGeometry(resultPolys);
  }


  public static Geometry overlayIndexedNG(Geometry coll) {
    return overlayIndexed(coll, true, null);
  }
  
  private static Geometry overlayIndexed(Geometry coll, boolean useNG, PrecisionModel pm) {
    Quadtree tree = new Quadtree();
    for (int i = 0; i < coll.getNumGeometries(); i++) {
      
      Geometry inGeom = coll.getGeometryN(i);
      List<Polygon> results = tree.query( inGeom.getEnvelopeInternal() );
      
      for (Polygon resPoly : results) {
        
        Geometry intGeom = extractPolygons(overlayIntersection(resPoly, inGeom, useNG, pm));
        List<Polygon> intList = PolygonExtracter.getPolygons( intGeom );
        
        // resultant is overlapped by next input
        if (! intGeom.isEmpty() && intList.size() > 0) {
          tree.remove(resPoly.getEnvelopeInternal(), resPoly);
          
          for (Polygon intPoly : intList) {
            tree.insert( intPoly.getEnvelopeInternal(), intPoly );
            Geometry resDiff = overlayDifference(resPoly, intGeom, useNG, pm);
            insertPolys(resDiff, tree);
            
            inGeom = extractPolygons(overlayDifference(inGeom, intPoly, useNG, pm));
          }
        }
      }
      // keep remainder of input
      insertPolys(inGeom, tree);
    }
    List result = tree.queryAll();
    return coll.getFactory().buildGeometry(result);
  }
  
  private static void insertPolys(Geometry geom, Quadtree tree) {
    if (geom.isEmpty()) return;
    List<Polygon> polyList = PolygonExtracter.getPolygons( geom );
    for (Polygon poly : polyList) {
      tree.insert(poly.getEnvelopeInternal(), poly);
    }  
  }

  private static Geometry overlayIntersection(Geometry a, Geometry b, boolean useNG, PrecisionModel pm) {
    if (useNG) {
      if (pm == null)
        return OverlayNGRobust.overlay(a, b, OverlayNG.INTERSECTION);
      return OverlayNG.overlay(a, b, OverlayNG.INTERSECTION, pm);
    }
    return a.intersection(b);
  }
  
  private static Geometry overlayDifference(Geometry a, Geometry b, boolean useNG, PrecisionModel pm) {
    if (useNG) {
      if (pm == null)
        return OverlayNGRobust.overlay(a, b, OverlayNG.DIFFERENCE);
      return OverlayNG.overlay(a, b, OverlayNG.DIFFERENCE, pm);
    }
    return a.difference(b);
  }
  
  private static Geometry extractPolygons(Geometry geom) {
    List polys = PolygonExtracter.getPolygons(geom);
    return geom.getFactory().buildGeometry(polys);
  }
  
  private static List<Polygon> extractPolygonsNonEmpty(List<Geometry> geoms) {
    List<Polygon> exPolys = new ArrayList<Polygon>();
    for (Geometry geom : geoms) {
      if (! geom.isEmpty()) {
        if (geom instanceof Polygon) {
          exPolys.add((Polygon) geom);
        }
        else if (geom instanceof MultiPolygon) {
          exPolys.addAll(PolygonExtracter.getPolygons(geom));
        }
      }
     }
    return exPolys;
  }
}
