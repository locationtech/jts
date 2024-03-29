/**
 * This script can been used to generate test data against a PostgreSQL database + PostGIS extension.
 * Just run on it against the PostgreSQL database, and it will generate CSV files in /tmp directory.
 */

CREATE OR REPLACE FUNCTION generate_twkb_test_data(
    arr text[],
    xyprecisions int[] default ARRAY[-7, 0, 7],
    zprecisions int[] default ARRAY[0, 7],
    mprecisions int[] default ARRAY[0, 7]
)
RETURNS TABLE (input_wkt TEXT, xyprecision INT, zprecision INT, mprecision INT, withsize TEXT, withbbox TEXT, expected_wkt TEXT, expected_twkb TEXT)
AS $$
DECLARE
	inputwkt TEXT;
	xy INT;
	z INT;
	m INT;
	withsize BOOLEAN;
	withbbox BOOLEAN;
	expected_wkt TEXT;
	expected_twkb TEXT;
BEGIN

	CREATE TEMPORARY TABLE td (
  		input_wkt TEXT NOT NULL,
  		xyprecision INT NOT NULL,
  		zprecision INT NOT NULL,
  		mprecision INT NOT NULL,
  		withsize TEXT NOT NULL,
  		withbbox TEXT NOT NULL,
  		expected_wkt TEXT NOT NULL,
  		expected_twkb TEXT UNIQUE NOT NULL);

   	withbbox = FALSE;
   	withsize = FALSE;

   FOREACH inputwkt IN ARRAY arr
   LOOP
   	FOREACH xy IN ARRAY xyprecisions LOOP
	   	FOREACH z IN ARRAY zprecisions LOOP
		   	FOREACH m IN ARRAY mprecisions LOOP
		   	     --FOR withsize IN 0..1 LOOP
			   	 	FOR withbbox IN 0..1 LOOP
						expected_twkb = encode(ST_AsTWKB(inputwkt::geometry, xy, z, m, withsize::boolean, withbbox::boolean), 'hex');
						expected_wkt = ST_AsText(ST_GeomFromTWKB(decode(expected_twkb, 'hex')));
						INSERT INTO td VALUES(inputwkt, xy, z, m,
											  CASE WHEN withsize::boolean = TRUE THEN 'true' ELSE 'false' END,
											  CASE WHEN withbbox::boolean = TRUE THEN 'true' ELSE 'false' END,
											  expected_wkt, expected_twkb)
									   ON CONFLICT DO NOTHING;
					END LOOP;
				-- END LOOP;
			END LOOP;
		END LOOP;
	END LOOP;
   END LOOP;
   RETURN QUERY SELECT * FROM td;
   DROP TABLE td;
END
$$ LANGUAGE plpgsql;


COPY (SELECT * FROM generate_twkb_test_data(ARRAY[
        'POINT (1 1)',
        'POINT Z(12345678.12345678 -12345678.12345678 0.12345678)',
        'POINT M(12345678.12345678 -12345678.12345678 9.87654321)',
        'POINT ZM(12345678.12345678 -12345678.12345678 0.12345678 9.87654321)'
    ])
 ORDER BY input_wkt, xyprecision, zprecision, mprecision, withsize, withbbox
 ) TO '/tmp/points.csv' WITH (FORMAT CSV, HEADER, DELIMITER '|');

COPY (SELECT * FROM generate_twkb_test_data(ARRAY[
		'LINESTRING (12345678.12345678 -12345678.12345678, 87654321.87654321 -87654321.87654321)',
		'LINESTRING Z(12345678.12345678 -12345678.12345678 0.12345678, 87654321.87654321 -87654321.87654321 1.12345678)',
		'LINESTRING M(12345678.12345678 -12345678.12345678 9.87654321, 87654321.87654321 -87654321.87654321 8.87654321)',
		'LINESTRING ZM(12345678.12345678 -12345678.12345678 0.12345678 9.87654321, 87654321.87654321 -87654321.87654321 1.12345678 8.87654321)'
	])
 ORDER BY input_wkt, xyprecision, zprecision, mprecision, withsize, withbbox
 ) TO '/tmp/linestrings.csv' WITH (FORMAT CSV, HEADER, DELIMITER '|');

COPY (SELECT * FROM generate_twkb_test_data(ARRAY[
		'POLYGON((0 0,0 10,10 10,1000 1000,1000 0,0 0),(1 1,1 2,2 2,2 1,1 1))',
		'POLYGON Z((0 0 1,0 10 3,10 10 5,0 0 1),(1 1 9,1 2 9,2 2 9,2 1 9,1 1 9))',
		'POLYGON M((0 0 2,0 10 4,10 10 6,0 0 2),(1 1 0.9,1 2 1.9,2 2 2.9,2 1 3.9,1 1 0.9))',
		'POLYGON ZM((0 0 1 2,0 10 3 4,10 10 5 6,0 0 1 2),(1 1 9 9,1 2 9 9,2 2 9 9,2 1 9 9,1 1 9 9))'
	])
 ORDER BY input_wkt, xyprecision, zprecision, mprecision, withsize, withbbox
 ) TO '/tmp/polygons.csv' WITH (FORMAT CSV, HEADER, DELIMITER '|');

COPY (SELECT * FROM generate_twkb_test_data(ARRAY[
		'MULTIPOINT(12345678.12345678 -12345678.12345678,-1 1,10 10)',
		'MULTIPOINT Z(12345678.12345678 -12345678.12345678 1e8,-1 1 -1,10 10 5)',
		'MULTIPOINT M(12345678.12345678 -12345678.12345678 1e-8,-1 1 2,10 10 6)',
		'MULTIPOINT ZM(12345678.12345678 -12345678.12345678 1e8 1e-8,-1 1 -1 2,10 10 5 6)'
	])
 ORDER BY input_wkt, xyprecision, zprecision, mprecision, withsize, withbbox
 ) TO '/tmp/multipoints.csv' WITH (FORMAT CSV, HEADER, DELIMITER '|');

COPY (SELECT * FROM generate_twkb_test_data(ARRAY[
		'MULTILINESTRING((1 1, 9 10),EMPTY,(7787.60977606 5723.96163229,7788.25094530 5724.26827888))',
		'MULTILINESTRING Z((1 1 1, 9 10 11),EMPTY,(7787.60977606 5723.96163229 7.6,7788.25094530 5724.26827888 8.6))',
		'MULTILINESTRING M((1 1 1, 9 10 12),EMPTY,(7787.60977606 5723.96163229 5.02,7788.25094530 5724.26827888 9.02))',
		'MULTILINESTRING ZM((1 1 1 1, 9 10 11 12),EMPTY,(7787.60977606 5723.96163229 7.6 5.02,7788.25094530 5724.26827888 8.6 9.02))'
	])
 ORDER BY input_wkt, xyprecision, zprecision, mprecision, withsize, withbbox
 ) TO '/tmp/multilinestrings.csv' WITH (FORMAT CSV, HEADER, DELIMITER '|');

COPY (SELECT * FROM generate_twkb_test_data(ARRAY[
		'MULTIPOLYGON(((0 0,0 10,10 10,0 0),(1 1,1 2,2 2,2 1,1 1)),EMPTY,((1 1, -1 -1, 5 5, 1 1)))',
		'MULTIPOLYGON Z(((0 0 1,0 10 3,10 10 5,0 0 1),(1 1 9,1 2 9,2 2 9,2 1 9,1 1 9)),EMPTY,((1 1 9, -1 -1 7, 5 5 5, 1 1 3)))',
		'MULTIPOLYGON M(((0 0 2,0 10 4,10 10 6,0 0 2),(1 1 9,1 2 9,2 2 9,2 1 9,1 1 9)),EMPTY,((1 1 8, -1 -1 6, 5 5 4, 1 1 2)))',
		'MULTIPOLYGON ZM(((0 0 1 2,0 10 3 4,10 10 5 6,0 0 1 2),(1 1 9 9,1 2 9 9,2 2 9 9,2 1 9 9,1 1 9 9)),EMPTY,((1 1 9 8, -1 -1 7 6, 5 5 5 4, 1 1 3 2)))'
	], ARRAY[-5, 1, 3], ARRAY[2, 4], ARRAY[3, 6])
 ORDER BY input_wkt, xyprecision, zprecision, mprecision, withsize, withbbox
 ) TO '/tmp/multipolygons.csv' WITH (FORMAT CSV, HEADER, DELIMITER '|');

COPY (SELECT * FROM generate_twkb_test_data(ARRAY[
		'GEOMETRYCOLLECTION (MULTIPOLYGON(((0 0,0 10,10 10,0 0))),LINESTRING EMPTY, POINT(3 4))',
		'GEOMETRYCOLLECTION Z(MULTIPOLYGON Z(((0 0 1,0 10 3,10 10 5,0 0 1))),LINESTRING EMPTY, POINT Z(3 4 5))',
		'GEOMETRYCOLLECTION M(MULTIPOLYGON M(((0 0 2,0 10 4,10 10 6,0 0 2))),LINESTRING EMPTY, POINT M(3 4 6))',
		'GEOMETRYCOLLECTION ZM(MULTIPOLYGON ZM(((0 0 1 2,0 10 3 4,10 10 5 6,0 0 1 2))),LINESTRING EMPTY, POINT ZM(3 4 5 6))'
	])
 ORDER BY input_wkt, xyprecision, zprecision, mprecision, withsize, withbbox
 ) TO '/tmp/geometrycollections.csv' WITH (FORMAT CSV, HEADER, DELIMITER '|');
