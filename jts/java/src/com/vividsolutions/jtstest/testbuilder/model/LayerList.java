package com.vividsolutions.jtstest.testbuilder.model;

public class LayerList 
{
  public static final int LYR_A = 0;
  public static final int LYR_B = 1;
  public static final int LYR_RESULT = 2;
  
  private Layer[] layer = new Layer[3];
  
  public LayerList() 
  {
    layer[0] = new Layer("A");
    layer[1] = new Layer("B");
    layer[2] = new Layer("Result");
  }

  public int size() { return layer.length; }
  
  public Layer getLayer(int i)
  { 
    return layer[i];
  }
  
}
