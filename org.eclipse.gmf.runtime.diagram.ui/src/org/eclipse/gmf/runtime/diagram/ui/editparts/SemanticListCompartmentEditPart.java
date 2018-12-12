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

package org.eclipse.gmf.runtime.diagram.ui.editparts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationUtil;
import org.eclipse.gmf.runtime.notation.DrawerStyle;
import org.eclipse.gmf.runtime.notation.Filtering;
import org.eclipse.gmf.runtime.notation.FilteringStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.Sorting;
import org.eclipse.gmf.runtime.notation.SortingDirection;
import org.eclipse.gmf.runtime.notation.SortingStyle;
import org.eclipse.gmf.runtime.notation.View;

/**
 * A List compartment Edit part that contains semantic edit parts. A semantic
 * edit part is an edit part that controls a semantic element, it does not
 * control a Notation <code>View<code>. 
 * This list compartment is canonical by default, to turn off the canonical 
 * support just override the isCanonicalEnabled and return false
 * 
 * To convert your List compartment Edit Part to a semantic List compartment, make the Edit Part 
 * extends <code>SemanticListCompartmentEditPart</code> then implement the abstrtact methods:
 *  
 *  semanticChildAdded(EObject child,int index)
 *   Which will be called when a semantic child is added 
 *  And, getSemanticChildrenList()
 *   Which returns a list of all semantic children inside this editpart's model
 *   
 * Then on the EditPart's you add to this list make sure thathasNotationView 
 * returns <code>false</code> 
 *   
 * @author mmostafa
 */

abstract public class SemanticListCompartmentEditPart
	extends ListCompartmentEditPart {
	
	/**
	 * constructor
	 * @param model the mdoel controlled by this edit part
	 */
	public SemanticListCompartmentEditPart(EObject model) {
		super(model);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshChildren()
	 */
	protected void refreshChildren() {
		int i;
		GraphicalEditPart editPart;		
		List modelObjects = getModelChildren();
		List _children = getChildren();
		
		if ((modelObjects.size() == 0) && (_children.size() == 0)) {
			return;
		}
		
		Map modelToEditPart = new HashMap();
		for (i = 0; i < _children.size(); i++) {
			editPart = (GraphicalEditPart)_children.get(i);
			modelToEditPart.put(editPart.basicGetModel(), editPart);
		}

		

		for (i = 0; i < modelObjects.size(); i++) {
			EObject element  = (EObject)modelObjects.get(i);
			//Do a quick check to see if editPart[i] == model[i]
			if (i < _children.size()
				&& (((GraphicalEditPart)_children.get(i)).basicGetModel()) == element)
					continue;

			//Look to see if the EditPart is already around but in the wrong location
			editPart = (GraphicalEditPart)modelToEditPart.get(element);

			if (editPart != null)
				reorderChild (editPart, i);
			else {
				//An editpart for this model doesn't exist yet.  Create and insert one.
				semanticChildAdded(element,i);
			}
		}
		List trash = new ArrayList();
		for (; i < _children.size(); i++)
			trash.add(_children.get(i));
		for (i = 0; i < trash.size(); i++) {
			EditPart ep = (EditPart)trash.get(i);
			removeChild(ep);
		}
	}


	/**
	 * @param child
	 */
	protected void semanticChildRemoved(EObject child){
        if (children==null)
            return;
        for (Iterator iter = children.iterator(); iter.hasNext();) {
			GraphicalEditPart ep = (GraphicalEditPart) iter.next();
			if (ep.basicGetModel().equals(child)){
				removeChild(ep);
				break;
			}
		}
	}
	
	/**
	 * Updates the set of children views so that it
	 * is in sync with the semantic children. This method is called 
	 * in response to notification from the model.
	 * <P>
	 * The update is performed by comparing the exising views with the set of
	 * semantic children returned from {@link #getViewChildren()}. Views whose
	 * semantic element no longer exists are {@link #deleteViews(Iterator) removed}. 
	 * New semantic children have their View {@link  #createViews(List)
	 * created}.  Subclasses must override <code>getSemanticChildren()</code>.
	 * <P>
	 * Unlike <code>AbstractEditPart#refreshChildren()</code>, this refresh will not
	 * reorder the view list to ensure both it and the semantic children are
	 * in the same order since it is possible that this edit policy will handle
	 * a specifc subset of the host's views.  
	 * <P>
	 * The host is refreshed if a view has created or deleted as a result of this
	 * refresh.
	 */
	protected void refreshSemanticChildren() {
		if (!isCanonicalEnabled())
			return ; 
		// Don't try to refresh children if the semantic element
		// cannot be resolved.
		if (resolveSemanticElement() == null) {
			return;		
		}
		
		//
		// current EditParts
		List editPartsChildren = getChildren();
		List modelChildren = getModelChildren();
		if ((editPartsChildren.size() == 0) && (modelChildren.size() == 0)) {
			return;
		}		
		
		List semanticChildren = new ArrayList(modelChildren);
		
		//View viewChild;
		EObject semanticChild;
				
		

		Iterator childrenIT = editPartsChildren.iterator();		
		while( childrenIT.hasNext() ) {
			GraphicalEditPart eP = (GraphicalEditPart)childrenIT.next();
			semanticChild = (EObject)eP.basicGetModel();
			semanticChildren.remove(semanticChild);
		}
		
		//
		// create a view for each remaining semantic element.
		if ( !semanticChildren.isEmpty()) {
			for (Iterator iter = semanticChildren.iterator(); iter.hasNext();) {
				EObject element = (EObject) iter.next();
				semanticChildAdded(element,-1);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.EditPart#activate()
	 */
	public void activate() {
		super.activate();
		refreshSemanticChildren();
	}
	
	/**
	 * This method will be called when a child is added to the
	 * EditPart's model id Canonical is enabled
	 * @param child	the child being added, the index where its edit part should be created 
	 * @param index
	 */
	abstract protected void semanticChildAdded(EObject child,int index);
	
	/**
	 * Returns a list of all semantic children inside this editpart's model
	 * @return
	 */
	abstract protected List getSemanticChildrenList();


	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#handleNotificationEvent(org.eclipse.emf.common.notify.Notification)
	 */
	protected void handleNotificationEvent(Notification event) {
		Object feature = event.getFeature();
		if (hasModelChildrenChanged(event)){
			semanticChildrenListChanged(event);
		}
		else if ((NotationPackage.eINSTANCE.getDrawerStyle_Collapsed() == feature ||
				  NotationPackage.eINSTANCE.getView_Visible() == feature&& 
			       !event.getNewBooleanValue())||
			      (NotationPackage.eINSTANCE.getView_Visible() == feature && 
				   event.getNewBooleanValue())){
			refreshSemanticChildren();
		}
		super.handleNotificationEvent(event);
	}
	
	/**
	 * called by the <code>handlePropertyChangeEvent</code> when the semantic children list 
	 * is changed, then this method will check if the change was add or delete of an element
	 * and calls either <code>semanticChildAdded</code> or <code>semanticChildDeleted</code>
	 * this could be used to implement a canonical list without a canonical edit policy
	 * @param evt
	 */
	protected void semanticChildrenListChanged(Notification event) {
		if (isCanonicalEnabled()){
			if (NotificationUtil.isElementAddedToSlot(event) ||
				NotificationUtil.isMove(event)){
				refreshChildren();
			} else if (NotificationUtil.isElementRemovedFromSlot(event)) {
				if (event.getOldValue() instanceof Collection) {
					 for (Iterator iter = ((Collection) event
	                         .getOldValue()).iterator(); iter.hasNext();) {
	                         EObject removedObject = (EObject) iter.next();
	                         semanticChildRemoved(removedObject);
					 }									
				} else if (event.getOldValue() instanceof  EObject){
					semanticChildRemoved((EObject)event.getOldValue());
				}
		
			}
		} else if (NotificationUtil.isElementRemovedFromSlot(event))
			if (!getChildren().isEmpty()) {
				if (event.getOldValue() instanceof Collection) {
					 for (Iterator iter = ((Collection) event
	                         .getOldValue()).iterator(); iter.hasNext();) {
	                         EObject removedObject = (EObject) iter.next();
	                         semanticChildRemoved(removedObject);
					 }					
				} else if (event.getOldValue() instanceof  EObject) {
					semanticChildRemoved((EObject)event.getOldValue()); 
			    }
		   }
	}
	
	/**
     * indicated if canonical is enabled or not Canonical is disabled if the
     * edit part's view is collapsed or hidden
     * 
     * @return
     */
    protected boolean isCanonicalEnabled() {
        DrawerStyle dstyle = (DrawerStyle) ((View) getModel())
            .getStyle(NotationPackage.Literals.DRAWER_STYLE);
        boolean isCollapsed = dstyle == null ? false
            : dstyle.isCollapsed();

        if (isCollapsed) {
            return false;
        }

        return ((View) getModel()).isVisible();
    }
	
	/**
     * indicated if canonical is enabled or not Canonical is disabled if the
     * edit part's view is collapsed or hidden
     * 
     * @return
     */
    public boolean isCanonicalOn() {
        return isCanonicalEnabled();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ListCompartmentEditPart#getSortedChildren()
	 */
	protected List getSortedChildren() {
		SortingStyle style = (SortingStyle)  ((View)getModel()).getStyle(NotationPackage.eINSTANCE.getSortingStyle());

		if (style != null) {
			Sorting sorting = style.getSorting();
			if (Sorting.NONE_LITERAL == sorting) {
				
				return getSemanticChildrenList();
				
			} else if (Sorting.MANUAL_LITERAL == sorting) {
	
				List allChildren = getSemanticChildrenList();
				// Return the empty list if the model children have not yet been
				// created.
				if (allChildren.isEmpty())
					return allChildren;
				
				List sortedChildren = style.eIsSet(NotationPackage.eINSTANCE.getSortingStyle_SortedObjects())
					? style.getSortedObjects() 
					: Collections.EMPTY_LIST;
	
				List sorted = new ArrayList();
				
				// Get the corresponding views...
				Iterator i = sortedChildren.iterator();
				while(i.hasNext()) {
					EObject eObject = (EObject) i.next();
					if (allChildren.contains(eObject))
						sorted.add(eObject);
				}	
				
				// Add any remaining model children to the end
				for (int j = 0; j < allChildren.size(); j++) {
					EObject element = (EObject) allChildren.get(j);
					if (!sorted.contains(element))
						sorted.add(element);
				}
				
				return sorted;
				
			} else if (Sorting.AUTOMATIC_LITERAL == sorting) {
				Map sortingKeys = style.eIsSet(NotationPackage.eINSTANCE.getSortingStyle_SortingKeys())
					? style.getSortingKeys() 
					: Collections.EMPTY_MAP;
				return getChildrenSortedBy(sortingKeys);
			}
		}
		
		return getSemanticChildrenList();		
	}
	
	/**
	 * Returns the model children sorted by the order specified by Properties.ID_SORTING_KEYS.
	 * This is used to support dynamic list compartment sorting.
	 * @param sortingKeys
	 * @return the model children sorted
	 */
	protected List getChildrenSortedBy(Map sortingKeys) {
		List allChildren = new ArrayList(getSemanticChildrenList());
		// Currently only one sorting key is supported.
		if (sortingKeys != null && !sortingKeys.isEmpty()) {
			Collection keySet = sortingKeys.keySet();
			Iterator i = keySet.iterator();
			String name = (String) i.next();
			SortingDirection direction = (SortingDirection) sortingKeys.get(name);
			
			Comparator comparator = getComparator(name, direction);
			
			if (comparator != null)
				Collections.sort(allChildren, getComparator(name, direction));									
		} 
		return allChildren;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ListCompartmentEditPart#getFilteredChildren()
	 */
	protected List getFilteredChildren() {
		Object model = getModel();
		if (model instanceof View){
			View view = (View)model;
			FilteringStyle style = (FilteringStyle)  view.getStyle(NotationPackage.eINSTANCE.getFilteringStyle());
			if (style != null) {
				Filtering filtering = style.getFiltering();
				if (Filtering.NONE_LITERAL == filtering) {
					
					return Collections.EMPTY_LIST;
					
				} else if (Filtering.MANUAL_LITERAL == filtering) {
					
					List filteredChildren = style.eIsSet(NotationPackage.eINSTANCE.getFilteringStyle_FilteredObjects())
						? style.getFilteredObjects() 
						: Collections.EMPTY_LIST;
					return filteredChildren;	
				
				} else if (Filtering.AUTOMATIC_LITERAL == filtering) {
					List filteringKeys = style.eIsSet(NotationPackage.eINSTANCE.getFilteringStyle_FilteringKeys())
						? style.getFilteringKeys() 
						: Collections.EMPTY_LIST;
					return getChildrenFilteredBy(filteringKeys);
				}
			}
		}
		return Collections.EMPTY_LIST;
	}
	
	public boolean isCanonical() {
        return true;
    }
	
}
