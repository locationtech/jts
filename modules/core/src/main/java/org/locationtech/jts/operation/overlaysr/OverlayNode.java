package org.locationtech.jts.operation.overlaysr;

import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.Debug;

public class OverlayNode {

  /**
   * Scan around node CCW and propagate labels until fully populated.
   * @param node node to compute labelling for
   */
  public void nodeComputeLabelling(OverlayEdge e) {
    nodePropagateAreaLabels(e, 0);
    nodePropagateAreaLabels(e, 1);
    
    // this now is not needed - done during propagation
    //nodeMergeSymLabels();
  }

  /**
   * Scans around a node CCW, propagating the labels
   * for a given area geometry to all edges (and their sym)
   * with unknown locations for that geometry.
   * @param e2 
   * 
   * @param geomIndex the geometry to propagate locations for
   */
  private void nodePropagateAreaLabels(OverlayEdge e2, int geomIndex) {
    OverlayEdge eStart = nodeFindPropStartEdge(e2, geomIndex);
    // no labelled edge found, so nothing to propagate
    if ( eStart == null )
      return;
    
    // initialize currLoc to location of L side
    int currLoc = eStart.getLabel().getLocation(geomIndex, Position.LEFT);
    OverlayEdge e = eStart.oNextOE();

    Debug.println("\npropagateAreaLabels geomIndex = " + geomIndex + " : " + eStart);
    Debug.print("BEFORE: " + eStart.toStringNode());
    
    do {
      OverlayLabel label = e.getLabel();
      /**
       * If location is unknown 
       * they are all set to current location
       */
      if ( ! label.hasLocation(geomIndex) ) {
        e.setLocationAreaBoth(geomIndex, currLoc);
      }
      else {
        /**
         *  Location is known, so update curr loc
         *  (which may change moving from R to L across the edge
         */
        int locRight = e.getLabel().getLocation(geomIndex, Position.RIGHT);
        if (locRight != currLoc) {
          Debug.println("side location conflict: edge R loc " 
        + Location.toLocationSymbol(locRight) + " <>  curr loc " + Location.toLocationSymbol(currLoc) 
        + " for " + e);
          throw new TopologyException("side location conflict", e.getCoordinate());
        }
        int locLeft = e.getLabel().getLocation(geomIndex, Position.LEFT);
        if (locLeft == Location.NONE) {
          Assert.shouldNeverReachHere("found single null side at " + e);
        }
        currLoc = locLeft;
      }
      e = e.oNextOE();
    } while (e != eStart);
    Debug.print("AFTER: " + eStart.toStringNode());
  }

  /**
   * Finds a node edge which has a labelling for this geom.
   * @param e2 
   * 
   * @param geomIndex
   * @return labelled edge, or null if no edges are labelled
   */
  private OverlayEdge nodeFindPropStartEdge(OverlayEdge e, int geomIndex) {
    OverlayEdge eStart = e;
    do {
      OverlayLabel label = e.getLabel();
      if (label.hasLocation(geomIndex)) {
        return eStart;
      }
      eStart = (OverlayEdge) eStart.oNext();
    } while (eStart != e);
    return null;
  }
}
