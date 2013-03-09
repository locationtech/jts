package com.vividsolutions.jtstest.testbuilder.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;

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
