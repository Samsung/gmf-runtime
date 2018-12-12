/******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/


package org.eclipse.gmf.runtime.diagram.ui.dnd.parts;

import org.eclipse.gef.dnd.DelegatingDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;

import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer;


/**
 * @author tmacdoug
 *
 * Extension of the diagram graphical viewer to include drag and drop 
 * functionality
 * 
 */
public class DiagramGraphicalViewerWithDnD extends DiagramGraphicalViewer {

    /* 
     * Attribute for the drop adapter
     */
    private final DiagramViewerDropAdapter dropAdapter;	
	
	/**
	 * Constructor
	 * 
	 * @param dropAdapter
	 *            The dropAdapter to set.
	 */
	public DiagramGraphicalViewerWithDnD(DiagramViewerDropAdapter dropAdapter) {
		super();
		this.dropAdapter = dropAdapter;
	}

    /* 
     * @see org.eclipse.gef.ui.parts.AbstractEditPartViewer#getDelegatingDropAdapter()
     */
    protected DelegatingDropAdapter getDelegatingDropAdapter() {
    	if ( this.dropAdapter == null ) {
    		return super.getDelegatingDropAdapter();
    	}
    	
    	return this.dropAdapter;
    }

	/* 
	 * @see org.eclipse.gef.ui.parts.AbstractEditPartViewer#refreshDropTargetAdapter()
	 */
	protected void refreshDropTargetAdapter() {
		if (getControl() == null)
			return;
		if (getDelegatingDropAdapter() == null)
			setDropTarget(null);
		else {
			if (getDropTarget() == null)
				setDropTarget(
					new DropTarget(
						getControl(),
						DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK));
			
			if (this.dropAdapter != null && !this.dropAdapter.isInitialized())
				this.dropAdapter.initialize(getControl());		

			getDropTarget().setTransfer(getDelegatingDropAdapter().getTransfers());
		}
	}  
	
	
}
