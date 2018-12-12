/******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.actions.internal;

import org.eclipse.gmf.runtime.common.ui.action.ActionMenuManager;
import org.eclipse.gmf.runtime.diagram.ui.actions.ActionIds;
import org.eclipse.gmf.runtime.diagram.ui.actions.internal.l10n.DiagramUIActionsMessages;
import org.eclipse.gmf.runtime.diagram.ui.actions.internal.l10n.DiagramUIActionsPluginImages;
import org.eclipse.jface.action.Action;

/**
 * @author melaasar
 *
 * The router menu manager. It contains all router-related actions
 */
public class RouterMenuManager extends ActionMenuManager {

	/**
	 * The router menu action containing the UI for the router menu manager
	 */
	private static class RouterMenuAction extends Action {
		public RouterMenuAction() {
			setText(DiagramUIActionsMessages.RouterActionMenu_LineStyleDropDownText);
			setToolTipText(DiagramUIActionsMessages.RouterActionMenu_LineStyleDropDownTooltip);
			
			setImageDescriptor(DiagramUIActionsPluginImages.DESC_CHANGEROUTER_GROUP);
			setDisabledImageDescriptor(DiagramUIActionsPluginImages.DESC_CHANGEROUTER_GROUP_DISABLED);
			setHoverImageDescriptor(DiagramUIActionsPluginImages.DESC_CHANGEROUTER_GROUP);			
		}
	}

	/**
	 * Creates a new instance of the router menu manager
	 */
	public RouterMenuManager() {
		super(ActionIds.MENU_ROUTER, new RouterMenuAction(), true);
	}

}
