/******************************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others.
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
 * The file menu manager. It contains all file-related actions.
 *
 * @author chmahone
 */
public class FileMenuManager extends ActionMenuManager {

	/**
	 * The file menu action containing the UI for the file menu manager
	 */
	private static class FileMenuAction extends Action {
		public FileMenuAction() {
			setText(DiagramUIActionsMessages.FileMenuManager_File_ActionLabelText);
			setToolTipText(DiagramUIActionsMessages.FileMenuManager_File_ActionToolTipText);
		}
	}

	/**
	 * Creates a new instance of the file menu manager
	 */
	public FileMenuManager() {
		super(ActionIds.MENU_FILE, new FileMenuAction(), false);
	}

}
