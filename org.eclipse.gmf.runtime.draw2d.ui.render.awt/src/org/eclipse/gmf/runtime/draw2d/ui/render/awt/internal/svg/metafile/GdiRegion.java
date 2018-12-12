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
import java.awt.Shape;
import java.awt.geom.GeneralPath;

/**
 * Represents a selectable region.  Not currently implemented or used, this is just a placeholder.
 *  
 * @author dhabib
 */
public class GdiRegion 
{
	private GeneralPath	m_curClipRegion = null;

	public GdiRegion( Shape shape )
	{
		if( shape != null )
		{	
			m_curClipRegion = new GeneralPath();
			m_curClipRegion.append( shape, false );
		}
	}
	
	public GdiRegion( GdiRegion clip )
	{
		if( clip.m_curClipRegion != null )
		{
			m_curClipRegion = (GeneralPath) clip.m_curClipRegion.clone();
		}
	}

	public void apply( Graphics2D g )
	{
		g.setClip( m_curClipRegion );
	}
}
