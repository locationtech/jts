/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jtstest.testbuilder.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;

public class GeometryTransferable implements Transferable 
{
  public static final DataFlavor GEOMETRY_FLAVOR =
    new DataFlavor(Geometry.class, "Geometry");
  
  private Geometry geom;
  private boolean isFormatted;
  
  private static final DataFlavor[] flavors = { 
  	DataFlavor.stringFlavor,       
  	GEOMETRY_FLAVOR };

  public GeometryTransferable(Geometry geom) {
    this.geom = geom;
  }

  public GeometryTransferable(Geometry geom, boolean isFormatted) {
    this.geom = geom;
    this.isFormatted = isFormatted;
  }

  public DataFlavor[] getTransferDataFlavors() {
      return flavors;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
      for (int i = 0; i < flavors.length; i++) {
          if (flavor.equals(flavors[i])) {
              return true;
          }
      }
      return false;
  }

  public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException
  {
    if (flavor.equals(GEOMETRY_FLAVOR)) {
      return geom;
  }
  if (flavor.equals(DataFlavor.stringFlavor)) {
    if (isFormatted) {
      WKTWriter writer = new WKTWriter();
      writer.setFormatted(true);
      writer.setMaxCoordinatesPerLine(5);
      String wkt = writer.writeFormatted(geom);
      return wkt;
    }
    return geom.toString();
  }
  throw new UnsupportedFlavorException(flavor);

  }
}
