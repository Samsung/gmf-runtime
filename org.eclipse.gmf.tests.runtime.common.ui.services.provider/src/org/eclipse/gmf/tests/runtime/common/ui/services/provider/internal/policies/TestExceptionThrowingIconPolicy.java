/******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/
package org.eclipse.gmf.tests.runtime.common.ui.services.provider.internal.policies;

import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.core.service.IProviderPolicy;
import org.eclipse.gmf.runtime.common.ui.services.icon.GetIconOperation;
import org.eclipse.gmf.runtime.common.ui.services.icon.IIconOperation;
import org.eclipse.gmf.tests.runtime.common.ui.services.provider.internal.testClasses.TestAdaptable;

/**
 * A policy that purposely throws an exception
 * 
 * @author wdiu, Wayne Diu
 */
public class TestExceptionThrowingIconPolicy
	implements IProviderPolicy {

	/**
	 * Set to true after the provides method has been executed, implying an
	 * exception has been thrown
	 */
	public static boolean providesExecuted = false;

	/*
	 * This test method purposely throws an exception
	 */
	public boolean provides(IOperation operation) {
		providesExecuted = true;

		if (operation instanceof GetIconOperation) {
			if (((IIconOperation) operation).getHint() instanceof TestAdaptable) {
				throw new NullPointerException();
			}
		}
		return false;

	}

}
