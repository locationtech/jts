/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.polygonize;

import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.strtree.STRtree;

/**
 * Assigns hole rings to shell rings 
 * during polygonization.
 * Uses spatial indexing to improve performance
 * of shell lookup.
 * 
 * @author mdavis
 *
 */
public class HoleAssigner 
{
  /**
   * Assigns hole rings to shell rings.
   * 
   * @param holes list of hole rings to assign
   * @param shells list of shell rings
   */
  public static void assignHolesToShells(List holes, List shells) {
    HoleAssigner assigner = new HoleAssigner(shells);
    assigner.assignHolesToShells(holes);
  }
  
  private List<EdgeRing> shells;
  private SpatialIndex shellIndex;
  
  /**
   * Creates a new hole assigner.
   * 
   * @param shells the shells to be assigned to
   */
  public HoleAssigner(List<EdgeRing> shells) {
    this.shells = shells;
    buildIndex();
  }
  
  private void buildIndex() {
    shellIndex = new STRtree();
    for (EdgeRing shell : shells) {
      shellIndex.insert(shell.getRing().getEnvelopeInternal(), shell);
    }
  }

  /**
   * Assigns holes to the shells.
   * 
   * @param holeList list of hole rings to assign
   */
  public void assignHolesToShells(List<EdgeRing> holeList)
  {
    for (Iterator i = holeList.iterator(); i.hasNext(); ) {
      EdgeRing holeER = (EdgeRing) i.next();
      assignHoleToShell(holeER);
    }
  }
  
  private void assignHoleToShell(EdgeRing holeER)
  {
    EdgeRing shell = findShellContaining(holeER);
    if (shell != null) {
      shell.addHole(holeER);
    }
  }
  
  private List<EdgeRing> queryOverlappingShells(Envelope ringEnv) {
    return (List<EdgeRing>) shellIndex.query(ringEnv);
  }
  
  /**
   * Find the innermost enclosing shell EdgeRing containing the argument EdgeRing, if any.
   * The innermost enclosing ring is the <i>smallest</i> enclosing ring.
   * The algorithm used depends on the fact that:
   * <br>
   *  ring A contains ring B if envelope(ring A) contains envelope(ring B)
   * <br>
   * This routine is only safe to use if the chosen point of the hole
   * is known to be properly contained in a shell
   * (which is guaranteed to be the case if the hole does not touch its shell)
   *
   * @return containing shell EdgeRing, if there is one
   * or null if no containing EdgeRing is found
   */
  private EdgeRing findShellContaining(EdgeRing testEr)
  {
    Envelope testEnv = testEr.getRing().getEnvelopeInternal();   
    List<EdgeRing> candidateShells = queryOverlappingShells(testEnv);
    return EdgeRing.findEdgeRingContaining(testEr, candidateShells);
  }
}
