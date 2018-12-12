/******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import org.eclipse.jface.action.Action;

/**
 * The menu manager for the source end of arrow type.
 * 
 * @author Anthony Hunter
 */
public class ArrowTypeSourceMenuManager extends ActionMenuManager {

	/**
	 * The arrow type menu action containing the UI for the arrow type menu manager.
	 */
	private static class ArrowTypeSourceMenuAction extends Action {
		public ArrowTypeSourceMenuAction() {
			setText(DiagramUIActionsMessages.ArrowTypeActionMenu_arrowTypeSourceText);
			setToolTipText(DiagramUIActionsMessages.ArrowTypeActionMenu_arrowTypeSourceToolTip);
		}
	}

	/**
	 * Constructor for an ArrowTypeSourceMenuManager.
	 */
	public ArrowTypeSourceMenuManager() {
		super(ActionIds.MENU_ARROW_TYPE_SOURCE, new ArrowTypeSourceMenuAction(), true);
	}

}
