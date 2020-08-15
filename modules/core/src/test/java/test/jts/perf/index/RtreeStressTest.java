package test.jts.perf.index;

import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.hprtree.HPRtree;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.util.Stopwatch;

public class RtreeStressTest {
  
  private static final int NUM_ITEMS = 1000;
  private static final int NUM_QUERY = 100000;
  
  private static final double BASE_MIN = -1000;
  private static final double BASE_MAX = 1000;
  private static final double SIZE_MAX = 100;

  public static void main(String[] args) throws Exception
  {
    RtreeStressTest test = new RtreeStressTest();
    test.run();
  }
  HPRtree hpRtree;
  STRtree stRtree;
  
  private void run() {
    hpRtree = new HPRtree();
    stRtree = new STRtree();
    
    //loadRandom(NUM_ITEMS);
    loadGrid(NUM_ITEMS);
    
    Stopwatch sw = new Stopwatch();
    //hpRtree.build();
    stRtree.build();
    System.out.println("Build time: " + sw.getTimeString());
    
    Stopwatch sw2 = new Stopwatch();
    for (int i = 0; i < NUM_QUERY; i++) {
      queryRandom();
    }
    System.out.println("Query time: " + sw2.getTimeString());
  }

  private void queryRandom() {
    Envelope env = randomEnvelope(BASE_MIN, BASE_MAX, 10 * SIZE_MAX);
    
    CountItemVisitor hpVisitor = new CountItemVisitor();
    hpRtree.query(env, hpVisitor);
    
    //List hpResult = hpRtree.query(env);
    List hprResult = null;
    
    //CountItemVisitor stVisitor = new CountItemVisitor();
    //stRtree.query(env, stVisitor);

    //List strResult = stRtree.query(env);
    List strResult = null;
    
    checkResults(hprResult, strResult);
  }

  private void checkResults(List hprResult, List strResult) {
    if (hprResult == null) return;
    if (strResult == null) return;
    
    System.out.println("Result size: HPR = " + hprResult.size() 
    + " - STR = " + strResult.size());

    if (hprResult.size() != strResult.size()) {
      System.out.println("Result sizes are not equal: HPR = " + hprResult.size() 
      + " - STR = " + strResult.size());
    }

  }
  
  private void loadRandom(int numItems) {
    for (int i = 0; i < numItems; i++) {
      Envelope env = randomEnvelope(BASE_MIN, BASE_MAX, SIZE_MAX);
      insert(env, i + "");
    }
  }

  private void loadGrid(int numItems) {
    int numSide = (int) Math.sqrt(numItems);
    double gridSize = (BASE_MAX - BASE_MIN) / numSide;
    for (int i = 0; i < numSide; i++) {
      for (int j = 0; j < numSide; j++) {
        Envelope env = new Envelope(
            BASE_MIN, BASE_MIN + i * gridSize, 
            BASE_MIN, BASE_MIN + j * gridSize);
        insert(env, i + "-" + j);
      }
    }
  }

  private Envelope randomEnvelope(double baseMin, double baseMax, double size) {
    double x = random(baseMin, baseMax);
    double y = random(baseMin, baseMax);
    double sizeX = random(size);
    double sizeY = random(size);
    Envelope env = new Envelope(x, x + sizeX, y, y + sizeY);
    return env;
  }

  private void insert(Envelope env, String id) {
    hpRtree.insert(env, id);
    stRtree.insert(env, id);
  }

  private double random(double x) {
    return x * Math.random();
  }

  private static double random(double x1, double x2) {
    double del = x2 - x1;
    return x1 + del * Math.random();
  }
  
  
}
