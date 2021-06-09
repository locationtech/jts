package org.locationtech.jts.triangulate;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class VoronoiDiagramBuilderTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(VoronoiDiagramBuilderTest.class);
  }

  public VoronoiDiagramBuilderTest(String name) { super(name); }
  
  public void testClipEnvelope() {
    Geometry sites = read("MULTIPOINT ((50 100), (50 50), (100 50), (100 100))");
    Geometry clip = read("POLYGON ((0 0, 0 200, 200 200, 200 0, 0 0))");
    Geometry voronoi = voronoiDiagram(sites, clip);
    assertTrue(voronoi.getEnvelopeInternal().equals(clip.getEnvelopeInternal()));
  }
  
  public void testClipEnvelopeBig() {
    Geometry sites = read("MULTIPOINT ((50 100), (50 50), (100 50), (100 100))");
    Geometry clip = read("POLYGON ((-1000 1000, 1000 1000, 1000 -1000, -1000 -1000, -1000 1000))");
    Geometry voronoi = voronoiDiagram(sites, clip);
    assertTrue(voronoi.getEnvelopeInternal().equals(clip.getEnvelopeInternal()));
  }
  
  private static final double TRIANGULATION_TOLERANCE = 0.0;

  public static Geometry voronoiDiagram(Geometry sitesGeom, Geometry clipGeom)
  {
    VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
    builder.setSites(sitesGeom);
    if (clipGeom != null)
      builder.setClipEnvelope(clipGeom.getEnvelopeInternal());
    builder.setTolerance(TRIANGULATION_TOLERANCE);
    Geometry diagram = builder.getDiagram(sitesGeom.getFactory()); 
    return diagram;
  }
}
