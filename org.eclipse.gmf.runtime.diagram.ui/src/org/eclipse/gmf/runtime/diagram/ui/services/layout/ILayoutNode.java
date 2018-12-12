/******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.services.layout;

import org.eclipse.gmf.runtime.notation.Node;

/**
 * Interface that wraps the Node view in order to retrieve the size when the
 * Node's extent (either width or height) have been autosized.
 * 
 * This interface can be used in the layout provider implementation class.
 * 
 * IMPORTANT: This interface is <EM>not</EM> intended to be implemented by clients.
 * New methods may be added in the future.
 * 
 * @author sshaw
 */
public interface ILayoutNode {

	/**
	 * Accessor method to return the notation meta-model <code>Node</code>
	 * object.
	 * 
	 * @return <code>Node</code> that this interface object wraps
	 */
	public Node getNode();

	/**
	 * Accessor method to return the actual height of the node irrespective of
	 * whether the Nodes extent (width/height) is in auto-size mode.
	 * 
	 * @return <code>int</code> value representing the actual height of the
	 *         <code>Node</code>.
	 */

	public int getHeight();

	/**
	 * Accessor method to return the actual width of the node irrespective of
	 * whether the Nodes extent (width/height) is in auto-size mode.
	 * 
	 * @return <code>int</code> value representing the actual width of the
	 *         <code>Node</code>.
	 */
	public int getWidth();
}
