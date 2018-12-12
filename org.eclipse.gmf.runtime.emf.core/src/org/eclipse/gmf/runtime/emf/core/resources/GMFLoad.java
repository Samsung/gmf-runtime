/******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.emf.core.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.UnresolvedReferenceException;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.SAXWrapper;
import org.eclipse.emf.ecore.xmi.impl.XMILoadImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class changes the behavior of the default XMILoader so that
 * UnresolvedReferenceExceptions are not thrown back.
 * 
 * @author rafikj
 */
public class GMFLoad
	extends XMILoadImpl {

	/**
	 * Constructor.
	 */
	public GMFLoad(XMLHelper helper) {
		super(helper);
	}

	/**
	 * @see org.eclipse.emf.ecore.xmi.XMLLoad#load(org.eclipse.emf.ecore.xmi.XMLResource,
	 *      java.io.InputStream, java.util.Map)
	 */
	public void load(XMLResource r, InputStream s, Map o)
		throws IOException {

		try {

			super.load(r, s, o);

		} catch (Resource.IOWrappedException e) {
			if (!(e.getCause() instanceof UnresolvedReferenceException))
				throw e;
		} catch (AbortResourceLoadException arle) {
			throw new Resource.IOWrappedException((Exception)arle.getCause());
		}
	}

	/**
	 * @see org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl#makeDefaultHandler()
	 */
	protected DefaultHandler makeDefaultHandler() {
		return new SAXWrapper(new GMFHandler(resource, helper, options));
	}
}
