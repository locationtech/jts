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
 * Generates a few shapes and saves them to a shapefiles
 * 
 * @see #usage()
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class SpectrumShpGenerator {

	private static void usage(){
		System.out.println("SpectrumShpGenerator <bounds> <filename_prefix>");
		System.out.println("\n\t[bounds] is minx,maxx.miny,maxy without any spaces");
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if(args == null || args.length!=2){
			usage();
			return;
		}
		
		for(int i=0;i<args.length;i++)
			if(args[i] == null){
				usage();
				return;
			}

		GridGenerator grid = GeometryGenerator.createGridGenerator();
		
		// gg is defined, not null
		grid.setGeometryFactory(new GeometryFactory());
		
		double x1,x2,y1,y2;
		String[] coords = args[0].split(",");
		
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
		
		ArrayList generators = new ArrayList(6);
		generators.add(GeometryGenerator.createPointGenerator());
		generators.add(GeometryGenerator.createLineStringGenerator());
		generators.add(GeometryGenerator.createPolygonGenerator());
		generators.add(GeometryGenerator.createMultiPointGenerator());
		generators.add(GeometryGenerator.createMultiLineStringGenerator());
		generators.add(GeometryGenerator.createMultiPolygonGenerator());
		
		for(int i=0;i<6;i++){
			GeometryGenerator gg = (GeometryGenerator) generators.get(i);
			gg.setGeometryFactory(grid.geometryFactory);
			grid.reset();
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
			
			IOUtil.saveShapefile(fc,args[1]+"_"+i+".shp");
		}
	}
}
