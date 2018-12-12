/******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.printing.render.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gmf.runtime.common.core.util.Log;
import org.eclipse.gmf.runtime.common.core.util.Trace;
import org.eclipse.gmf.runtime.common.ui.action.actions.IPrintActionHelper;
import org.eclipse.gmf.runtime.common.ui.services.editor.EditorService;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IDiagramPreferenceSupport;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.printing.render.internal.DiagramUIPrintingRenderDebugOptions;
import org.eclipse.gmf.runtime.diagram.ui.printing.render.internal.DiagramUIPrintingRenderPlugin;
import org.eclipse.gmf.runtime.diagram.ui.printing.render.internal.JPSDiagramPrinter;
import org.eclipse.gmf.runtime.diagram.ui.printing.render.internal.JPSDiagramPrinterHelper;
import org.eclipse.gmf.runtime.diagram.ui.printing.render.util.RenderedDiagramPrinter;
import org.eclipse.gmf.runtime.diagram.ui.printing.util.DiagramPrinterUtil;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Enhanced printing.  The doPrint() method will invoke a dialog prompting the
 * user to choose options for printing.  The user will be able to choose from
 * printing diagrams of the current type.  If possible, the IFile path of the
 * appicable diagrams will be displayed to the user, when prompting the user
 * to select a diagram for printing.  If the diagram does not correspond to an
 * IFile, its part name will be used as the next choice.
 * 
 * This class implements the IPrintActionHelper interface that can be passed
 * into Print Preview, enabling the print action from there.
 * 
 * @author Wayne Diu, wdiu
 */
public class EnhancedPrintActionHelper implements IPrintActionHelper {
	
	/**
	 * Show the print dialog and print
	 * 
	 * @param IWorkbenchPart the workbenchPart containing the diagram to print
	 */
	
	public void doPrint(IWorkbenchPart workbenchPart) {
		DiagramEditor diagramEditor = null;

		if (workbenchPart instanceof DiagramEditor) {
			diagramEditor = (DiagramEditor) workbenchPart;
		}
		else {
			Log.error(DiagramUIPrintingRenderPlugin.getInstance(), IStatus.ERROR, "Invalid IWorkbenchPart"); //$NON-NLS-1$
			IllegalArgumentException e = new IllegalArgumentException("Invalid IWorkbenchPart."); //$NON-NLS-1$
			Trace.throwing(DiagramUIPrintingRenderPlugin.getInstance(), DiagramUIPrintingRenderDebugOptions.EXCEPTIONS_THROWING,  EnhancedPrintActionHelper.class, "doPrint()", e); //$NON-NLS-1$
			throw e;
		}
		
		IDiagramGraphicalViewer viewer = diagramEditor.getDiagramGraphicalViewer(); 
		RootEditPart rootEP = (viewer == null)?  null : viewer.getRootEditPart();
		
		//splitting the instanceof checks for readability, DiagramRootEditPart implements IDiagramPreferenceSupport 
		
		//try to get actual preferences, if not then use default of PreferencesHint.USE_DEFAULTS
		PreferencesHint preferencesHint = (rootEP instanceof IDiagramPreferenceSupport) ? ((IDiagramPreferenceSupport) rootEP)
			.getPreferencesHint()
			: PreferencesHint.USE_DEFAULTS;
		
		//get actual map mode, default is MapModeUtil.getMapMode()
		IMapMode mapMode = (rootEP instanceof DiagramRootEditPart) ? ((DiagramRootEditPart) rootEP)
				.getMapMode()
				: MapModeUtil.getMapMode();				
							
		if (Platform.getOS().startsWith(Platform.OS_WIN32) 
				&& Platform.getOSArch().equals(Platform.ARCH_X86)) {
			DiagramPrinterUtil.printWithSettings(diagramEditor,
					createDiagramMap(), new RenderedDiagramPrinter(
							preferencesHint, mapMode));
		} else {
			JPSDiagramPrinterHelper.getDiagramPrinterHelper()
					.printWithSettings(diagramEditor, createDiagramMap(),
							new JPSDiagramPrinter(preferencesHint, mapMode));
		}
	}

	/**
	 * Return a Map with diagram name String as key and Diagram as value All
	 * entries in the map correspond to open editors.
	 * 
	 * @return Map with diagram name String as key and Diagram as value All
	 *         entries in the map correspond to open editors with the
	 *         diagramEditor's id.
	 */
	protected Map<String, Diagram> createDiagramMap() {
		
		Map<String, Diagram> diagramMap = new HashMap<String, Diagram>();
		
		//get all diagram editors with the matching id 
		List diagramEditors = EditorService.getInstance().getRegisteredEditorParts();

		Iterator it = diagramEditors.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			
			if (obj instanceof DiagramEditor) { //DiagramDocumentEditor
				DiagramEditor dEditor = (DiagramEditor) obj;
				
				String diagramName = null;
				
				IEditorInput editorInput = dEditor.getEditorInput();
				          
                
				//try to be more descriptive and get the IFile path which includes the project
				IFile file = (IFile)(editorInput.getAdapter(IFile.class));
				if (file != null) {
					diagramName = file.getFullPath().toOSString();
				}
				else {
					//otherwise we can only get the editor title or part name
					diagramName = dEditor.getPartName();
					
					if (diagramName == null) {
						diagramName = dEditor.getTitle();
					}
				}
				
				if (diagramName == null) {
					//the last choice is to use the actual name of the diagram
                    //this has to exist!
					diagramName = dEditor.getDiagram().getName();
				}
                
				diagramMap.put(diagramName, dEditor.getDiagram());
				
			}
		}
		return diagramMap;
	}
}
