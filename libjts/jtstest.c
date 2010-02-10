/*
 * JTS C-Wrapper library tester
 *
 * (C) 2005 Sandro Santilli, strk@refractions.net
 */ 

#define _GNU_SOURCE

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>

#include "jts_c.h"

#define MAXWKTLEN 1047551

void
usage(char *me)
{
	fprintf(stderr, "Usage: %s <wktfile>\n", me);
	exit(1);
}

void
notice(const char *fmt, ...) {
	char *msg;
	va_list ap;

	va_start (ap, fmt);

	/*
	 * This is a GNU extension.
	 * Dunno how to handle errors here.
	 */
	if (!vasprintf (&msg, fmt, ap))
	{
		va_end (ap);
		return;
	}
	printf("NOTICE: %s\n", msg);
	va_end(ap);
	free(msg);
}

void
log_and_exit(const char *fmt, ...) {
	char *msg;
	va_list ap;

	va_start (ap, fmt);

	/*
	 * This is a GNU extension.
	 * Dunno how to handle errors here.
	 */
	if (!vasprintf (&msg, fmt, ap))
	{
		va_end (ap);
		return;
	}
	fprintf(stderr, "ERROR: %s\n", msg);
	va_end(ap);
	free(msg);
	exit(1);
}

void
printHEX(FILE *where, const unsigned char *bytes, size_t n)
{
	static char hex[] = "0123456789ABCDEF";
	int i;

	for (i=0; i<n; i++)
	{
		fprintf(where, "%c%c", hex[bytes[i]>>4], hex[bytes[i]&0x0F]);
	}
}

int
do_all(char *inputfile)
{
	JTSGeom g1, g2, g3;
	char wkt[MAXWKTLEN+1];
	FILE *input;
	const char *ptr;
	const unsigned char *uptr;
	size_t size;

	input = fopen(inputfile, "r");
	if ( ! input ) { perror("fopen"); exit(1); }

	fread(wkt, MAXWKTLEN, 1, input);
	fclose(input);
	wkt[MAXWKTLEN] = '\0'; /* ensure it is null terminated */

	/* WKT input */
	g1 = JTSGeomFromWKT(wkt);

	/* WKT output */
	ptr = JTSGeomToWKT(g1);
	printf("Input (WKT): %s\n", ptr); 

	/* WKB output */
	uptr = JTSGeomToWKB(g1, &size);
	printf("Input (WKB): "); printHEX(stdout, uptr, size); putchar('\n');

	/* WKB input */
	g2 = JTSGeomFromWKB(uptr, size); uptr=NULL; 
	if ( ! JTSEquals(g1, g2) ) log_and_exit("Round WKB conversion failed");

	/* Unary predicates */
	if ( JTSisEmpty(g1) ) printf("isEmpty\n");
	if ( JTSisValid(g1) ) printf("isValid\n");
	if ( JTSisSimple(g1) ) printf("isSimple\n");
	if ( JTSisRing(g1) ) printf("isRing\n");

	/* Convex Hull */
	g2 = JTSConvexHull(g1);
	ptr = JTSGeomToWKT(g2);
	printf("ConvexHull: %s\n", ptr); 

	/* Buffer */
	g1 = JTSBuffer(g2, 100, 30);
	ptr = JTSGeomToWKT(g1);
	printf("Buffer: %s\n", ptr); 

	/* Intersection */
	g3 = JTSIntersection(g1, g2);
	if ( ! JTSEquals(g3, g2) )
		log_and_exit("Intersection(g, Buffer(g)) didn't return g");
	ptr = JTSGeomToWKT(g3);
	printf("Intersection: %s\n", ptr); 

	/* Difference */
	g3 = JTSDifference(g1, g2);
	ptr = JTSGeomToWKT(g3);
	printf("Difference: %s\n", ptr); 

	/* SymDifference */
	g3 = JTSSymDifference(g1, g2);
	ptr = JTSGeomToWKT(g3);
	printf("SymDifference: %s\n", ptr); 

	/* Boundary */
	g3 = JTSBoundary(g3);
	ptr = JTSGeomToWKT(g3);
	printf("Boundary: %s\n", ptr); 

	/* Union */
	g3 = JTSUnion(g1, g2);
	if ( ! JTSEquals(g3, g1) )
		log_and_exit("Union(g, Buffer(g)) didn't return Buffer(g)");
	ptr = JTSGeomToWKT(g3);
	printf("Union: %s\n", ptr); 

	/* PointOnSurcace */
	g3 = JTSPointOnSurface(g3);
	ptr = JTSGeomToWKT(g3);
	printf("PointOnSurface: %s\n", ptr); 

	/* Centroid */
	g3 = JTSGetCentroid(g2);
	ptr = JTSGeomToWKT(g3);
	printf("Centroid: %s\n", ptr); 

	/* Relate (and RelatePattern )*/
	ptr = JTSRelate(g1, g2);
	if ( ! JTSRelatePattern(g1, g2, ptr) )
		log_and_exit("! RelatePattern(g1, g2, Relate(g1, g2))");
	printf("Relate: %s\n", ptr); 

#if 0
	/* Polygonize (UNIMPLEMENTED) */
	gg = malloc(2*sizeof(JTSGeom*));
	gg[0] = g1;
	gg[1] = g2;
	g3 = JTSPolygonize(gg, 2);
	ptr = JTSGeomToWKT(g3);
	printf("Polygonize: %s\n", ptr); 
#endif

	/* Binary predicates */
	if ( JTSIntersects(g1, g2) ) printf("Intersect\n");
	if ( JTSDisjoint(g1, g2) ) printf("Disjoint\n");
	if ( JTSTouches(g1, g2) ) printf("Touches\n");
	if ( JTSCrosses(g1, g2) ) printf("Crosses\n");
	if ( JTSWithin(g1, g2) ) printf("Within\n");
	if ( JTSContains(g1, g2) ) printf("Contains\n");
	if ( JTSOverlaps(g1, g2) ) printf("Overlaps\n");

	return 0;
}

int
main(int argc, char **argv)
{
	int i, n=1;

	initJTS(notice, log_and_exit);
	printf("JTS version %s\n", JTSversion());

	if ( argc < 2 ) usage(argv[0]);
	if ( argc > 2 ) n=atoi(argv[2]);
	if ( ! n ) n=1;

	for (i=0; i<n; i++) {
		putc('.', stderr); fflush(stderr);
		do_all(argv[1]);
		putc('+', stderr); fflush(stderr);
	}
	putc('\n', stderr);

	return i;
}

