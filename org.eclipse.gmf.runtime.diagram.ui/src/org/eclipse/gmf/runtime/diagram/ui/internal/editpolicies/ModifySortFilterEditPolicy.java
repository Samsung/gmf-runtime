/******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.internal.editpolicies;

import java.util.Collections;

import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;
import org.eclipse.gmf.runtime.diagram.core.commands.SetPropertyCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ListCompartmentEditPart;
import org.eclipse.gmf.runtime.diagram.ui.internal.properties.Properties;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.requests.ChangeSortFilterRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.View;


/**
 * EditPolicy which supports modifying sorting and filtering properties.
 * 
 * @author jcorchis
 */
public class ModifySortFilterEditPolicy
	extends AbstractEditPolicy {
	
	
	/**
	 * Returns a command which sets all the sort/filter properties specified in the
	 * Request of type <code>RequestConstants.REQ_CHANGE_SORT_FILTER</code>
	 * 
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	public Command getCommand(Request request) {
		if (RequestConstants.REQ_CHANGE_SORT_FILTER.equals(request.getType())) {
			ChangeSortFilterRequest req = (ChangeSortFilterRequest) request;
			View view = (View)((ListCompartmentEditPart)getHost()).getModel();
            TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
			CompositeTransactionalCommand command = new CompositeTransactionalCommand(editingDomain, DiagramUIMessages.Command_SortFilterCommand);
            SetPropertyCommand filteringCommand = new SetPropertyCommand(editingDomain, new EObjectAdapter(view), Properties.ID_FILTERING, Properties.ID_FILTERING, req.getFiltering());
			SetPropertyCommand filterKeyCommand = new SetPropertyCommand(editingDomain, new EObjectAdapter(view), Properties.ID_FILTERING_KEYS, Properties.ID_FILTERING_KEYS, req.getFilterKeys());
			SetPropertyCommand filteredObjectsCommand = new SetPropertyCommand(editingDomain, new EObjectAdapter(view), Properties.ID_FILTERED_OBJECTS, Properties.ID_FILTERED_OBJECTS, req.getFilteredObjects());
			
			command.compose(filteringCommand);
			command.compose(filterKeyCommand);
			command.compose(filteredObjectsCommand);			
			
			SetPropertyCommand sortingCommand = new SetPropertyCommand(editingDomain, new EObjectAdapter(view), Properties.ID_SORTING, Properties.ID_SORTING, req.getSorting());
			SetPropertyCommand sortingKeysCommand = new SetPropertyCommand(editingDomain, new EObjectAdapter(view), Properties.ID_SORTING_KEYS, Properties.ID_SORTING_KEYS, req.getSortKeys() == null ? Collections.EMPTY_MAP : req.getSortKeys());
			SetPropertyCommand sortedObjectsCommand = new SetPropertyCommand(editingDomain, new EObjectAdapter(view), Properties.ID_SORTED_OBJECTS, Properties.ID_SORTED_OBJECTS, req.getSortedObjects());
			
			command.compose(sortingCommand);
			command.compose(sortingKeysCommand);
			command.compose(sortedObjectsCommand);
			
			return new ICommandProxy(command.reduce());
		}
		return super.getCommand(request);
	}
	
	
	/**
	 * Understands requests of type <code>RequestConstants.REQ_CHANGE_SORT_FILTER</code>
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#understandsRequest(org.eclipse.gef.Request)
	 */
	public boolean understandsRequest(Request req) {
		if (RequestConstants.REQ_CHANGE_SORT_FILTER.equals(req.getType()))
			return true;
		return super.understandsRequest(req);
	}

}
