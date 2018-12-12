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

package org.eclipse.gmf.runtime.common.ui.action.ide.actions.global;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.ui.action.actions.global.ResourceGlobalActionHandler;
import org.eclipse.gmf.runtime.common.ui.action.ide.internal.l10n.CommonUIActionIDEMessages;
import org.eclipse.gmf.runtime.common.ui.services.action.global.IGlobalActionContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Global action handler that copies resources.
 * 
 * @author ldamus
 */
public class CopyResourceGlobalActionHandler
	extends ResourceGlobalActionHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gmf.runtime.common.ui.services.action.global.IGlobalActionHandler#getCommand(org.eclipse.gmf.runtime.common.ui.services.action.global.IGlobalActionContext)
	 */
	public ICommand getCommand(IGlobalActionContext cntxt) {

		List selectedResources = getResourceSelection(
			(IStructuredSelection) cntxt.getSelection()).toList();
		IResource[] resources = (IResource[]) selectedResources
			.toArray(new IResource[selectedResources.size()]);

		// Get the file names and a string representation
		final int length = resources.length;
		int actualLength = 0;
		String[] fileNames = new String[length];
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			IPath location = resources[i].getLocation();

			if (location != null)
				fileNames[actualLength++] = location.toOSString();
			if (i > 0)
				buf.append("\n"); //$NON-NLS-1$
			buf.append(resources[i].getName());
		}
		// was one or more of the locations null?
		if (actualLength < length) {
			String[] tempFileNames = fileNames;
			fileNames = new String[actualLength];
			for (int i = 0; i < actualLength; i++)
				fileNames[i] = tempFileNames[i];
		}

		setClipboard(resources, fileNames, buf.toString(), cntxt
			.getActivePart());
		return null;
	}

	/**
	 * Set the clipboard contents. Prompt to retry if clipboard is busy.
	 * 
	 * @param resources
	 *            the resources to copy to the clipboard
	 * @param fileNames
	 *            file names of the resources to copy to the clipboard
	 * @param names
	 *            string representation of all names
	 */
	private void setClipboard(IResource[] resources, String[] fileNames,
			String names, IWorkbenchPart part) {
		try {
			Clipboard clipboard = new Clipboard(Display.getCurrent());

			// set the clipboard contents
			if (fileNames.length > 0) {
				clipboard
					.setContents(new Object[] {resources, fileNames, names},
						new Transfer[] {ResourceTransfer.getInstance(),
							FileTransfer.getInstance(),
							TextTransfer.getInstance()});
			} else {
				clipboard.setContents(new Object[] {resources, names},
					new Transfer[] {ResourceTransfer.getInstance(),
						TextTransfer.getInstance()});
			}
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
				throw e;
			if (MessageDialog.openQuestion(getShell(part),
				CommonUIActionIDEMessages.CopyToClipboardProblemDialog_title,
				CommonUIActionIDEMessages.CopyToClipboardProblemDialog_message))
				setClipboard(resources, fileNames, names, part);
		}
	}
}