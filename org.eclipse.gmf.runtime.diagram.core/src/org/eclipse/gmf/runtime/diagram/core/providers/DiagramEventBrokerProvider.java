/******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.core.providers;

import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.service.IProvider;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;

/**
 * @author MMostafa The diagram event broker provider interface
 *         <p>
 *         This interface is <EM>not</EM> intended to be implemented by
 *         clients as new methods may be added in the future. Extend
 *         {@link AbstractDiagramEventBrokerProvider} instead.
 *         </p>
 */

public interface DiagramEventBrokerProvider
    extends IProvider {

    /**
     * Creates a <code>DiagramEventBroker</code> instance and associate it
     * with the passed <code>TransactionalEditingDomain</code>
     * 
     * @param editingDomain
     *            the editing domain that will be associated with the diagram
     *            event broker
     * @return a new diagram event broker instance
     */
    public DiagramEventBroker createDiagramEventBroker(
            TransactionalEditingDomain editingDomain);

}
