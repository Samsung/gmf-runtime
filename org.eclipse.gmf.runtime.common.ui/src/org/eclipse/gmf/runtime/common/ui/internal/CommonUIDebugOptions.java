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

package org.eclipse.gmf.runtime.common.ui.internal;

/**
 * A list of debug options for this plug-in.
 * 
 * @author khussey
 *
 */
public final class CommonUIDebugOptions {

    /**
     * This class should not be instantiated since it is a static constant
     * class.
     * 
     */
    private CommonUIDebugOptions() {
		/* private constructor */
	}

    /** Debug option. */
    public static final String DEBUG = CommonUIPlugin.getPluginId() + "/debug"; //$NON-NLS-1$

    /** Debug option used to trace exception catching. */
    public static final String EXCEPTIONS_CATCHING = DEBUG + "/exceptions/catching"; //$NON-NLS-1$
    
    /** Debug option used to trace thrown exception. */
    public static final String EXCEPTIONS_THROWING = DEBUG + "/exceptions/throwing"; //$NON-NLS-1$

    /** Debug option used to trace method entering. */
    public static final String METHODS_ENTERING = DEBUG + "/methods/entering"; //$NON-NLS-1$
    
    /** Debug option used to trace method exiting. */
    public static final String METHODS_EXITING = DEBUG + "/methods/exiting"; //$NON-NLS-1$

    /** Debug option used to trace actions admin. */
    public static final String ACTIONS_ADMIN = DEBUG + "/actions/admin"; //$NON-NLS-1$
    
    /** Debug option used to trace action run. */
    public static final String ACTIONS_RUN = DEBUG + "/actions/run"; //$NON-NLS-1$
    
    /** Debug option used to trace action repeat. */
    public static final String ACTIONS_REPEAT = DEBUG + "/actions/repeat"; //$NON-NLS-1$
    
    /** Debug option used to trace resources. */
	public static final String RESOURCE = DEBUG + "/resource/tracing"; //$NON-NLS-1$	
	
	/** Debug option used to trace service configuration. */
	public static final String SERVICES_CONFIG = DEBUG + "/services/config"; //$NON-NLS-1$
}
