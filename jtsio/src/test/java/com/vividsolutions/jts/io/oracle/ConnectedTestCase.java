/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.io.oracle;

import java.net.URL;
import java.sql.*;
import java.util.Properties;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import oracle.jdbc.OracleConnection;
import junit.framework.TestCase;

/**
 * 
 * Abstract Test Case. Intended to provide a connection with which test may be performed.
 * 
 * Sub-classes should not assume that either the connection will always exist, or 
 * the connection has the required permissions.
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class ConnectedTestCase extends TestCase {
	
	/**
	 * @param arg
	 */
	public ConnectedTestCase(String arg){
		super(arg);
	}
	
	private OracleConnection connection = null;
	
	/**
	 * Sub-classes should not assume that either the connection will always exist, or 
	 * the connection has the required permissions.
	 * 
	 * @return OracleConnection
	 */
	protected OracleConnection getConnection(){
		return connection;
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		Properties props = new Properties();
		URL path = ClassLoader.getSystemResource("com/vividsolutions/jts/io/oracle/connection.properties");
		props.load(path.openStream());
		
		connection = getOracleConnection(props.getProperty("test.server"),props.getProperty("test.port"),
				props.getProperty("test.sid"),props.getProperty("test.user"),props.getProperty("test.pwd"));
	}

    private static OracleConnection getOracleConnection(String server, String port, String sid, String userid, String pwd) throws SQLException {
        String url       = "jdbc:oracle:thin:@"+server+":"+port+":"+sid;
        return (OracleConnection)openConnection( "oracle.jdbc.driver.OracleDriver", url, userid, pwd );
    }



    private static Connection openConnection( String driver, String url, String uid, String pwd ) throws SQLException {

        Connection conn  = null;
        try {
            Class.forName( driver );
        } catch ( java.lang.ClassNotFoundException e ) {
            fail( e.getMessage() );
        }
        conn = DriverManager.getConnection( url, uid, pwd );
        return conn;
    }

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if(connection != null && !connection.isClosed())
			connection.close();
	}
	
	protected static PrecisionModel precisionModel = new PrecisionModel(1000);
	protected static GeometryFactory geometryFactory = new GeometryFactory(precisionModel);

}
