/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.geomfunction;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.function.AffineTransformationFunctions;
import org.locationtech.jtstest.function.BoundaryFunctions;
import org.locationtech.jtstest.function.BufferByUnionFunctions;
import org.locationtech.jtstest.function.BufferFunctions;
import org.locationtech.jtstest.function.ConstructionFunctions;
import org.locationtech.jtstest.function.ConversionFunctions;
import org.locationtech.jtstest.function.CreateFractalShapeFunctions;
import org.locationtech.jtstest.function.CreateRandomShapeFunctions;
import org.locationtech.jtstest.function.CreateShapeFunctions;
import org.locationtech.jtstest.function.DiffFunctions;
import org.locationtech.jtstest.function.DissolveFunctions;
import org.locationtech.jtstest.function.DistanceFunctions;
import org.locationtech.jtstest.function.DoubleKeyMap;
import org.locationtech.jtstest.function.EditFunctions;
import org.locationtech.jtstest.function.GeometryFunctions;
import org.locationtech.jtstest.function.JTSFunctions;
import org.locationtech.jtstest.function.LineHandlingFunctions;
import org.locationtech.jtstest.function.LineSegmentFunctions;
import org.locationtech.jtstest.function.LinearReferencingFunctions;
import org.locationtech.jtstest.function.NodingFunctions;
import org.locationtech.jtstest.function.OffsetCurveFunctions;
import org.locationtech.jtstest.function.OrientationFunctions;
import org.locationtech.jtstest.function.OverlayFunctions;
import org.locationtech.jtstest.function.OverlayNGFunctions;
import org.locationtech.jtstest.function.OverlayNGOptFunctions;
import org.locationtech.jtstest.function.OverlayNGRobustFunctions;
import org.locationtech.jtstest.function.OverlayNGSRFunctions;
import org.locationtech.jtstest.function.OverlayNGSnappingFunctions;
import org.locationtech.jtstest.function.OverlayNGStrictFunctions;
import org.locationtech.jtstest.function.OverlayNGTestFunctions;
import org.locationtech.jtstest.function.OverlayNoSnapFunctions;
import org.locationtech.jtstest.function.PointLocationFunctions;
import org.locationtech.jtstest.function.PolygonOverlayFunctions;
import org.locationtech.jtstest.function.PolygonizeFunctions;
import org.locationtech.jtstest.function.PrecisionFunctions;
import org.locationtech.jtstest.function.PreparedGeometryFunctions;
import org.locationtech.jtstest.function.SelectionFunctions;
import org.locationtech.jtstest.function.SimplificationFunctions;
import org.locationtech.jtstest.function.SnappingFunctions;
import org.locationtech.jtstest.function.SortingFunctions;
import org.locationtech.jtstest.function.SpatialIndexFunctions;
import org.locationtech.jtstest.function.SpatialPredicateFunctions;
import org.locationtech.jtstest.function.TriangleFunctions;
import org.locationtech.jtstest.function.TriangulationFunctions;
import org.locationtech.jtstest.function.ValidationFunctions;
import org.locationtech.jtstest.function.WriterFunctions;


/**
 * A registry to manage a collection of {@link GeometryFunction}s.
 * 
 * @author Martin Davis
 *
 */
public class GeometryFunctionRegistry 
{
  public static GeometryFunctionRegistry createTestBuilderRegistry()
  {
    GeometryFunctionRegistry funcRegistry = new GeometryFunctionRegistry();
    
    funcRegistry.add(GeometryFunctions.class);
    funcRegistry.add(BoundaryFunctions.class);
    funcRegistry.add(BufferFunctions.class);
    funcRegistry.add(BufferByUnionFunctions.class);
    funcRegistry.add(ConstructionFunctions.class);
    funcRegistry.add(ConversionFunctions.class);
    funcRegistry.add(EditFunctions.class);
    funcRegistry.add(LinearReferencingFunctions.class);
    funcRegistry.add(LineHandlingFunctions.class);
    funcRegistry.add(NodingFunctions.class);
    funcRegistry.add(PolygonizeFunctions.class);
    funcRegistry.add(PrecisionFunctions.class);
    funcRegistry.add(PreparedGeometryFunctions.class);
    funcRegistry.add(SelectionFunctions.class);
    funcRegistry.add(SimplificationFunctions.class);
    funcRegistry.add(AffineTransformationFunctions.class);
    funcRegistry.add(DiffFunctions.class);
    funcRegistry.add(DissolveFunctions.class);
    funcRegistry.add(DistanceFunctions.class);
    funcRegistry.add(CreateShapeFunctions.class);
    funcRegistry.add(CreateFractalShapeFunctions.class);
    funcRegistry.add(CreateRandomShapeFunctions.class);
    funcRegistry.add(SpatialIndexFunctions.class);
    funcRegistry.add(SpatialPredicateFunctions.class);
    funcRegistry.add(JTSFunctions.class);
    //funcRegistry.add(MemoryFunctions.class);
    funcRegistry.add(OffsetCurveFunctions.class);
    funcRegistry.add(OrientationFunctions.class);
    funcRegistry.add(LineSegmentFunctions.class);
    funcRegistry.add(OverlayFunctions.class);
    
    funcRegistry.add(OverlayNGSRFunctions.class);
    funcRegistry.add(OverlayNGFunctions.class);
    funcRegistry.add(OverlayNGRobustFunctions.class);
    funcRegistry.add(OverlayNGSnappingFunctions.class);
    funcRegistry.add(OverlayNGStrictFunctions.class);
    
    funcRegistry.add(OverlayNGTestFunctions.class);

    funcRegistry.add(OverlayNGOptFunctions.class);

    funcRegistry.add(OverlayNoSnapFunctions.class);
    funcRegistry.add(PointLocationFunctions.class);
    funcRegistry.add(PolygonOverlayFunctions.class);
    //funcRegistry.add(OverlayEnhancedPrecisionFunctions.class);
    //funcRegistry.add(OverlayCommonBitsRemovedFunctions.class);
    funcRegistry.add(SnappingFunctions.class);
    funcRegistry.add(SortingFunctions.class);
    funcRegistry.add(TriangulationFunctions.class);
    funcRegistry.add(TriangleFunctions.class);
    funcRegistry.add(ValidationFunctions.class);
    funcRegistry.add(WriterFunctions.class);
    
    return funcRegistry;
  }
  public static String functionDescriptionHTML(GeometryFunction func)
  {
    String txt = "<b>" + func.getSignature() + "</b>";
    String desc = func.getDescription();
    if (desc != null) {
      txt += "<br><br>" + desc;
    }
    return "<html>" + txt + "</html>";
  }
  
	private List functions = new ArrayList();
	private Map sortedFunctions = new TreeMap();
	private DoubleKeyMap categorizedFunctions = new DoubleKeyMap();
	private DoubleKeyMap categorizedGeometryFunctions = new DoubleKeyMap();
  private DoubleKeyMap categorizedScalarFunctions = new DoubleKeyMap();
	
	public GeometryFunctionRegistry()
	{
	}
	
	public GeometryFunctionRegistry(Class clz)
	{
		add(clz);
	}
	  
	public List getFunctions()
	{
		return functions;
	}

	public List getGeometryFunctions()
	{
		List funList = new ArrayList();
		for (Iterator i = sortedFunctions.values().iterator(); i.hasNext(); )
		{
			GeometryFunction fun = (GeometryFunction) i.next();
			if (hasGeometryResult(fun))
				funList.add(fun);
		}
		return funList;
	}
	
	public static boolean hasGeometryResult(GeometryFunction func)
	{
		return Geometry.class.isAssignableFrom(func.getReturnType());
	}
	
	public List getScalarFunctions()
	{
		List scalarFun = new ArrayList();
		for (Iterator i = sortedFunctions.values().iterator(); i.hasNext(); )
		{
			GeometryFunction fun = (GeometryFunction) i.next();
			if (! hasGeometryResult(fun))
				scalarFun.add(fun);
		}
		return scalarFun;
	}
	
	/**
	 * Adds functions for all the static methods in the given class.
	 * 
	 * @param geomFuncClass
	 */
	public void add(Class geomFuncClass)
	{
		List funcs = createFunctions(geomFuncClass);
		// sort list of functions so they appear nicely in the UI list
		Collections.sort(funcs);
		add(funcs);
	}
	
	/**
	 * Adds functions for all the static methods in the given class.
	 * 
	 * @param geomFuncClassname the name of the class to load and extract functions from
	 */
	public void add(String geomFuncClassname)
	 throws ClassNotFoundException
	{
		Class geomFuncClass = null;
		geomFuncClass = this.getClass().getClassLoader().loadClass(geomFuncClassname);
		add(geomFuncClass);
	}
	

	public void add(Collection funcs)
	{
		for (Iterator i = funcs.iterator(); i.hasNext(); ) {
			GeometryFunction f = (GeometryFunction) i.next();
			add(f);
		}
	}
	
	/**
	 * Create {@link GeometryFunction}s for all the static 
	 * methods in the given class
	 * 
	 * @param functionClass
	 * @return a list of the functions created
	 */
	public List createFunctions(Class functionClass) {
		List funcs = new ArrayList();
		Method[] method = functionClass.getMethods();
		for (int i = 0; i < method.length; i++) {
			int mod = method[i].getModifiers();
			if (Modifier.isStatic(mod) 
			    && Modifier.isPublic(mod)
			    && isGeometryFunction(method[i])) {
				funcs.add(StaticMethodGeometryFunction.createFunction(method[i]));
			}
		}
		return funcs;
	}

  public static boolean isGeometryFunction(Method method)
  {
    return Geometry.class.isAssignableFrom((method.getParameterTypes())[0]);
  }
	
	/**
	 * Adds a function if it does not currently
   * exist in the registry, or replaces the existing one
	 * with the same signature.
	 * 
	 * @param func a function
	 */
	public void add(GeometryFunction func)
	{
		functions.add(func);
		sortedFunctions.put(func.getName(), func);
		categorizedFunctions.put(func.getCategory(), func.getName(), func);
		if (hasGeometryResult(func)) {
			categorizedGeometryFunctions.put(func.getCategory(), func.getName(), func);
		}
		else {
      categorizedScalarFunctions.put(func.getCategory(), func.getName(), func);		  
		}
	}
	
  public DoubleKeyMap getCategorizedGeometryFunctions()
  {
    return categorizedGeometryFunctions;
  }
  
  public DoubleKeyMap getCategorizedScalarFunctions()
  {
    return categorizedScalarFunctions;
  }
  
	public Collection getCategories()
	{
		return categorizedFunctions.keySet();
	}
	
	public Collection getFunctions(String category)
	{
		return categorizedFunctions.values(category);
	}
	
	/*
		int index = functions.indexOf(func);
		if (index == -1) {
			sortedFunctions.put(func.getName(), func);
		}
		else {
			functions.set(index, func);
		}	
	}
	*/

	
  /**
   * Finds the first function which matches the given signature.
   * 
   * @param name
   * @param paramTypes
   * @return a matching function, or null
   */
  public GeometryFunction find(String name, Class[] paramTypes)
  {
    return null;
  }
  
  /**
   * Finds the first function which matches the given name and argument count.
   * 
   * @param name
   * @return a matching function, or null
   */
  public GeometryFunction find(String name, int argCount)
  {
    for (Iterator i = functions.iterator(); i.hasNext(); ) {
      GeometryFunction func = (GeometryFunction) i.next();
      String funcName = func.getName();
      if (funcName.equalsIgnoreCase(name) 
      		&& func.getParameterTypes().length == argCount)
        return func;
    }
    return null;
  }
  /**
   * Finds the first function which matches the given name.
   * 
   * @param name
   * @return a matching function, or null
   */
  public GeometryFunction find(String name)
  {
    for (Iterator i = functions.iterator(); i.hasNext(); ) {
      GeometryFunction func = (GeometryFunction) i.next();
      String funcName = func.getName();
      if (funcName.equalsIgnoreCase(name))
        return func;
    }
    return null;
  }
  /**
   * Finds the first function which matches the given category and name.
   * 
   * @param name
   * @return a matching function, or null
   */
  public GeometryFunction find(String category, String name)
  {
    for (Iterator i = functions.iterator(); i.hasNext(); ) {
      GeometryFunction func = (GeometryFunction) i.next();
      String funcName = func.getName();
      if (category.equalsIgnoreCase(func.getCategory()) && funcName.equalsIgnoreCase(name))
        return func;
    }
    return null;
  }
}
