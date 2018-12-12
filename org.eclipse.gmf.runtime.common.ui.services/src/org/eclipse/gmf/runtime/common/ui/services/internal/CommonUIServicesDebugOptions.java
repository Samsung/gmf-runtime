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

package org.eclipse.gmf.runtime.common.ui.services.internal;

/**
 * A list of debug options for this plug-in.
 * 
 * @author khussey
 *  
 */
public final class CommonUIServicesDebugOptions {

	/**
	 * This class should not be instantiated since it is a static constant
	 * class.
	 *  
	 */
	private CommonUIServicesDebugOptions() {
		/* private constructor */
	}

	/**
	 * General debug string
	 */
	public static final String DEBUG = CommonUIServicesPlugin.getPluginId()
		+ "/debug"; //$NON-NLS-1$

	/**
	 * Debug option for exceptions being caught
	 */
	public static final String EXCEPTIONS_CATCHING = DEBUG
		+ "/exceptions/catching"; //$NON-NLS-1$

	/**
	 * Debug option for exceptions being thrown
	 */
	public static final String EXCEPTIONS_THROWING = DEBUG
		+ "/exceptions/throwing"; //$NON-NLS-1$
}