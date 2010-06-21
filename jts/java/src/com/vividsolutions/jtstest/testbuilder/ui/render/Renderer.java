package com.vividsolutions.jtstest.testbuilder.ui.render;

import java.awt.Graphics2D;

/**
 * A process object which renders a one or more objects to a graphics context
 * and allows cancellation.
 * A Rendered may or may not respect cancellation.
 * The client is expected to ensure that 
 * cancelled rendering is not drawn to the screen
 * (e.g. by rendering to an image and only displaying
 * the most recently drawn image, possibly in multiple increments)
 * 
 * @author mbdavis
 *
 */
public interface Renderer 
{
	void render(Graphics2D g);
	
	/**
	 * Informs this process that it can stop rendering,
	 * because the rendered context will not be displayed.
	 *
	 */
	void cancel();
}
