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

package org.eclipse.gmf.tests.runtime.emf.type.core.internal;

import java.util.List;

import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.AbstractEditHelperAdvice;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.tests.runtime.emf.type.core.employee.Customer;

public class DestroyCustomerAdvice extends AbstractEditHelperAdvice {

	public static String BEFORE = "org.eclipse.gmf.tests.runtime.emf.type.core.before"; //$NON-NLS-1$

	public static String AFTER = "org.eclipse.gmf.tests.runtime.emf.type.core.after"; //$NON-NLS-1$

	protected ICommand getBeforeDestroyElementCommand(
			DestroyElementRequest request) {

		List before = (List) request.getParameter(BEFORE);
		
		if (before != null && request.getElementToDestroy() instanceof Customer) {
			before.add(request.getElementToDestroy());
		}

		return null;
	}

	protected ICommand getAfterDestroyElementCommand(
			DestroyElementRequest request) {

		List after = (List) request.getParameter(AFTER);
		
		if (after != null && request.getElementToDestroy() instanceof Customer) {
			after.add(request.getElementToDestroy());
		}

		return null;
	}
}
