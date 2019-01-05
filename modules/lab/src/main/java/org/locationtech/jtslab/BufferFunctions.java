package org.locationtech.jtslab;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.buffer.VariableWidthBuffer;

public class BufferFunctions {
  //@Metadata(description="Buffer a line by a width varying along the line")
  public static Geometry bufferVariableWidth(Geometry line,
      //@Metadata(title="Start width")
      double startWidth,
      //@Metadata(title="End width")
      double endWidth) {
    return VariableWidthBuffer.buffer(line, startWidth, endWidth);
  }
}
