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

package org.eclipse.gmf.runtime.common.ui.util;

import org.eclipse.ui.IWorkbenchPart;

/**
 * Interface implemented by objects that know how to match an
 * <code>IWorkbenchPart</code> against themselves.
 * <P>
 * This interface should not be implemented by clients.
 * 
 * @author ldamus
 */
public interface IPartSelector {

	/**
	 * Answers whether or not I select a given workbench <code>part</code>.
	 * 
	 * @param part
	 *            the workbench part to be tested
	 * @return <code>true</code> if the part matches my criteria,
	 *         <code>false</code> otherwise
	 */
	public boolean selects(IWorkbenchPart part);
}
