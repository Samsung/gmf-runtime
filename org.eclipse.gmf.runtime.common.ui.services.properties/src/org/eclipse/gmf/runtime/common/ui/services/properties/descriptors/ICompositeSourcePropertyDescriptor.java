/******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.common.ui.services.properties.descriptors;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * A descriptor object to work together with the
 * <code>CompositePropertySource</code> object.
 * 
 * Clients who would like to take advantage of the concrete implementation of the
 * <code>ICompositePropertySource</code>
 * <code>CompositePropertySource</code>
 * class, should implement interface for custom property descriptors.
 * 
 * This interface can also be used by custom property provider objects to
 * initialize properties - e.g. write protect, set initial property value, etc.
 */
public interface ICompositeSourcePropertyDescriptor
	extends IPropertyDescriptor {

	/**
	 * Set read-only to true if the property is read-only, or to false otherwise
	 * 
	 * @param read -
	 *            true if the property is read-only, or to false otherwise
	 */
	public void setReadOnly(boolean read);

	/**
	 * Return read-only status - true if editing of the property is not allowed,
	 * false otherwise
	 * 
	 * @return - read-only status - true if editing of the property is not
	 *         allowed, false otherwise
	 */
	public boolean isReadOnly();

	/**
	 * Sets the property category name
	 * 
	 * @param name
	 *            The category name in which the property is to be displayed.
	 */
	public void setCategory(String name);

	/**
	 * Return a value of the the property represented by this descriptor
	 * 
	 * @return - a value of this property
	 */
	public Object getPropertyValue();

	/**
	 * This method is used by the property source to set the property that this
	 * property descriptor maps to.
	 * 
	 * @param value
	 */
	public void setPropertyValue(Object value);

	/**
	 * This method is used by the property source to reset the property that
	 * this property descriptor maps to.
	 */
	public void resetPropertyValue();
}