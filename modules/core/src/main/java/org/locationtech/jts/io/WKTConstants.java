/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io;

/**
 * Constants used in the WKT (Well-Known Text) format.
 * 
 * @author Martin Davis
 *
 */
public class WKTConstants {

  public static final String GEOMETRYCOLLECTION = "GEOMETRYCOLLECTION";
  public static final String LINEARRING = "LINEARRING";
  public static final String LINESTRING = "LINESTRING";
  public static final String MULTIPOLYGON = "MULTIPOLYGON";
  public static final String MULTILINESTRING = "MULTILINESTRING";
  public static final String MULTIPOINT = "MULTIPOINT";
  public static final String POINT = "POINT";
  public static final String POLYGON = "POLYGON";

  /* Extended OGC SFA / ISO 19125-2 type keywords. The core JTS readers and
   * writers do not handle these directly; they are exposed here so that
   * extension modules (e.g. {@code jts-curved}) and downstream tooling can
   * share a single canonical set of strings. */
  public static final String CIRCULARSTRING = "CIRCULARSTRING";
  public static final String COMPOUNDCURVE = "COMPOUNDCURVE";
  public static final String CURVEPOLYGON = "CURVEPOLYGON";
  public static final String MULTICURVE = "MULTICURVE";
  public static final String MULTISURFACE = "MULTISURFACE";
  public static final String POLYHEDRALSURFACE = "POLYHEDRALSURFACE";
  public static final String TIN = "TIN";
  public static final String TRIANGLE = "TRIANGLE";

  public static final String EMPTY = "EMPTY";

  public static final String M = "M";
  public static final String Z = "Z";
  public static final String ZM = "ZM";

}
