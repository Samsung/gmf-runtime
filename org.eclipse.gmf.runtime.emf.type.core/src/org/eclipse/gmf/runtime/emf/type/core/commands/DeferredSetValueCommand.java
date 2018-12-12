/******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.emf.type.core.commands;

import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;

/**
 * Command to set the value of a feature of a model element. The model element
 * is not known at the time of command construction. Subclasses can override
 * {@link #getElementToEdit()} to compute or find the elementToEdit.
 * 
 * @author ldamus
 */
public class DeferredSetValueCommand extends SetValueCommand {

	/**
	 * Constructs a new command.
	 * 
	 * @param request
	 *            the set value request
	 */
	public DeferredSetValueCommand(SetRequest request) {
		super(request);
	}

	/**
	 * Always executable when we can't get the elementToEdit.
	 */
	public boolean canExecute() {

		if (getElementToEdit() == null) {
			return true;
		}
		return super.canExecute();
	}
}
