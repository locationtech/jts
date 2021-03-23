package org.locationtech.jtstest.cmd;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import junit.framework.TestCase;

public class JTSOpCmdTest extends TestCase {
  private boolean isVerbose = true;

  public JTSOpCmdTest(String Name_) {
    super(Name_);
  }

  public static void main(String[] args) {
    String[] testCaseName = {JTSOpCmdTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }
  
  public void testErrorFileNotFoundA() {
    runCmdError( args("-a", "missing.wkt"), 
        JTSOpRunner.ERR_FILE_NOT_FOUND );
  }
  
  public void testErrorFileNotFoundB() {
    runCmdError( args("-b", "missing.wkt"), 
        JTSOpRunner.ERR_FILE_NOT_FOUND );
  }
  
  public void testErrorFunctioNotFound() {
    runCmdError( args("-a", "POINT ( 1 1 )", "buffer" ),
        JTSOpRunner.ERR_FUNCTION_NOT_FOUND );
  }
  
  public void testErrorMissingArgBuffer() {
    runCmdError( args("-a", "POINT ( 1 1 )", "Buffer.buffer" ),
        JTSOpRunner.ERR_WRONG_ARG_COUNT );
  }
  
  public void testErrorbadMultiArgsNoRParen() {
    runCmdError( args("-a", "POINT ( 1 1 )", "Buffer.buffer", "(1,2,3" ),
        JTSOpCmd.ERR_INVALID_ARG_PARAM );
  }
  
  public void testErrorbadMultiArgsNoLParen() {
    runCmdError( args("-a", "POINT ( 1 1 )", "Buffer.buffer", "1,2,3)" ),
        JTSOpCmd.ERR_INVALID_ARG_PARAM );
  }
  
  public void testErrorMissingGeomABuffer() {
    runCmdError( args("Buffer.buffer", "10" ),
        JTSOpRunner.ERR_REQUIRED_A );
  }
  
  /*
   // Missing B check is disabled for now
  public void testErrorMissingGeomBUnion() {
    runCmdError( args("-a", "POINT ( 1 1 )", "Overlay.union" ),
        JTSOpRunner.ERR_REQUIRED_B );
  }
  */
  
  public void testErrorMissingGeomAUnion() {
    runCmdError( args("-b", "POINT ( 1 1 )", "Overlay.union" ),
        JTSOpRunner.ERR_REQUIRED_A );
  }
  //===========================================
  
  public void testOpEnvelope() {
    runCmd( args("-a", "LINESTRING ( 1 1, 2 2)", "-f", "wkt", "envelope"), 
        "POLYGON" );
  }
  
  public void testOpLength() {
    runCmd( args("-a", "LINESTRING ( 1 0, 2 0 )", "-f", "txt", "length"), 
        "1" );
  }
  
  public void testOpUnionLines() {
    runCmd( args("-a", "LINESTRING ( 1 0, 2 0 )", "-b", "LINESTRING ( 2 0, 3 0 )", "-f", "wkt", "Overlay.union"), 
        "MULTILINESTRING ((1 0, 2 0), (2 0, 3 0))" );
  }
  
  public void testOpNoArg() {
    runCmd( args("-f", "wkt", "CreateRandomShape.randomPoints", "10"), 
        "MULTIPOINT" );
  }
  
  public void testCollectUnion() {
    runCmd( args("-a", "stdin", 
                "-collect", 
                "-f", "wkt", "Overlay.unaryUnion"), 
        stdin(new String[] {
            "POLYGON ((1 3, 3 3, 3 1, 1 1, 1 3))",
            "POLYGON ((5 3, 5 1, 3 1, 3 3, 5 3))"
        }),
        "POLYGON ((1 3, 3 3, 5 3, 5 1, 3 1, 1 1, 1 3))" );
    
  }
  
  public void testCollectLimitUnion() {
    runCmd( args("-a", "stdin", 
                "-collect", 
                "-limit", "2",
                "-f", "wkt", "Overlay.unaryUnion"), 
        stdin(new String[] {
            "POLYGON ((1 3, 3 3, 3 1, 1 1, 1 3))",
            "POLYGON ((5 3, 5 1, 3 1, 3 3, 5 3))",
            "POLYGON ((1 5, 5 5, 5 3, 1 3, 1 5))"
        }),
        "POLYGON ((1 3, 3 3, 5 3, 5 1, 3 1, 1 1, 1 3))" );
    
  }

  //===========================================

  public void testOpEachA() {
    runCmd( args("-a", "MULTILINESTRING((0 0, 10 10), (100 100, 110 110))", 
        "-eacha",
        "-f", "wkt", "envelope"), 
        "POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))\nPOLYGON ((100 100, 100 110, 110 110, 110 100, 100 100))" );
  }

  public void testOpEachAEachB() {
    runCmd( args(
        "-a", "MULTIPOINT((0 0), (0 1))", 
        "-b", "MULTIPOINT((9 9), (8 8))", 
        "-eacha", "-eachb",
        "-f", "wkt", "Distance.nearestPoints"), 
        "LINESTRING (0 0, 9 9)\nLINESTRING (0 0, 8 8)\nLINESTRING (0 1, 9 9)\nLINESTRING (0 1, 8 8)" );
  }

  public void testOpEachB() {
    runCmd( args(
        "-a", "MULTIPOINT((0 0), (0 1))", 
        "-b", "MULTIPOINT((9 9), (8 8))", 
        "-eachb",
        "-f", "wkt", "Distance.nearestPoints" ), 
        "LINESTRING (0 1, 9 9)\nLINESTRING (0 1, 8 8)" );
  }

  public void testOpEachAA() {
    runCmd( args(
        "-ab", "MULTIPOINT((0 0), (0 1))", 
        "-eacha",
        "-f", "wkt", "Distance.nearestPoints"), 
        "LINESTRING (0 0, 0 0)\nLINESTRING (0 0, 0 1)\nLINESTRING (0 1, 0 0)\nLINESTRING (0 1, 0 1)" );
  }

  public void testOpEachABIndexed() {
    runCmd( args(
        "-a", "MULTILINESTRING((0 0, 5 5), (10 0, 15 5))", 
        "-b", "MULTIPOINT((1 1), (11 1))", 
        "-eacha", "-eachb",
        "-index",
        "-f", "wkt", "Distance.nearestPoints"), 
        "LINESTRING (1 1, 1 1)\nLINESTRING (11 1, 11 1)" );
  }

  public void testOpBufferVals() {
    JTSOpCmd cmd = runCmd( args(
        "-a", "POINT(0 0)", 
        "-f", "wkt", 
        "Buffer.buffer", "val(1,2,3,4)" ), 
        null, null );
    List<Geometry> results = cmd.getResultGeometry();
    assertTrue("Not enough results for arg values",  results.size() == 4 );
    assertEquals("Incorrect summary value for arg values",  computeArea(results), 93.6, 1);
  }

  public void testOpBufferMultiArgParen() {
    JTSOpCmd cmd = runCmd( args(
        "-a", "POINT(0 0)", 
        "-f", "wkt", 
        "Buffer.buffer", "(1,2,3,4)" ), 
        null, null );
    List<Geometry> results = cmd.getResultGeometry();
    assertTrue("Not enough results for arg values",  results.size() == 4 );
    assertEquals("Incorrect summary value for arg values",  computeArea(results), 93.6, 1);
  }

  public void testOpBufferMultiArgNoParen() {
    JTSOpCmd cmd = runCmd( args(
        "-a", "POINT(0 0)", 
        "-f", "wkt", 
        "Buffer.buffer", "1,2,3,4" ), 
        null, null );
    List<Geometry> results = cmd.getResultGeometry();
    assertTrue("Not enough results for arg values",  results.size() == 4 );
    assertEquals("Incorrect summary value for arg values",  computeArea(results), 93.6, 1);
  }

  public void testWhereValid() {
    JTSOpCmd cmd = runCmd( args(
        "-a", "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))", 
        "-f", "wkt", 
        "-where", "1",
        "isValid" ), 
        null, null );
    List<Geometry> results = cmd.getResultGeometry();
    assertTrue("Not enough results for arg values",  results.size() == 1 );
  }

  public void testWhereInvalid() {
    JTSOpCmd cmd = runCmd( args(
        "-a", "POLYGON ((1 9, 9 1, 9 9, 1 1, 1 9))", 
        "-f", "wkt", 
        "-where","0",
        "isValid" ), 
        null, null );
    List<Geometry> results = cmd.getResultGeometry();
    assertTrue("Not enough results for arg values",  results.size() == 1 );
  }

  //----------------------------------------------------------------
  
  public void testSRIDBuffer() throws ParseException {
    JTSOpCmd cmd = runCmd( args(
        "-a", "POINT(0 0)", 
        "-srid", "4326",
        "-f", "wkb", 
        "Buffer.buffer", "1" ), 
        null, null );
    List<Geometry> results = cmd.getResultGeometry();
    assertEquals("Incorrect SRID", 4326, results.get(0).getSRID());
    
    Geometry outGeom = readWKB(cmd.getOutput());
    assertEquals("Incorrect SRID in WKB", 4326, outGeom.getSRID());
  }

  public void testSRIDStdIn() throws ParseException {
    JTSOpCmd cmd = runCmd( args(
        "-a", "stdin", 
        "-srid", "4326",
        "-f", "wkb", 
        "Buffer.buffer", "1" ), 
        stdin("POINT(0 0)"), null );
    List<Geometry> results = cmd.getResultGeometry();
    assertEquals("Incorrect SRID", 4326, results.get(0).getSRID());
    
    Geometry outGeom = readWKB(cmd.getOutput());
    assertEquals("Incorrect SRID in WKB", 4326, outGeom.getSRID());
  }

  public void testSRIDPolygonize() throws ParseException {
    JTSOpCmd cmd = runCmd( args(
        "-a", "MULTILINESTRING ((1 1, 9 9), (9 9, 9 1), (9 1, 1 1), (9 1, 16 9), (9 9, 16 9))", 
        "-srid", "4326",
        "-explode",
        "-f", "wkb", 
        "Polygonize.polygonize" ), 
        null, null );
    List<Geometry> results = cmd.getResultGeometry();
    assertEquals("Incorrect SRID", 4326,  results.get(0).getSRID());
    assertEquals("Incorrect SRID", 4326,  results.get(1).getSRID());
    
    String[] output = cmd.getOutputLines();
    for (String out : output) {
      Geometry outGeom = readWKB(out);
      assertEquals("Incorrect SRID in WKB",  outGeom.getSRID(), 4326);
    }
  }


  //----------------------------------------------------------------

  private Geometry readWKB(String wkbHex) throws ParseException {
    byte[] wkb = WKBReader.hexToBytes(wkbHex);
    WKBReader rdr = new WKBReader();
    return rdr.read(wkb);
  }

  public void testExplode() {
    JTSOpCmd cmd = runCmd( args(
        "-a", "LINESTRING(0 0, 10 10)", 
        "-b", "LINESTRING(0 10, 10 0)", 
        "-explode", 
        "-f", "wkt", 
        "Overlay.union" ), 
        null, null );
    List<Geometry> results = cmd.getResultGeometry();
    assertEquals("Not enough results for explode",  results.size(), 4 );
  }

  public void testLiteralEmptyLinestring() {
    JTSOpCmd cmd = runCmd( args(
        "-a", "LINESTRING EMPTY", 
        "-f", "wkt", 
        "Construction.boundary" ), 
        null, null );
    List<Geometry> results = cmd.getResultGeometry();
    assertEquals("Too many results for operation",  results.size(), 1 );
    assertTrue("Expected empty result",  results.get(0).isEmpty() );
  }

  public void testLiteralEmptyPoint() {
    JTSOpCmd cmd = runCmd( args(
        "-a", "POINT EMPTY", 
        "-f", "wkt", 
        "Construction.boundary" ), 
        null, null );
    List<Geometry> results = cmd.getResultGeometry();
    assertEquals("Too many results for operation",  results.size(), 1 );
    assertTrue("Expected empty result",  results.get(0).isEmpty() );
  }

  //===========================================
  
  public void testFormatWKB() {
    runCmd( args("-a", "LINESTRING ( 1 1, 2 2)", "-f", "wkb"), 
        "0000000002000000023FF00000000000003FF000000000000040000000000000004000000000000000" );
  }
  
  public void testFormatGeoJSON() {
    runCmd( args("-a", "LINESTRING ( 1 1, 2 2)", "-f", "geojson"), 
        "{\"type\":\"LineString\",\"coordinates\":[[1,1],[2,2]]}" );
  }
  
  public void testFormatSVG() {
    runCmd( args("-a", "LINESTRING ( 1 1, 2 2)", "-f", "svg"), 
        "<polyline" );
  }
  
  public void testFormatGML() {
    runCmd( args("-a", "LINESTRING ( 1 1, 2 2)", "-f", "gml"), 
        "<gml:LineString>" );
  }
  
  //===========================================

  public void testStdInWKT() {
    runCmd( args("-a", "stdin", "-f", "wkt", "envelope"), 
        stdin("LINESTRING ( 1 1, 2 2)"),
        "POLYGON" );
  }
  
  public void testStdInWKB() {
    runCmd( args("-a", "stdin", "-f", "wkt", "envelope"), 
        stdin("000000000200000005405900000000000040590000000000004072C000000000004062C00000000000405900000000000040690000000000004072C00000000000406F40000000000040590000000000004072C00000000000"),
        "POLYGON" );
  }
  
  /*
  // no longer supporting this semantic
  public void testGeomABStdIn() {
    runCmd( args("-ab", "stdin", "-f", "wkt", "Overlay.intersection"), 
        stdin("MULTILINESTRING (( 1 1, 3 3), (1 3, 3 1))"),
        "POINT (2 2)" );
  }
  */

  public void testErrorStdInBadFormat() {
    runCmdError( args("-a", "stdin", "-f", "wkt", "envelope"), 
        stdin("<gml fdlfld >"),
        JTSOpRunner.ERR_PARSE_GEOM );
  }
  
  private String[] args(String ... args) {
    return args;
  }

  private static InputStream stdin(String data) {
    InputStream instr = new ByteArrayInputStream(data.getBytes(Charset.forName("UTF-8")));
    return instr;
  }
  
  private static InputStream stdin(String[] dataArr) {
    String data = String.join("\n", dataArr);
    return stdin(data);
  }
  
  private static InputStream stdinFile(String filename) {
    try {
      return new FileInputStream(filename);
    }
    catch (FileNotFoundException ex) {
      throw new RuntimeException("File not found: " + filename);
    }
  }
  
  public void runCmd(String[] args, String expected)
  {    
    runCmd(args, null, expected);
  }

  private JTSOpCmd runCmd(String[] args, InputStream stdin, String expected) {
    JTSOpCmd cmd = new JTSOpCmd();
    cmd.captureOutput();
    cmd.captureResult();
    if (stdin != null) cmd.replaceStdIn(stdin);
    try {
      JTSOpRunner.OpParams cmdArgs = cmd.parseArgs(args);
      cmd.execute(cmdArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    checkExpected( cmd.getOutput(), expected);
    return cmd;
  }
  
  public void runCmdError(String[] args) {
    runCmdError(args, null);
  }

  public void runCmdError(String[] args, String expected) {
    runCmdError(args, null, expected);
  }

  public void runCmdError(String[] args, InputStream stdin, String expected)
  {    
    JTSOpCmd cmd = new JTSOpCmd();
    if (stdin != null) cmd.replaceStdIn(stdin);
    try {
      JTSOpRunner.OpParams cmdArgs = cmd.parseArgs(args);
      cmd.execute(cmdArgs);
    } 
    catch (CommandError e) {
      if (expected != null) checkExpected( e.getMessage(), expected );
      return;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    assertTrue("Expected error but command completed successfully", false);
  }

  private void checkExpected(String actual, String expected) {
    if (expected == null) return;
    
    boolean found = actual.contains(expected);
    if (isVerbose  && ! found) {
      System.out.println("Expected: " + expected);
      System.out.println("Actual: " + actual);
    }
    assertTrue( "Output does not contain string " + expected, found );
  }
  
  private static double computeArea(List<Geometry> results) {
    GeometryFactory fact = new GeometryFactory();
    Geometry geom = fact.buildGeometry(results);
    return geom.getArea();
  }
}
