/******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.commands.CommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * This Action is used to send a request that will destroy a semantic element
 * from the model.
 * 
 * @author melaasar
 * @author choang 
 */
public class DeleteFromModelAction
	extends AbstractDeleteFromAction {
    
    
	/**
	 * Creates a <code>DeleteFromModelAction</code> with a default label.
	 *
	 * @param part The part this action will be associated with.
	 */
	public DeleteFromModelAction(IWorkbenchPart part) {
		super(part);
		
		
	}
 
	/**
	 * Constructor
	 * @param workbenchPage
	 */
	public DeleteFromModelAction(IWorkbenchPage workbenchPage) {
		super(workbenchPage);
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.actions.DiagramAction#calculateEnabled()
	 */
	protected boolean calculateEnabled() {
		List operationSet = getOperationSet();
		if (operationSet.isEmpty()) {
			return false;
		}
		Request request = getTargetRequest();
		Iterator editParts = operationSet.iterator();
		while (editParts.hasNext()) {
			EditPart editPart = (EditPart) editParts.next();
			// disable on diagram links 
			if (editPart instanceof IGraphicalEditPart) {
				IGraphicalEditPart gEditPart = (IGraphicalEditPart) editPart;
				
				// disable the action if any of the edit parts are disabled.
				if (!gEditPart.isEditModeEnabled()) {
					return false;
				}
				
				View view = (View) gEditPart.getModel();
				// Disallow diagram deletion from model only if it is the top most diagram
				EObject container = view.eContainer();
				EObject element = ViewUtil.resolveSemanticElement(view);
				if ((element == null) || (element.eIsProxy())
						|| (element instanceof Diagram)
						|| (view instanceof Diagram && (container == null || !(container instanceof View)))) {
					return false;
				}
			} else {
				Command curCommand = editPart.getCommand(request);
				if (curCommand == null || (curCommand.canExecute() == false)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Initializes this action's text and images.
	 */
	public void init() {
		super.init();
		setId(ActionIds.ACTION_DELETE_FROM_MODEL);
		setText(DiagramUIMessages.DiagramEditor_Delete_from_Model);
		setToolTipText(DiagramUIMessages.DiagramEditor_Delete_from_ModelToolTip);
		ISharedImages workbenchImages = PlatformUI.getWorkbench().getSharedImages();
		setHoverImageDescriptor(
			workbenchImages.getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE));
		setImageDescriptor(
			workbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		setDisabledImageDescriptor(
			workbenchImages.getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE_DISABLED));
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.actions.DiagramAction#getCommand(org.eclipse.gef.Request)
	 */
	protected Command getCommand(Request request) {	
		List operationSet = getOperationSet();
		Iterator editParts = operationSet.iterator();
		CompositeTransactionalCommand command =
			new CompositeTransactionalCommand(getEditingDomain(), getCommandLabel());
		while (editParts.hasNext()) {
			EditPart editPart = (EditPart) editParts.next();
			// disable on diagram links 
			if (editPart instanceof IGraphicalEditPart){
				IGraphicalEditPart gEditPart = 
					(IGraphicalEditPart) editPart;
				View view = (View)gEditPart.getModel();
				// Don't delete diagram from model only if it is the top most diagram
				EObject container = view.eContainer();
				EObject element = ViewUtil.resolveSemanticElement(view);
				if ((element instanceof Diagram)
						|| (view instanceof Diagram && (container == null || !(container instanceof View)))) {
					return null;
				}
			}
			Command curCommand = editPart.getCommand(request);
			if (curCommand != null) {
				command.compose(new CommandProxy(curCommand));				
			}
		}
		
		if ((command.isEmpty())
			|| (command.size() != operationSet.size())){
			return UnexecutableCommand.INSTANCE;
		}
		return new ICommandProxy(command);
	}
    
    protected String getCommandLabel() {
        return DiagramUIMessages.DiagramEditor_Delete_from_Model;
    };
    
    /* (non-Javadoc)
     * @see org.eclipse.gmf.runtime.diagram.ui.actions.DiagramAction#doRun(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void doRun(IProgressMonitor progressMonitor) {
    	Command command = getCommand();
    	//For performance improvement, sometimes, calculateEnable method
    	//won't disable the action for all valid unexecutable cases. At
    	//that time, we need this error check here to report delete 
    	//error to the users.
    	if (command == null || !command.canExecute()) {
    		MessageDialog
			.openError(
				Display.getCurrent().getActiveShell(),
				DiagramUIMessages.DeleteFromModelAction_ErrorDialog_Title,
				DiagramUIMessages.DeleteFromModelAction_ErrorDialog_Text);
    	}
		execute(getCommand(), progressMonitor);
	}
}
