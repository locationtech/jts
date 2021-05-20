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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.shape.fractal.HilbertCode;
import org.locationtech.jts.shape.fractal.MortonCode;


public class SortingFunctions
{
  public static Geometry sortByLength(Geometry g) {
    List<Geometry> geoms = components(g);
    // annotate geometries with length
    for (Geometry geom : geoms) {
      geom.setUserData(geom.getLength());
    }
    Collections.sort(geoms, new UserDataDoubleComparator());
    return g.getFactory().buildGeometry(geoms);
  }
  
  public static Geometry sortByArea(Geometry g)
  {
    List<Geometry> geoms = components(g);
    // annotate geometries with area
    for (Geometry geom : geoms) {
      geom.setUserData(geom.getArea());
    }
    Collections.sort(geoms, new UserDataDoubleComparator());
    return g.getFactory().buildGeometry(geoms);
  }
  
  public static Geometry sortByMinX(Geometry g)
  {
    List<Geometry> geoms = components(g);
    // annotate geometries with area
    for (Geometry geom : geoms) {
      geom.setUserData(geom.getEnvelopeInternal().getMinX());
    }
    Collections.sort(geoms, new UserDataDoubleComparator());
    return g.getFactory().buildGeometry(geoms);
  }
  
  public static Geometry sortByMinY(Geometry g)
  {
    List<Geometry> geoms = components(g);
    // annotate geometries with area
    for (Geometry geom : geoms) {
      geom.setUserData(geom.getEnvelopeInternal().getMinY());
    }
    Collections.sort(geoms, new UserDataDoubleComparator());
    return g.getFactory().buildGeometry(geoms);
  }
  
  private static List<Geometry> components(Geometry g)
  {
    List<Geometry> comp = new ArrayList<Geometry>();
    for (int i = 0; i < g.getNumGeometries(); i++) {
      comp.add(g.getGeometryN(i));
    }
    return comp;
  }
  
  public static Geometry sortByHilbertCode(Geometry g)
  {
    List<Geometry> geoms = components(g);
    Envelope env = g.getEnvelopeInternal();
    // use level one less than max to avoid hitting negative integers
    int level = 15;
    int maxOrd = HilbertCode.maxOrdinate(level);
    
    double strideX = env.getWidth() / maxOrd;
    double strideY = env.getHeight() / maxOrd;
    
    for (Geometry geom : geoms) {
      Coordinate centre = geom.getEnvelopeInternal().centre();
      int x = (int) (( centre.getX() - env.getMinX() ) / strideX);
      int y = (int) (( centre.getY() - env.getMinY() ) / strideY);
      int code = HilbertCode.encode(level, x, y);
      geom.setUserData(code);
    }
    
    Collections.sort(geoms, new UserDataIntComparator());
    
    return g.getFactory().buildGeometry(geoms);
  }

  public static Geometry sortByMortonCode(Geometry g)
  {
    List<Geometry> geoms = components(g);
    Envelope env = g.getEnvelopeInternal();
    // use level one less than max to avoid hitting negative integers
    int level = 15;
    int maxOrd = MortonCode.maxOrdinate(level);
    
    double strideX = env.getWidth() / maxOrd;
    double strideY = env.getHeight() / maxOrd;
    
    for (Geometry geom : geoms) {
      Coordinate centre = geom.getEnvelopeInternal().centre();
      int x = (int) (( centre.getX() - env.getMinX() ) / strideX);
      int y = (int) (( centre.getY() - env.getMinY() ) / strideY);
      int code = MortonCode.encode(x, y);
      geom.setUserData(code);
    }
    
    Collections.sort(geoms, new UserDataIntComparator());
    
    return g.getFactory().buildGeometry(geoms);
  }

  private static class UserDataIntComparator implements Comparator<Geometry>
  {
    @Override
    public int compare(Geometry g1, Geometry g2) {
      return Integer.compare((Integer) g1.getUserData(), (Integer) g2.getUserData());
    }
  }
  private static class UserDataDoubleComparator implements Comparator<Geometry>
  {
    @Override
    public int compare(Geometry g1, Geometry g2) {
      return Double.compare((Double) g1.getUserData(), (Double) g2.getUserData());
    }
  }

  
}
