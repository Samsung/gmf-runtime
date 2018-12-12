/******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.internal.editparts;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.Graphics;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.draw2d.ui.figures.LabelEx;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.draw2d.ui.internal.figures.OnConnectionLocator;
import org.eclipse.gmf.runtime.notation.View;

/**
 * An editpart to show a default connection view.
 * 
 * @author cmahoney
 */
public class DefaultConnectionEditPart
	extends ConnectionNodeEditPart {

	/**
	 * Constructs a new instance.
	 * 
	 * @param view
	 */
	public DefaultConnectionEditPart(View view) {
		super(view);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart#createConnectionFigure()
	 */
	protected Connection createConnectionFigure() {

		PolylineConnectionEx conn = new PolylineConnectionEx() {

			public void paint(Graphics graphics) {
				setForegroundColor(ColorConstants.red);
				super.paint(graphics);
			}

		};

		conn.add(new LabelEx(DiagramUIMessages.InvalidView), new OnConnectionLocator(conn, 50));
		return conn;
	}
}