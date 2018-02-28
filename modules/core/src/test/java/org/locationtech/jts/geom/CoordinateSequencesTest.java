
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
package org.locationtech.jts.geom;

import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import java.util.Random;


/**
 * @version 1.7
 */
public class CoordinateSequencesTest extends TestCase {

  private PrecisionModel precisionModel = new PrecisionModel();
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);
  
  private static final double TOLERANCE = 1E-5;
  
  public static void main(String args[]) {
    TestRunner.run(CoordinateSequencesTest.class);
  }

  public CoordinateSequencesTest(String name) { super(name); }

  public void testCopyToLargerDim() throws Exception
  {
    PackedCoordinateSequenceFactory csFactory = new PackedCoordinateSequenceFactory();
    CoordinateSequence cs2D = createTestSequence(csFactory, 10,  2);
    CoordinateSequence cs3D = csFactory.create(10,  3);
    CoordinateSequences.copy(cs2D,  0, cs3D, 0, cs3D.size());
    assertTrue(CoordinateSequences.isEqual(cs2D, cs3D));
  }

  public void testCopyToSmallerDim() throws Exception
  {
    PackedCoordinateSequenceFactory csFactory = new PackedCoordinateSequenceFactory();
    CoordinateSequence cs3D = createTestSequence(csFactory, 10,  3);
    CoordinateSequence cs2D = csFactory.create(10,  2);
    CoordinateSequences.copy(cs3D,  0, cs2D, 0, cs2D.size());
    assertTrue(CoordinateSequences.isEqual(cs2D, cs3D));
 }
  

  public void testScrollRing() {
    System.out.println("Testing scrolling of closed ring");
    doTestScrollRing(CoordinateArraySequenceFactory.instance(), 2);
    doTestScrollRing(CoordinateArraySequenceFactory.instance(), 3);
    doTestScrollRing(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 2);
    doTestScrollRing(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 4);
    doTestScrollRing(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 2);
    doTestScrollRing(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 4);
  }

  public void testScroll() {
    System.out.println("Testing scrolling of circular string");
    doTestScroll(CoordinateArraySequenceFactory.instance(), 2);
    doTestScroll(CoordinateArraySequenceFactory.instance(), 3);
    doTestScroll(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 2);
    doTestScroll(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 4);
    doTestScroll(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 2);
    doTestScroll(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 4);
  }

  public void testIndexOf() {
    System.out.println("Testing indexOf");
    doTestIndexOf(CoordinateArraySequenceFactory.instance(), 2);
    doTestIndexOf(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 5);
    doTestIndexOf(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 7);
  }

  public void testMinCoordinateIndex() {
    System.out.println("Testing minCoordinateIndex");
    doTestMinCoordinateIndex(CoordinateArraySequenceFactory.instance(), 2);
    doTestMinCoordinateIndex(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 5);
    doTestMinCoordinateIndex(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 7);
  }

  public void testIsRing() {
    System.out.println("Testing isRing");
    doTestIsRing(CoordinateArraySequenceFactory.instance(), 2);
    doTestIsRing(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 5);
    doTestIsRing(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 7);
  }

  public void testCopy() {
    System.out.println("Testing copy");
    doTestCopy(CoordinateArraySequenceFactory.instance(), 2);
    doTestCopy(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 5);
    doTestCopy(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 7);
  }

  public void testReverse() {
    System.out.println("Testing reverse");
    doTestReverse(CoordinateArraySequenceFactory.instance(), 2);
    doTestReverse(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 5);
    doTestReverse(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 7);
  }

  private static CoordinateSequence createTestSequence(CoordinateSequenceFactory csFactory, int size, int dim)
  {
    CoordinateSequence cs = csFactory.create(size,  dim);
    // initialize with a data signature where coords look like [1, 10, 100, ...]
    for (int i = 0; i < size; i++) {
      for (int d = 0; d < dim; d++) {
        cs.setOrdinate(i, d, i * Math.pow(10, d));
      }
    }
    return cs;
  }

  private static CoordinateSequence createRandomTestSequence(CoordinateSequenceFactory csFactory, int size, int dim,
                                                   Random rnd, Envelope range)
  {
    CoordinateSequence cs = csFactory.create(size,  dim);
    // initialize with a data signature where coords look like [1, 10, 100, ...]
    for (int i = 0; i < size; i++) {
      {
        cs.setOrdinate(i, 0, range.getWidth() * rnd.nextDouble() + range.getMinX());
        cs.setOrdinate(i, 1, range.getHeight() * rnd.nextDouble() + range.getMinY());
        for (int d = 2; d < dim; d++)
          cs.setOrdinate(i, d, i * Math.pow(10, d));
      }
    }
    return cs;
  }

  private static void doTestReverse(CoordinateSequenceFactory factory, int dimension) {

    final Random rnd = new Random();

    // arrange
    CoordinateSequence sequence = createRandomTestSequence(factory, 20, dimension, rnd,
            new Envelope(10, 100, 10, 100));
    CoordinateSequence reversed = sequence.copy();

    // act
    CoordinateSequences.reverse(reversed);

    // assert
    for (int i = 0; i < sequence.size(); i++)
      checkCoordinateAt(sequence, i, reversed, sequence.size() - i - 1, dimension);
  }

  private static void doTestCopy(CoordinateSequenceFactory factory, int dimension) {

    final Random rnd = new Random();

    // arrange
    CoordinateSequence sequence = createRandomTestSequence(factory, 20, dimension, rnd,
            new Envelope(10, 100, 10, 100));
    CoordinateSequence fullCopy = factory.create(20, dimension);
    CoordinateSequence partialCopy = factory.create(15, dimension);

    // act
    CoordinateSequences.copy(sequence, 0, fullCopy, 0, sequence.size());
    CoordinateSequences.copy(sequence, 2, partialCopy, 0, partialCopy.size());

    // assert
    for (int i = 0; i < fullCopy.size(); i++)
      checkCoordinateAt(sequence, i, fullCopy, i, dimension);
    for (int i = 0; i < partialCopy.size(); i++)
      checkCoordinateAt(sequence, 2 + i, partialCopy, i, dimension);

    // ToDo test if dimensions don't match
  }

  private static void doTestIsRing(CoordinateSequenceFactory factory, int dimension) {

    // arrange
    CoordinateSequence ring = createCircle(factory, dimension, new Coordinate(), 5);
    CoordinateSequence noRing = createCircularString(factory, dimension, new Coordinate(), 5,
            0.1, 22);
    CoordinateSequence incomplete0 = createAlmostRing(factory, dimension, 1);
    CoordinateSequence incomplete1 = createAlmostRing(factory, dimension, 1);
    CoordinateSequence incomplete2 = createAlmostRing(factory, dimension, 2);;
    CoordinateSequence incomplete3 = createAlmostRing(factory, dimension, 3);;
    CoordinateSequence incomplete4a = createAlmostRing(factory, dimension, 4);;
    CoordinateSequence incomplete4b = CoordinateSequences.ensureValidRing(factory, incomplete4a);


    //incomplete1

    // act
    boolean isRingRing = CoordinateSequences.isRing(ring);
    boolean isRingNoRing = CoordinateSequences.isRing(noRing);
    boolean isRingIncomplete0 = CoordinateSequences.isRing(incomplete0);
    boolean isRingIncomplete1 = CoordinateSequences.isRing(incomplete1);
    boolean isRingIncomplete2 = CoordinateSequences.isRing(incomplete2);
    boolean isRingIncomplete3 = CoordinateSequences.isRing(incomplete3);
    boolean isRingIncomplete4a = CoordinateSequences.isRing(incomplete4a);
    boolean isRingIncomplete4b = CoordinateSequences.isRing(incomplete4b);

    // assert
    assertTrue(isRingRing);
    assertTrue(!isRingNoRing);
    assertTrue(!isRingIncomplete0);
    assertTrue(!isRingIncomplete1);
    assertTrue(!isRingIncomplete2);
    assertTrue(!isRingIncomplete3);
    assertTrue(!isRingIncomplete4a);
    assertTrue(isRingIncomplete4b);
  }

  private static void doTestIndexOf(CoordinateSequenceFactory factory, int dimension) {

    final Random rnd = new Random();

    // arrange
    //System.out.println("Testing '" + factory.getClass().getSimpleName() + "' with dim=" +dimension );
    CoordinateSequence sequence = createRandomTestSequence(factory, 100, dimension, rnd,
            new Envelope(20, 100, 20, 100));

    Coordinate[] coordinates = sequence.toCoordinateArray();
    for (int i = 0; i < sequence.size(); i++)
      assertEquals(i, CoordinateSequences.indexOf(coordinates[i], sequence));

  }

  private static void doTestMinCoordinateIndex(CoordinateSequenceFactory factory, int dimension) {

    final Random rnd = new Random();

    // arrange
    //System.out.println("Testing '" + factory.getClass().getSimpleName() + "' with dim=" +dimension );
    CoordinateSequence sequence = createRandomTestSequence(factory, 100, dimension, rnd,
            new Envelope(20, 100, 20, 100));

    int minIndex = sequence.size() / 2;
    sequence.setOrdinate(minIndex, 0, 10);
    sequence.setOrdinate(minIndex, 1, 10);

    assertEquals(minIndex, CoordinateSequences.minCoordinateIndex(sequence));
    assertEquals(minIndex, CoordinateSequences.minCoordinateIndex(sequence, 25, 75));

  }

  private static void doTestScroll(CoordinateSequenceFactory factory, int dimension) {

    // arrange
    //System.out.println("Testing '" + factory.getClass().getSimpleName() + "' with dim=" +dimension );
    CoordinateSequence sequence = createCircularString(factory, dimension, new Coordinate(20, 20), 7d,
            0.1, 22);
    CoordinateSequence scrolled = sequence.copy();

    // act
    CoordinateSequences.scroll(scrolled, 12);

    // assert
    int io = 12;
    for (int is = 0; is < scrolled.size() - 1; is++) {
      checkCoordinateAt(sequence, io, scrolled, is, dimension);
      io++;
      io%=scrolled.size();
    }
  }

  private static void doTestScrollRing(CoordinateSequenceFactory factory, int dimension) {

    // arrange
    //System.out.println("Testing '" + factory.getClass().getSimpleName() + "' with dim=" +dimension );
    CoordinateSequence sequence = createCircle(factory, dimension, new Coordinate(10, 10), 9d);
    CoordinateSequence scrolled = sequence.copy();

    // act
    CoordinateSequences.scroll(scrolled, 12);

    // assert
    int io = 12;
    for (int is = 0; is < scrolled.size() - 1; is++) {
      checkCoordinateAt(sequence, io, scrolled, is, dimension);
      io++;
      io%=scrolled.size()-1;
    }
    checkCoordinateAt(scrolled, 0, scrolled, scrolled.size()-1, dimension);
  }

  private static void checkCoordinateAt(CoordinateSequence seq1, int pos1,
                                        CoordinateSequence seq2, int pos2, int dim) {
    assertEquals("unexpected x-ordinate at pos " + pos2,
            seq1.getOrdinate(pos1, 0), seq2.getOrdinate(pos2, 0));
    assertEquals("unexpected y-ordinate at pos " + pos2,
            seq1.getOrdinate(pos1, 0), seq2.getOrdinate(pos2, 0));

    // check additional ordinates
    for (int j = 2; j < dim; j++) {
      assertEquals("unexpected "+ j + "-ordinate at pos " + pos2,
              seq1.getOrdinate(pos1, j), seq2.getOrdinate(pos2, j));
    }
  }

  private static CoordinateSequence createAlmostRing(CoordinateSequenceFactory factory, int dimension, int num) {

    if (num > 4) num = 4;

    CoordinateSequence sequence = factory.create(num, dimension);
    if (num == 0) return fillNonPlanarDimensions(sequence);

    sequence.setOrdinate(0, 0, 10);
    sequence.setOrdinate(0, 0, 10);
    if (num == 1) return fillNonPlanarDimensions(sequence);

    sequence.setOrdinate(0, 0, 20);
    sequence.setOrdinate(0, 0, 10);
    if (num == 2) return fillNonPlanarDimensions(sequence);

    sequence.setOrdinate(0, 0, 20);
    sequence.setOrdinate(0, 0, 20);
    if (num == 3) return fillNonPlanarDimensions(sequence);

    sequence.setOrdinate(0, 0, 10.0000000000001);
    sequence.setOrdinate(0, 0,  9.9999999999999);
    return fillNonPlanarDimensions(sequence);

  }

  private static CoordinateSequence fillNonPlanarDimensions(CoordinateSequence seq) {

    if (seq.getDimension() < 3)
      return seq;

    for (int i = 0; i < seq.size(); i++)
      for (int j = 2; j < seq.getDimension(); j++)
        seq.setOrdinate(i, j, i* Math.pow(10, j-1));

    return seq;
  }

  private static CoordinateSequence createCircle(CoordinateSequenceFactory factory, int dimension,
                                                 Coordinate center, double radius) {
    // Get a complete circular string
    CoordinateSequence res = createCircularString(factory, dimension, center, radius, 0d,49);

    // ensure it is closed
    for (int i = 0; i < dimension; i++)
      res.setOrdinate(48, i, res.getOrdinate(0, i));

    return res;
  }
  private static CoordinateSequence createCircularString(CoordinateSequenceFactory factory, int dimension,
                                                         Coordinate center, double radius, double startAngle,
                                                         int numPoints) {
    final int numSegmentsCircle = 48;
    final double angleCircle = 2 * Math.PI;
    final double angleStep = angleCircle / numSegmentsCircle;

    CoordinateSequence sequence = factory.create(numPoints, dimension);
    PrecisionModel pm = new PrecisionModel(100);
    double angle = startAngle;
    for (int i = 0; i < numPoints; i++)
    {
      double dx = Math.cos(angle) * radius;
      sequence.setOrdinate(i, 0, pm.makePrecise(center.x +dx));
      double dy = Math.sin(angle) * radius;
      sequence.setOrdinate(i, 1, pm.makePrecise(center.y +dy));

      // set other ordinate values to predictable values
      for (int j = 2; j < dimension; j++ )
        sequence.setOrdinate(i, j, Math.pow(10, j-1)*i);

      angle += angleStep;
      angle %= angleCircle;
    }

    return sequence;
  }
}
