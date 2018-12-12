/******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.emf.type.core;

import java.net.URL;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;

/**
 * Abstract implementation for application element types.
 * 
 * @author ldamus
 */
public abstract class ElementType
	implements IElementType {

	/**
	 * The type id.
	 */
	private final String id;

	/**
	 * The URL for the icon.
	 */
	private final URL iconURL;

	/**
	 * The display name.
	 */
	private final String displayName;

	/**
	 * The metaclass.
	 */
	private final EClass eClass;
	
	/**
	 * The supertypes of this element type.
	 */
	private IElementType[] supertypes;

	/**
	 * Constructs a new element type.
	 * 
	 * @param id
	 *            the type ID
	 * @param iconURL
	 *            the URL for the icon, or <cOode>null</code> if none.
	 * @param displayName
	 *            the display name
	 */
	protected ElementType(String id, URL iconURL, String displayName) {
		this(id, iconURL, displayName, null);
	}

	/**
	 * Constructs a new element type.
	 * 
	 * @param id
	 *            the type ID
	 * @param iconURL
	 *            the URL for the icon, or <cOode>null</code> if none.
	 * @param displayName
	 *            the display name
	 * @param eClass
	 *            the metaclass associated with the element type
	 */
	protected ElementType(String id, URL iconURL, String displayName,
			EClass eClass) {
		super();
		this.id = id;
		this.iconURL = iconURL;
		this.displayName = displayName;
		this.eClass = eClass;
	}

	/**
	 * Gets the unique identifier.
	 * 
	 * @return the unique identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the icon.
	 * 
	 * @return the icon.
	 */
	public URL getIconURL() {
		return iconURL;
	}

	/**
	 * Gets the display name.
	 * 
	 * @return the display name.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Gets the metamodel type.
	 * 
	 * @return the metamodel type.
	 */
	public EClass getEClass() {
		return eClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gmf.runtime.emf.core.type.IElementType#getConfigureCommand()
	 */
	public ICommand getEditCommand(IEditCommandRequest request) {
		return getEditHelper().getEditCommand(request);
	}
    
    // documentation copied from interface
    public boolean canEdit(IEditCommandRequest req) {
        return getEditHelper().canEdit(req);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(this.getClass())) {
			return this;
		}
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.emf.type.core.IElementType#getAllSuperTypes()
	 */
	public IElementType[] getAllSuperTypes() {
		return supertypes;
	}
	
	protected void setAllSupertypes(IElementType[] supertypes) {
		this.supertypes = supertypes;
	}
}