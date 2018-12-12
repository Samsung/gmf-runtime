/******************************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.services.editpolicy;

import org.eclipse.gef.EditPart;

import org.eclipse.gmf.runtime.common.core.service.IProvider;

/**
 * The interface for providers of the EditPolicy Service.
 * 
 * @author chmahone
 */
public interface IEditPolicyProvider extends IProvider {

	/**
	 * Creates and installs the applicable editpolicies on the given editpart.
	 * @param editPart the <code>EditPart</code>
	 */
	public void createEditPolicies(EditPart editPart);

}
