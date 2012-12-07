package test.jts.junit.simplify;

import junit.framework.Assert;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.geom.*;

/**
 * Runs various validation tests on a the results of a geometry operation
 */
public class GeometryOperationValidator
{
  private static WKTReader rdr = new WKTReader();
  private Geometry[] ioGeometry;
  private boolean expectedSameStructure = false;
  private String wktExpected = null;

  public GeometryOperationValidator(Geometry[] ioGeometry)
  {
    this.ioGeometry = ioGeometry;
  }

  public GeometryOperationValidator setExpectedResult(String wktExpected)
  {
    this.wktExpected = wktExpected;
    return this;
  }

  public GeometryOperationValidator setExpectedSameStructure()
  {
    this.expectedSameStructure = true;
    return this;
  }

  public boolean isAllTestsPassed()
  {
    try {
      test();
    }
    catch (Throwable e) {
      return false;
    }
    return true;
  }
  /**
   * Tests if the result is valid.
   * Throws an exception if result is not valid.
   * This allows chaining multiple tests together.
   * 
   * @throws Exception if the result is not valid.
   */
  public void test()
      throws Exception
  {
    testSameStructure();
    testValid();
    testExpectedResult();
  }

  public GeometryOperationValidator testSameStructure() throws Exception {
		if (!expectedSameStructure)
			return this;
		Assert.assertTrue("simplified geometry has different structure than input",
				SameStructureTester.isSameStructure(ioGeometry[0], ioGeometry[1]));
		return this;
	}

	public GeometryOperationValidator testValid() throws Exception {
		Assert.assertTrue("simplified geometry is not valid", ioGeometry[1]
				.isValid());
		return this;
	}

	public GeometryOperationValidator testEmpty(boolean isEmpty) throws Exception {
		String failureCondition = isEmpty ? "not empty" : "empty";
		Assert.assertTrue("simplified geometry is " + failureCondition,
				ioGeometry[1].isEmpty() == isEmpty);
		return this;
	}

  private void testExpectedResult()
      throws Exception
  {
    if (wktExpected == null) return;
    Geometry expectedGeom = rdr.read(wktExpected);
    Assert.assertTrue("Expected result not found",
                      expectedGeom.equalsExact(ioGeometry[1]));

  }
}