/******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.view.factories.optimal;

import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationFactory;

/**
 * Factory for {@link org.eclipse.gmf.runtime.notation.DecorationNode} view.
 * <p>Styles always present:
 * <ul>
 * <li>None
 * </ul> 
 * </p>
 * <p>Smaller memory footprint then Node.</p>
 * <p>
 * Does not support addition/removal of:
 * <ul>
 * <li>Source Edges
 * <li>Target Edges
 * </ul>
 * </p>
 * 
 * @author aboyko
 * @since 1.2
 *
 */
public class DecorationNodeViewFactory extends BasicDecorationViewFactory {

	@Override
	protected Node createNode() {
		return NotationFactory.eINSTANCE.createDecorationNode();
	}
	
}
