package org.locationtech.jts.operation.overlaysr;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.precision.GeometryPrecisionReducer;

public class OverlaySR {


  /**
   * Computes an overlay operation for 
   * the given geometry arguments.
   * 
   * @param geom0 the first geometry argument
   * @param geom1 the second geometry argument
   * @param opCode the code for the desired overlay operation
   * @return the result of the overlay operation
   */
  public static Geometry overlayOp(Geometry geom0, Geometry geom1, PrecisionModel pm, int opCode)
  {
    OverlaySR gov = new OverlaySR(geom0, geom1, pm);
    Geometry geomOv = gov.getResultGeometry(opCode);
    return geomOv;
  }

  private Geometry[] geom;
  private GeometryFactory geomFact;
  private PrecisionModel pm;

  public OverlaySR(Geometry geom0, Geometry geom1, PrecisionModel pm) {
    geom = new Geometry[] { geom0, geom1 };
    this.pm = pm;
    geomFact = geom0.getFactory();
  }  
  
  private Geometry getResultGeometry(int opCode) {
    if (opCode == OverlayOp.UNION) {
      
      // **********  TESTIMG ONLY  **********
      Geometry gr0 = GeometryPrecisionReducer.reduce(geom[0], pm);
      Geometry gr1 = GeometryPrecisionReducer.reduce(geom[1], pm);
      return gr0.union(gr1);
      
    }
    // MD - will not implement other overlay ops yet
    throw new UnsupportedOperationException();
  }


}
