package test.jts.junit.geom.impl;

import com.vividsolutions.jts.geom.*;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * General test cases for CoordinateSequences.
 * Subclasses can set the factory to test different kinds of CoordinateSequences.
 *
 * @version 1.7
 */
public class CoordinateSequenceTestBase
     extends TestCase
{
  public static final int SIZE = 100;

  protected CoordinateSequenceFactory csFactory;

  public static void main(String args[]) {
    TestRunner.run(CoordinateSequenceTestBase.class);
  }

  public CoordinateSequenceTestBase(String name) { super(name); }

  public void testZeroLength()
  {
    CoordinateSequence seq = csFactory.create(0, 3);
    assertTrue(seq.size() == 0);

    CoordinateSequence seq2 = csFactory.create((Coordinate[]) null);
    assertTrue(seq2.size() == 0);
  }

  public void testCreateBySizeAndModify()
  {
    Coordinate[] coords = createArray(SIZE);

    CoordinateSequence seq = csFactory.create(SIZE, 3);
    for (int i = 0; i < seq.size(); i++) {
      seq.setOrdinate(i, 0, coords[i].x);
      seq.setOrdinate(i, 1, coords[i].y);
      seq.setOrdinate(i, 2, coords[i].z);
    }

    assertTrue(isEqual(seq, coords));
  }

  public void test2DZOrdinate()
  {
    Coordinate[] coords = createArray(SIZE);

    CoordinateSequence seq = csFactory.create(SIZE, 2);
    for (int i = 0; i < seq.size(); i++) {
      seq.setOrdinate(i, 0, coords[i].x);
      seq.setOrdinate(i, 1, coords[i].y);
    }

    for (int i = 0; i < seq.size(); i++) {
      Coordinate p = seq.getCoordinate(i);
      assertTrue(Double.isNaN(p.z));
    }
  }

  public void testCreateByInit()
  {
    Coordinate[] coords = createArray(SIZE);
    CoordinateSequence seq = csFactory.create(coords);
    assertTrue(isEqual(seq, coords));
  }

  public void testCreateByInitAndCopy()
  {
    Coordinate[] coords = createArray(SIZE);
    CoordinateSequence seq = csFactory.create(coords);
    CoordinateSequence seq2 = csFactory.create(seq);
    assertTrue(isEqual(seq2, coords));
  }

  Coordinate[] createArray(int size)
  {
    Coordinate[] coords = new Coordinate[size];
    for (int i = 0; i < size; i++) {
      double base = 2 * 1;
      coords[i] = new Coordinate(base, base + 1, base + 2);
    }
    return coords;
  }

  boolean isAllCoordsEqual(CoordinateSequence seq, Coordinate coord)
  {
    for (int i = 0; i < seq.size(); i++) {
      if (! coord.equals(seq.getCoordinate(i)))  return false;

      if (coord.x != seq.getOrdinate(i, CoordinateSequence.X))  return false;
      if (coord.y != seq.getOrdinate(i, CoordinateSequence.Y))  return false;
      if (coord.z != seq.getOrdinate(i, CoordinateSequence.Z))  return false;
    }
    return true;
  }

  /**
   * Tests for equality using all supported accessors,
   * to provides test coverage for them.
   * 
   * @param seq
   * @param coords
   * @return
   */
  boolean isEqual(CoordinateSequence seq, Coordinate[] coords)
  {
    if (seq.size() != coords.length) return false;

    Coordinate p = new Coordinate();
    
    for (int i = 0; i < seq.size(); i++) {
      if (! coords[i].equals(seq.getCoordinate(i)))  return false;

      // Ordinate named getters
      if (coords[i].x != seq.getX(i))  return false;
      if (coords[i].y != seq.getY(i))  return false;

      // Ordinate indexed getters
      if (coords[i].x != seq.getOrdinate(i, CoordinateSequence.X))  return false;
      if (coords[i].y != seq.getOrdinate(i, CoordinateSequence.Y))  return false;
      if (coords[i].z != seq.getOrdinate(i, CoordinateSequence.Z))  return false;
      
      // Coordinate getter
      seq.getCoordinate(i, p);
      if (coords[i].x != p.x) return false;
      if (coords[i].y != p.y)  return false;
      if (coords[i].z != p.z)  return false;
      
    }
    return true;
  }
}

