/******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.internal.figures;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * An Expandable Figure
 * 
 * @author mmostafa
 * @deprecated Use {org.eclipse.gmf.runtime.diagram.ui.figures.IExpandableFigure}
 */
public interface IExpandableFigure extends
		org.eclipse.gmf.runtime.diagram.ui.figures.IExpandableFigure {

	Rectangle getExtendedBounds();

}
