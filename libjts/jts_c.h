/************************************************************************
 *
 * C-Wrapper for JTS library
 *
 * (C) 2005 Sandro Santilli, strk@refractions.net
 *
 ***********************************************************************
 *
 * GENERAL NOTES:
 *
 *	- A garbage collector will release every object returned
 *	  by the functions below when no more needed (also char *).
 *	  In order to decide if an object is still needed the GC
 *	  will scan the stack looking for pointers to it.
 *	  DO NOT rely on heap-memory for storing pointers to objects
 *	  you need !!
 *
 *	- Remember to call initJTS() before any use of this library's
 *	  functions. The 'error' JTSMessageHandler will be invoked on
 *	  exceptions, for simple cases have it exit nicely so you
 *	  can avoid checking for 'exceptional' returns from every function
 *	  call.
 *
 *	- Compile your C (or C++) code must be compiled with -ljts_c
 *
 ***********************************************************************/

extern const char *JTSversion(void);

/************************************************************************
 *
 * Abstract type definitions
 *
 ***********************************************************************/

typedef void (*JTSMessageHandler)(const char *fmt, ...);
typedef struct JTSGeom *JTSGeom;

/************************************************************************
 *
 * Initialization, cleanup
 *
 ***********************************************************************/

extern void initJTS(JTSMessageHandler notice, JTSMessageHandler error);
extern void finishJTS(void);

/************************************************************************
 *
 * Input and Output functions, return NULL on exception.
 *
 ***********************************************************************/

extern const char *	JTSGeomToWKT(JTSGeom g1);
extern JTSGeom 		JTSGeomFromWKT(const char *wkt);

extern const unsigned char *	JTSGeomToWKB(JTSGeom g1, size_t *s);
extern JTSGeom 	JTSGeomFromWKB(const unsigned char *wkb, size_t s);

/************************************************************************
 *
 * Topology operations - return NULL on exception.
 *
 ***********************************************************************/

extern JTSGeom JTSIntersection(JTSGeom g1, JTSGeom g2);
extern JTSGeom JTSBuffer(JTSGeom g1,double width, int quadsegs);
extern JTSGeom JTSConvexHull(JTSGeom g1);
extern JTSGeom JTSDifference(JTSGeom g1,JTSGeom g2);
extern JTSGeom JTSSymDifference(JTSGeom g1,JTSGeom g2);
extern JTSGeom JTSBoundary(JTSGeom g1);
extern JTSGeom JTSUnion(JTSGeom g1,JTSGeom g2);
extern JTSGeom JTSPointOnSurface(JTSGeom g1);
extern JTSGeom JTSGetCentroid(JTSGeom g);
extern const char * JTSRelate(JTSGeom g1, JTSGeom g2);
extern JTSGeom JTSPolygonize(JTSGeom geoms[], unsigned int ngeoms);

/************************************************************************
 *
 *  Binary predicates - return 2 on exception.
 *
 ***********************************************************************/

extern char	JTSRelatePattern(JTSGeom g1, JTSGeom g2, const char *pat);
extern char	JTSDisjoint(JTSGeom g1, JTSGeom g2);
extern char	JTSTouches(JTSGeom g1, JTSGeom g2);
extern char	JTSIntersects(JTSGeom g1, JTSGeom g2);
extern char	JTSCrosses(JTSGeom g1, JTSGeom g2);
extern char	JTSWithin(JTSGeom g1, JTSGeom g2);
extern char	JTSContains(JTSGeom g1, JTSGeom g2);
extern char	JTSOverlaps(JTSGeom g1, JTSGeom g2);
extern char	JTSEquals(JTSGeom g1, JTSGeom g2);

/************************************************************************
 *
 *  Unary predicate - return 2 on exception
 *
 ***********************************************************************/

extern char	JTSisEmpty(JTSGeom g1);
extern char	JTSisValid(JTSGeom g1);
extern char	JTSisSimple(JTSGeom g1);
extern char	JTSisRing(JTSGeom g1);
extern char	JTSHasZ(JTSGeom g1);

/************************************************************************
 *
 *  Geometry info
 *
 ***********************************************************************/

extern int	JTSGeomTypeId(JTSGeom g1);
extern int      JTSGetNumCoordinate(JTSGeom g1);
extern JTSGeom	JTSGetGeometryN(JTSGeom g1, int n);
extern JTSGeom	JTSGetExteriorRing(JTSGeom g1);
extern JTSGeom	JTSGetInteriorRingN(JTSGeom g1,int n);
extern int	JTSGetNumInteriorRings(JTSGeom g1);
extern int      JTSGetSRID(JTSGeom g1);
extern int      JTSGetNumGeometries(JTSGeom g1);
extern void 	JTSSetSRID(JTSGeom g, int SRID);

