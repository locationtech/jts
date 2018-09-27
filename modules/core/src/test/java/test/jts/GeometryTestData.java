package test.jts;

public class GeometryTestData {

  public static String WKT_POINT = "POINT ( 10 10)";
  
  public static String WKT_LINESTRING = "LINESTRING (10 10, 20 20, 30 40)";
  
  public static String WKT_LINEARRING = "LINEARRING (10 10, 20 20, 30 40, 10 10)";
  
  public static String WKT_POLY = "POLYGON ((50 50, 50 150, 150 150, 150 50, 50 50))";
  
  public static String WKT_MULTIPOINT = "MULTIPOINT ((10 10), (20 20))";
  
  public static String WKT_MULTILINESTRING = "MULTILINESTRING ((10 10, 20 20), (15 15, 30 15))";
  
  public static String WKT_MULTIPOLYGON = "MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))";
  
  public static String WKT_GC = "GEOMETRYCOLLECTION (POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200)), LINESTRING (150 250, 250 250))";


}
