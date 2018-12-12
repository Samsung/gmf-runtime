/******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/
package org.eclipse.gmf.examples.runtime.diagram.logic.semantic;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Container Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.gmf.examples.runtime.diagram.logic.semantic.ContainerElement#getChildren <em>Children</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.gmf.examples.runtime.diagram.logic.semantic.SemanticPackage#getContainerElement()
 * @model abstract="true"
 * @generated
 */
public interface ContainerElement extends Element {
	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.gmf.examples.runtime.diagram.logic.semantic.Element}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Children</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.gmf.examples.runtime.diagram.logic.semantic.SemanticPackage#getContainerElement_Children()
	 * @model type="org.eclipse.gmf.examples.runtime.diagram.logic.semantic.Element" containment="true"
	 * @generated
	 */
	EList getChildren();

} // ContainerElement
