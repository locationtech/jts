package com.vividsolutions.jts.io.oracle;

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

  /** Presents Datum[] as a double[] */
  public static double[] asDoubleArray(Datum data[], final double defaultValue)
  {
    if (data == null)
      return null;
    double array[] = new double[data.length];
    for (int i = 0; i < data.length; i++) {
      array[i] = OraUtil.asDouble(data[i], defaultValue);
    }
    return array;
  }

  public static int[] asIntArray(ARRAY array, int defaultValue) throws SQLException
  {
    if (array == null)
      return null;
    if (defaultValue == 0)
      return array.getIntArray();

    return OraUtil.asIntArray(array.getOracleArray(), defaultValue);
  }

  /** Presents Datum[] as a int[] */
  public static int[] asIntArray(Datum data[], final int defaultValue)
      throws SQLException
  {
    if (data == null)
      return null;
    int array[] = new int[data.length];
    for (int i = 0; i < data.length; i++) {
      array[i] = OraUtil.asInteger(data[i], defaultValue);
    }
    return array;
  }

  /** Presents array as a double[] */
  public static double[] asDoubleArray(ARRAY array, final double defaultValue)
      throws SQLException
  {
    if (array == null)
      return null;
    if (defaultValue == 0)
      return array.getDoubleArray();

    return asDoubleArray(array.getOracleArray(), defaultValue);
  }

  /** Presents struct as a double[] */
  public static double[] asDoubleArray(STRUCT struct, final double defaultValue)
      throws SQLException
  {
    if (struct == null)
      return null;
    return asDoubleArray(struct.getOracleAttributes(), defaultValue);
  }

  /** Presents datum as a double */
  public static double asDouble(Datum datum, final double defaultValue)
  {
    if (datum == null)
      return defaultValue;
    return ((NUMBER) datum).doubleValue();
  }

  /** Presents datum as an int */
  public static int asInteger(Datum datum, final int defaultValue)
      throws SQLException
  {
    if (datum == null)
      return defaultValue;
    return ((NUMBER) datum).intValue();
  }

  /**
   * Convience method for NUMBER construction.
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
   * <p>
   * Compare and contrast with toORDINATE - which treats <code>Double.NaN</code>
   * as<code>NULL</code>
   * </p>
   */
  public static ARRAY toARRAY(double doubles[], String dataType,
      OracleConnection connection) throws SQLException
  {
    ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor(dataType,
        connection);

    return new ARRAY(descriptor, connection, doubles);
  }

  /**
   * Convience method for ARRAY construction.
   */
  public static ARRAY toARRAY(int ints[], String dataType,
      OracleConnection connection) throws SQLException
  {
    ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor(dataType,
        connection);

    return new ARRAY(descriptor, connection, ints);
  }

  /** Convenience method for STRUCT construction. */
  public static STRUCT toSTRUCT(Datum attributes[], String dataType,
      OracleConnection connection) throws SQLException
  {
    if (dataType.startsWith("*.")) {
      dataType = "DRA." + dataType.substring(2);// TODO here
    }
    StructDescriptor descriptor = StructDescriptor.createDescriptor(dataType,
        connection);

    return new STRUCT(descriptor, connection, attributes);
  }

}
