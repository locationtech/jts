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
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

class ElevationModel {
  
  private static final int DEFAULT_CELL_NUM = 3;

  public static ElevationModel create(Geometry geom1, Geometry geom2) {
    Envelope extent = geom1.getEnvelopeInternal().copy();
    if (geom2 != null) {
      extent.expandToInclude(geom2.getEnvelopeInternal());
    }
    ElevationModel model = new ElevationModel(extent, DEFAULT_CELL_NUM, DEFAULT_CELL_NUM);
    if (geom1 != null) model.add(geom1);
    if (geom2 != null) model.add(geom2);
    return model;
  }
  
  private Envelope extent;
  private int numCellX;
  private int numCellY;
  private double cellSizeX;
  private double cellSizeY;
  private ElevationCell[][] cells;
  private boolean isInitialized = false;
  private boolean hasZValue = false;
  private double averageZ = Double.NaN;

  public ElevationModel(Envelope extent, int numCellX, int numCellY) {
    this.extent = extent;
    this.numCellX = numCellX;
    this.numCellY = numCellY;
    
    cellSizeX = extent.getWidth() / numCellX;
    cellSizeY = extent.getHeight() / numCellY;
    if(cellSizeX <= 0.0) {
      numCellX = 1;
    }
    if(cellSizeY <= 0.0) {
      numCellY = 1;
    }
    cells = new ElevationCell[numCellX][numCellY];
  }
  
  public void add(Geometry geom) {
    geom.apply(new CoordinateSequenceFilter() {

      @Override
      public void filter(CoordinateSequence seq, int i) {
        double z = seq.getOrdinate(i, Coordinate.Z);
        add(seq.getOrdinate(i, Coordinate.X),
            seq.getOrdinate(i, Coordinate.Y),
            z);
      }

      @Override
      public boolean isDone() {
        return false;
      }

      @Override
      public boolean isGeometryChanged() {
        return false;
      }
      
    });
  }
  
  protected void add(double x, double y, double z) {
    if (Double.isNaN(z))
      return;
    hasZValue = true;
    ElevationCell cell = getCell(x, y, true);
    cell.add(z);
  }
  
  private void init() {
    isInitialized = true;
    int numCells = 0;
    double sumZ = 0.0;
    
    for (int i = 0; i < cells.length; i++) {
      for (int j = 0; j < cells[0].length; j++) {
        ElevationCell cell = cells[i][j];
        if (cell != null) {
          cell.compute();
          numCells++;
          sumZ += cell.getZ();
        }
      }
    }
    averageZ = Double.NaN;
    if (numCells > 0) {
      averageZ = sumZ / numCells;
    }
  }
  
  public double getZ(double x, double y) {
    if (! isInitialized) 
      init();
    ElevationCell cell = getCell(x, y, false);
    if (cell == null) 
      return averageZ;
    return cell.getZ();
  }
  
  /**
   * Computes Z values for any missing Z values in a geometry,
   * using the computed model.
   * If the model has no Z value, or the geometry coordinate dimension
   * does not include Z, no action is taken.
   * 
   * @param geom the geometry to elevate
   */
  public void populateZ(Geometry geom) {
    // short-circuit if no Zs are present in model
    if (! hasZValue)
      return;
    
    if (! isInitialized) 
      init();
    
    geom.apply(new CoordinateSequenceFilter() {

      private boolean isDone = false;

      @Override
      public void filter(CoordinateSequence seq, int i) {
        if (!seq.hasZ()) {
          // if no Z then short-circuit evaluation
          isDone = true;
          return;
        }
        // if Z not populated then assign using model
        if (Double.isNaN( seq.getZ(i) )) {
          double z = getZ(seq.getOrdinate(i, Coordinate.X),
                          seq.getOrdinate(i, Coordinate.Y));
          seq.setOrdinate(i, Coordinate.Z, z);
        }
      }

      @Override
      public boolean isDone() {
        return isDone;
      }

      @Override
      public boolean isGeometryChanged() {
        return false;
      }
      
    });
  }
  
  private ElevationCell getCell(double x, double y, boolean isCreateIfMissing) {
    int ix = 0;
    if (numCellX > 1) {
      ix = (int) ((x - extent.getMinX()) / cellSizeX);
      if (ix >= numCellX) ix = numCellX -1;
    }
    int iy = 0;
    if (numCellY > 1) {
      iy = (int) ((y - extent.getMinY()) / cellSizeY);
      if (iy >= numCellY) iy = numCellY - 1;
    }
    ElevationCell cell = cells[ix][iy];
    if (isCreateIfMissing && cell == null) {
      cell = new ElevationCell();
      cells[ix][iy] = cell;
    }
    return cell;
  }

  static class ElevationCell {

    private int numZ = 0;
    private double sumZ = 0.0;
    private double avgZ;

    public void add(double z) {
      numZ = 1;
      sumZ += z;
    }
    
    public void compute() {
      avgZ = Double.NaN;
      if (numZ > 0) 
        avgZ = sumZ / numZ;
    }
    
    public double getZ() {
      return avgZ;
    }
  }
}
