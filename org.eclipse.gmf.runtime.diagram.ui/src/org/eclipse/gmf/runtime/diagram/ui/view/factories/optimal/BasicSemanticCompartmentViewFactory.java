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

import java.util.Collections;
import java.util.List;

import org.eclipse.gmf.runtime.diagram.ui.view.factories.BasicNodeViewFactory;
import org.eclipse.gmf.runtime.notation.BasicSemanticCompartment;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.Style;
import org.eclipse.gmf.runtime.notation.View;

/**
 * Factory for BasicSemanticComaprtment view
 * 
 * @author aboyko
 * @since 1.2
 *
 */
public class BasicSemanticCompartmentViewFactory extends BasicNodeViewFactory {

	@Override
	protected BasicSemanticCompartment createNode() {
		return NotationFactory.eINSTANCE.createBasicSemanticCompartment();
	}

	@Override
	final protected List<Style> createStyles(View view) {
		return Collections.emptyList();
	}

}
