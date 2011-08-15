package test.jts.perf;

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
  
  public void setUp()
  throws Exception
  {
    
  }
  
  public void startRun(int size)
  throws Exception
  {
    
  }
  
  public void endRun()
  throws Exception
  {
    
  }
  
  public void tearDown()
  throws Exception
  {
    
  }
  
  
}
