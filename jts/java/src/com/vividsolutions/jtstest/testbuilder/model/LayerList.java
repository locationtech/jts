package com.vividsolutions.jtstest.testbuilder.model;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.vividsolutions.jtstest.testbuilder.Viewport;
import com.vividsolutions.jtstest.testbuilder.geom.*;
import com.vividsolutions.jtstest.testbuilder.ui.render.Renderer;

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
  
  public Renderer getRenderer(Viewport viewport)
  {
  	return new LayerListRenderer(viewport, layer);
  }
  
  class LayerListRenderer implements Renderer
  {
  	private Renderer[] layerRenderer = null;
  	
  	public LayerListRenderer(Viewport viewport, Layer[] layer)
  	{
  		layerRenderer = new Renderer[layer.length];
  		for (int i = 0; i < layer.length; i++) {
  			layerRenderer[i] = layer[i].getRenderer(viewport);
  		}
  	}
  	
    public void render(Graphics2D g)
    {
  		for (int i = 0; i < layer.length; i++) {
  			layerRenderer[i].render(g);
  		}
    }
    
  	public synchronized void cancel()
  	{
  		for (int i = 0; i < layerRenderer.length; i++) {
  			layerRenderer[i].cancel();
  		} 		
  	}
  }
  
}
