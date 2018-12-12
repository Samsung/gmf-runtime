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

package org.eclipse.gmf.runtime.common.ui.services.properties;

import org.eclipse.gmf.runtime.common.core.service.IProvider;

/**
 * A <code>IPropertiesProvider</code> object. Each such provider contributes a
 * set of properties as <code>ICompositePropertySource</code> object for the
 * given target.
 * 
 * All contributions from such providers will be assembled by the properties
 * service into a property source object.
 */

public interface IPropertiesProvider extends IProvider {

    /**
     * A call to contribute a set of properties as
     * <code>ICompositePropertySource</code> object for the given target.
     * 
     * This contribution will be appended to the properties contributed by other providers.
     * 
     * @param object -
     *            target of the properties
     * @return - properties contributed by this provider
     */
    public ICompositePropertySource getPropertySource(Object object);

}