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

package org.eclipse.gmf.examples.runtime.diagram.geoshapes.internal.providers;

import org.eclipse.gmf.runtime.common.ui.services.action.contributionitem.AbstractContributionItemProvider;
import org.eclipse.gmf.runtime.common.ui.util.IWorkbenchPartDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.printing.actions.PrintPreviewAction;
import org.eclipse.gmf.runtime.diagram.ui.printing.render.actions.EnhancedPrintActionHelper;
import org.eclipse.gmf.runtime.diagram.ui.printing.render.actions.RenderedPrintPreviewAction;
import org.eclipse.jface.action.IAction;

/**
 * A contribution item provider for the Geoshapes Example that adds support for
 * print preview.
 * 
 * @author Wayne Diu, wdiu
 */
public class GeoshapesContributionItemProvider
	extends AbstractContributionItemProvider {

	/**
	 * The constructor
	 */
	public GeoshapesContributionItemProvider() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.common.ui.services.action.contributionitem.AbstractContributionItemProvider#createAction(java.lang.String, org.eclipse.gmf.runtime.common.ui.util.IWorkbenchPartDescriptor)
	 */
	protected IAction createAction(String actionId,
			IWorkbenchPartDescriptor partDescriptor) {

		if (actionId.equals(PrintPreviewAction.ID)) {
			return new RenderedPrintPreviewAction(new EnhancedPrintActionHelper());
		}

		return super.createAction(actionId, partDescriptor);
	}

}
