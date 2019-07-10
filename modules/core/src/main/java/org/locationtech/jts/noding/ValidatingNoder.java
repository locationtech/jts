package org.locationtech.jts.noding;

import java.util.Collection;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;

/**
 * A wrapper for {@link Noder}s which validates
 * the noding is correct.
 * 
 * @author mdavis
 *
 */
public class ValidatingNoder implements Noder {

  private Noder noder;
  private Collection<SegmentString> nodedSS;
  
  /**
   * Creates a noding validator wrapping the given Noder
   * 
   * @param noder the Noder to validate
   */
  public ValidatingNoder(Noder noder) {
    this.noder = noder;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void computeNodes(@SuppressWarnings("rawtypes") Collection segStrings) {
    noder.computeNodes(segStrings);
    nodedSS = noder.getNodedSubstrings(); 
    validate();
  }

  private void validate() {
    FastNodingValidator nv = new FastNodingValidator( nodedSS );
    nv.checkValid();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Collection getNodedSubstrings() {
    return nodedSS;
  }

}
