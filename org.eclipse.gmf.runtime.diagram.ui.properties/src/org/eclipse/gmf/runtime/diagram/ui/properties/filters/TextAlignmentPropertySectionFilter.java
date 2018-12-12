/******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.properties.filters;

import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.TextStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.IFilter;

/**
 * Filter to display a property section if the selection is applicable to the
 * text alignment property.
 * 
 * @author Anthony Hunter
 */
public class TextAlignmentPropertySectionFilter implements IFilter {

	/*
	 * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
	 */
	public boolean select(Object object) {
		if (object instanceof IGraphicalEditPart) {
			IGraphicalEditPart node = (IGraphicalEditPart) object;
			if (node.getModel() != null) {
				View view = (View) node.getModel();
				TextStyle style = (TextStyle) view.getStyle(
						NotationPackage.Literals.TEXT_STYLE);
				if (style != null) {
					return true;
				}
			}
		}
		return false;
	}
}
