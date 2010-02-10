/*
 * C-Wrapper library for JTS
 *
 * (C) 2004 David Blasby, dblasby@gmail.com
 * (C) 2005 Sandro Santilli, strk@refractions.net
 */ 

#include <string>
#include <iostream>
#include <fstream>

#pragma GCC java_exceptions

#include "jts.h"
#include <java/lang/String.h>
#include <java/lang/System.h>
#include <java/io/PrintStream.h>
#include <java/lang/Throwable.h>

/*
 * This is missing from gcc-3.4.3, but is very helpful as it
 * allocates memory managed by the Garbage Collector
 */
/* Allocate space that is known to be pointer-free. */
extern void *_Jv_AllocBytes (jsize size) __attribute__((__malloc__));
#define Jv_AllocBytes(x) _Jv_AllocBytes((x))

using namespace com::vividsolutions::jts::geom;
using namespace com::vividsolutions::jts::io;
using namespace com::vividsolutions::jts;
using namespace java::lang;
using namespace std;

#define	POINTTYPE	1
#define	LINETYPE	2
#define	POLYGONTYPE	3
#define	MULTIPOINTTYPE	4
#define	MULTILINETYPE	5
#define	MULTIPOLYGONTYPE	6
#define	COLLECTIONTYPE	7

//###########################################################

typedef void (*noticefunc)(const char *fmt, ...);

/* Initialization, cleanup */
extern "C" void initJTS(noticefunc, noticefunc);
extern "C" void finishJTS(void);

/* Input and Output functions */
extern "C" Geometry * JTSGeomFromWKT(const char *wkt);
extern "C" Geometry * JTSGeomFromWKB(const unsigned char *wkb, size_t size);
extern "C" char *JTSGeomToWKT(Geometry *g1);
extern "C" unsigned char *JTSGeomToWKB(Geometry *g1, size_t *size);


extern "C" char *JTSRelate(Geometry *g1, Geometry*g2);
extern "C" void JTSSetSRID(Geometry *g, int SRID);
extern "C" char JTSRelatePattern(Geometry *g1, Geometry*g2,char *pat);
extern "C" char JTSDisjoint(Geometry *g1, Geometry*g2);
extern "C" char JTSTouches(Geometry *g1, Geometry*g2);
extern "C" char JTSIntersects(Geometry *g1, Geometry *g2);
extern "C" char JTSCrosses(Geometry *g1, Geometry*g2);
extern "C" char JTSWithin(Geometry *g1, Geometry*g2);
extern "C" char JTSContains(Geometry *g1, Geometry*g2);
extern "C" char JTSOverlaps(Geometry *g1, Geometry*g2);
extern "C" Geometry *JTSPolygonize(Geometry **geoms, unsigned int ngeoms);
extern "C" int JTSGeomTypeId(Geometry *g1);
extern "C" char JTSisValid(Geometry *g1);
extern "C" char JTSisEmpty(Geometry *g1);
extern "C" Geometry *JTSIntersection(Geometry *g1,Geometry *g1);
extern "C" Geometry *JTSBuffer(Geometry *g1,double width,int quadsegs);
extern "C" Geometry *JTSConvexHull(Geometry *g1);
extern "C" Geometry *JTSDifference(Geometry *g1,Geometry *g2);
extern "C" Geometry *JTSBoundary(Geometry *g1);
extern "C" Geometry *JTSSymDifference(Geometry *g1,Geometry *g2);
extern "C" Geometry *JTSUnion(Geometry *g1,Geometry *g2);
extern "C" char *JTSGeomType(Geometry *g1);
extern "C" Geometry *JTSGetGeometryN(Geometry *g1, int n);
extern "C" Geometry *JTSGetExteriorRing(Geometry *g1);
extern "C" Geometry *JTSGetInteriorRingN(Geometry *g1, int n);
extern "C" int JTSGetNumCoordinate(Geometry *g1);
extern "C" int JTSGetNumInteriorRings(Geometry *g1);
extern "C" int JTSGetNumGeometries(Geometry *g1);
extern "C" char JTSisSimple(Geometry *g1);
extern "C" char JTSEquals(Geometry *g1, Geometry*g2);
extern "C" char JTSisRing(Geometry *g1);
extern "C" Geometry *JTSPointOnSurface(Geometry *g1);
extern "C" Geometry *JTSGetCentroid(Geometry *g1);

extern "C" const char *JTSversion();

//###########################################################

static char * StringToChar(String *s);
static GeometryFactory *jtsGeomFactory = NULL;
static noticefunc NOTICE_MESSAGE;
static noticefunc ERROR_MESSAGE;

void
initJTS(noticefunc nf, noticefunc ef)
{
	if (jtsGeomFactory == NULL)
	{
		JvCreateJavaVM(NULL);
		JvAttachCurrentThread(NULL, NULL);	

		// These are needed to initialize static data
		JvInitClass(&Geometry::class$);
		JvInitClass(&GeometryFactory::class$);
		JvInitClass(&Coordinate::class$);
		JvInitClass(&JTSVersion::class$);

		// NOTE: SRID will have to be changed after geometry creation
		jtsGeomFactory = new GeometryFactory( new PrecisionModel(), -1);

	}
	NOTICE_MESSAGE = nf;
	ERROR_MESSAGE = ef;
}


void
finishJTS(void)
{
	//System::gc();
	JvDetachCurrentThread();
}

//-----------------------------------------------------------
// relate()-related functions
//  return 0 = false, 1 = true, 2 = error occured
//-----------------------------------------------------------

char
JTSDisjoint(Geometry *g1, Geometry*g2)
{
	try {
		bool result;
		result = g1->disjoint(g2);
		return result;
	}
	catch (Throwable *t)
	{
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}

char
JTSTouches(Geometry *g1, Geometry*g2)
{
	try {
		bool result;
		result =  g1->touches(g2);
		return result;
	}
	catch (Throwable *t)
	{
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}

char
JTSIntersects(Geometry *g1, Geometry*g2)
{
	try {
		bool result;
		result = g1->intersects(g2);
		return result;
	}
	catch (Throwable *t)
	{
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}

char
JTSCrosses(Geometry *g1, Geometry*g2)
{
	try {
		bool result;
		result = g1->crosses(g2);
		return result;
	}
	catch (Throwable *t)
	{
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}

char
JTSWithin(Geometry *g1, Geometry*g2)
{
	try {
		bool result;
		result = g1->within(g2);
		return result;
	}
	catch (Throwable *t)
	{
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}

// call g1->contains(g2)
// returns 0 = false
//         1 = true
//         2 = error was trapped
char
JTSContains(Geometry *g1, Geometry*g2)
{
	try {
		bool result;
		result = g1->contains(g2);
		return result;
	}
	catch (Throwable *t)
	{
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}

char
JTSOverlaps(Geometry *g1, Geometry*g2)
{
	try {
		bool result;
		result = g1->overlaps(g2);
		return result;
	}
	catch (Throwable *t)
	{
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}


//-------------------------------------------------------------------
// low-level relate functions
//------------------------------------------------------------------

char
JTSRelatePattern(Geometry *g1, Geometry*g2,char *pat)
{
	try {
		bool result;
		string s = pat;
		result = g1->relate(g2,JvNewStringLatin1(pat));
		return result;
	}
	catch (Throwable *t)
	{
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}

char *
JTSRelate(Geometry *g1, Geometry*g2)
{

	try {

		IntersectionMatrix *im = g1->relate(g2);

		if (im == NULL) return NULL;

		jstring s = im->toString();
		return StringToChar(s);
	}
	catch (Throwable *t)
	{
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}



//-----------------------------------------------------------------
// isValid
//-----------------------------------------------------------------


char
JTSisValid(Geometry *g1)
{
	try {
		return g1->isValid();
	}
	catch (Throwable *t)
	{
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}

}


//-----------------------------------------------------------------
// general purpose
//-----------------------------------------------------------------

char
JTSEquals(Geometry *g1, Geometry*g2)
{
	try {
		bool result;
		result = g1->equals(g2);
		return result;
	}
	catch (Throwable *t)
	{
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}



char
JTSisEmpty(Geometry *g1)
{
	try {
		return g1->isEmpty();
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}

char
JTSisSimple(Geometry *g1)
{
	try {
		return g1->isSimple();
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}

char
JTSisRing(Geometry *g1)
{
	try {
		int type = JTSGeomTypeId(g1);
		if ( type == LINETYPE )
		{
			return ((LineString*)g1)->isRing();
		} else {
			return 0;
		}
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 2;
	}
}



//free the result of this
char *
JTSGeomType(Geometry *g1)
{
	try {
		jstring s = g1->getGeometryType();
		return StringToChar(s);
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}


//-------------------------------------------------------------------
// JTS functions that return geometries
//-------------------------------------------------------------------

Geometry *
JTSIntersection(Geometry *g1,Geometry *g2)
{
	try
	{
		Geometry *g3 = g1->intersection(g2);
		return g3;
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

Geometry *
JTSBuffer(Geometry *g1, double width, int quadrantsegments)
{
	try
	{
		Geometry *g3 = g1->buffer(width, quadrantsegments);
		return g3;
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

Geometry *
JTSConvexHull(Geometry *g1)
{
	try
	{
		Geometry *g3 = g1->convexHull();
		return g3;
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

Geometry *
JTSDifference(Geometry *g1,Geometry *g2)
{
	try {
		Geometry *g3 = g1->difference(g2);
		return g3;
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

Geometry *
JTSBoundary(Geometry *g1)
{
	try {
		Geometry *g3 = g1->getBoundary();
		return g3;
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

Geometry *
JTSSymDifference(Geometry *g1,Geometry *g2)
{
	try {
		Geometry *g3 = g1->symDifference(g2);
		return g3;
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

Geometry *
JTSUnion(Geometry *g1,Geometry *g2)
{
	try {
		Geometry *g3 = g1->union$(g2);
		return g3;
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	} 
}


Geometry *
JTSPointOnSurface(Geometry *g1)
{
	try {
		Geometry *g3 = g1->getInteriorPoint();
		return g3;
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}





//-------------------------------------------------------------------
// memory management functions
//------------------------------------------------------------------


//BUG:: this leaks memory, but delete kills the PrecisionModel for ALL the geometries
void
JTSdeleteGeometry(Geometry *a)
{
	cerr<<"Don't call JTSdeleteGeometry, the GC will do.."<<endl;
	//finishJTS();
	//try { delete a; } catch(...){}
	//return; // run finishJTS() when done
}

void
JTSSetSRID(Geometry *g, int SRID)
{
	g->setSRID(SRID);
}


//-------------------------------------------------------------------
//JTS => POSTGIS conversions
//-------------------------------------------------------------------

int
JTSGetNumCoordinate(Geometry *g1)
{
	try{
		return g1->getNumPoints();
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 0;
	}
}

int
JTSGetNumInteriorRings(Geometry *g1)
{
	try{
		Polygon *p = (Polygon *) g1;
		return p->getNumInteriorRing();
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 0;
	}
}


//only call on GCs (or multi*)
int
JTSGetNumGeometries(Geometry *g1)
{
	try {
		GeometryCollection *gc = (GeometryCollection *) g1;
		return gc->getNumGeometries();
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 0;
	}
}


//call only on GEOMETRYCOLLECTION or MULTI*
Geometry *
JTSGetGeometryN(Geometry *g1, int n)
{
	try{
		return ((GeometryCollection *)g1)->getGeometryN(n);
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}


//call only on polygon
Geometry *
JTSGetExteriorRing(Geometry *g1)
{
	try{
		Polygon *p = (Polygon *) g1;
		return p->getExteriorRing();
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

//call only on polygon
Geometry *
JTSGetInteriorRingN(Geometry *g1,int n)
{
	try {
		Polygon *p = (Polygon *) g1;
		return p->getInteriorRingN(n);
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

Geometry *
JTSPolygonize(Geometry **g, unsigned int ngeoms)
{
	ERROR_MESSAGE("JTS polygonize unimplemented");
	return NULL;
}

Geometry *
JTSGetCentroid(Geometry *g)
{
	try{
		Geometry *ret = g->getCentroid();
		return ret;
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

int
JTSGetSRID(Geometry *g1)
{
	try {
		return g1->getSRID();
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return 0;
	}
}

const char *
JTSversion()
{
	JTSVersion *v = JTSVersion::CURRENT_VERSION;
	if ( ! v ) return "UNDEFINED";
	return StringToChar(v->toString());
}

bool 
JTSHasZ(Geometry *g)
{
	//double az = g->getCoordinate()->z;
	//return (finite(az) && az != DoubleNotANumber);
	return false;
}

int
JTSGeomTypeId(Geometry *g1)
{
	jstring s = g1->getGeometryType();
	char *type = StringToChar(s);
	if ( ! strcmp(type, "Point") ) return POINTTYPE;
	if ( ! strcmp(type, "Polygon") ) return POLYGONTYPE;
	if ( ! strcmp(type, "LineString") ) return LINETYPE;
	if ( ! strcmp(type, "LinearRing") ) return LINETYPE;
	if ( ! strcmp(type, "MultiLineString") ) return MULTILINETYPE;
	if ( ! strcmp(type, "MultiPoint") ) return MULTIPOINTTYPE;
	if ( ! strcmp(type, "MultiPolygon") ) return MULTIPOLYGONTYPE;
	if ( ! strcmp(type, "GeometryCollection") ) return COLLECTIONTYPE;
	else
	{
		ERROR_MESSAGE("JTSGeomTypeId: unknown geometry type: %s",
			type);
		return -1; // unknown type
	}
}

/* CONVERTERS */

char *
JTSGeomToWKT(Geometry *g1)
{
	try {
		NOTICE_MESSAGE("JTSGeomToWKT called");
		jstring s = g1->toString();
		return StringToChar(s);
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

unsigned char *
JTSGeomToWKB(Geometry *g1, size_t *size)
{
	try {
		NOTICE_MESSAGE("JTSasWKB called");
		static WKBWriter *w = new WKBWriter;
		jbyteArray wkb = w->write(g1);
		*size = JvGetArrayLength(wkb);
		return (unsigned char *)elements(wkb);
		//return ByteArrayToChar(wkb);
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

Geometry *
JTSGeomFromWKT(const char *wkt)
{
	try {
		NOTICE_MESSAGE("JTSGeomFromWKT called");
		static WKTReader *r = new WKTReader;
		jstring wkt_j = JvNewStringLatin1(wkt);
		Geometry *g = r->read(wkt_j);
		return g;
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}

Geometry *
JTSGeomFromWKB(const unsigned char *wkb, size_t size)
{
	try {
		NOTICE_MESSAGE("JTSGeomFromWKB called");
		static WKBReader *r = new WKBReader;
		jbyteArray wkb_j = JvNewByteArray(size);
		memcpy(elements(wkb_j), wkb, size);
		Geometry *g = r->read(wkb_j);
		return g;
	} catch (Throwable *t) {
		ERROR_MESSAGE(StringToChar(t->getMessage()));
		return NULL;
	}
}


/****************************************************
 * Module-static routines
 ***************************************************/

static char *
StringToChar(String *s)
{
	int len = JvGetStringUTFLength(s);
	char *buf = (char *)Jv_AllocBytes(len);
	//char *buf = (char *)malloc(len);
	JvGetStringUTFRegion(s, 0, len, buf);
	buf[len] = '\0';
	return buf;
}

#if 0
static unsigned char * ByteArrayToChar(jbyteArray s);
static unsigned char *
ByteArrayToChar(jbyteArray s)
{
	jsize size = JvGetArrayLength(s);
	jbyte *bytes = elements(s);
	unsigned char *ret = (unsigned char *)malloc(size);
	memcpy(ret, bytes, size);
	return ret;
}
#endif
