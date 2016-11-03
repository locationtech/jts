/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.io.oracle;

import java.sql.SQLException;

import oracle.jdbc.OracleConnection;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.Datum;
import oracle.sql.NUMBER;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

/**
 * Utility methods for working with Oracle structures.
 * 
 * @author Martin Davis
 * 
 */
public class OraUtil
{
  /**
   * Converts an Oracle <code>Datum</code> into an <code>int</code> value, 
   * with a default value used if the datum is NULL.
   * 
   * @param datum the Oracle Datum
   * @param defaultValue the value to use for NULLs
   * @return an integer value
   * @throws SQLException if an error occurs
   */
  public static int toInteger(Datum datum, final int defaultValue)
      throws SQLException
  {
    if (datum == null)
      return defaultValue;
    return ((NUMBER) datum).intValue();
  }

  /**
   * Converts an Oracle <code>ARRAY</code> into a <code>int</code> array, 
   * with a default value used to represent NULL values.
   * 
   * @param array an Oracle ARRAY of integer values
   * @param defaultValue the value to use for NULL values
   * @return an array of ints
   * @throws SQLException if an error was encountered
   */
  public static int[] toIntArray(ARRAY array, int defaultValue) throws SQLException
  {
    if (array == null)
      return null;
    if (defaultValue == 0)
      return array.getIntArray();

    return toIntArray(array.getOracleArray(), defaultValue);
  }

  /** Presents Datum[] as a int[] */
  public static int[] toIntArray(Datum data[], final int defaultValue)
      throws SQLException
  {
    if (data == null)
      return null;
    int array[] = new int[data.length];
    for (int i = 0; i < data.length; i++) {
      array[i] = toInteger(data[i], defaultValue);
    }
    return array;
  }

  /** Presents Datum[] as a double[] */
  public static double[] toDoubleArray(Datum[] data, final double defaultValue)
  {
    if (data == null)
      return null;
    double array[] = new double[data.length];
    for (int i = 0; i < data.length; i++) {
      array[i] = toDouble(data[i], defaultValue);
    }
    return array;
  }

  /** Presents array as a double[] */
  public static double[] toDoubleArray(ARRAY array, final double defaultValue)
      throws SQLException
  {
    if (array == null)
      return null;
    if (defaultValue == 0)
      return array.getDoubleArray();

    return toDoubleArray(array.getOracleArray(), defaultValue);
  }

  /** Presents struct as a double[] */
  public static double[] toDoubleArray(STRUCT struct, final double defaultValue)
      throws SQLException
  {
    if (struct == null)
      return null;
    return toDoubleArray(struct.getOracleAttributes(), defaultValue);
  }

  /** Presents datum as a double */
  public static double toDouble(Datum datum, final double defaultValue)
  {
    if (datum == null)
      return defaultValue;
    return ((NUMBER) datum).doubleValue();
  }

  /**
   * Convenience method for NUMBER construction.
   * <p>
   * Double.NaN is represented as <code>NULL</code> to agree with JTS use.
   * </p>
   */
  public static NUMBER toNUMBER(double number) throws SQLException
  {
    if (Double.isNaN(number)) {
      return null;
    }
    return new NUMBER(number);
  }

  /**
   * Convience method for ARRAY construction.
   * </p>
   */
  public static ARRAY toARRAY(double[] doubles, String dataType,
      OracleConnection connection) throws SQLException
  {
    ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor(dataType,
        connection);
    return new ARRAY(descriptor, connection, doubles);
  }

  /**
   * Convience method for ARRAY construction.
   */
  public static ARRAY toARRAY(int[] ints, String dataType,
      OracleConnection connection) throws SQLException
  {
    ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor(dataType,
        connection);
    return new ARRAY(descriptor, connection, ints);
  }

  /** Convenience method for STRUCT construction. */
  public static STRUCT toSTRUCT(Datum[] attributes, String dataType,
      OracleConnection connection) throws SQLException
  {
    //TODO: fix this to be more generic
    if (dataType.startsWith("*.")) {
      dataType = "DRA." + dataType.substring(2);
    }
    StructDescriptor descriptor = StructDescriptor.createDescriptor(dataType,
        connection);
    return new STRUCT(descriptor, connection, attributes);
  }

}
