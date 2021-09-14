package org.locationtech.jts.algorithm.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 * Linear Discrete Fréchet Distance computation
 */
public class DiscreteFrechetDistanceLinear {

  /**
   * Computes the Discrete Fréchet Distance between two {@link Geometry}s
   * using a {@code cartesian} distance computation function.
   *
   * @param g0 the 1st geometry
   * @param g1 the 2nd geometry
   * @return the cartesian distance between {#g0} and {#g1}
   */
  public static double distance(Geometry g0, Geometry g1) {
    DiscreteFrechetDistanceLinear dist = new DiscreteFrechetDistanceLinear(g0, g1);
    return dist.distance();
  }

  private final Geometry g0;
  private final Geometry g1;

  public DiscreteFrechetDistanceLinear(Geometry g0, Geometry g1) {
    this.g0 = g0;
    this.g1 = g1;
  }

  public double distance() {

    Coordinate[] coords0 = this.g0.getCoordinates();
    Coordinate[] coords1 = this.g1.getCoordinates();
    double[][] distances = new double[coords0.length][];
    for (int i = 0; i < coords0.length; i++)
      distances[i] = new double[coords1.length];

    for (int i = 0; i < coords0.length; i++) {
      for (int j = 0; j < coords1.length; j++)
      {
        double distance = coords0[i].distance(coords1[j]);
        if (i > 0 && j > 0)
        {
          distances[i][j] = Math.max(Math.min(Math.min(distances[i-1][j], distances[i-1][j-1]), distances[i][j-1]), distance);
        }
        else if (i > 0)
        {
          distances[i][j] = Math.max(distances[i-1][0], distance);
        }
        else if (j > 0)
        {
          distances[i][j] = Math.max(distances[0][j-1], distance);
        }
        else
        {
          distances[i][j] = distance;
        }
      }
    }

    //System.out.println(toString(coords0.length, coords1.length, distances));
    //System.out.println();
    return distances[coords0.length-1][coords1.length-1];
  }

  /*
  // For debugging purposes only
  private static String toString(int numRows, int numCols,
                                 double[][] sparse) {

    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < numRows; i++)
    {
      sb.append('[');
      for(int j = 0; j < numCols; j++)
      {
        if (j > 0)
          sb.append(", ");
        sb.append(String.format("%8.4f", sparse[i][j]));
      }
      sb.append(']');
      if (i < numRows - 1) sb.append(",\n");
    }
    sb.append(']');
    return sb.toString();
  }
   */

}
