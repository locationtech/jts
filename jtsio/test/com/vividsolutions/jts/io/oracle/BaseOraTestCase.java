/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.io.oracle;

import junit.framework.TestCase;

/**
 * Tests OraReader without requiring an Oracle connection.
 * 
 * @author mbdavis
 *
 */
public class BaseOraTestCase extends TestCase
{
  public BaseOraTestCase(String name)
  {
    super(name);
  }

  int NULL = -1;

  
  public static double[] SDO_POINT_TYPE(int x, int y, int z) {
	return new double[] { x, y, z};
  }


  public static double[] SDO_ORDINATE_ARRAY(double i, double j)
  {
    return new double[] { i, j };
  }
  
  public static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3)
  {
    return new double[] { i0, i1, i2, i3 };
  }

  public static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3, double i4, double i5)
  {
    return new double[] { i0, i1, i2, i3, i4, i5 };
  }

  public static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3, double i4, double i5, 
		  double i6, double i7, double i8, double i9, double i10, double i11)
  {
    return new double[] { i0, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11 };
  }

  public static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3, double i4, double i5, 
		  double i6, double i7, double i8, double i9)
  {
    return new double[] { i0, i1, i2, i3, i4, i5, i6, i7, i8, i9 };
  }

  public static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3, double i4, double i5, 
		  double i6, double i7)
  {
    return new double[] { i0, i1, i2, i3, i4, i5, i6, i7 };
  }

  public static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3, double i4, double i5, 
		  double i6, double i7, double i8, double i9, double i10, double i11,
		  double i12, double i13, double i14, double i15)
  {
    return new double[] { i0, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15 };
  }

  public static int[] SDO_ELEM_INFO_ARRAY(int t11, int t12, int t13)
  {
    return new int[] { t11, t12, t13 };
  }

  public static int[] SDO_ELEM_INFO_ARRAY(int t11, int t12, int t13, int t21, int t22, int t23)
  {
    return new int[] { t11, t12, t13, t21, t22, t23 };
  }

  public static int[] SDO_ELEM_INFO_ARRAY(int t11, int t12, int t13, int t21, int t22, int t23, int t31, int t32, int t33)
  {
    return new int[] { t11, t12, t13, t21, t22, t23, t31, t32, t33 };
  }

}
