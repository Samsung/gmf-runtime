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

package org.eclipse.gmf.runtime.diagram.ui.editparts;


/**
 * Interface checks whether editpart supports rotation. 
 * 
 * @author oboyko
 */
public interface IRotatableEditPart {

	/**
	 * This determines if an <code>EditPart</code> can be rotated or not.  By introducing
	 * an interface, this allows the clients to determine dynamically based on some state whether or
	 * not the EditPart can be rotated.
	 * 
	 * @return <code>booean</code> <code>true</code> if the shape is rotatable, 
	 * or <code>false</code> otherwise.
	 */
	abstract public boolean isRotatable();
}
