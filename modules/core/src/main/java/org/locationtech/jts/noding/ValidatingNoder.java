package org.locationtech.jts.noding;

import java.util.Collection;


/**
 * A wrapper for {@link Noder}s which validates
 * the output arrangement is correctly noded.
 * An arrangement of line segments is fully noded if 
 * there is no line segment 
 * which has another segment intersecting its interior.
 * If the noding is not correct, a {@link TopologyException} is thrown
 * with details of the first invalid location found.
 * 
 * @author mdavis
 * 
 * @see FastNodingValidator
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
  
  /**
   * Checks whether the output of the wrapped noder is fully noded.
   * Throws an exception if it is not.
   * 
   * @throws TopologyException
   */
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
