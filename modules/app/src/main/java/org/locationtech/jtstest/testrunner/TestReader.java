

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
package org.locationtech.jtstest.testrunner;


import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;
import org.locationtech.jtstest.*;
import org.locationtech.jtstest.geomop.*;
import org.locationtech.jtstest.util.*;
import org.locationtech.jtstest.util.io.LineNumberElement;
import org.locationtech.jtstest.util.io.LineNumberSAXBuilder;
import org.locationtech.jtstest.util.io.WKTOrWKBReader;


/**
 * @version 1.7
 */
public class TestReader
{
	private static final String TAG_geometryOperation = "geometryOperation"; 
	private static final String TAG_resultMatcher = "resultMatcher"; 
	
    Vector parsingProblems = new Vector();
    private GeometryFactory geometryFactory;
    private WKTOrWKBReader wktorbReader;
    private double tolerance = 0.0;
    private GeometryOperation geomOp = null;
    private ResultMatcher resultMatcher = null;
    
    public TestReader() 
    {
    }

    public GeometryOperation getGeometryOperation()
    {
    	// use the main one if it was user-specified or this run does not have an op specified
    	if (TopologyTestApp.isGeometryOperationSpecified()
    			|| geomOp == null)
    		return TopologyTestApp.getGeometryOperation();
    	
    	return geomOp;
    }

    public boolean isBooleanFunction(String name) {
        return getGeometryOperation().getReturnType(name) == boolean.class;
    }

    public boolean isIntegerFunction(String name) {
        return getGeometryOperation().getReturnType(name) == int.class;
    }

    public boolean isDoubleFunction(String name) {
        return getGeometryOperation().getReturnType(name) == double.class;
    }

    public boolean isGeometryFunction(String name) 
    {
    	Class returnType = getGeometryOperation().getReturnType(name);
    	if (returnType == null)
    		return false;
    	return Geometry.class.isAssignableFrom(returnType);
	}

    public List getParsingProblems() {
        return Collections.unmodifiableList(parsingProblems);
    }

    public void clearParsingProblems() {
        parsingProblems.clear();
    }

    public TestRun createTestRun(File testFile, int runIndex) {
        try {
            SAXBuilder builder = new LineNumberSAXBuilder();
            Document document = builder.build(new FileInputStream(testFile));
            Element runElement = document.getRootElement();
            if (!runElement.getName().equalsIgnoreCase("run")) {
                throw new TestParseException(
                    "Expected <run> but encountered <" + runElement.getName() + ">");
            }
            return parseTestRun(runElement, testFile, runIndex);
        } catch (Exception e) {
            parsingProblems.add(
                "An exception occurred while parsing " + testFile + ": " + e.toString());
            return null;
        }
    }
    
    /**
     *  Creates a List of Test's from the given <test> Element's.
     */
    private List parseTests(
        List testElements,
        int caseIndex,
        File testFile,
        TestCase testCase,
        double tolerance)
        throws TestParseException {
        List tests = new ArrayList();
        int testIndex = 0;
        for (Iterator i = testElements.iterator(); i.hasNext();) {
            Element testElement = (Element) i.next();
            testIndex++;
            try {
                Element descElement = testElement.getChild("desc");
                if (testElement.getChildren("op").size() > 1) {
                    throw new TestParseException("Multiple <op>s in <test>");
                }
                Element opElement = testElement.getChild("op");
                if (opElement == null) {
                    throw new TestParseException("Missing <op> in <test>");
                }
                Attribute nameAttribute = opElement.getAttribute("name");
                if (nameAttribute == null) {
                    throw new TestParseException("Missing name attribute in <op>");
                }
                String arg1 =
                    opElement.getAttribute("arg1") == null
                        ? "A"
                        : opElement.getAttribute("arg1").getValue().trim();
                String arg2 =
                    opElement.getAttribute("arg2") == null
                        ? null
                        : opElement.getAttribute("arg2").getValue().trim();
                String arg3 =
                    opElement.getAttribute("arg3") == null
                        ? null
                        : opElement.getAttribute("arg3").getValue().trim();
                if (arg3 == null && nameAttribute.getValue().trim().equalsIgnoreCase("relate")) {
                    arg3 =
                        opElement.getAttribute("pattern") == null
                            ? null
                            : opElement.getAttribute("pattern").getValue().trim();
                }
                ArrayList arguments = new ArrayList();
                if (arg2 != null) {
                    arguments.add(arg2);
                }
                if (arg3 != null) {
                    arguments.add(arg3);
                }
                Result result = toResult(
                        opElement.getTextTrim(),
                        nameAttribute.getValue().trim(),
                        testCase.getTestRun());
                Test test = new Test(
                		testCase, 
                		testIndex,
                		descElement != null ? descElement.getTextTrim() : "", 
                		nameAttribute.getValue().trim(), 
                		arg1, 
                		arguments, 
                		result, 
                		tolerance);

                tests.add(test);
            } catch (Exception e) {
                parsingProblems.add(
                    "An exception occurred while parsing <test> "
                        + testIndex
                        + " in <case> "
                        + caseIndex
                        + " in "
                        + testFile
                        + ": "
                        + e.toString() + "\n" + StringUtil.getStackTrace(e));
            }
        }
        return tests;
    }

    private Result toResult(String value, String name, TestRun testRun)
        throws TestParseException, ParseException {
        if (isBooleanFunction(name)) {
            return toBooleanResult(value);
        }
        if (isIntegerFunction(name)) {
            return toIntegerResult(value);
        }
        if (isDoubleFunction(name)) {
            return toDoubleResult(value);
        }
        if (isGeometryFunction(name)) {
            return toGeometryResult(value, testRun);
        }
        throw new TestParseException(
            "Unknown operation name '" + name + "'");
//        return null;
    }

    private BooleanResult toBooleanResult(String value) throws TestParseException {
        if (value.equalsIgnoreCase("true")) {
            return new BooleanResult(true);
        } else if (value.equalsIgnoreCase("false")) {
            return new BooleanResult(false);
        } else {
            throw new TestParseException(
                "Expected 'true' or 'false' but encountered '" + value + "'");
        }
    }

    private DoubleResult toDoubleResult(String value) throws TestParseException {
        try {
            return new DoubleResult(Double.valueOf(value));
        } catch (NumberFormatException e) {
            throw new TestParseException("Expected double but encountered '" + value + "'");
        }
    }

    private IntegerResult toIntegerResult(String value) throws TestParseException {
        try {
            return new IntegerResult(Integer.valueOf(value));
        } catch (NumberFormatException e) {
            throw new TestParseException("Expected integer but encountered '" + value + "'");
        }
    }

    private GeometryResult toGeometryResult(String value, TestRun testRun) throws ParseException {
        GeometryFactory geometryFactory = new GeometryFactory(testRun.getPrecisionModel(), 0);
        WKTOrWKBReader wktorbReader = new WKTOrWKBReader(geometryFactory);
        return new GeometryResult(wktorbReader.read(value));
    }

    /**
     *  Creates a List of TestCase's from the given <case> Element's.
     */
    private List parseTestCases(
        List caseElements,
        File testFile,
        TestRun testRun,
        double tolerance)
        throws TestParseException {
        geometryFactory = new GeometryFactory(testRun.getPrecisionModel(), 0, TestCoordinateSequenceFactory.instance());
        wktorbReader = new WKTOrWKBReader(geometryFactory);
        Vector testCases = new Vector();
        int caseIndex = 0;
        for (Iterator i = caseElements.iterator(); i.hasNext();) {
            Element caseElement = (Element) i.next();
            //System.out.println("Line: " + ((LineNumberElement)caseElement).getStartLine());
            caseIndex++;
            try {
                Element descElement = caseElement.getChild("desc");
                Element aElement = caseElement.getChild("a");
                Element bElement = caseElement.getChild("b");
                File aWktFile = wktFile(aElement, testRun);
                File bWktFile = wktFile(bElement, testRun);
                Geometry a = readGeometry(aElement, absoluteWktFile(aWktFile, testRun));
                Geometry b = readGeometry(bElement, absoluteWktFile(bWktFile, testRun));
                TestCase testCase =
                    new TestCase(
                        descElement != null ? descElement.getTextTrim() : "",
                        a,
                        b,
                        aWktFile,
                        bWktFile,
                        testRun,
                        caseIndex,
                        ((LineNumberElement)caseElement).getStartLine());
                List testElements = caseElement.getChildren("test");
                //        if (testElements.size() == 0) {
                //          throw  new TestParseException("Missing <test> in <case>");
                //        }
                List tests = parseTests(testElements, caseIndex, testFile, testCase, tolerance);
                for (Iterator j = tests.iterator(); j.hasNext();) {
                    Test test = (Test) j.next();
                    testCase.add(test);
                }
                testCases.add(testCase);
            } catch (Exception e) {
                parsingProblems.add(
                    "An exception occurred while parsing <case> "
                        + caseIndex
                        + " in "
                        + testFile
                        + ": "
                        + e.toString());
            }
        }
        return testCases;
    }

    /**
     *  Creates a TestRun from the <run> Element.
     */
    private TestRun parseTestRun(Element runElement, File testFile, int runIndex)
        throws TestParseException 
    {
    	
      //----------- <workspace> (optional) ------------------
        File workspace = null;
        if (runElement.getChild("workspace") != null) {
            if (runElement.getChild("workspace").getAttribute("dir") == null) {
                throw new TestParseException("Missing <dir> in <workspace>");
            }
            workspace =
                new File(runElement.getChild("workspace").getAttribute("dir").getValue().trim());
            if (!workspace.exists()) {
                throw new TestParseException("<workspace> does not exist: " + workspace);
            }
            if (!workspace.isDirectory()) {
                throw new TestParseException("<workspace> is not a directory: " + workspace);
            }
        }
        
        //----------- <tolerance> (optional) ------------------
        tolerance = parseTolerance(runElement);
        
        Element descElement = runElement.getChild("desc");

        //----------- <geometryOperation> (optional) ------------------
        geomOp = parseGeometryOperation(runElement);
        
        //----------- <geometryMatcher> (optional) ------------------
        resultMatcher = parseResultMatcher(runElement);
        
        //-----------  <precisionModel> (optional) ----------------
        PrecisionModel precisionModel = parsePrecisionModel(runElement);
        
        //--------------- build TestRun  ---------------------
        TestRun testRun =
            new TestRun(
                descElement != null ? descElement.getTextTrim() : "",
                runIndex,
                precisionModel,
                geomOp,
                resultMatcher,
                testFile);
        testRun.setWorkspace(workspace);
        List caseElements = runElement.getChildren("case");
        if (caseElements.size() == 0) {
            throw new TestParseException("Missing <case> in <run>");
        }
        for (Iterator i = parseTestCases(caseElements, testFile, testRun, tolerance).iterator();
            i.hasNext();
            ) {
            TestCase testCase = (TestCase) i.next();
            testRun.addTestCase(testCase);
        }
        return testRun;
    }

    /**
     * Parses an optional <tt>precisionModel</tt> element.
     * The default is to use a FLOATING model.
     * 
     * @param runElement
     * @return a PrecisionModel instance (default if not specified)
     * @throws TestParseException
     */
    private PrecisionModel parsePrecisionModel(Element runElement)
    	throws TestParseException
    {
      PrecisionModel precisionModel = new PrecisionModel();
      Element precisionModelElement = runElement.getChild("precisionModel");
      if (precisionModelElement == null) {
        return precisionModel;
      }
      Attribute typeAttribute = precisionModelElement.getAttribute("type");
      Attribute scaleAttribute = precisionModelElement.getAttribute("scale");
      if (typeAttribute == null && scaleAttribute == null) {
          throw new TestParseException("Missing type attribute in <precisionModel>");
      }
      if (scaleAttribute != null
          || (typeAttribute != null && typeAttribute.getValue().trim().equalsIgnoreCase("FIXED"))) {
          if (typeAttribute != null
              && typeAttribute.getValue().trim().equalsIgnoreCase("FLOATING")) {
              throw new TestParseException("scale attribute not allowed in floating <precisionModel>");
          }
          precisionModel = createPrecisionModel(precisionModelElement);
      }
      return precisionModel;
    }
    
    private PrecisionModel createPrecisionModel(Element precisionModelElement)
			throws TestParseException {
		Attribute scaleAttribute = precisionModelElement.getAttribute("scale");
		if (scaleAttribute == null) {
			throw new TestParseException(
					"Missing scale attribute in <precisionModel>");
		}
		double scale;
		try {
			scale = scaleAttribute.getDoubleValue();
		} catch (DataConversionException e) {
			throw new TestParseException(
					"Could not convert scale attribute to double: "
							+ scaleAttribute.getValue());
		}
		return new PrecisionModel(scale);
	}


    /**
		 * Parses an optional <tt>geometryOperation</tt> element. 
		 * The default is to leave this unspecified .
		 * 
		 * @param runElement
		 * @return an instance of the GeometryOperation class, if specified, or
		 * null if no geometry operation was specified
		 * @throws TestParseException if a parsing error was encountered
		 */
    private GeometryOperation parseGeometryOperation(Element runElement)
  	throws TestParseException
  {
    Element goElement = runElement.getChild(TAG_geometryOperation);
    if (goElement == null) {
      return null;
    }
    String goClass = goElement.getTextTrim();
    GeometryOperation geomOp = (GeometryOperation) getInstance(goClass, GeometryOperation.class);
    if (geomOp == null) {
    	throw new TestParseException("Could not create instance of GeometryOperation from class " + goClass);
    }
    return geomOp;
  }
 
    /**
		 * Parses an optional <tt>resultMatcher</tt> element. 
		 * The default is to leave this unspecified .
		 * 
		 * @param runElement
		 * @return an instance of the ResultMatcher class, if specified, or
		 *  null if no result matcher was specified
		 * @throws TestParseException if a parsing error was encountered
		 */
    private ResultMatcher parseResultMatcher(Element runElement)
  	throws TestParseException
  {
    Element goElement = runElement.getChild(TAG_resultMatcher);
    if (goElement == null) {
      return null;
    }
    String goClass = goElement.getTextTrim();
    ResultMatcher resultMatcher = (ResultMatcher) getInstance(goClass, ResultMatcher.class);
    if (resultMatcher == null) {
    	throw new TestParseException("Could not create instance of ResultMatcher from class " + goClass);
    }
    return resultMatcher;
  }
 
  private double parseTolerance(Element runElement) throws TestParseException 
    {
		double tolerance = 0.0;
		// Note: the tolerance element applies to the coordinate-by-coordinate
		// comparisons of spatial functions. It does not apply to binary predicates.
		// [Jon Aquino]
		Element toleranceElement = runElement.getChild("tolerance");
		if (toleranceElement != null) {
			try {
				tolerance = Double.parseDouble(toleranceElement.getTextTrim());
			} catch (NumberFormatException e) {
				throw new TestParseException("Could not parse tolerance from string: "
						+ toleranceElement.getTextTrim());
			}
		}
		return tolerance;
	}

  /*
	private GeometryOperation getGeometryOperationInstance(String classname) {
		GeometryOperation op = null;
		try {
			Class goClass = Class.forName(classname);
			if (!(GeometryOperation.class.isAssignableFrom(goClass)))
				return null;
			op = (GeometryOperation) goClass.newInstance();
		} catch (Exception ex) {
			return null;
		}
		return op;
	}
    */
  
  /**
   * Gets an instance of a class with the given name, 
   * and ensures that the class is assignable to a specified baseClass.
   * 
   * @return an instance of the class, if it is assignment-compatible, or
   *  null if the requested class is not assigment-compatible
   */
	private Object getInstance(String classname, Class baseClass) {
		Object o = null;
		try {
			Class goClass = Class.forName(classname);
			if (!(baseClass.isAssignableFrom(goClass)))
				return null;
			o = goClass.newInstance();
		} catch (Exception ex) {
			return null;
		}
		return o;
	}
    
    private File wktFile(Element geometryElement, TestRun testRun) throws TestParseException {
        if (geometryElement == null) {
            return null;
        }
        if (geometryElement.getAttribute("file") == null) {
            return null;
        }
        if (!geometryElement.getTextTrim().equals("")) {
            throw new TestParseException("WKT specified both in-line and in external file");
        }

        File wktFile = new File(geometryElement.getAttribute("file").getValue().trim());
        File absoluteWktFile = absoluteWktFile(wktFile, testRun);

        if (!absoluteWktFile.exists()) {
            throw new TestParseException("WKT file does not exist: " + absoluteWktFile);
        }
        if (absoluteWktFile.isDirectory()) {
            throw new TestParseException("WKT file is a directory: " + absoluteWktFile);
        }

        return wktFile;
    }

    private Geometry readGeometry(Element geometryElement, File wktFile)
        throws FileNotFoundException, ParseException, IOException
    {
      String geomText = null;
      if (wktFile != null) {
        List wktList = FileUtil.getContents(wktFile.getPath());
        geomText = toString(wktList);
      }
      else {
        if (geometryElement == null)
          return null;
        geomText = geometryElement.getTextTrim();
      }
      return wktorbReader.read(geomText);
        /*
        if (isHex(geomText, 6))
          return wkbReader.read(WKBReader.hexToBytes(geomText));
        reurn wktReader.read(geomText);
        */
    }

    private String toString(List stringList) {
        String string = "";
        for (Iterator i = stringList.iterator(); i.hasNext();) {
            String line = (String) i.next();
            string += line + "\n";
        }
        return string;
    }

    private File absoluteWktFile(File wktFile, TestRun testRun) {
        if (wktFile == null) {
            return null;
        }
        File absoluteWktFile = wktFile;
        if (!absoluteWktFile.isAbsolute()) {
            File directory =
                testRun.getWorkspace() != null
                    ? testRun.getWorkspace()
                    : testRun.getTestFile().getParentFile();
            absoluteWktFile = new File(directory + File.separator + absoluteWktFile.getName());
        }
        return absoluteWktFile;
    }
}
