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
package org.locationtech.jts.operation.union;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.operation.overlay.snap.SnapIfNeededOverlayOp;
import org.locationtech.jts.util.Debug;


/**
 * Provides an efficient method of unioning a collection of
 * {@link Polygonal} geometries.
 * The geometries are indexed using a spatial index,
 * and unioned recursively in index order.
 * For geometries with a high degree of overlap,
 * this has the effect of reducing the number of vertices
 * early in the process, which increases speed
 * and robustness.
 * <p>
 * This algorithm is faster and more robust than
 * the simple iterated approach of
 * repeatedly unioning each polygon to a result geometry.
 * <p>
 * The <tt>buffer(0)</tt> trick is sometimes faster, but can be less robust and
 * can sometimes take a long time to complete.
 * This is particularly the case where there is a high degree of overlap
 * between the polygons.  In this case, <tt>buffer(0)</tt> is forced to compute
 * with <i>all</i> line segments from the outset,
 * whereas cascading can eliminate many segments
 * at each stage of processing.
 * The best situation for using <tt>buffer(0)</tt> is the trivial case
 * where there is <i>no</i> overlap between the input geometries.
 * However, this case is likely rare in practice.
 *
 * @author Martin Davis
 *
 */
public class CascadedPolygonUnion
{
  /**
   * A union strategy that uses the classic JTS {@link SnapIfNeededOverlayOp},
   * and for polygonal geometries a robustness fallback using <cod>buffer(0)</code>.
   */
  final static  UnionStrategy CLASSIC_UNION = new UnionStrategy() {
    public Geometry union(Geometry g0, Geometry g1) {
      try {
        return SnapIfNeededOverlayOp.union(g0, g1);
      }
      catch (TopologyException ex) {
        // union-by-buffer only works for polygons
        if (g0.getDimension() != 2 || g1.getDimension() != 2)
          throw ex;
        return unionPolygonsByBuffer(g0, g1);
      }
    }

    @Override
    public boolean isFloatingPrecision() {
      return true;
    }
    
    /**
     * An alternative way of unioning polygonal geometries 
     * by using <code>bufer(0)</code>.
     * Only worth using if regular overlay union fails.
     * 
     * @param g0 a polygonal geometry
     * @param g1 a polygonal geometry
     * @return the union of the geometries
     */
    private Geometry unionPolygonsByBuffer(Geometry g0, Geometry g1) {
      //System.out.println("Unioning by buffer");
      GeometryCollection coll = g0.getFactory().createGeometryCollection(
          new Geometry[] { g0, g1 });
      return coll.buffer(0);
    }
  };

  
  /**
   * Computes the union of
   * a collection of {@link Polygonal} {@link Geometry}s.
   *
   * @param polys a collection of {@link Polygonal} {@link Geometry}s
   */
  public static Geometry union(Collection polys)
  {
    CascadedPolygonUnion op = new CascadedPolygonUnion(polys);
    return op.union();
  }

  /**
   * Computes the union of
   * a collection of {@link Polygonal} {@link Geometry}s.
   *
   * @param polys a collection of {@link Polygonal} {@link Geometry}s
   */
  public static Geometry union(Collection polys, UnionStrategy unionFun)
  {
    CascadedPolygonUnion op = new CascadedPolygonUnion(polys, unionFun);
    return op.union();
  }

	private Collection inputPolys;
	private GeometryFactory geomFactory = null;
  private UnionStrategy unionFun;

  private int countRemainder = 0;
  private int countInput = 0;

  /**
   * Creates a new instance to union
   * the given collection of {@link Geometry}s.
   *
   * @param polys a collection of {@link Polygonal} {@link Geometry}s
   */
  public CascadedPolygonUnion(Collection polys)
  {
    this(polys, CLASSIC_UNION );
  }

	 /**
   * Creates a new instance to union
   * the given collection of {@link Geometry}s.
   *
   * @param polys a collection of {@link Polygonal} {@link Geometry}s
   */
  public CascadedPolygonUnion(Collection polys, UnionStrategy unionFun)
  {
    this.inputPolys = polys;
    this.unionFun = unionFun;
    // guard against null input
    if (inputPolys == null)
      inputPolys = new ArrayList();
    this.countInput = inputPolys.size();
    this.countRemainder = countInput;
  }
  /**
   * The effectiveness of the index is somewhat sensitive
   * to the node capacity.
   * Testing indicates that a smaller capacity is better.
   * For an STRtree, 4 is probably a good number (since
   * this produces 2x2 "squares").
   */
  private static final int STRTREE_NODE_CAPACITY = 4;

	/**
	 * Computes the union of the input geometries.
	 * <p>
	 * This method discards the input geometries as they are processed.
	 * In many input cases this reduces the memory retained
	 * as the operation proceeds.
	 * Optimal memory usage is achieved
	 * by disposing of the original input collection
	 * before calling this method.
	 *
	 * @return the union of the input geometries
	 * or null if no input geometries were provided
	 * @throws IllegalStateException if this method is called more than once
	 */
	public Geometry union()
	{
	  if (inputPolys == null)
	    throw new IllegalStateException("union() method cannot be called twice");
		if (inputPolys.isEmpty())
			return null;
		geomFactory = ((Geometry) inputPolys.iterator().next()).getFactory();

		/**
		 * A spatial index to organize the collection
		 * into groups of close geometries.
		 * This makes unioning more efficient, since vertices are more likely
		 * to be eliminated on each round.
		 */
//    STRtree index = new STRtree();
    STRtree index = new STRtree(STRTREE_NODE_CAPACITY);
    for (Iterator i = inputPolys.iterator(); i.hasNext(); ) {
      Geometry item = (Geometry) i.next();
      index.insert(item.getEnvelopeInternal(), item);
    }
    // To avoiding holding memory remove references to the input geometries,
    inputPolys = null;

    List itemTree = index.itemsTree();
//    printItemEnvelopes(itemTree);
    Geometry unionAll = unionTree(itemTree);
    return unionAll;
	}

  private Geometry unionTree(List geomTree)
  {
    /**
     * Recursively unions all subtrees in the list into single geometries.
     * The result is a list of Geometrys only
     */
    List geoms = reduceToGeometries(geomTree);
//    Geometry union = bufferUnion(geoms);
    Geometry union = binaryUnion(geoms);

    // print out union (allows visualizing hierarchy)
//    System.out.println(union);

    return union;
  }

  //========================================================
  /*
   * The following methods are for experimentation only
   */
/*
  private Geometry repeatedUnion(List geoms)
  {
  	Geometry union = null;
  	for (Iterator i = geoms.iterator(); i.hasNext(); ) {
  		Geometry g = (Geometry) i.next();
  		if (union == null)
  			union = g.copy();
  		else
  			union = unionFun.union(union, g);
  	}
  	return union;
  }
  */

  //=======================================

  /**
   * Unions a list of geometries
   * by treating the list as a flattened binary tree,
   * and performing a cascaded union on the tree.
   */
  private Geometry binaryUnion(List geoms)
  {
  	return binaryUnion(geoms, 0, geoms.size());
  }

  /**
   * Unions a section of a list using a recursive binary union on each half
   * of the section.
   *
   * @param geoms the list of geometries containing the section to union
   * @param start the start index of the section
   * @param end the index after the end of the section
   * @return the union of the list section
   */
  private Geometry binaryUnion(List geoms, int start, int end)
  {
  	if (end - start <= 1) {
  		Geometry g0 = getGeometry(geoms, start);
  		return unionSafe(g0, null);
  	}
  	else if (end - start == 2) {
  		return unionSafe(getGeometry(geoms, start), getGeometry(geoms, start + 1));
  	}
  	else {
  		// recurse on both halves of the list
  		int mid = (end + start) / 2;
  		Geometry g0 = binaryUnion(geoms, start, mid);
  		Geometry g1 = binaryUnion(geoms, mid, end);
  		return unionSafe(g0, g1);
  	}
  }

  /**
   * Gets the element at a given list index, or
   * null if the index is out of range.
   *
   * @param list
   * @param index
   * @return the geometry at the given index
   * or null if the index is out of range
   */
  private static Geometry getGeometry(List list, int index)
  {
  	if (index >= list.size()) return null;
  	return (Geometry) list.get(index);
  }

  /**
   * Reduces a tree of geometries to a list of geometries
   * by recursively unioning the subtrees in the list.
   *
   * @param geomTree a tree-structured list of geometries
   * @return a list of Geometrys
   */
  private List reduceToGeometries(List geomTree)
  {
    List geoms = new ArrayList();
    for (Iterator i = geomTree.iterator(); i.hasNext(); ) {
      Object o = i.next();
      Geometry geom = null;
      if (o instanceof List) {
        geom = unionTree((List) o);
      }
      else if (o instanceof Geometry) {
        geom = (Geometry) o;
      }
      geoms.add(geom);
    }
    return geoms;
  }

  /**
   * Computes the union of two geometries,
   * either or both of which may be null.
   *
   * @param g0 a Geometry
   * @param g1 a Geometry
   * @return the union of the input(s)
   * or null if both inputs are null
   */
  private Geometry unionSafe(Geometry g0, Geometry g1)
  {
  	if (g0 == null && g1 == null)
  		return null;

  	if (g0 == null)
  		return g1.copy();
  	if (g1 == null)
  		return g0.copy();

  	countRemainder--;
  	if (Debug.isDebugging()) {
  	  Debug.println("Remainder: " + countRemainder + " out of " + countInput);
      Debug.print("Union: A: " + g0.getNumPoints() + " / B: " + g1.getNumPoints() + "  ---  "  );
  	}

  	Geometry union = unionActual( g0, g1 );
  	
    if (Debug.isDebugging()) Debug.println(" Result: " + union.getNumPoints());
    //if (TestBuilderProxy.isActive()) TestBuilderProxy.showIndicator(union);
    
    return union;
  }

  /**
   * Encapsulates the actual unioning of two polygonal geometries.
   *
   * @param g0
   * @param g1
   * @return
   */
  private Geometry unionActual(Geometry g0, Geometry g1)
  {
    Geometry union = unionFun.union(g0, g1);
    Geometry unionPoly = restrictToPolygons( union );
  	return unionPoly;
  }

  /**
   * Computes a {@link Geometry} containing only {@link Polygonal} components.
   * Extracts the {@link Polygon}s from the input
   * and returns them as an appropriate {@link Polygonal} geometry.
   * <p>
   * If the input is already <tt>Polygonal</tt>, it is returned unchanged.
   * <p>
   * A particular use case is to filter out non-polygonal components
   * returned from an overlay operation.
   *
   * @param g the geometry to filter
   * @return a Polygonal geometry
   */
  private static Geometry restrictToPolygons(Geometry g)
  {
    if (g instanceof Polygonal) {
      return g;
    }
    List polygons = PolygonExtracter.getPolygons(g);
    if (polygons.size() == 1)
      return (Polygon) polygons.get(0);
    return g.getFactory().createMultiPolygon(GeometryFactory.toPolygonArray(polygons));
  }
}
