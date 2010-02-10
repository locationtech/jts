/*
 * Created on Nov 3, 2005
 * 
 */
package com.vividsolutions.jts.generator;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jumpex.debug.IOUtil;

/**
 * Generates a few shapes and saves them to a shapefile
 * 
 * @see #usage()
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class ShpGenerator {

	private static void usage(){
		System.out.println("ShpGenerator -<type>[+] <bounds> <filename>");
		System.out.println("\n\t<type> is one of 'pt', 'line', 'poly', 'grid'");
		System.out.println("\n\tInclude a '+' for multi geometries, not valid for grid type");
		System.out.println("\n\t[bounds] is minx,maxx.miny,maxy without any spaces");
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if(args == null || args.length!=3){
			usage();
			return;
		}
		
		for(int i=0;i<args.length;i++)
			if(args[i] == null){
				usage();
				return;
			}
		
		if(!(args[0].charAt(0) == '-')){
			usage();
			return;
		}
		// char 0 is '-'
		
		GeometryGenerator gg = null;
		if(args[0].indexOf("pt")==1){
			if(args[0].endsWith("+")){
				gg = GeometryGenerator.createMultiPointGenerator();
			}else{
				gg = GeometryGenerator.createPointGenerator();
			}
		}else{
		if(args[0].indexOf("line")==1){
			if(args[0].endsWith("+")){
				gg = GeometryGenerator.createMultiLineStringGenerator();
			}else{
				gg = GeometryGenerator.createLineStringGenerator();
			}
		}else{
		if(args[0].indexOf("poly")==1){
			if(args[0].endsWith("+")){
				gg = GeometryGenerator.createMultiPolygonGenerator();
			}else{
				gg = GeometryGenerator.createPolygonGenerator();
			}
		}else{
		if(args[0].indexOf("grid")==1){
			if(args[0].endsWith("+")){	
				usage();
				return;
			}
			// grid below
		}else{
			usage();
			return;
		}}}}

		GridGenerator grid = GeometryGenerator.createGridGenerator();
		
		// gg is defined, not null
		grid.setGeometryFactory(new GeometryFactory());
		if(gg != null)
			gg.setGeometryFactory(grid.getGeometryFactory());
		
		double x1,x2,y1,y2;
		String[] coords = args[1].split(",");
		
		if(coords == null || coords.length!=4){
			usage();
			return;
		}
		
		x1 = Double.parseDouble(coords[0]);
		x2 = Double.parseDouble(coords[1]);
		y1 = Double.parseDouble(coords[2]);
		y2 = Double.parseDouble(coords[3]);
		
		Envelope bounds = new Envelope(x1,x2,y1,y2);
		grid.setBoundingBox(bounds);
		
		ArrayList geomList = new ArrayList(25);
		grid.setNumberColumns(5);
		grid.setNumberRows(5);
		while(grid.canCreate()){
			Envelope env = grid.createEnv();
			if(gg == null){
				geomList.add(grid.getGeometryFactory().toGeometry(env));
			}else{
				gg.setBoundingBox(env);
				geomList.add(gg.create());
			}
		}
		
		FeatureCollection fc = FeatureDatasetFactory.createFromGeometry(geomList);
		
		IOUtil.saveShapefile(fc,args[2]);
	}
}
