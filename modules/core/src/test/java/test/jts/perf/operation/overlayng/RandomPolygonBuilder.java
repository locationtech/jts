package test.jts.perf.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.shape.random.RandomPointsBuilder;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

public class RandomPolygonBuilder {
  
  public static Geometry build(int npts) {
    RandomPolygonBuilder builder = new RandomPolygonBuilder(npts);
    return builder.createPolygon();
  }
  
  private Envelope extent = new Envelope(0,100,0,100);
  private GeometryFactory geomFact = new GeometryFactory();
  private int npts;
  private Geometry voronoi;
  
  public RandomPolygonBuilder(int npts) {
    this.npts = npts;
    Geometry sites = randomPoints(extent, npts);
    voronoi = voronoiDiagram(sites, extent);
  }
  
  public Geometry createPolygon() {
    Geometry cellsSelect = select(voronoi, npts / 2);
    Geometry poly = cellsSelect.union();
    return poly;
  }
  
  private Geometry select(Geometry geoms, int n) {
    List<Geometry> selection = new ArrayList<Geometry>();
    // add all the geometries
    for (int i = 0; i < geoms.getNumGeometries(); i++) {
      selection.add(geoms.getGeometryN(i));
    }
    // toss out random ones to leave n
    while (selection.size() > n) {
      int index = (int) (selection.size() * Math.random());
      selection.remove(index);
    }
    return geomFact.buildGeometry(selection);
  }

  public Geometry randomPoints(Envelope extent, int nPts) {
    RandomPointsBuilder shapeBuilder = new RandomPointsBuilder( geomFact );
    shapeBuilder.setExtent(extent);
    shapeBuilder.setNumPoints(nPts);
    return shapeBuilder.getGeometry();
  }
  
  public Geometry voronoiDiagram(Geometry sitesGeom, Envelope extent)
  {
    VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
    builder.setSites(sitesGeom);
    builder.setClipEnvelope(extent);
    builder.setTolerance(.0001);
    Geometry diagram = builder.getDiagram( sitesGeom.getFactory() ); 
    return diagram;
  }
}
