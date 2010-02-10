package test.jts.index;


import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Adapter for different kinds of indexes
 * @version 1.7
 */
public interface Index
{
  void insert(Envelope itemEnv, Object item);
  List query(Envelope searchEnv);
  void finishInserting();
}