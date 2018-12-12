/******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.examples.runtime.diagram.logic.internal.providers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.gmf.examples.runtime.diagram.logic.internal.editparts.CircuitEditPart;
import org.eclipse.gmf.examples.runtime.diagram.logic.internal.editparts.LEDEditPart;
import org.eclipse.gmf.examples.runtime.diagram.logic.internal.editparts.LogicFlowCompartmentEditPart;
import org.eclipse.gmf.examples.runtime.diagram.logic.internal.editparts.LogicFlowContainerEditPart;
import org.eclipse.gmf.examples.runtime.diagram.logic.internal.editparts.LogicGateEditPart;
import org.eclipse.gmf.examples.runtime.diagram.logic.internal.editparts.LogicShapeCompartmentEditPart;
import org.eclipse.gmf.examples.runtime.diagram.logic.internal.editparts.TerminalEditPart;
import org.eclipse.gmf.examples.runtime.diagram.logic.internal.editparts.WireEditPart;
import org.eclipse.gmf.examples.runtime.diagram.logic.semantic.SemanticPackage;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.editpart.AbstractEditPartProvider;
import org.eclipse.gmf.runtime.notation.View;

/**
 * Editpart provider for the logic diagram.
 * 
 * @author qili
 * @canBeSeenBy org.eclipse.gmf.examples.runtime.diagram.logic.*
 */

public class LogicEditPartProvider extends AbstractEditPartProvider {	
	
	/** list of supported shape editparts. */
	private Map<EClass, Class> shapeMap = new HashMap<EClass, Class>();
	{
		shapeMap.put( SemanticPackage.eINSTANCE.getLED(), LEDEditPart.class );
		shapeMap.put( SemanticPackage.eINSTANCE.getFlowContainer(), LogicFlowContainerEditPart.class );
		shapeMap.put( SemanticPackage.eINSTANCE.getCircuit(), CircuitEditPart.class );
		shapeMap.put( SemanticPackage.eINSTANCE.getOrGate(), LogicGateEditPart.class );
		shapeMap.put( SemanticPackage.eINSTANCE.getAndGate(), LogicGateEditPart.class );
		shapeMap.put( SemanticPackage.eINSTANCE.getXORGate(), LogicGateEditPart.class );
		shapeMap.put( SemanticPackage.eINSTANCE.getInputTerminal(), TerminalEditPart.class );
		shapeMap.put( SemanticPackage.eINSTANCE.getOutputTerminal(), TerminalEditPart.class );
		shapeMap.put( SemanticPackage.eINSTANCE.getInputOutputTerminal(), TerminalEditPart.class );
	}
	
	/** list of supported connector editparts. */
	private Map<EClass, Class> connectorMap = new HashMap<EClass, Class>();
	{
		connectorMap.put(SemanticPackage.eINSTANCE.getWire(), WireEditPart.class );
	}
	
	/** list of supported shape compartment editparts */
	private Map<String, Class> shapeCompartmentMap = new HashMap<String, Class>();
	{
		shapeCompartmentMap.put(LogicConstants.LOGIC_SHAPE_COMPARTMENT, LogicShapeCompartmentEditPart.class); 
	}
	
	/** list of supported list compartment editparts */
	private Map<String, Class> listCompartmentMap = new HashMap<String, Class>();
	{
		listCompartmentMap.put(LogicConstants.LOGIC_FLOW_COMPARTMENT, LogicFlowCompartmentEditPart.class); 
	}

	/**
	 * Gets a diagram's editpart class.
	 * This method should be overridden by a provider if it wants to provide this service. 
	 * @param view the view to be <i>controlled</code> by the created editpart
	 */
	protected Class getDiagramEditPartClass(View view ) {
		if (view.getType().equals("logic")) { //$NON-NLS-1$
            return(DiagramEditPart.class);
        }
		return null;
	}
	
	/**
	 * Set the editpart class to the editpart mapped to the supplied view's semantic hint.
	 * @see org.eclipse.gmf.runtime.diagram.ui.services.editpart.AbstractEditPartProvider#setConnectorEditPartClass(org.eclipse.gmf.runtime.diagram.ui.internal.view.IConnectorView)
	 */
	protected Class getEdgeEditPartClass(View view) {
		return connectorMap.get(getReferencedElementEClass(view));
	}

	/**
	 * Gets a Node's editpart class.
	 * This method should be overridden by a provider if it wants to provide this service. 
	 * @param view the view to be <i>controlled</code> by the created editpart
	 */
	protected Class getNodeEditPartClass(View view) {
		Class clazz = null;
		String semanticHint = view.getType();
		EClass eClass = getReferencedElementEClass(view);
		clazz = listCompartmentMap.get(semanticHint);
		if(clazz != null) {
			return clazz;
		}
		clazz = shapeCompartmentMap.get(semanticHint);
		if(clazz != null) {
			return clazz;
		}
		clazz = shapeMap.get(eClass);
		return clazz;
	}
}
