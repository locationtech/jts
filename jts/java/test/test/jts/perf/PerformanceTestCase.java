package test.jts.perf;

/**
 * A base class for classes implementing performance tests
 * to be run by the {@link PerformanceTestRunner}.
 * <p>
 * All public methods in a class which start with "run" are 
 * executed as performance tests.
 * <p>
 * Multiple test runs with different run sizes may be made.
 * Within each run, each run method is executed 
 * the specified number of iterations.
 * The time to run the method is printed for each one.
 * 
 * @author Martin Davis
 *
 */
public abstract class PerformanceTestCase
{
  private String name;
  private int[] runSize = new int[] { 1 };
  private int runIter = 1;
  
  public PerformanceTestCase(String name)
  {
    this.name = name;
  }
  
  public String getName()
  {
    return name;
  }
  
  protected void setRunSize(int[] runSize)
  {
    this.runSize = runSize;
  }
  
  public int[] getRunSize()
  {
    return runSize;
  }
  
  protected void setRunIterations(int runIter)
  {
    this.runIter = runIter;
  }
  
  public int getRunIterations()
  {
    return runIter;
  }
  
  /**
   * Sets up any fixtures needed for the test runs.
   * 
   * @throws Exception
   */
  public void setUp()
  throws Exception
  {
    
  }
  
  /**
   * Starts a test run with the given size.
   * 
   * @param size
   * @throws Exception
   */
  public void startRun(int size)
  throws Exception
  {
    
  }
  
  /**
   * Ends a test run.
   * 
   * @throws Exception
   */
  public void endRun()
  throws Exception
  {
    
  }
  
  /**
   * Tear down any fixtures made for the testing.
   * 
   * @throws Exception
   */
  public void tearDown()
  throws Exception
  {
    
  }
  
  
}
