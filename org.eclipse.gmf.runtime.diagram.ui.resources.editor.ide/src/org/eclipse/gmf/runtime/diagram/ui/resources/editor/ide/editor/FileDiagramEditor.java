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

package org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.editor;

import org.eclipse.core.runtime.NullProgressMonitor;


/**
 * FileDiagramEditor with optional flyout palette.
 * 
 * @author mgoyal
 */
public class FileDiagramEditor
	extends IDEDiagramDocumentEditor {

	/**
	 * Constructs a file diagram editor, with flyout palette if
	 * <code>hasFlyoutPalette</code> is true and without flyout palette
	 * if <code>hasFlyoutPalette</code> is false.
	 * 
	 * @param hasFlyoutPalette true if flyoutPalette is required.
	 */
	public FileDiagramEditor(boolean hasFlyoutPalette) {
		super(hasFlyoutPalette);
	}

	/**
	 * Constructs a file diagram editor without flyout palette.
	 */
	public FileDiagramEditor() {
		this(false);
	}
	
	public void doSaveAs() {
		performSaveAs(new NullProgressMonitor());
	}

	public boolean isSaveAsAllowed() {
		return true;
	}

}
