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

package org.eclipse.gmf.runtime.common.ui.services.statusline;

import java.util.List;

import org.eclipse.gmf.runtime.common.core.service.IProvider;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * The interface for providers of the "statusLineContributionItemProvider"
 * extension point. A status line item provider is responsible for retrieving a
 * status line contribution for a {@link WorkbenchPart}.
 * 
 * @noimplement This interface is not intended to be implemented by clients, use
 *              {@link AbstractStatusLineContributionItemProvider}
 * @author Paulina Masson
 * @author Anthony Hunter
 * @since 1.2
 */
public interface IStatusLineContributionItemProvider extends IProvider {

	/**
	 * Gets a list of contribution items for the status line.
	 * 
	 * @param workbenchPage
	 *            the workbench page.
	 * @return a list of contribution items for the status line .
	 */
	public List<IContributionItem> getStatusLineContributionItems(
			IWorkbenchPage workbenchPage);

}
