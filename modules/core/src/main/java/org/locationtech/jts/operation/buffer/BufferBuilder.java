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
package org.locationtech.jts.operation.buffer;

/**
 * @version 1.7
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Position;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geomgraph.Edge;
import org.locationtech.jts.geomgraph.EdgeList;
import org.locationtech.jts.geomgraph.Label;
import org.locationtech.jts.geomgraph.Node;
import org.locationtech.jts.geomgraph.PlanarGraph;
import org.locationtech.jts.noding.FastNodingValidator;
import org.locationtech.jts.noding.IntersectionAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentString;

/**
 * Builds the buffer geometry for a given input geometry and precision model.
 * Allows setting the level of approximation for circular arcs,
 * and the precision model in which to carry out the computation.
 * <p>
 * When computing buffers in floating point double-precision
 * it can happen that the process of iterated noding can fail to converge (terminate).
 * In this case a {@link TopologyException} will be thrown.
 * Retrying the computation in a fixed precision
 * can produce more robust results.
 *
 * @version 1.7
 */
class BufferBuilder
{
  /**
   * Compute the change in depth as an edge is crossed from R to L
   */
  private static int depthDelta(Label label)
  {
    int lLoc = label.getLocation(0, Position.LEFT);
    int rLoc = label.getLocation(0, Position.RIGHT);
    if (lLoc == Location.INTERIOR && rLoc == Location.EXTERIOR)
      return 1;
    else if (lLoc == Location.EXTERIOR && rLoc == Location.INTERIOR)
      return -1;
    return 0;
  }

  private BufferParameters bufParams;

  private PrecisionModel workingPrecisionModel;
  private Noder workingNoder;
  private GeometryFactory geomFact;
  private PlanarGraph graph;
  private EdgeList edgeList     = new EdgeList();

  private boolean isInvertOrientation = false;

  /**
   * Creates a new BufferBuilder,
   * using the given parameters.
   * 
   * @param bufParams the buffer parameters to use
   */
  public BufferBuilder(BufferParameters bufParams)
  {
    this.bufParams = bufParams;
  }

  /**
   * Sets the precision model to use during the curve computation and noding,
   * if it is different to the precision model of the Geometry.
   * If the precision model is less than the precision of the Geometry precision model,
   * the Geometry must have previously been rounded to that precision.
   *
   * @param pm the precision model to use
   */
  public void setWorkingPrecisionModel(PrecisionModel pm)
  {
    workingPrecisionModel = pm;
  }

  /**
   * Sets the {@link Noder} to use during noding.
   * This allows choosing fast but non-robust noding, or slower
   * but robust noding.
   *
   * @param noder the noder to use
   */
  public void setNoder(Noder noder) { workingNoder = noder; }

  /**
   * Sets whether the offset curve is generated 
   * using the inverted orientation of input rings.
   * This allows generating a buffer(0) polygon from the smaller lobes
   * of self-crossing rings.
   * 
   * @param isInvertOrientation true if input ring orientation should be inverted
   */
  void setInvertOrientation(boolean isInvertOrientation) {
    this.isInvertOrientation = isInvertOrientation;
  }

  public Geometry buffer(Geometry g, double distance)
  {
    PrecisionModel precisionModel = workingPrecisionModel;
    if (precisionModel == null)
      precisionModel = g.getPrecisionModel();

    // factory must be the same as the one used by the input
    geomFact = g.getFactory();

    BufferCurveSetBuilder curveSetBuilder = new BufferCurveSetBuilder(g, distance, precisionModel, bufParams);
    curveSetBuilder.setInvertOrientation(isInvertOrientation);
    
    List bufferSegStrList = curveSetBuilder.getCurves();

    // short-circuit test
    if (bufferSegStrList.size() <= 0) {
      return createEmptyResultGeometry();
    }

//BufferDebug.runCount++;
//String filename = "run" + BufferDebug.runCount + "_curves";
//System.out.println("saving " + filename);
//BufferDebug.saveEdges(bufferEdgeList, filename);
// DEBUGGING ONLY
//WKTWriter wktWriter = new WKTWriter();
//Debug.println("Rings: " + wktWriter.write(convertSegStrings(bufferSegStrList.iterator())));
//wktWriter.setMaxCoordinatesPerLine(10);
//System.out.println(wktWriter.writeFormatted(convertSegStrings(bufferSegStrList.iterator())));

    /**
     * Currently only zero-distance buffers are validated, 
     * to avoid reducing performance for other buffers.
     * This fixes some noding failure cases found via GeometryFixer
     * (see JTS-852).
     */
    boolean isNodingValidated = distance == 0.0;
    computeNodedEdges(bufferSegStrList, precisionModel, isNodingValidated);
    
    graph = new PlanarGraph(new BufferNodeFactory());
    graph.addEdges(edgeList.getEdges());

    List subgraphList = createSubgraphs(graph);
    PolygonBuilder polyBuilder = new PolygonBuilder(geomFact);
    buildSubgraphs(subgraphList, polyBuilder);
    List resultPolyList = polyBuilder.getPolygons();

    // just in case...
    if (resultPolyList.size() <= 0) {
      return createEmptyResultGeometry();
    }

    Geometry resultGeom = geomFact.buildGeometry(resultPolyList);
    return resultGeom;
  }

  private Noder getNoder(PrecisionModel precisionModel)
  {
    if (workingNoder != null) return workingNoder;

    // otherwise use a fast (but non-robust) noder
    MCIndexNoder noder = new MCIndexNoder();
    LineIntersector li = new RobustLineIntersector();
    li.setPrecisionModel(precisionModel);
    noder.setSegmentIntersector(new IntersectionAdder(li));
//    Noder noder = new IteratedNoder(precisionModel);
    return noder;
//    Noder noder = new SimpleSnapRounder(precisionModel);
//    Noder noder = new MCIndexSnapRounder(precisionModel);
//    Noder noder = new ScaledNoder(new MCIndexSnapRounder(new PrecisionModel(1.0)),
//                                  precisionModel.getScale());
  }

  private void computeNodedEdges(List bufferSegStrList, PrecisionModel precisionModel, boolean isNodingValidated)
  {
    Noder noder = getNoder(precisionModel);
    noder.computeNodes(bufferSegStrList);
    Collection nodedSegStrings = noder.getNodedSubstrings();
    
    if (isNodingValidated) {
      FastNodingValidator nv = new FastNodingValidator(nodedSegStrings);
      nv.checkValid();
    }
    
// DEBUGGING ONLY
//BufferDebug.saveEdges(nodedEdges, "run" + BufferDebug.runCount + "_nodedEdges");

    for (Iterator i = nodedSegStrings.iterator(); i.hasNext(); ) {
      SegmentString segStr = (SegmentString) i.next();
      
      /**
       * Discard edges which have zero length, 
       * since they carry no information and cause problems with topology building
       */
      Coordinate[] pts = segStr.getCoordinates();
      if (pts.length == 2 && pts[0].equals2D(pts[1]))
        continue;

      Label oldLabel = (Label) segStr.getData();
      Edge edge = new Edge(segStr.getCoordinates(), new Label(oldLabel));
      insertUniqueEdge(edge);
    }
    //saveEdges(edgeList.getEdges(), "run" + runCount + "_collapsedEdges");
  }


  /**
   * Inserted edges are checked to see if an identical edge already exists.
   * If so, the edge is not inserted, but its label is merged
   * with the existing edge.
   */
  protected void insertUniqueEdge(Edge e)
  {
//<FIX> MD 8 Oct 03  speed up identical edge lookup
    // fast lookup
    Edge existingEdge = edgeList.findEqualEdge(e);

    // If an identical edge already exists, simply update its label
    if (existingEdge != null) {
      Label existingLabel = existingEdge.getLabel();

      Label labelToMerge = e.getLabel();
      // check if new edge is in reverse direction to existing edge
      // if so, must flip the label before merging it
      if (! existingEdge.isPointwiseEqual(e)) {
        labelToMerge = new Label(e.getLabel());
        labelToMerge.flip();
      }
      existingLabel.merge(labelToMerge);

      // compute new depth delta of sum of edges
      int mergeDelta = depthDelta(labelToMerge);
      int existingDelta = existingEdge.getDepthDelta();
      int newDelta = existingDelta + mergeDelta;
      existingEdge.setDepthDelta(newDelta);
    }
    else {   // no matching existing edge was found
      // add this new edge to the list of edges in this graph
      //e.setName(name + edges.size());
      edgeList.add(e);
      e.setDepthDelta(depthDelta(e.getLabel()));
    }
  }

  private List createSubgraphs(PlanarGraph graph)
  {
    List subgraphList = new ArrayList();
    for (Iterator i = graph.getNodes().iterator(); i.hasNext(); ) {
      Node node = (Node) i.next();
      if (! node.isVisited()) {
        BufferSubgraph subgraph = new BufferSubgraph();
        subgraph.create(node);
        subgraphList.add(subgraph);
      }
    }
    /**
     * Sort the subgraphs in descending order of their rightmost coordinate.
     * This ensures that when the Polygons for the subgraphs are built,
     * subgraphs for shells will have been built before the subgraphs for
     * any holes they contain.
     */
    Collections.sort(subgraphList, Collections.reverseOrder());
    return subgraphList;
  }

  /**
   * Completes the building of the input subgraphs by depth-labelling them,
   * and adds them to the PolygonBuilder.
   * The subgraph list must be sorted in rightmost-coordinate order.
   *
   * @param subgraphList the subgraphs to build
   * @param polyBuilder the PolygonBuilder which will build the final polygons
   */
  private void buildSubgraphs(List subgraphList, PolygonBuilder polyBuilder)
  {
    List processedGraphs = new ArrayList();
    for (Iterator i = subgraphList.iterator(); i.hasNext(); ) {
      BufferSubgraph subgraph = (BufferSubgraph) i.next();
      Coordinate p = subgraph.getRightmostCoordinate();
//      int outsideDepth = 0;
//      if (polyBuilder.containsPoint(p))
//        outsideDepth = 1;
      SubgraphDepthLocater locater = new SubgraphDepthLocater(processedGraphs);
      int outsideDepth = locater.getDepth(p);
//      try {
      subgraph.computeDepth(outsideDepth);
//      }
//      catch (RuntimeException ex) {
//        // debugging only
//        //subgraph.saveDirEdges();
//        throw ex;
//      }
      subgraph.findResultEdges();
      processedGraphs.add(subgraph);
      polyBuilder.add(subgraph.getDirectedEdges(), subgraph.getNodes());
    }
  }
  
  private static Geometry convertSegStrings(Iterator it)
  {
  	GeometryFactory fact = new GeometryFactory();
  	List lines = new ArrayList();
  	while (it.hasNext()) {
  		SegmentString ss = (SegmentString) it.next();
  		LineString line = fact.createLineString(ss.getCoordinates());
  		lines.add(line);
  	}
  	return fact.buildGeometry(lines);
  }
  
  /**
   * Gets the standard result for an empty buffer.
   * Since buffer always returns a polygonal result,
   * this is chosen to be an empty polygon.
   * 
   * @return the empty result geometry
   */
  private Geometry createEmptyResultGeometry()
  {
    Geometry emptyGeom = geomFact.createPolygon();
    return emptyGeom;
  }
}
