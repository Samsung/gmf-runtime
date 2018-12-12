/******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.common.ui.services.dnd.drag;

import org.eclipse.swt.dnd.DragSourceEvent;

import org.eclipse.gmf.runtime.common.ui.services.dnd.core.ITransferAgent;

/**
 * Concrete implementation of the transfer drag source adapter for the selection
 * transfer type.
 * 
 * @author Vishy Ramaswamy
 */
public class SelectionDragAdapter
	extends TransferDragSourceAdapter {

	/**
	 * Constructor for SelectionDragAdapter.
	 * 
	 * @param transferAgent
	 *            ITransferAgent to use
	 */
	public SelectionDragAdapter(ITransferAgent transferAgent) {
		super(transferAgent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragStart(DragSourceEvent event) {
		/* Set the selection */
		getTransferAgent().setSelection(getContext().getSelection());

		/* Invoke the super */
		super.dragStart(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragSetData(DragSourceEvent event) {
		/*
		 * For consistency, set the data to the selection even though the
		 * selection is provided by the transfer to the drop target adapter
		 */
		event.data = getTransferAgent().getSelection(event.dataType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragFinished(DragSourceEvent event) {
		/* Set the selection back to null */
		getTransferAgent().setSelection(null);

		/* Invoke the super */
		super.dragSetData(event);
	}
}