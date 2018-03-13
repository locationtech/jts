/*
 * Copyright (c) 2018 Felix Obermaier.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.geom;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.ExtendableCoordinateSequence;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.util.Assert;
import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

import java.util.ArrayList;
import java.util.Random;

public class ExtendableCoordinateSequencePerfTest extends PerformanceTestCase {

  private static final int RANDOM_SEED = 13;
  private long durationExtend;
  private long durationMerge;

  private CoordinateSequenceFactory currentFactory;
  private int currentSize;
  private int currentDimension;

  public ExtendableCoordinateSequencePerfTest(String name) {
    super(name);
    setRunSize(new int[] {10, 10, 15, 16, 25, 48, 50, 96, 192, 200, 500, 1000, 1500, 1537});
    setRunIterations(100);
  }


  public static void main(String[] args) {
    PerformanceTestRunner.run(ExtendableCoordinateSequencePerfTest.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public void startRun(int size) throws Exception {
    super.startRun(size);
    currentSize = size;
    durationExtend = 0;
    durationMerge = 0;
    System.out.println(
            "======================================================================================================");
    System.out.println("Testing with size of " + size);
  }

  @Override
  public void endRun() throws Exception {
    super.endRun();
    System.out.println("\n");
  }

  @Override
  protected void setTime(int runNum, long time) {
    super.setTime(runNum, time);

    System.out.println(reportTime("Extend", this.currentFactory, this.currentSize,
            this.currentDimension, durationExtend / getRunIterations()));
    System.out.println(reportTime("Merge ", this.currentFactory, this.currentSize,
            this.currentDimension, durationMerge / getRunIterations()));
    System.out.println(
            "------------------------------------------------------------------------------------------------------");

  }

  private String reportTime(String approach, CoordinateSequenceFactory csf, int size, int dimension, long ms) {
    return String.format("%s with %s (size=%d, dim=%d) took average %d (nanotime()).",
            approach, csf.getClass().getSimpleName(), size, dimension, ms);
  }

  public void runCoordinateArraySequenceDim2() {
    this.currentFactory = CoordinateArraySequenceFactory.instance();
    this.currentDimension = 2;
    performExtendableVsMerge(currentFactory, currentDimension);
  }

  public void runCoordinateArraySequenceDim3() {
    this.currentFactory = CoordinateArraySequenceFactory.instance();
    this.currentDimension = 3;
    performExtendableVsMerge(currentFactory, currentDimension);
  }

  public void runPackedCoordinateSequenceDim2() {
    this.currentFactory = PackedCoordinateSequenceFactory.DOUBLE_FACTORY;
    this.currentDimension = 2;
    performExtendableVsMerge(currentFactory, currentDimension);
  }

  public void runPackedCoordinateSequenceDim4() {
    this.currentFactory = PackedCoordinateSequenceFactory.DOUBLE_FACTORY;
    this.currentDimension = 4;
    performExtendableVsMerge(currentFactory, currentDimension);
  }

  private void performExtendableVsMerge(CoordinateSequenceFactory csf, int dimension) {

    long start, duration;

    start = System.nanoTime();
    CoordinateSequence seq1 = createUsingExtendable(csf, dimension, this.currentSize, RANDOM_SEED);
    duration = System.nanoTime() - start;
    durationExtend += duration;

    start = System.nanoTime();
    CoordinateSequence seq2 = createUsingMerge(csf, dimension, this.currentSize, RANDOM_SEED);
    duration = System.nanoTime() - start;
    durationMerge += duration;

    Assert.isTrue(CoordinateSequences.isEqual(seq1, seq2));
  }


  private static CoordinateSequence mergeSequences(CoordinateSequenceFactory factory, ArrayList sequences) {

    int dimension = ((CoordinateSequence)sequences.get(0)).getDimension();
    CoordinateSequence res = factory.create(sequences.size(), dimension);

    for (int i = 0; i < sequences.size(); i++) {
      CoordinateSequence coord = (CoordinateSequence) sequences.get(i);
      for (int j = 0; j < res.getDimension(); j++) {
        res.setOrdinate(i, j, coord.getOrdinate(0, j));
      }
    }
    return res;
  }

  private static CoordinateSequence createUsingExtendable(CoordinateSequenceFactory factory, int dimension, int size, int seed) {

    final ExtendableCoordinateSequence eseq = new ExtendableCoordinateSequence(factory, dimension);
    final Random rnd = new Random(seed);

    for (int i = 0; i < size; i++) {
      eseq.setOrdinate(i, CoordinateSequence.X, rnd.nextDouble() * 640);
      eseq.setOrdinate(i, CoordinateSequence.Y, rnd.nextDouble() * 480);
      for (int j = 2; j < dimension; j++)
        eseq.setOrdinate(i, j, rnd.nextDouble() * 10);
    }

    return eseq;//.truncated();
  }

  private static CoordinateSequence createUsingMerge(CoordinateSequenceFactory factory, int dimension, int size, int seed) {

    final ArrayList sequences = new ArrayList();
    final Random rnd = new Random(seed);

    for (int i = 0; i < size; i++) {
      CoordinateSequence seq = factory.create(1, dimension);
      seq.setOrdinate(0, CoordinateSequence.X, rnd.nextDouble() * 640);
      seq.setOrdinate(0, CoordinateSequence.Y, rnd.nextDouble() * 480);
      for (int j = 2; j < dimension; j++)
        seq.setOrdinate(0, j, rnd.nextDouble() * 10);
      sequences.add(seq);
    }

    return mergeSequences(factory, sequences);
  }
}
