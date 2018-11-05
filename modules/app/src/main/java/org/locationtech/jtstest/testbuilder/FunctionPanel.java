package org.locationtech.jtstest.testbuilder;

import org.locationtech.jtstest.geomfunction.GeometryFunction;

public interface FunctionPanel {

  GeometryFunction getFunction();

  Object[] getFunctionParams();

}
