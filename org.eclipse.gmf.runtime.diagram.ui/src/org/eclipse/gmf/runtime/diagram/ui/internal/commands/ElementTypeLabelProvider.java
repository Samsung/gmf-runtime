/******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.internal.commands;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import org.eclipse.gmf.runtime.common.ui.services.icon.IconService;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;

/**
 * A label provider that provides the name and image for 
 * <code>IElementType</code> objects. 
 * 
 * @author cmahoney
 */
public class ElementTypeLabelProvider
	extends LabelProvider {

	/**
	 * Retrieves the image for <code>IElementType</code> objects using
	 * the <code>IconService</code>.
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object object) {
		if (object instanceof IElementType) {
			return IconService.getInstance()
				.getIcon((IElementType) object);
		}
		return null;
	}

	/**
	 * Uses <code>IElementType.getDisplayName()</code> for the text.
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object object) {
		if (object instanceof IElementType) {
			return ((IElementType) object).getDisplayName();
		} else {
			return object.toString();
		}
	}
}
