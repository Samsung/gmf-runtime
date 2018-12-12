/******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
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
import java.io.IOException;

import org.apache.batik.transcoder.TranscoderException;
import org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.metafile.DeviceContext;
import org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.metafile.IEmf2SvgConverter;
import org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.metafile.Record;

/**
 * This is a placeholder.  It handles the EOF metafile record which appears at the end of
 * every Enhanced Metafile, though it does nothing.
 * @author dhabib
 */
public class EOF implements IEmf2SvgConverter
{
	public void readEMFRecord( Record rec ) throws IOException
	{
	    // No data to read at the moment.
	}

	public void render( Graphics2D g, DeviceContext context ) throws TranscoderException
	{
	    // Currently we just discard this record.
	}

}
