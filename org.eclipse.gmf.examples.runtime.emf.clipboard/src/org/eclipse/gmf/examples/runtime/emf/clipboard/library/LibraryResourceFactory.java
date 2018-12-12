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


package org.eclipse.gmf.examples.runtime.emf.clipboard.library;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;


/**
 * Custom factory to create our custom resources.
 * 
 * @see LibraryResource
 */
public class LibraryResourceFactory
	extends XMIResourceFactoryImpl {

	/**
	 * Initializes me.
	 */
	public LibraryResourceFactory() {
		super();
	}

	/**
	 * Creates a {@link LibraryResource}.
	 * 
	 * @return a new {@link LibraryResource}
	 */
	public Resource createResource(URI uri) {
		return new LibraryResource(uri);
	}
}
