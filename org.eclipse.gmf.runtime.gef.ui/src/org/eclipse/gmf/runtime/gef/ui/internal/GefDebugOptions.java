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

package org.eclipse.gmf.runtime.gef.ui.internal;


/**
 * A list of debug options for this plug-in.
 * 
 * @author khussey
 *
 */
public final class GefDebugOptions {

	/**
	 * This class should not be instantiated since it is a static constant
	 * class.
	 * 
	 */
	private GefDebugOptions() {
		// empty constructor
	}

	public static final String DEBUG = GefPlugin.getPluginId() + "/debug"; //$NON-NLS-1$

	public static final String EXCEPTIONS_CATCHING = DEBUG + "/exceptions/catching"; //$NON-NLS-1$
	public static final String EXCEPTIONS_THROWING = DEBUG + "/exceptions/throwing"; //$NON-NLS-1$

	public static final String METHODS_ENTERING = DEBUG + "/methods/entering"; //$NON-NLS-1$
	public static final String METHODS_EXITING = DEBUG + "/methods/exiting"; //$NON-NLS-1$
    public static final String DND = DEBUG + "/dnd/tracing"; //$NON-NLS-1$  
}
