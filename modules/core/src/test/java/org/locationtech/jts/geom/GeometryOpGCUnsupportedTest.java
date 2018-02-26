package org.locationtech.jts.geom;

import test.jts.GeometryTestCase;

public class GeometryOpGCUnsupportedTest extends GeometryTestCase {

  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(GeometryOpGCUnsupportedTest.class);
  }
  
  public GeometryOpGCUnsupportedTest(String name) {
    super(name);
  }
  
  static String WKT_GC = "GEOMETRYCOLLECTION (POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200)), LINESTRING (150 250, 250 250))";
  static String WKT_POLY = "POLYGON ((50 50, 50 150, 150 150, 150 50, 50 50))"; 
      
  public void testBoundary() {
    final Geometry a = read(WKT_GC);
    final Geometry b = read(WKT_POLY);
   
    (new FailureChecker() { void operation() {
      a.getBoundary();
    }  }).check(IllegalArgumentException.class);

  }

  public void testRelate() {
    final Geometry a = read(WKT_GC);
    final Geometry b = read(WKT_POLY);
   
    (new FailureChecker() { void operation() {
      a.relate(b);
    }  }).check(IllegalArgumentException.class);
    
    (new FailureChecker() { void operation() {
      b.relate(a);
    }  }).check(IllegalArgumentException.class);

  }

  public void testUnion() {
    final Geometry a = read(WKT_GC);
    final Geometry b = read(WKT_POLY);
   
    (new FailureChecker() { void operation() {
        a.union(b);
     }  }).check(IllegalArgumentException.class);
    
    (new FailureChecker() { void operation() {
      b.union(a);
   }  }).check(IllegalArgumentException.class);
  }
  
  public void testDifference() {
    final Geometry a = read(WKT_GC);
    final Geometry b = read(WKT_POLY);
   
    (new FailureChecker() { void operation() {
        a.difference(b);
     }  }).check(IllegalArgumentException.class);
    
    (new FailureChecker() { void operation() {
      b.difference(a);
   }  }).check(IllegalArgumentException.class);
  }
  
  public void testSymDifference() {
    final Geometry a = read(WKT_GC);
    final Geometry b = read(WKT_POLY);
   
    (new FailureChecker() { void operation() {
        a.symDifference(b);
     }  }).check(IllegalArgumentException.class);
    
    (new FailureChecker() { void operation() {
      b.symDifference(a);
   }  }).check(IllegalArgumentException.class);
  }
  
  
  
  
  static abstract class FailureChecker {
    
    /**
     * An operation which should throw an exception of the specified class
     */
    abstract void operation();
   
    void check(Class exClz) {
      assertTrue( isError( exClz ) );
    }
    
    boolean isError(Class exClz) {
      try {
        operation();
        return false;
      }
      catch (Throwable t) {
        if (t.getClass() == exClz) return true;
      }
      return false;
    }
  }
}
