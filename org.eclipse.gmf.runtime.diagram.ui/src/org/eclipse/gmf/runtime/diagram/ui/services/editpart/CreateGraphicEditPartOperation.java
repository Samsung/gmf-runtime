/******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.services.editpart;

import org.eclipse.gmf.runtime.common.core.service.IProvider;
import org.eclipse.gmf.runtime.diagram.ui.internal.services.editpart.EditPartOperation;
import org.eclipse.gmf.runtime.diagram.ui.internal.services.editpart.IEditPartProvider;
import org.eclipse.gmf.runtime.notation.View;
 
/**
 * Concrete operation to create a <code>IGraphicalEditPart</code> (or subclass) element.
 * 
 * @author gsturov
 */
public class CreateGraphicEditPartOperation
	extends EditPartOperation {
	
	/**
	 * Constructor. Caches the supplied view.
	 * 
	 * @param view
	 *            the view element to be <i>controlled </i> by the created
	 *            editpart.
	 */
	protected CreateGraphicEditPartOperation(View view) {
		super(view);
	}

	/** 
	 * creates the editpart.
	 * @param provider the provider capable of honoring this operation.
	 * @return the created editpart instance.
	 */
	public Object execute(IProvider provider) {
		return ((IEditPartProvider) provider).createGraphicEditPart(getView());
	}
}
