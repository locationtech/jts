/*
 * Copyright (c) 2026 grootstebozewolf
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io.curved;

import org.locationtech.jts.io.WKTWriter;

/**
 * A {@link WKTWriter} subclass for the OGC SFA / ISO 19125-2 extended
 * geometry types.
 * <p>
 * In the current phase-1 implementation this class is a no-op marker:
 * the curve geometry classes ({@code CircularString}, {@code Triangle},
 * {@code CurvePolygon}, etc.) extend their nearest core counterparts
 * and the core {@code WKTWriter} already emits each subclass's keyword
 * via {@code Geometry.getGeometryType().toUpperCase(Locale.ROOT)}.
 * <p>
 * The class is provided here so that callers can pair {@code
 * CurvedWKTReader} with {@code CurvedWKTWriter} symmetrically, and so
 * that future enhancements (member-structured emission for
 * {@code CompoundCurve}, etc.) can land here without changing caller
 * code.
 */
public class CurvedWKTWriter extends WKTWriter {

  public CurvedWKTWriter() {
    super();
  }

  public CurvedWKTWriter(int outputDimension) {
    super(outputDimension);
  }
}
