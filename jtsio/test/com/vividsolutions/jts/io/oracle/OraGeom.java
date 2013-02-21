package com.vividsolutions.jts.io.oracle;

public class OraGeom
{

  public int gType;
  public int[] elemInfo;
  public double[] ordinates;

  public OraGeom(int gType, int[] elemInfo, double[] ordinates)
  {
    this.gType = gType;
    this.elemInfo = elemInfo;
    this.ordinates = ordinates;
  }

  public static OraGeom sdo_geometry(int gType, int null1, int null2,
      int[] elemInfo, double[] ordinates)
  {
    return new OraGeom(gType, elemInfo, ordinates);
  }
}
