package org.locationtech.jts.index;

import org.junit.Test;
import org.locationtech.jts.index.strtree.STRtree;

public class GenericSpatialIndex {
    @Test
    public void GenericSpatialIndexAllowsTypeSafeRecallTest(){
        final STRtree<Integer> str= new STRtree<>();
    }
}
