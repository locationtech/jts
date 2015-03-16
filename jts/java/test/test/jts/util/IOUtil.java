package test.jts.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class IOUtil {
	  public static Geometry read(String wkt)
	  {
		  WKTReader rdr = new WKTReader();
	    try {
	      return rdr.read(wkt);
	    }
	    catch (ParseException ex) {
	      throw new RuntimeException(ex);
	    }
	  }

}
