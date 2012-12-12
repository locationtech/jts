package test.jts.perf;

/**
 * Runs a {@link ThreadTestCase}.
 * 
 * @author Martin Davis
 *
 */
public class ThreadTestRunner
{

  public static final int DEFAULT_THREAD_COUNT = 10;

  public static void run(ThreadTestCase testcase)
  {
    testcase.setup();
    
    for (int i = 0; i < testcase.getThreadCount(); i++) {
      Runnable runnable = testcase.getRunnable(i);
      Thread t = new Thread(runnable);
      t.start();
    }
  }
  
 
}
