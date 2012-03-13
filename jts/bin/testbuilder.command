#/bin/sh

CDIR=`dirname "$0"`

## define a default look&feel(laf)
#  "" - empty for system default
#  "javax.swing.plaf.metal" - for a cross platform ui, if problems occur
JAVA_LOOKANDFEEL=""

## run the basic script
. "$CDIR/testbuilder.sh"

