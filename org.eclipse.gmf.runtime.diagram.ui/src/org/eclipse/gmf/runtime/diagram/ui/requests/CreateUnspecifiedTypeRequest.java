/******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.requests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.commands.DeferredCreateConnectionViewCommand;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest.ViewAndElementDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.util.INotationType;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.notation.Node;

/**
 * This request encapsulates a list of <code>CreateViewRequest</code> or
 * <code>CreateViewAndElementRequest</code> for each type that this tool
 * supports. Each method in <code>CreateRequest</code> that is called to
 * configure the request in <code>CreationTool</code> is propagated to each
 * individual request.
 * 
 * @author cmahoney
 * @author Mickael Istria, BonitaSoft - bug 288695
 */
public class CreateUnspecifiedTypeRequest
	extends CreateRequest {

	/**
	 * List of element types of which one will be created (of type
	 * <code>IElementType</code>).
	 */
	protected List elementTypes;

	/**
	 * A map containing the <code>CreateRequest</code> for each element type.
	 */
	protected Map requests = new HashMap();

	/** The result to be returned from which the new views can be retrieved. */
	private List newObjectList = new ArrayList();

	/**
	 * The hint used to find the appropriate preference store from which general
	 * diagramming preference values for properties of shapes, connections, and
	 * diagrams can be retrieved. This hint is mapped to a preference store in
	 * the {@link DiagramPreferencesRegistry}.
	 */
	private PreferencesHint preferencesHint;
	
	/**
	 * Creates a new <code>CreateUnspecifiedTypeRequest</code>.
	 * 
	 * @param elementTypes
	 *            List of element types of which one will be created (of type
	 *            <code>IElementType</code>).
	 * @param preferencesHint
	 *            The preference hint that is to be used to find the appropriate
	 *            preference store from which to retrieve diagram preference
	 *            values. The preference hint is mapped to a preference store in
	 *            the preference registry <@link DiagramPreferencesRegistry>.
	 */
	public CreateUnspecifiedTypeRequest(List elementTypes, PreferencesHint preferencesHint) {
		super();
		this.elementTypes = elementTypes;
		this.preferencesHint = preferencesHint;
		createRequests();
	}

	/**
	 * Creates a <code>CreateViewRequest</code> or
	 * <code>CreateViewAndElementRequest</code> for each creation hint and
	 * adds it to the map of requests.
	 */
	protected void createRequests() {
		for (Iterator iter = elementTypes.iterator(); iter.hasNext();) {
			IElementType elementType = (IElementType) iter.next();

			Request request = null;
			if (elementType instanceof INotationType) {
				ViewDescriptor viewDescriptor = new ViewDescriptor(null,
					Node.class, ((INotationType) elementType)
						.getSemanticHint(), getPreferencesHint());
				request = new CreateViewRequest(viewDescriptor);
			} else if (elementType instanceof IHintedType) {
				ViewAndElementDescriptor viewDescriptor = new ViewAndElementDescriptor(
					new CreateElementRequestAdapter(new CreateElementRequest(
						elementType)), Node.class,
					((IHintedType) elementType).getSemanticHint(), getPreferencesHint());
				request = new CreateViewAndElementRequest(viewDescriptor);
				request.setExtendedData(getExtendedData());
			} else {
				ViewAndElementDescriptor viewDescriptor = new ViewAndElementDescriptor(
					new CreateElementRequestAdapter(new CreateElementRequest(
						elementType)), Node.class, getPreferencesHint());
				request = new CreateViewAndElementRequest(viewDescriptor);
				request.setExtendedData(getExtendedData());
			}

			request.setType(getType());
			requests.put(elementType, request);
		}
	}

	/**
	 * Returns the <code>CreateRequest</code> for the element type passed in.
	 * 
	 * @param creationHint
	 * @return the <code>CreateRequest</code>
	 */
	public CreateRequest getRequestForType(IElementType creationHint) {
		if (requests != null) {
			return (CreateRequest) requests.get(creationHint);
		}
		return null;
	}

	/**
	 * Returns the list of element types.
	 * 
	 * @return Returns the list of element types.
	 */
	public List getElementTypes() {
		return elementTypes;
	}
	
	/**
	 * Gets the preferences hint that is to be used to find the appropriate
	 * preference store from which to retrieve diagram preference values. The
	 * preference hint is mapped to a preference store in the preference
	 * registry <@link DiagramPreferencesRegistry>.
	 * 
	 * @return the preferences hint
	 */
	protected PreferencesHint getPreferencesHint() {
		return preferencesHint;
	}

	public void setLocation(Point location) {
		if (requests != null) {
			for (Iterator iter = requests.values().iterator(); iter.hasNext();) {
				CreateRequest request = (CreateRequest) iter.next();
				request.setLocation(location);
			}
		}
		super.setLocation(location);
	}

	public void setType(Object type) {
		if (requests != null) {
			for (Iterator iter = requests.values().iterator(); iter.hasNext();) {
				CreateRequest request = (CreateRequest) iter.next();
				request.setType(type);
			}
		}
		super.setType(type);
	}

	public void setSize(Dimension size) {
		if (requests != null) {
			for (Iterator iter = requests.values().iterator(); iter.hasNext();) {
				CreateRequest request = (CreateRequest) iter.next();
				request.setSize(size);
			}
		}
		super.setSize(size);
	}

	public void setFactory(CreationFactory factory) {
		if (requests != null) {
			for (Iterator iter = requests.values().iterator(); iter.hasNext();) {
				CreateRequest request = (CreateRequest) iter.next();
				request.setFactory(factory);
			}
		}
		super.setFactory(factory);
	}

	public void setExtendedData(Map map) {
		if (requests != null) {
			for (Iterator iter = requests.values().iterator(); iter.hasNext();) {
				CreateRequest request = (CreateRequest) iter.next();
				request.setExtendedData(map);
			}
		}
		super.setExtendedData(map);
	}

	/**
	 * Sets the new object to be returned. Must be of the type expected in
	 * getNewObjectType().
	 * @param theNewObjects
	 */
	public void setNewObject(Collection theNewObjects) {
		newObjectList.addAll(theNewObjects);
	}

	/**
	 * See bug 288695
	 * 
	 * if there is only one {@link CreateRequest}, returns this {@link CreateRequest#getNewObject()},
	 * else, returns a list containing a single {@link IAdaptable} that delegates to the
	 * {@link CreateRequest#getNewObject()} after the command is executed.
	 * It is safe to use the return {@link IAdaptable} in a {@link DeferredCreateConnectionViewCommand}.
	 */
	public Object getNewObject() {
		if (newObjectList.isEmpty()) {
			if (requests.size() == 1) {
	            Object createRequest = requests.values().toArray()[0];
	            if (createRequest instanceof CreateRequest) {
	                return ((CreateRequest) createRequest).getNewObject();
	            }
	        } else if (requests.size() > 1) {
	        	/* See bug 288695 */
	        	CreateUnspecifiedAdapter adapter = new CreateUnspecifiedAdapter();
	        	for (Object request : requests.values()) {
	        		if (request instanceof CreateRequest) {
	        			adapter.add((CreateRequest)request);
	        		}
	        	}
	        	List<IAdaptable> newObjects = new ArrayList<IAdaptable>();
	        	newObjects.add(adapter);
	        	return newObjects;
	        }
		}
        return newObjectList;
    }

	public Object getNewObjectType() {
		return List.class;
	}
}