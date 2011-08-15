package test.jts.perf;

import junit.textui.TestRunner;
import test.jts.junit.algorithm.AngleTest;
import test.jts.perf.*;

public class ExamplePerfTest
extends PerformanceTestCase
{

  public static void main(String args[]) {
    PerformanceTestRunner.run(ExamplePerfTest.class);
  }

  public ExamplePerfTest(String name)
  {
    super(name);
    setRunSize(new int[] {10, 20});
    setRunIterations(10);
  }

  public void setUp()
  {
    // read data and allocate resources here
  }
  
  public void startRun(int size)
  {
    System.out.println("Running with size " + size);
    iter = 0;
  }
  
  private int iter = 0;
  
  public void runExample()
  {
    System.out.println("Iter # " + iter++);
    // do test work here
  }
  
  public void tearDown()
  {
    // deallocate resources here
  }
}
