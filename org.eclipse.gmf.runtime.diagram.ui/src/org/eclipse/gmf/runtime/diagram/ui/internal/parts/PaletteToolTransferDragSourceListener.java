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

package org.eclipse.gmf.runtime.diagram.ui.internal.parts;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Tool;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gmf.runtime.diagram.ui.internal.services.palette.PaletteToolEntry;
import org.eclipse.gmf.runtime.diagram.ui.tools.CreationTool;

/**
 * Allows a single palette entry with a creation tool to be dragged from an
 * EditPartViewer. The palette entry must be a
 * {@link org.eclipse.gmf.runtime.diagram.ui.internal.services.palette.PaletteToolEntry}
 * that uses a {@link org.eclipse.gmf.runtime.diagram.ui.tools.CreationTool}.
 * 
 * @see org.eclipse.gmf.runtime.diagram.ui.internal.parts.PaletteToolTransferDropTargetListener
 * @see org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditorWithFlyOutPalette
 * 
 * @author cmahoney
 */
public class PaletteToolTransferDragSourceListener
	extends TemplateTransferDragSourceListener {

	/**
	 * Constructs a listener on the specified viewer.
	 * 
	 * @param viewer
	 */
	public PaletteToolTransferDragSourceListener(EditPartViewer viewer) {
		super(viewer);
	}

	protected Object getTemplate() {
		Object template = super.getTemplate();
		if (template instanceof PaletteToolEntry) {
			Tool tool = ((PaletteToolEntry) template).createTool();
			if (tool instanceof CreationTool) {
				return template;
			}
		}
		return null;
	}

}
