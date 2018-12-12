/******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.actions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.Request;
import org.eclipse.gmf.runtime.common.core.util.Log;
import org.eclipse.gmf.runtime.common.core.util.Trace;
import org.eclipse.gmf.runtime.diagram.ui.actions.internal.DiagramActionsDebugOptions;
import org.eclipse.gmf.runtime.diagram.ui.actions.internal.DiagramActionsPlugin;
import org.eclipse.gmf.runtime.diagram.ui.actions.internal.DiagramActionsStatusCodes;
import org.eclipse.gmf.runtime.diagram.ui.actions.internal.l10n.DiagramUIActionsMessages;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * The copy action that copies the bitmap information on to the clipboard
 * This action is not really a <code>DiagramAction</code> as it doesn't have
 * a request.  The doRun() and the refresh() and calculatedEnabled() have been overwritten
 * appropriately.
 * @author Vishy Ramaswamy
 * @author choang refactor to use ActionContribution item service
 */
abstract public class CopyAction extends DiagramAction {
	/**
	 * JRE Version
	 */
	private static final String JRE_VERSION = System.getProperty("java.version"); //$NON-NLS-1$

	
	/**
	 * Constructor
	 * @param page the active workbenchpage
	 */
	public CopyAction(IWorkbenchPage page) {
		super(page);
	}	
	
	/**
	 * initialize with the correct images and action id
	 * @see org.eclipse.gmf.runtime.common.ui.action.IDisposableAction#init()
	 */
	public void init(){
		super.init();
		/* set the label for the action */
		setText(DiagramUIActionsMessages.CopyAction_Copy);

		/*  set the image */
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));

		/* set the id */
		setId(ActionIds.ACTION_COPY_BITMAP);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.actions.DiagramAction#calculateEnabled()
	 */
	protected boolean calculateEnabled() {

		/* Check if the JRE version is 1.4 or greater */
		if (!JRE_VERSION.startsWith("1.4")) { //$NON-NLS-1$
			return false;
		}

		if (isCopyAll()) {
			return true;
		}

		return canCopy(getSelectedObjects());
	}

	

	/**
	 * If the selected object is a DiagramEditPart,
	 * return true, otherwise return false.
	 * If the selected object is a DiagramEditPart, the diagram
	 * is selected, and all the diagram parts are copied.
	 * 
	 * @return boolean answering whether to copy all selected objects or not
	 */
	protected boolean isCopyAll() {
		
		List selectObjects = getStructuredSelection().toList();
		if (selectObjects.size() == 1) {
			if (selectObjects.get(0) instanceof DiagramEditPart) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If this list of parts contains a ShapeEditPart,
	 * return true, otherwise false.
	 * Copy feature enabled only if at at least one
	 * non-connection is selected.
	 * 
	 * @param parts the parts to check
	 * @return boolean answering whether it is OK to copy the passed parts or not
	 */
	protected boolean canCopy(List parts) {
		/*if one selected part is a non-connectable part, return true */
		for (int i = 0; i < parts.size(); i++) {
			Object o = parts.get(i);
			if (o instanceof ShapeEditPart) {
				return true;
			}
		}
		return false;
	}
	

	/** 
	 * return true so this action cares about selection changes
	 * @see org.eclipse.gmf.runtime.common.ui.action.AbstractActionHandler#isSelectionListener()
	 */
	protected boolean isSelectionListener() {
		
		return true;
	}

	/**
	 * This action is not really a <code>DiagramAction</code> as it doesn't have
	 * a request.  The doRun() and the refresh() and calculatedEnabled() have been overwritten
	 * appropriately.
	 * @see org.eclipse.gmf.runtime.diagram.ui.actions.DiagramAction#createTargetRequest()
	 */
	protected Request createTargetRequest() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.common.ui.action.AbstractActionHandler#doRun(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doRun(IProgressMonitor progressMonitor) {
		try {
			//whatever we are copying belongs to the same editing domain as 
			//the Diagram
			getDiagramEditPart().getEditingDomain().runExclusive(
				new Runnable() {

					public void run() {
						CopyAction.this.run();
					}
				});
		} catch (Exception e) {
			Trace.catching(DiagramActionsPlugin.getInstance(),
				DiagramActionsDebugOptions.EXCEPTIONS_CATCHING, getClass(),
				"doRun()", //$NON-NLS-1$
				e);
			Log.error(DiagramActionsPlugin.getInstance(),
				DiagramActionsStatusCodes.IGNORED_EXCEPTION_WARNING,
				"getPropertyValue", e); //$NON-NLS-1$
		}	
	}

	/** 
	 * Refresh the enablement status based on whether the action is enabled or not.
	 * @see org.eclipse.gmf.runtime.common.ui.action.IActionWithProgress#refresh()
	 */
	public void refresh() {
		setEnabled(calculateEnabled());
	}

}
