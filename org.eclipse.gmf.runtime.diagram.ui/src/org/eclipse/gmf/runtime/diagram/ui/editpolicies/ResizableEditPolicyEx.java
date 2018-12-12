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

package org.eclipse.gmf.runtime.diagram.ui.editpolicies;

import java.util.Iterator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.Request;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.handles.AbstractHandle;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.diagram.ui.tools.DragEditPartsTrackerEx;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;

/**
 * A resizable edit policy that understands REQ_DRAG and REQ_DROP requests 
 * 
 * @author melaasar
 */
public class ResizableEditPolicyEx extends ResizableEditPolicy {

	/**
	 * @see org.eclipse.gef.EditPolicy#eraseSourceFeedback(org.eclipse.gef.Request)
	 */
	public void eraseSourceFeedback(Request request) {
		if (RequestConstants.REQ_DROP.equals(request.getType()))
			eraseChangeBoundsFeedback((ChangeBoundsRequest) request);
		else
			super.eraseSourceFeedback(request);
	}

	/**
	 * @see org.eclipse.gef.EditPolicy#showSourceFeedback(org.eclipse.gef.Request)
	 */
	public void showSourceFeedback(Request request) {
		if (RequestConstants.REQ_DROP.equals(request.getType()))
			showChangeBoundsFeedback((ChangeBoundsRequest) request);
		else
			super.showSourceFeedback(request);
	}

	/**
	 * @see org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#addSelectionHandles()
	 */
	protected void addSelectionHandles() {
		super.addSelectionHandles();
		Iterator iter = handles.iterator();
		while (iter.hasNext()) {
			Handle handle = (Handle) iter.next();
			if (handle.getDragTracker().getClass() == DragEditPartsTracker.class)
				replaceHandleDragEditPartsTracker(handle);
		}
	}

	/**
	 * Replaces the handle's default DragEditPartsTracker with the extended
	 * DragEditPartsTrackerEx 
	 * @param handle
	 */
	protected void replaceHandleDragEditPartsTracker(Handle handle) {
		if (handle instanceof AbstractHandle) {
			AbstractHandle h = (AbstractHandle) handle;
			h.setDragTracker(new DragEditPartsTrackerEx(getHost()));
		}
	}

	/**
	 * @see org.eclipse.gef.EditPolicy#getTargetEditPart(org.eclipse.gef.Request)
	 */
	public EditPart getTargetEditPart(Request request) {
		if (understandsRequest(request))
			return getHost();
		return super.getTargetEditPart(request);
	}

    protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
        IFigure feedback = getDragSourceFeedbackFigure();
        
        PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
        getHostFigure().translateToAbsolute(rect);
        rect.translate(request.getMoveDelta());
        rect.resize(request.getSizeDelta());
        
        IFigure f = getHostFigure();
        Dimension min = f.getMinimumSize().getCopy();
        Dimension max = f.getMaximumSize().getCopy();
        IMapMode mmode = MapModeUtil.getMapMode(f);
        min.height = mmode.LPtoDP(min.height);
        min.width = mmode.LPtoDP(min.width);
        max.height = mmode.LPtoDP(max.height);
        max.width = mmode.LPtoDP(max.width);
        
        if (min.width>rect.width)
            rect.width = min.width;
        else if (max.width < rect.width)
            rect.width = max.width;
        
        if (min.height>rect.height)
            rect.height = min.height;
        else if (max.height < rect.height)
            rect.height = max.height;
        
        feedback.translateToRelative(rect);
        feedback.setBounds(rect);
    }
  }
