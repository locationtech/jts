package test.jts.perf;

/**
 * An example of the usage of the {@PerformanceTestRunner}.
 * 
 * @author Martin Davis
 *
 */
public class ExamplePerfTest
extends PerformanceTestCase
{

  public static void main(String args[]) {
    PerformanceTestRunner.run(ExamplePerfTest.class);
  }

  public ExamplePerfTest(String name)
  {
    super(name);
    setRunSize(new int[] { 5, 10, 20 });
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
  
  public void runTest1()
  {
    System.out.println("Test 1 : Iter # " + iter++);
    // do test work here
  }
  
  public void runTest2()
  {
    System.out.println("Test 2 : Iter # " + iter++);
    // do test work here
  }
  
  public void tearDown()
  {
    // deallocate resources here
  }
}
