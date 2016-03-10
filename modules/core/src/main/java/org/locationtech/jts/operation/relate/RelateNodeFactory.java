

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
package org.locationtech.jts.operation.relate;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geomgraph.Node;
import org.locationtech.jts.geomgraph.NodeFactory;
import org.locationtech.jts.geomgraph.NodeMap;

/**
 * Used by the {@link NodeMap} in a {@link RelateNodeGraph} to create {@link RelateNode}s.
 *
 * @version 1.7
 */
public class RelateNodeFactory
  extends NodeFactory
{
  public Node createNode(Coordinate coord)
  {
    return new RelateNode(coord, new EdgeEndBundleStar());
  }
}
