package com.vividsolutions.jtstest.testbuilder.model;

import java.awt.Graphics2D;

import com.vividsolutions.jtstest.testbuilder.Viewport;
import com.vividsolutions.jtstest.testbuilder.geom.*;

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
  
  public void paint(Graphics2D g, Viewport viewport)
  {
    layer[0].paint(g, viewport);
    layer[1].paint(g, viewport);
    layer[2].paint(g, viewport);
  }
  
  
}
