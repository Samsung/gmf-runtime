/******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/


package org.eclipse.gmf.examples.runtime.common.service.application;

import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.core.service.IProvider;

/**
 * Concrete operation for creating Widgets.
 * 
 */
public class CreateWidgetOperation
	implements IOperation {

	private int orderSize;

	/**
	 * Creates an instance of the CreateWidgetOperation with the specified 
	 * order size.
	 * @param orderSize the order size requested.
	 */
	public CreateWidgetOperation(int orderSize) {
		this.orderSize = orderSize;
	}

	/**
	 * Delegates the creation of the Widgets to the Provider.
	 * @see org.eclipse.gmf.runtime.common.core.service.IOperation#execute(org.eclipse.gmf.runtime.common.core.service.IProvider)
	 */
	public Object execute(IProvider provider) {
		return ((IWidgetProvider) provider).createWidget(orderSize);
	}

	/**
	 * Returns the order size for this CreateWidgetOperation.
	 * @return Returns the orderSize.
	 */
	public int getOrderSize() {
		return orderSize;
	}
}