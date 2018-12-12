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

package org.eclipse.gmf.runtime.diagram.ui.editpolicies;

import org.eclipse.gef.EditPart;

import org.eclipse.gmf.runtime.diagram.ui.editparts.IInsertableEditPart;
import org.eclipse.gmf.runtime.notation.View;



/**
 * EditPolicy to be installed on {@link org.eclipse.gmf.runtime.diagram.ui.editparts.ListItemEditPart}.
 * This editpolicy will delete the selected element.
 * @author mhanner
 */
public class ListItemComponentEditPolicy
	extends ComponentEditPolicy {


	/**
	 * 
	 */
	public ListItemComponentEditPolicy() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Returns the view element to be deleted.
	 * @return the host's view element.
	 */
	protected View getView() {
		return (View) getHost().getModel();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.ComponentEditPolicy#getInsertableEditPart()
	 */
	protected IInsertableEditPart getInsertableEditPart() {
		// get the container of the host list item
		EditPart container = getHost().getParent();
		if (container instanceof IInsertableEditPart) {
			return (IInsertableEditPart)container;
		}
		
		return null;
	}
}
