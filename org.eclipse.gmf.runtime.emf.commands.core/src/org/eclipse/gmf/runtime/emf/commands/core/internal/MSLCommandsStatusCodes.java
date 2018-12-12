/******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.emf.commands.core.internal;

/**
 * A list of status codes for this plug-in.
 * 
 * @author Christian W. Damus
 */
public final class MSLCommandsStatusCodes {

	/**
	 * This class should not be instantiated since it is a static constant
	 * class.
	 * 
	 */
	private MSLCommandsStatusCodes() {
		// private constructor.
	}

	/** Status code indicating command executed succesfully. */
	public static final int OK = 0;

	/** Statis code indicating model command failed. */
	public static final int MODEL_COMMAND_FAILURE = 1;

	/**
	 * Status code indicating that an operation was rolled back due to live
	 * validation errors.
	 */
	public static final int VALIDATION_FAILURE = 7;
}
