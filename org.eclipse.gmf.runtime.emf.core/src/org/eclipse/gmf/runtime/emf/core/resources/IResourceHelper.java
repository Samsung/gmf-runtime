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

package org.eclipse.gmf.runtime.emf.core.resources;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;


/**
 * Interface of an adapter for {@link Resource}s that provides
 * services such as creation of objects in that resource.
 * This interface is used by the {@link EMFCoreUtil} class to implement
 * extensible/overrideable object creation behaviours.
 *
 * @author Christian W. Damus (cdamus)
 */
public interface IResourceHelper extends Adapter {
	/**
	 * Creates an instance of the specified <code>EClass</code> for the
	 * intention of attention of attaching it to the resource providing this
	 * helper.
	 * 
	 * @param eClass the <code>EClass</code> to instantiate
	 * 
	 * @return the instance
	 */
	EObject create(EClass eClass);
}
