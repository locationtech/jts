package com.vividsolutions.jts.io.oracle;

public class OraGeom
{

  public int gType;
  public double[] ptType = null;
  public int[] elemInfo = null;
  public double[] ordinates = null;

  public OraGeom(int gType, int[] elemInfo, double[] ordinates)
  {
    this.gType = gType;
    this.elemInfo = elemInfo;
    this.ordinates = ordinates;
  }

  public OraGeom(int gType, double[] ptType)
  {
    this.gType = gType;
    this.ptType = ptType;
  }

  public static OraGeom sdo_geometry(int gType, int srid, int ptType,
	      int[] elemInfo, double[] ordinates)
	  {
	    return new OraGeom(gType, elemInfo, ordinates);
	  }
  public static OraGeom sdo_geometry(int gType, int srid, double[] ptType,
	      int elemInfo, int ordinates)
	  {
	    return new OraGeom(gType, ptType);
	  }
}
