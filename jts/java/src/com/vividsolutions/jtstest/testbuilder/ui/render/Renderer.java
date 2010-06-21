package com.vividsolutions.jtstest.testbuilder.ui.render;

import java.awt.Graphics2D;

/**
 * A process object which renders a scene to a graphics context
 * and allows cancellation.
 * A Renderer can be cancelled, which may allow it to 
 * stop or short-circuit processing, thus saving cycles.
 * A cancelled rendering will not be displayed.
 * 
 * @author mbdavis
 *
 */
public interface Renderer 
{
	/**
	 * Renders the scene to the graphics context.
	 * 
	 * @param g the graphics context to render to
	 */
	void render(Graphics2D g);
	
	/**
	 * Informs this process that it can stop rendering,
	 * because the rendered context will not be displayed.
	 *
	 */
	void cancel();
}
