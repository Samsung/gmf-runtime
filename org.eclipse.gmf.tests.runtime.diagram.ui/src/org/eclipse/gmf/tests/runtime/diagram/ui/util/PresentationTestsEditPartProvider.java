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

package org.eclipse.gmf.tests.runtime.diagram.ui.util;

import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.editpart.AbstractEditPartProvider;
import org.eclipse.gmf.runtime.notation.View;


/**
 * @author mmostafa
 */
public class PresentationTestsEditPartProvider
	extends AbstractEditPartProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.services.editpart.AbstractEditPartProvider#getDiagramEditPartClass(org.eclipse.gmf.runtime.notation.View)
	 */
	protected Class getDiagramEditPartClass(View view) {
		if (view.getType().equals(PresentationTestsViewProvider.PRESENTATION_TESTS_DIAGRAM_KIND))
			return DiagramEditPart.class;
		return super.getDiagramEditPartClass(view);
	}
}
