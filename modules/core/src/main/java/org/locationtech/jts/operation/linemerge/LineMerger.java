
/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.linemerge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryComponentFilter;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.planargraph.GraphComponent;
import org.locationtech.jts.planargraph.Node;
import org.locationtech.jts.util.Assert;


/**
 * Merges a collection of linear components to form maximal-length linestrings. 
 * <p> 
 * Merging stops at nodes of degree 1 or degree 3 or more.
 * In other words, all nodes of degree 2 are merged together. 
 * The exception is in the case of an isolated loop, which only has degree-2 nodes.
 * In this case one of the nodes is chosen as a starting point.
 * <p> 
 * The direction of each
 * merged LineString will be that of the majority of the LineStrings from which it
 * was derived.
 * <p>
 * Any dimension of Geometry is handled - the constituent linework is extracted to 
 * form the edges. The edges must be correctly noded; that is, they must only meet
 * at their endpoints.  The LineMerger will accept non-noded input
 * but will not merge non-noded edges.
 * <p>
 * Input lines which are empty or contain only a single unique coordinate are not included
 * in the merging.
 *
 * @version 1.7
 */
public class LineMerger 
{
  private LineMergeGraph graph = new LineMergeGraph();
  private Collection mergedLineStrings = null;
  private GeometryFactory factory = null;
  
  /**
   * Creates a new line merger.
   *
   */
  public LineMerger()
  {
  	
  }
  
  /**
   * Adds a Geometry to be processed. May be called multiple times.
   * Any dimension of Geometry may be added; the constituent linework will be
   * extracted.
   * 
   * @param geometry geometry to be line-merged
   */  
  public void add(Geometry geometry) {
    geometry.apply(new GeometryComponentFilter() {
      public void filter(Geometry component) {
        if (component instanceof LineString) {
          add((LineString)component);
        }
      }      
    });
  }
  /**
   * Adds a collection of Geometries to be processed. May be called multiple times.
   * Any dimension of Geometry may be added; the constituent linework will be
   * extracted.
   * 
   * @param geometries the geometries to be line-merged
   */
  public void add(Collection geometries) 
  {
  	mergedLineStrings = null;
    for (Iterator i = geometries.iterator(); i.hasNext(); ) {
      Geometry geometry = (Geometry) i.next();
      add(geometry);
    }
  }
  private void add(LineString lineString) {
    if (factory == null) {
      this.factory = lineString.getFactory();
    }
    graph.addEdge(lineString);
  }
  
  private Collection edgeStrings = null;
  
  private void merge() 
  {
    if (mergedLineStrings != null) { return; }
    
    // reset marks (this allows incremental processing)
    GraphComponent.setMarked(graph.nodeIterator(), false);
    GraphComponent.setMarked(graph.edgeIterator(), false);
    
    edgeStrings = new ArrayList();
    buildEdgeStringsForObviousStartNodes();
    buildEdgeStringsForIsolatedLoops();
    mergedLineStrings = new ArrayList();    
    for (Iterator i = edgeStrings.iterator(); i.hasNext(); ) {
      EdgeString edgeString = (EdgeString) i.next();
      mergedLineStrings.add(edgeString.toLineString());
    }    
  }
  
  private void buildEdgeStringsForObviousStartNodes() {
    buildEdgeStringsForNonDegree2Nodes();
  }
  
  private void buildEdgeStringsForIsolatedLoops() {
    buildEdgeStringsForUnprocessedNodes();
  }  
  
  private void buildEdgeStringsForUnprocessedNodes() {
    for (Iterator i = graph.getNodes().iterator(); i.hasNext(); ) {
      Node node = (Node) i.next();
      if (!node.isMarked()) { 
        Assert.isTrue(node.getDegree() == 2);
        buildEdgeStringsStartingAt(node);
        node.setMarked(true);
      }
    }
  }  
  private void buildEdgeStringsForNonDegree2Nodes() {
    for (Iterator i = graph.getNodes().iterator(); i.hasNext(); ) {
      Node node = (Node) i.next();
      if (node.getDegree() != 2) { 
        buildEdgeStringsStartingAt(node);
        node.setMarked(true);
      }
    }
  }
  private void buildEdgeStringsStartingAt(Node node) {
    for (Iterator i = node.getOutEdges().iterator(); i.hasNext(); ) {
      LineMergeDirectedEdge directedEdge = (LineMergeDirectedEdge) i.next();
      if (directedEdge.getEdge().isMarked()) { continue; }
      edgeStrings.add(buildEdgeStringStartingWith(directedEdge));
    }
  }
  
  private EdgeString buildEdgeStringStartingWith(LineMergeDirectedEdge start) {    
    EdgeString edgeString = new EdgeString(factory);
    LineMergeDirectedEdge current = start;
    do {
      edgeString.add(current);
      current.getEdge().setMarked(true);
      current = current.getNext();      
    } while (current != null && current != start);
    return edgeString;
  }
  
  /**
   * Gets the {@link LineString}s created by the merging process.
   * 
   * @return the collection of merged LineStrings
   */
  public Collection getMergedLineStrings() {
    merge();
    return mergedLineStrings;
  }
}
