<run>
  <precisionModel scale="1.0" offsetx="0.0" offsety="0.0"/>

<case>
  <desc>P - empty</desc>
  <a>    POINT EMPTY  </a>
<test><op name="getInteriorPoint" arg1="A" >    POINT EMPTY   </op></test>
</case>

<case>
  <desc>P - single point</desc>
  <a>    POINT(10 10)  </a>
<test><op name="getInteriorPoint" arg1="A" >    POINT(10 10)   </op></test>
</case>

<case>
  <desc>P - single point</desc>
  <a>    MULTIPOINT ((60 300), (200 200), (240 240), (200 300), (40 140), (80 240), (140 240), (100 160), (140 200), (60 200))
	</a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (140 240)   </op></test>
</case>

<case>
  <desc>L - linestring with single segment</desc>
  <a>    LINESTRING (0 0, 7 14)
	</a>
<test><op name="getInteriorPoint" arg1="A" >   POINT (7 14)   </op></test>
</case>

<case>
  <desc>L - linestring with multiple segments </desc>
  <a>    LINESTRING (0 0, 3 15, 6 2, 11 14, 16 5, 16 18, 2 22)
	</a>
<test><op name="getInteriorPoint" arg1="A" >   POINT (11 14)  </op></test>
</case>

<case>
  <desc>L - zero length line</desc>
  <a>    LINESTRING (10 10, 10 10)  </a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (10 10)   </op></test>
</case>

<case>
  <desc>mL - zero length lines</desc>
  <a>    MULTILINESTRING ((10 10, 10 10), (20 20, 20 20))  </a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (10 10)   </op></test>
</case>

<case>
  <desc>mL - complex linestrings</desc>
  <a>    MULTILINESTRING ((60 240, 140 300, 180 200, 40 140, 100 100, 120 220), 
  (240 80, 260 160, 200 240, 180 340, 280 340, 240 180, 180 140, 40 200, 140 260))
	</a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (180 200)   </op></test>
</case>

<case>
  <desc>A - box</desc>
  <a>    POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))
	</a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (5 5)   </op></test>
</case>

<case>
  <desc>A - empty</desc>
  <a>    POLYGON EMPTY
	</a>
<test><op name="getInteriorPoint" arg1="A" >    POINT EMPTY   </op></test>
</case>

<case>
  <desc>A - polygon with horizontal segment at centre (L shape)</desc>
  <a>    POLYGON ((0 2, 0 4, 6 4, 6 0, 2 0, 2 2, 0 2))
	</a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (3 3)   </op></test>
</case>

<case>
  <desc>A - polygon with horizontal segment at centre (narrower L shape)</desc>
  <a>    POLYGON ((0 2, 0 4, 3 4, 3 0, 2 0, 2 2, 0 2))
	</a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (2 3)   </op></test>
</case>

<case>
  <desc>mA - polygons with holes</desc>
  <a>    MULTIPOLYGON (((50 260, 240 340, 260 100, 20 60, 90 140, 50 260), (200 280, 140 240, 180 160, 240 140, 200 280)), ((380 280, 300 260, 340 100, 440 80, 380 280), (380 220, 340 200, 400 100, 380 220)))
	</a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (115 200)  </op></test>
</case>

<case>
  <desc>GC - collection of polygons, lines, points</desc>
  <a>    GEOMETRYCOLLECTION (POLYGON ((0 40, 40 40, 40 0, 0 0, 0 40)), 
  LINESTRING (80 0, 80 80, 120 40), 
  MULTIPOINT ((20 60), (40 80), (60 60)))
	</a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (20 20)   </op></test>
</case>

<case>
  <desc>GC - collection of zero-area polygons and lines</desc>
  <a>    GEOMETRYCOLLECTION (POLYGON ((10 10, 10 10, 10 10, 10 10)), 
  LINESTRING (20 20, 30 30))
	</a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (10 10)   </op></test>
</case>

<case>
  <desc>GC - collection of zero-area polygons and zero-length lines</desc>
  <a>    GEOMETRYCOLLECTION (POLYGON ((10 10, 10 10, 10 10, 10 10)), 
  LINESTRING (20 20, 20 20))
	</a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (10 10)   </op></test>
</case>

<case>
  <desc>GC - collection of zero-area polygons, zero-length lines, and points</desc>
  <a>    GEOMETRYCOLLECTION (POLYGON ((10 10, 10 10, 10 10, 10 10)), 
  LINESTRING (20 20, 20 20),
  MULTIPOINT ((20 10), (10 20)) )
	</a>
<test><op name="getInteriorPoint" arg1="A" >    POINT (10 10)   </op></test>
</case>



</run>
