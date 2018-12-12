/******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/


package org.eclipse.gmf.runtime.draw2d.ui.internal.graphics;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gmf.runtime.draw2d.ui.internal.mapmode.DiagramMapModeUtil;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;


/**
 * The MapModeGraphics class is used to translate the various map modes.
 *
 * @author sshaw
 */
public class MapModeGraphics extends ScaledGraphics {

	private IMapMode mm;
	
	/**
	 * Constructs a new ScaledGraphics based on the given Graphics object.
	 * @param g the base graphics object
	 * @param mm <code>IMapMode</code> to retrieve the scale factor from.
	 */
	public MapModeGraphics(Graphics g, IMapMode mm) {
		super(g);
		setScale(DiagramMapModeUtil.getScale(mm));
		this.mm = mm;
	}

	/**
	 * @return <code>IMapMode</code>
	 */
	public IMapMode getMapMode() {
		return mm;
	}

	/** @see Graphics#drawImage(Image, int, int) */
	public void drawImage(Image srcImage, int x, int y) {
		org.eclipse.swt.graphics.Rectangle size = srcImage.getBounds();
		drawImage(srcImage, 0, 0, size.width, size.height, x, y, getMapMode().DPtoLP(size.width), getMapMode().DPtoLP(size.height));
	}
	
	/** @see Graphics#scale(double) */
	public void scale(double amount) {
		getGraphics().scale(amount);
	}
	
	/**
	 * Override to prevent zooming of the font height.
	 */
	int zoomFontHeight(int height) {
		return height;
	}

	Point zoomTextPoint(int x, int y) {
		return zoomRect(x, y, 0, 0).getTopLeft();
	}
	
	Font zoomFont(Font f) {
		if (f == null)
			f = Display.getCurrent().getSystemFont();
		return f;
	}
		
}
