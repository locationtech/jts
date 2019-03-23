package test.jts.perf.clip;

import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.Stopwatch;
import org.locationtech.jtslab.clip.RectangleClipPolygon;

public class TestPerfRectangleClipPolygon {
  
  static GeometryFactory factory = new GeometryFactory();
  
  public static void main(String[] args) {
    TestPerfRectangleClipPolygon test = new TestPerfRectangleClipPolygon();
    try {
      test.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  public static List<Geometry> readWKTFile(String filename) throws Exception {
    WKTFileReader fileRdr = new WKTFileReader(filename, new WKTReader());
    return (List<Geometry>) fileRdr.read();
  }
  
  private void run() {
    Geometry data = loadData();
    
    System.out.println("Dataset: # geometries = " + data.getNumGeometries()
        + "   # pts = " + data.getNumPoints());
    
    Stopwatch sw = new Stopwatch();
    
    runClip(data);
    System.out.println("Time: " + sw.getTimeString());
  }
  
  private GeometryCollection loadData() {
    List<Geometry> data = null;
    try {
       data = readWKTFile("testdata/world.wkt");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return factory.createGeometryCollection(GeometryFactory.toGeometryArray(data));
  }
  
  private void runClip(Geometry data) {
    Envelope dataEnv = data.getEnvelopeInternal();

    int gridSize = 20;
    for (int x = -180; x < 180; x += gridSize) {
      for (int y = -90; y < 90; y += gridSize) {
        Envelope env = new Envelope(x, x+gridSize, y, y+gridSize);
        Geometry rect = factory.toGeometry(env);
        runClip(rect, data);
      }
    }
  }
  private void runClip(Geometry rect,Geometry data) {
    for (int i = 0; i < data.getNumGeometries(); i++) {
      Geometry geom = data.getGeometryN(i);
      clip(rect, geom);
      //rectangleIntersection(rect, geom);
    }
  }
  
  private Geometry clip(Geometry rect, Geometry geom) {
    RectangleClipPolygon clipper = new RectangleClipPolygon(rect);
    return clipper.clip(geom);
  }
  
  private Geometry rectangleIntersection(Geometry rect, Geometry geom) {
    Envelope env = rect.getEnvelopeInternal();
    Geometry result;
    if (env.contains(geom.getEnvelopeInternal())) {
      return geom.copy();
    }    
    // Use intersects check first as that is faster
    if (! rect.intersects(geom)) return null;
    
    return rect.intersection(geom);
  }
  
  private Envelope envelope(List<Geometry> world) {
    Envelope env = new Envelope();
    for (Geometry geom : world) {
      env.expandToInclude(geom.getEnvelopeInternal());
    }
    return env;
  }
}
