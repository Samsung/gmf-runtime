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

package org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.metafile;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;

import org.apache.batik.transcoder.TranscoderException;
import org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.metafile.DeviceContext;
import org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.metafile.IWmf2SvgConverter;
import org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.metafile.Record;

/**
 * @author dhabib
 */
class IntersectClipRect implements IWmf2SvgConverter 
{
	private static final int LEFT_OFFSET	= 6;
	private static final int TOP_OFFSET		= 4;
	private static final int RIGHT_OFFSET	= 2;
	private static final int BOTTOM_OFFSET	= 0;
	
	private static Rectangle	m_rect;
	
	public void readWMFRecord( Record rec ) throws IOException
	{
		int x = rec.getShortAt( LEFT_OFFSET );
		int y = rec.getShortAt( TOP_OFFSET );
		int x1 = rec.getShortAt( RIGHT_OFFSET );
		int y1 = rec.getShortAt( BOTTOM_OFFSET );
		
		m_rect = new Rectangle( x, y, x1 - x, y1 - y );
	}
	
	public void render( Graphics2D g, DeviceContext context ) throws TranscoderException
	{
		Rectangle rect = context.convertRectangleToSVGLogicalUnits( m_rect );
		g.clip( rect );
	}

}
