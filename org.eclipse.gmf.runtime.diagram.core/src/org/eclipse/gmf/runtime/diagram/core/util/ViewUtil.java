/******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Mariot Chauvin - Bugzilla 242283
 *    Matt Biggs - Bugzilla 298155
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.util.Log;
import org.eclipse.gmf.runtime.common.core.util.StringStatics;
import org.eclipse.gmf.runtime.common.core.util.Trace;
import org.eclipse.gmf.runtime.diagram.core.internal.DiagramDebugOptions;
import org.eclipse.gmf.runtime.diagram.core.internal.DiagramPlugin;
import org.eclipse.gmf.runtime.diagram.core.internal.DiagramStatusCodes;
import org.eclipse.gmf.runtime.diagram.core.internal.commands.PersistElementCommand;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.core.util.PackageUtil;
import org.eclipse.gmf.runtime.emf.type.core.commands.DestroyElementCommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.LayoutConstraint;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.Style;
import org.eclipse.gmf.runtime.notation.View;

/**
 * provides different utility functions for the notation view
 * 
 * @author mmostafa
 */
public class ViewUtil {

    /**
     * the append index, this is the index you should use to append a view to a
     * container
     */
    public static final int APPEND = -1;

    /**
     * create a list of View Adapters from a Notation View collection
     * 
     * @param views
     *            a collection of Notation <code>View</code>s
     * @return list of <code>EObjectAdapter</code>s
     */
    public static List makeViewsAdaptable(Collection views) {
        List list = new ArrayList();
        Iterator it = views.iterator();
        while (it.hasNext()) {
            list.add(new EObjectAdapter((View) it.next()));
        }
        return list;
    }

    /**
     * move the supplied view from, and all of its parents from the transient
     * collections to the persisted collections. This Api will modify the model
     * and make it dirty, it needs to run within a write action or unchecked
     * operation. A view will get persisted if the following conditions are met
     * <UL>
     * <LI> this method is invoked inside an UNDO interval
     * <LI> the supplied view is in a transient list or owned by a transient
     * container
     * </UL>
     * 
     * @param view
     *            the <code>View</code> to persist
     */
    public static void persistElement(View view) {
        assert null != view : "null view in ViewUtil.persistElement";//$NON-NLS-1$

        TransactionalEditingDomain editingDomain = TransactionUtil
            .getEditingDomain(view);

        if (!view.isMutable()) {
            // get first view needs to get persisted
            View viewToPersist = getTopViewToPersist(view);
            if (viewToPersist != null) {
                // now create a command to persisted the view and exectue it
                PersistElementCommand pvc = new PersistElementCommand(
                    editingDomain, viewToPersist);
                try {
                    pvc.execute(new NullProgressMonitor(), null);
                } catch (ExecutionException e) {
                    Trace.catching(DiagramPlugin.getInstance(),
                        DiagramDebugOptions.EXCEPTIONS_CATCHING,
                        ViewUtil.class, "persistElement", e); //$NON-NLS-1$
                    Log.error(DiagramPlugin.getInstance(),
                        DiagramStatusCodes.IGNORED_EXCEPTION_WARNING, e
                            .getLocalizedMessage(), e);
                }

                CommandResult result = pvc.getCommandResult();
                view = (View) result.getReturnValue();
            }
        }
    }

    /**
     * Returns the top view that should be persisted, starting from the passed
     * view, it could return the passed view itself if it is a transient view,
     * other wise it will check its parent and so on ...
     * 
     * @param view ,
     *            view to start from
     * @return first view needs to get persisted
     */
    static public View getTopViewToPersist(View view) {
        EObject container = view.eContainer();
        // if the view had no container then it can not get persisted
        if (container == null)
            return null;
        // now edges are special case, becuase they do not exist in the
        // children lists, but in the edgs lists
        if (view instanceof Edge) {
            Diagram dContainer = (Diagram) container;
            // always make sure that the feature is set before calling get
            // to avoid creating unwanted EList that will stay in the memory
            // till the model is closed
            if (dContainer.eIsSet(NotationPackage.Literals
                .DIAGRAM__TRANSIENT_EDGES))
                return view;
            else
                return (getTopViewToPersist(dContainer));
        } else if (container instanceof View) {
            View vContainer = (View) container;
            // always make sure that the feature is set before calling get
            // to avoid creating unwanted EList that will stay in the memory
            // till the model is closed
            if (vContainer.eIsSet(NotationPackage.Literals
                .VIEW__TRANSIENT_CHILDREN))
                return view;
            else
                return (getTopViewToPersist(vContainer));
        }
        return null;
    }

    /**
     * Destroys the supplied view notational element and remove any references
     * this class may have to it.
     * 
     * @param view
     *            view to destroy
     */
    public static void destroy(View view) {
        if (view == null)
            return;        
        Iterator it = new ArrayList(view.getChildren()).iterator();        
        while (it.hasNext()) {
            View child = (View) it.next();
            destroy(child);
        }
        DestroyElementCommand.destroy(view);
    }

    /**
     * Returns the container view, or null if the container is not a view or
     * null
     * 
     * @param eObject
     *            a notation view
     * @return the container <code>View</code>
     */
    static public View getContainerView(View eObject) {
        EObject container = eObject.eContainer();
        if (container instanceof View) {
            return (View) container;
        }
        return null;
    }

    /**
     * inserts a child <code>View</code> in a container. the view will be
     * inserted in the persisted collection if the <tt>persisted</tt> flag is
     * <tt>true</tt>; otherwise it will be added to the transied collection.
     * inserting a transient child does not dirty the model, inserting a
     * persisted child will dirty the model
     * 
     * @param containerView
     *            the view's container
     * @param childView
     *            notation <code>View</Code> to insert 
     * @param index the view's position within the container's list
     * @param persisted indicats the persisted state of the view
     *
     */
    public static void insertChildView(View containerView, View childView,
            int index, boolean persisted) {
        if (persisted) {
            insertPersistedElement(containerView, childView, index);
        } else {
            insertTransientElement(containerView, childView);
        }
    }

    /**
     * inserts a child into the transient list, inserting a transient child does
     * not dirty
     * 
     * @param child ,
     *            the child to insert
     * @param container
     *            notational element's container
     */
    static private void insertTransientElement(final View container,
            final View child) {
        if (child instanceof Edge) {
            Diagram diagram = (Diagram) container;
            diagram.insertEdge((Edge) child, false);
        } else {
            container.insertChild(child, false);
        }
        return;
    }

    /**
     * inserts a child into the persisted list
     * 
     * @param container
     *            the notational element's container
     * @param child ,
     *            the child to insert
     * @param index
     *            the notational element's position within the container.
     */
    static private void insertPersistedElement(View container, View child,
            int index) {
        if (child instanceof Edge) {
            Diagram diagram = (Diagram) container;
            if (index == -1)
                diagram.insertEdge((Edge) child);
            else
                diagram.insertEdgeAt((Edge) child, index);
        } else {
            if (index == -1)
                container.insertChild(child);
            else
                container.insertChildAt(child, index);
        }
    }

    /**
     * checks if the passed view is transient or exists in a transient branch
     * 
     * @param view
     *            <code>View</code> to check
     * @return true if transient otherwise false
     */
    static public boolean isTransient(EObject view) {
        EStructuralFeature sFeature = view.eContainingFeature();
        // root element will have a null containing feature
        if (sFeature == null)
            return false;
        if (sFeature.isTransient()) {
            return true;
        }
        EObject container = view.eContainer();
        if (container != null) {
            return isTransient(container);
        }
        return false;
    }

    /**
     * gets a the first child in the passed <code>View</code> that had the
     * same type as the passed semantic hint.
     * 
     * @param view
     *            the view to search inside
     * @param semanticHint
     *            the semantic hint to look for
     * @return the found view or null if none is found
     */
    public static View getChildBySemanticHint(View view, String semanticHint) {
        for (Iterator children = view.getChildren().iterator(); children
            .hasNext();) {
            View child = (View) children.next();
            if (semanticHint.equals(child.getType())) {
                return child;
            }
        }
        return null;
    }

    /**
     * checks if the passed property is supported bythe passed view
     * 
     * @param view
     *            the view to use for the search
     * @param id
     *            the property to look for
     * @return boolean <tt>true</tt> if supported otherwise <tt>false</tt>
     */
    public static boolean isPropertySupported(View view, Object id) {
        if (id instanceof String) {
            EStructuralFeature feature = (EStructuralFeature) PackageUtil
                .getElement((String) id);
            if (feature != null) {
                return isPropertySupported(view, feature, feature
                    .getEContainingClass());
            }
        }
        return false;
    }

    /**
     * checks if the passed feature is supported by the passed view
     * 
     * @param view
     *            the view to use for the search
     * @param feature
     *            the feature to look for
     * @return boolean <tt>true</tt> if supported otherwise <tt>false</tt>
     */
    public static boolean isFeatureSupported(View view,
            EStructuralFeature feature) {
        if (feature != null) {
            return isPropertySupported(view, feature, feature
                .getEContainingClass());
        }
        return false;
    }

    /**
     * checks if the passed feature is supported by the passed view
     * 
     * @param view
     *            the view to use for the search
     * @param feature
     *            the feature to look for
     * @param featureClass
     *            the feature's <code>EClass</code>
     * @return boolean <tt>true</tt> if supported otherwise <tt>false</tt>
     */
    public static boolean isPropertySupported(View view,
            EStructuralFeature feature, EClass featureClass) {
        // check if the id belongs to the view
        if (view.getStyle(featureClass) != null)
            return true;

        if (view instanceof Node) {
            LayoutConstraint constraint = ((Node) view).getLayoutConstraint();
            if (constraint != null && featureClass.isInstance(constraint))
                return true;
        }

        // check if the id belongs to a style owned by the view
        return featureClass.isInstance(view);
    }

    /**
     * Returns the value of the passed feature inside the passed view
     * 
     * @param view
     *            the view to use to get the value
     * @param feature
     *            the feature to use
     * @return the value of the property, or <code>null</code>
     */
    static public final Object getStructuralFeatureValue(View view,
            EStructuralFeature feature) {
        if (feature != null) {
            return ViewUtil.getPropertyValue(view, feature, feature
                .getEContainingClass());
        }
        return null;
    }

    /**
     * Returns the value of the featrue inside a specific EClass within the
     * passed view
     * 
     * @param view
     *            the view to use to get the value
     * @param feature
     *            the featrue to use to get the value
     * @param featureClass
     *            the <code>EClass</code> to use to get the feature
     * @return the value of the feature, or <code>null</code>
     */
    public static Object getPropertyValue(View view,
            EStructuralFeature feature, EClass featureClass) {
        // check if the id belongs to a style owned by the view
        Style style = view.getStyle(featureClass);
        if (style != null)
            return style.eGet(feature);

        if (view instanceof Node) {
            LayoutConstraint constraint = ((Node) view).getLayoutConstraint();
            if (constraint != null && featureClass.isInstance(constraint))
                return constraint.eGet(feature);
        }

        // check if the id belongs to the view
        if (featureClass.isInstance(view))
            return view.eGet(feature);

        return feature.getDefaultValue(); // for extra robustness
    }

    /**
     * Sets the passed feature if possible on the passed view to the passed
     * value.
     * 
     * @param view
     *            the view to set the value on
     * @param feature
     *            the feature to use
     * @param value
     *            the value of the property being set
     */
    public static void setStructuralFeatureValue(View view,
            EStructuralFeature feature, Object value) {
        if (feature != null) {
            ViewUtil.setPropertyValue(view, feature, feature
                .getEContainingClass(), value);
            return;
        }
    }

    /**
     * Sets the passed featrue on the passed EClass inside the passed view to
     * the new value if possible
     * 
     * @param view
     *            the view to set the value on
     * @param feature
     *            the feature to set
     * @param featureClass
     *            <code> EClass </code> that owns the feature
     * @param value
     *            the value of the feature being set
     */
    public static void setPropertyValue(View view, EStructuralFeature feature,
            EClass featureClass, Object value) {
        if (view == null)
            return;
        // check if the id belongs to a style owned by the view
        Style style = view.getStyle(featureClass);
        if (style != null) {
            style.eSet(feature, value);
            return;
        }

        if (view instanceof Node) {
            Node node = (Node) view;
            LayoutConstraint constraint = node.getLayoutConstraint();
            if (constraint != null & featureClass.isInstance(constraint)) {
                constraint.eSet(feature, value);
                return;
            }
        }

        // check if the id belongs to the view
        if (featureClass.isInstance(view)) {
            view.eSet(feature, value);
            return;
        }
    }

    /**
     * resolves the passed <code>View<code>'s semantic element, and returns it.
     * If the semantic element is unresolvable the method will returns <code>null</code>
     * @param view the view to use to get the semantic element
     * @return the semanticelement or null if there is no semantic element or if it is unresolvable
     */
    public static EObject resolveSemanticElement(View view) {
        EObject element = view.getElement();
        if (element != null && element.eIsProxy()) {
            TransactionalEditingDomain domain = TransactionUtil
                .getEditingDomain(view);
            if (domain != null) {
                return EMFCoreUtil.resolve(domain, element);
            }
        }
        return element;
    }

    /**
     * resolves the passed element, and returns it. If the element is
     * unresolvable the method will returns <code>null</code>
     * 
     * @param the
     *            element to resolve
     * @return the element or null if it is unresolvable
     */
    public static EObject resolve(EObject object) {
        if (object != null && object.eIsProxy()) {
            TransactionalEditingDomain domain = TransactionUtil
                .getEditingDomain(object);
            if (domain != null)
                return EMFCoreUtil.resolve(domain, object);
        }
        return object;
    }

    /**
     * gets the <code>View</code>'s semantic element Class Id, this could be
     * used to check the semantic element type
     * 
     * @param view
     *            the owner of the semantic element
     * @return the semantic element class Id
     */
    public static String getSemanticElementClassId(View view) {
        EObject element = view.getElement();
        return element == null ? null
            : PackageUtil.getID(EMFCoreUtil.getProxyClass(element));
    }

    /**
     * gets all the <code>Edge</code>'s whose source is this view
     * 
     * @param view
     *            the view to use
     * @return List the edges list
     */
    public static List getSourceConnections(View view) {
        if (!view.eIsSet(NotationPackage.Literals.VIEW__SOURCE_EDGES))
            return Collections.EMPTY_LIST;
        return view.getSourceEdges();
    }
    
    /**
     * gets all the <code>Edge</code>'s whose target is this view
     * 
     * @param view
     *            the view to use
     * @return List the edges list
     */
    public static List getTargetConnections(View view) {
        if (!view.eIsSet(NotationPackage.Literals.VIEW__TARGET_EDGES))
            return Collections.EMPTY_LIST;
        return view.getTargetEdges();
    }
    
    /**
     * gets all the <code>Edge</code>'s whose source is this view
     * 
     * @param view
     *            the view to use
     * @return List the edges list
     */
     public static List getSourceConnectionsConnectingVisibleViews(View view) {
        if (!view.eIsSet(NotationPackage.Literals.VIEW__SOURCE_EDGES))
            return Collections.EMPTY_LIST;
        List sourceConnections = new ArrayList();
        Iterator iter = view.getSourceEdges().iterator();
        while (iter.hasNext()) {
            Edge edge = (Edge)iter.next();
            View target = edge.getTarget();
            if (edge.isVisible() && isVisible(target)){
                sourceConnections.add(edge);
            }
            
        }
        return sourceConnections;
     }
    
    

     private static boolean isVisible(View target) {
        if (target != null && target.isVisible()){
            EObject parent = target.eContainer();
            if (parent instanceof View){
                return isVisible((View)parent);
            }
            return true;
        }
        return false;
    }
     
    /**
     * gets all the <code>Edge</code>'s whose target is this view
     * 
     * @param view
     *            the view to use
     * @return List the edges list
     */
     public static List getTargetConnectionsConnectingVisibleViews(View view) {
        if (!view.eIsSet(NotationPackage.Literals.VIEW__TARGET_EDGES))
            return Collections.EMPTY_LIST;
        List targteConnections = new ArrayList();
        Iterator iter = view.getTargetEdges().iterator();
        while (iter.hasNext()) {
            Edge edge = (Edge)iter.next();
            View source = edge.getSource();
            if (edge.isVisible() && isVisible(source)){
                targteConnections.add(edge);
            }
        }
        return targteConnections;
     }

    /**
     * return eClass Name of the view's semantic element, this method works only
     * if the semantic element is a NameElement, otherwise it will return an
     * Empty String
     * 
     * @param view
     *            the view object
     * @return the eClass name
     */
    public static String getSemanticEClassName(View view) {
        EObject eObject = view.getElement();
        if (eObject != null)
            return PackageUtil.getID(EMFCoreUtil.getProxyClass(eObject));
        return ""; //$NON-NLS-1$
    }

    /**
     * returns the unique GUID of the view
     * 
     * @param view
     *            the view
     * @return String the GUID of a view (constant)
     */
    public static String getIdStr(View view) {
    	Resource resource = view.eResource();
    	if( resource instanceof XMLResource ) {
	        String id = ((XMLResource) resource).getID(view);
	        if (id != null) {
	            return id;
	        }
    	}

        // Remain compatible with previous behavior.
        return StringStatics.BLANK;
    }

    /**
     * reorders the child at the oldIndex to the newIndex
     * 
     * @param container
     *            the view's container
     * @param child
     *            the child to reposition
     * @param newIndex
     *            (zero-based)
     * @throws IndexOutOfBoundsException
     *             if index is out of bounds
     * @throws IllegalArgumentException
     *             if the child is not contianed by the container, or if the new
     *             position is the <code>ViewUtil.APPEND</code>position
     */
    static public void repositionChildAt(View container, View child,
            int newIndex) {
        ViewUtil.persistElement(child);
        if (child.eContainer() != container)
            throw new IllegalArgumentException(
                "child is not an existing child of the view"); //$NON-NLS-1$
        if (newIndex == APPEND)
            throw new IllegalArgumentException(
                "append position is not allowed for reposition"); //$NON-NLS-1$
        container.removeChild(child);
        container.insertChildAt(child, newIndex);
    }

    /**
     * returns the first child whose id matched the given id
     * 
     * @param view
     *            the view to search in
     * @param idStr
     *            the child's id
     * @return View the first matching child or null if no one was found
     */
    static public View getChildByIdStr(View view, String idStr) {
        for (Iterator children = view.getChildren().iterator(); children
            .hasNext();) {
            View child = (View) children.next();
            Resource resource = child.eResource();
            if( resource instanceof XMLResource ) {
	            if (idStr.equals(((XMLResource) resource).getID(child))) {
	                return child;
	            }
            }
        }
        return null;
    }

    /**
     * Sets the property with the given id if possible on the passed view to the
     * passed value.
     * 
     * @param view
     *            the view to set the value on
     * @param id
     *            the id of the property being set
     * @param value
     *            the value of the property being set
     * @deprecated use
     *             {@link ViewUtil#setStructuralFeatureValue(View, EStructuralFeature, Object}}
     *             instead
     */
    public static void setPropertyValue(View view, Object id, Object value) {
        if (id instanceof String) {
            EStructuralFeature feature = (EStructuralFeature) PackageUtil
                .getElement((String) id);
            if (feature != null) {
                ViewUtil.setPropertyValue(view, feature, feature
                    .getEContainingClass(), value);
                return;
            }
        }
    }

    /**
     * Returns the value of the property with the given id inside the passed
     * view
     * 
     * @param view
     *            the view to use to get the value
     * @param id
     *            the id of the property to get
     * @return the value of the property, or <code>null</code>
     * @deprecated use
     *             {@link ViewUtil#getStructuralFeatureValue(View, EStructuralFeature)}
     *             instead
     */
    static public final Object getPropertyValue(View view, Object id) {
        if (id instanceof String) {
            EStructuralFeature feature = (EStructuralFeature) PackageUtil
                .getElement((String) id);
            if (feature != null) {
                return ViewUtil.getPropertyValue(view, feature, feature
                    .getEContainingClass());
            }
        }
        return null;
    }
    
    /**
	 * Goes up through the containers of the passed in <code>EObject</code>
	 * and returns the first container that is a <code>View</code>
	 * 
	 * @param obj
	 *            the <code>EObject</code>
	 * @return the first found <code>View</code> container of the object
	 */
	static public View getViewContainer(EObject obj) {
		while (obj != null) {
			if (obj.eContainer() instanceof View)
				return (View) obj.eContainer();
			obj = obj.eContainer();
		}
		return null;
	}

	/**
	 * Constructs a set of all source and target edges from a view and all its
	 * children down to the leaves of a view hierarchy
	 * 
	 * @param view
	 * @param allEdges
	 */
	static public void  getAllRelatedEdgesForView(View view, Set<Edge> allEdges) {
		allEdges.addAll(ViewUtil.getSourceConnections(view));
		allEdges.addAll(ViewUtil.getTargetConnections(view));
		for (Iterator itr = view.getChildren().iterator(); itr.hasNext();) {
			Object obj = itr.next();
			if (obj instanceof View) {
				getAllRelatedEdgesForView((View)obj, allEdges);
			}
		}
	}
	
	/**
	 * Constructs a set of all source and target edges from a list of view and all their
	 * children down to the leaves of a view hierarchy
	 * 
	 * @param views
	 * @param allEdges
	 */
	static public void getAllRelatedEdgesFromViews(List views, HashSet<Edge> allEdges) {
		for (Iterator itr = views.iterator(); itr.hasNext();) {
			Object obj = itr.next();
			if (obj instanceof View) {
				getAllRelatedEdgesForView((View)obj, allEdges);
			}
		}
	}
	
	static public Set<Edge> getAllInnerEdges(View view) {
		Set<View> allViews = new HashSet<View>();
		Set<Edge> edges = new HashSet<Edge>();
		Set<Edge> edgesConnectingViews = new HashSet<Edge>();
		getAllNestedViews(view, allViews);
		for (View v : allViews) {
			getAllEdgesFromView(v, edges);
		}
		Stack<Edge> connectionsPath = new Stack<Edge>();
		/*
		 * Create a set of connections constained within the given editpart
		 */
		while (!edges.isEmpty()) {
			/*
			 * Take the first connection and check whethe there is a path
			 * through that connection that leads to the target contained within
			 * the given editpart
			 */
			Edge edge = edges.iterator().next();
			edges.remove(edge);
			connectionsPath.add(edge);
			
			/*
			 * Initialize the target for the current path
			 */
			View target = edge.getTarget();
			while(edges.contains(target)) {
				/*
				 * If the target end is a connection, check if it's one of the
				 * connection's whose target is a connection and within the
				 * given editpart. Append it to the path if it is. Otherwise
				 * check if the target is within the actual connections or nodes
				 * contained within the given editpart
				 */
				Edge targetEdge = (Edge) target;
				edges.remove(targetEdge);
				connectionsPath.add(targetEdge);
				
				/*
				 * Update the target for the new path
				 */
				target = targetEdge.getTarget();
			}
			
			/*
			 * The path is built, check if it's target is a node or a connection
			 * contained within the given editpart
			 */
			if (allViews.contains(target) || edgesConnectingViews.contains(target)) {
				edgesConnectingViews.addAll(connectionsPath);
			}
			connectionsPath.clear();
		}
		return edgesConnectingViews;
	}
	
	static private void getAllNestedViews(View view, Set<View> allViews) {
		for (View childView : (List<View>) view.getChildren() ) {
			getAllNestedViews(childView, allViews);
			allViews.add(childView);
		}
	}
	
	static private void getAllEdgesFromView(View view, Set<Edge> edges) {
		for (Edge e : (List<Edge>) view.getSourceEdges()) {
			getAllEdgesFromView(e, edges);
			edges.add(e);
		}
	}
	
}
