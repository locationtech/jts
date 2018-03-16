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

import org.locationtech.jts.geom.*;
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
  private long durationCoord;
  private double[] durationsExtend, durationsMerge, durationsCoord;

  private CoordinateSequenceFactory currentFactory;
  private int currentSize;
  private int currentDimension;
  private int iteration = 0;

  public ExtendableCoordinateSequencePerfTest(String name) {
    super(name);
    setRunSize(new int[] {10, 15, 16, 25, 48, 50, 96, 192, 200, 500, 1000, 1500, 1537});
    setRunIterations(500);
    //setRunSize(new int[] {500});
    //setRunIterations(500);
  }


  public static void main(String[] args) /*throws InterruptedException */{
    //Thread.sleep(15000);
    PerformanceTestRunner.run(ExtendableCoordinateSequencePerfTest.class);
    //Thread.sleep(60000);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    System.out.println(
            "================================================================================================================");
    System.out.println("\nWarming up\n");

    this.durationsExtend = new double[getRunIterations()];
    this.durationsMerge = new double[getRunIterations()];
    this.durationsCoord = new double[getRunIterations()];

    //warm up
    this.currentSize = 1;
    for (int i = 0; i < 3; i++) {
      this.currentSize *= 10;
      run01_CoordinateArraySequenceDim2();
      run02_PackedCoordinateSequenceFloatDim2();
      run03_PackedCoordinateSequenceDoubleDim2();
      run04_CoordinateArraySequenceDim3();
      run05_PackedCoordinateSequenceFloatDim3();
      run06_PackedCoordinateSequenceDoubleDim3();
      run07_PackedCoordinateSequenceFloatDim4();
      run08_PackedCoordinateSequenceDoubleDim4();
    }
    this.durationCoord = this.durationMerge = this.durationExtend = 0;
    this.durationsExtend = new double[getRunIterations()];
    this.durationsMerge = new double[getRunIterations()];
    this.durationsCoord = new double[getRunIterations()];
  }

  @Override
  public void startRun(int size) throws Exception {
    super.startRun(size);
    currentSize = size;
    System.out.println(
            "================================================================================================================");
    System.out.println("Testing with size of " + size + "(" + getRunIterations() + " iterations)");
    int numIterations = getRunIterations();
    durationsExtend = new double[numIterations];
    durationsMerge = new double[numIterations];
    durationsCoord = new double[numIterations];
    iteration = 0;
  }

  @Override
  public void endRun() throws Exception {
    super.endRun();
    System.out.println("\n");
  }

  @Override
  protected void setTime(int runNum, long time) {
    super.setTime(runNum, time);

    int numIterations = getRunIterations();
    long minTime = this.durationMerge < this.durationExtend
            ? this.durationMerge
            : this.durationExtend;
    if (this.durationCoord > 0 && this.durationCoord < minTime)
      minTime = this.durationCoord;
    minTime = minTime / numIterations;

    long mean = durationExtend / numIterations;
    System.out.println(reportTime("Extend", this.currentFactory, this.currentSize,
            this.currentDimension, mean, (long)deviation((double)mean, durationsExtend), minTime));
    mean = durationMerge / numIterations;
    System.out.println(reportTime("Merge ", this.currentFactory, this.currentSize,
            this.currentDimension, mean, (long)deviation((double)mean, durationsMerge), minTime));

    mean = durationCoord / numIterations;
    System.out.println(reportTime("Coords", this.currentFactory, this.currentSize,
            this.currentDimension, mean, (long)deviation((double)mean, durationsCoord), minTime));
    System.out.println(
            "----------------------------------------------------------------------------------------------------------------");

    // init counter
    durationExtend = 0;
    durationMerge = 0;
    durationCoord = 0;
    for (int i = 0; i < numIterations; i++) {
      durationsExtend[i] = 0d;
      durationsMerge[i] = 0d;
      durationsCoord[i] = 0d;
    }
    iteration = 0;
  }

  private static double deviation(double mean, double[] values) {
    return Math.sqrt(variance(mean, values));
  }

  private static double variance(double mean, double[] values) {

    double s2 = 0;
    for (int i = 0; i < values.length; i++)
    {
      double val = values[i]-mean;
      s2 += (val * val);
    };
    return (s2 / values.length);
  }



  private String reportTime(String approach, CoordinateSequenceFactory csf, int size,
                            int dimension, long time, long devTime, long minTime) {

    return String.format("%s with %s (size=%d, dim=%d): mean=%s, stddev=%s (%.1f%%).",
            approach, csf.getClass().getSimpleName(), size, dimension,
            PerformanceTestRunner.getTimeString(time),
            PerformanceTestRunner.getTimeString(devTime),
            100d * ((double)time/(double)minTime - 1d));
  }

  public void run01_CoordinateArraySequenceDim2() {
    this.currentFactory = CoordinateArraySequenceFactory.instance();
    this.currentDimension = 2;
    performExtendableVsMerge(currentFactory, currentDimension);
  }

  public void run04_CoordinateArraySequenceDim3() {
    this.currentFactory = CoordinateArraySequenceFactory.instance();
    this.currentDimension = 3;
    performExtendableVsMerge(currentFactory, currentDimension);
  }

  public void run02_PackedCoordinateSequenceFloatDim2() {
    this.currentFactory = PackedCoordinateSequenceFactory.FLOAT_FACTORY;
    this.currentDimension = 2;
    ((PackedCoordinateSequenceFactory)this.currentFactory).setDimension(this.currentDimension);
    performExtendableVsMerge(currentFactory, currentDimension);
  }
  public void run05_PackedCoordinateSequenceFloatDim3() {
    this.currentFactory = PackedCoordinateSequenceFactory.FLOAT_FACTORY;
    this.currentDimension = 3;
    ((PackedCoordinateSequenceFactory)this.currentFactory).setDimension(this.currentDimension);
    performExtendableVsMerge(currentFactory, currentDimension);
  }
  public void run07_PackedCoordinateSequenceFloatDim4() {
    this.currentFactory = PackedCoordinateSequenceFactory.FLOAT_FACTORY;
    this.currentDimension = 4;
    ((PackedCoordinateSequenceFactory)this.currentFactory).setDimension(this.currentDimension);
    performExtendableVsMerge(currentFactory, currentDimension);
  }

  public void run03_PackedCoordinateSequenceDoubleDim2() {
    this.currentFactory = PackedCoordinateSequenceFactory.DOUBLE_FACTORY;
    this.currentDimension = 2;
    ((PackedCoordinateSequenceFactory)this.currentFactory).setDimension(this.currentDimension);
    performExtendableVsMerge(currentFactory, currentDimension);
  }
  public void run06_PackedCoordinateSequenceDoubleDim3() {
    this.currentFactory = PackedCoordinateSequenceFactory.DOUBLE_FACTORY;
    this.currentDimension = 3;
    ((PackedCoordinateSequenceFactory)this.currentFactory).setDimension(this.currentDimension);
    performExtendableVsMerge(currentFactory, currentDimension);
  }
  public void run08_PackedCoordinateSequenceDoubleDim4() {
    this.currentFactory = PackedCoordinateSequenceFactory.DOUBLE_FACTORY;
    this.currentDimension = 4;
    ((PackedCoordinateSequenceFactory)this.currentFactory).setDimension(this.currentDimension);
    performExtendableVsMerge(currentFactory, currentDimension);
  }

  private void performExtendableVsMerge(CoordinateSequenceFactory csf, int dimension) {

    long start, duration;

    start = System.nanoTime();
    CoordinateSequence seq3 = createUsingArrayListAndFactoryCreate(csf, dimension, this.currentSize, RANDOM_SEED);
    duration = System.nanoTime() - start;
    durationCoord += duration;
    durationsCoord[iteration] = (double)duration;

    start = System.nanoTime();
    CoordinateSequence seq2 = createUsingMerge(csf, dimension, this.currentSize, RANDOM_SEED);
    duration = System.nanoTime() - start;
    durationMerge += duration;
    durationsMerge[iteration] = (double)duration;

    start = System.nanoTime();
    CoordinateSequence seq1 = createUsingExtendable(csf, dimension, this.currentSize, RANDOM_SEED);
    duration = System.nanoTime() - start;
    durationExtend += duration;
    durationsExtend[iteration] = (double)duration;

    iteration+=1;

    /*
    Assert.isTrue(CoordinateSequences.isEqual(seq1, seq2));
    CoordinateSequences.isEqual(seq1, seq3);
     */
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

  private static CoordinateSequence createUsingArrayListAndFactoryCreate(CoordinateSequenceFactory factory, int dimension, int size, int seed) {

    final ArrayList sequences = new ArrayList();
    final Random rnd = new Random(seed);
    double msum = 0;
    for (int i = 0; i < size; i++) {
      Coordinate pt = new Coordinate(rnd.nextDouble() * 640, rnd.nextDouble() * 480);
      if (dimension > 2)
        pt.z = rnd.nextDouble() * 10;
      if (dimension > 3)
        msum += rnd.nextDouble() * 10;
      sequences.add(pt);
    }

    Coordinate[] points = new Coordinate[size];
    System.arraycopy(sequences.toArray(), 0, points, 0, size);
    return factory.create(points);
  }

}
