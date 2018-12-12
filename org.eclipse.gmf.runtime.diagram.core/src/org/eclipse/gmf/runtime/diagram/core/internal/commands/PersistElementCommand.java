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


package org.eclipse.gmf.runtime.diagram.core.internal.commands;

import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.util.Log;
import org.eclipse.gmf.runtime.diagram.core.internal.DiagramPlugin;
import org.eclipse.gmf.runtime.diagram.core.internal.l10n.DiagramCoreMessages;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;


/**
 * @author mmostafa
 * Command that will persist transient views.
 */
public class PersistElementCommand extends AbstractTransactionalCommand { 
	private View _view;

    /**
     * @param editingDomain
     *            the editing domain through which model changes are made
     * @param view
     */
	public PersistElementCommand(TransactionalEditingDomain editingDomain, View view) {
		super(editingDomain, DiagramCoreMessages.AddCommand_Label,
            null);
		_view = view;
	}
	
	/**
	 * Creates a new instance.
	 * @param domain
	 * @param label
	 * @param options
	 * @param affectedFiles
	 */
	public PersistElementCommand(TransactionalEditingDomain editingDomain, View view, Map options) {
		super(editingDomain, DiagramCoreMessages.AddCommand_Label, options, 
            null);
		_view = view;
	}

	/**
	 * Walks up the supplied element's container tree until a container is
	 * found in the detached element map and then moves all of the detached
	 * element's children over to the attached elements.
	 *
	 * @return the detached root element.
	 */
	protected CommandResult doExecuteWithResult(
            IProgressMonitor progressMonitor, IAdaptable info)
        throws ExecutionException {
        
		try {
			assert null != _view: "Null view in PersistElementCommand::doExecute";//$NON-NLS-1$
			EObject container = _view.eContainer();
			if (_view instanceof Edge){
				Diagram diagram = (Diagram)container;
				diagram.persistEdges();
			}
			else if (container instanceof View)
				((View)container).persistChildren();
			
			return CommandResult.newOKCommandResult(_view);
		}
		catch (Exception e) {
			Log.error(DiagramPlugin.getInstance(), IStatus.ERROR,
				e.getMessage(), e);
			return CommandResult.newErrorCommandResult(e.getMessage());
		}
	}
}
