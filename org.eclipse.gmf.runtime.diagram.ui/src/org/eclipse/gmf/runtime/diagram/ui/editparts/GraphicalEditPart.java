/******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.requests.TargetRequest;
import org.eclipse.gmf.runtime.common.core.util.IAdaptableSelection;
import org.eclipse.gmf.runtime.common.core.util.Log;
import org.eclipse.gmf.runtime.common.core.util.Trace;
import org.eclipse.gmf.runtime.common.ui.services.action.filter.ActionFilterService;
import org.eclipse.gmf.runtime.common.ui.services.parser.CommonParserHint;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DecorationEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.PropertyHandlerEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.SemanticEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIDebugOptions;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIPlugin;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIStatusCodes;
import org.eclipse.gmf.runtime.diagram.ui.internal.commands.ToggleCanonicalModeCommand;
import org.eclipse.gmf.runtime.diagram.ui.internal.editparts.DummyEditPart;
import org.eclipse.gmf.runtime.diagram.ui.internal.ruler.SnapToHelperUtil;
import org.eclipse.gmf.runtime.diagram.ui.internal.services.editpolicy.EditPolicyService;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramColorRegistry;
import org.eclipse.gmf.runtime.diagram.ui.label.ILabelDelegate;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramEditDomain;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.EditCommandRequestWrapper;
import org.eclipse.gmf.runtime.diagram.ui.tools.DragEditPartsTrackerEx;
import org.eclipse.gmf.runtime.diagram.ui.util.EditPartUtil;
import org.eclipse.gmf.runtime.draw2d.ui.figures.FigureUtilities;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.gef.ui.internal.editpolicies.GraphicalEditPolicyEx;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.FillStyle;
import org.eclipse.gmf.runtime.notation.FontStyle;
import org.eclipse.gmf.runtime.notation.LineStyle;
import org.eclipse.gmf.runtime.notation.LineType;
import org.eclipse.gmf.runtime.notation.LineTypeStyle;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.IActionFilter;

/**
 * @author melaasar,mmostafa
 * 
 * The root of all component graphical edit parts that own a view as a model
 */
public abstract class GraphicalEditPart
    extends AbstractGraphicalEditPart
    implements IGraphicalEditPart, IAdaptableSelection, NotificationListener {
  
    /** A map of listener filters ids to filter data */
    private Map listenerFilters;
    
    /** Used for accessibility. */
    protected AccessibleEditPart accessibleEP;

    /** Used for registering and unregistering the edit part */
    protected String elementGuid;

    /**
     * Cache the editing domain after it is retrieved.
     */
    private TransactionalEditingDomain editingDomain;

    /**
     * Cache the font data when a font is created so that it can be
     * disposed later.
     */
    private FontData cachedFontData;
    
    /**
     * Flag to indicate if the edit part is in edit mode
     * @since 1.2
     */
    final protected static int FLAG__IS_EDITABLE = MAX_FLAG << 1;
    
    /**
     * Flag to indicate automatic updates of source/target connections visibility triggered by the change
     * of this editpart's visibility
     * @since 1.2
     */
    final protected static int FLAG__AUTO_CONNECTIONS_VISIBILITY = MAX_FLAG << 2;
    
    /**
     * The left-most bit that is reserved by this class for setting flags. Subclasses may
     * define additional flags starting at <code>(GRAPHICAL_EDIT_PART__MAX_FLAG << 1)</code>.
     * @since 1.2
     */
    final protected static int GRAPHICAL_EDIT_PART__MAX_FLAG = FLAG__AUTO_CONNECTIONS_VISIBILITY;
    
    /**
     * Create an instance.
     * 
     * @param model
     *            the underlying model.
     */
    public GraphicalEditPart(EObject model) {
        setModel(model);
        setFlag(FLAG__IS_EDITABLE, true);
        setFlag(FLAG__AUTO_CONNECTIONS_VISIBILITY, true);
    }
    
    /** Registers this editpart to recieve notation and semantic events. */
    public void activate() {
        if (isActive()) {
            return;
        }
        addNotationalListeners();

        EObject semanticElement;
        EObject semanticProxy;
        if (hasNotationView()) {
            semanticProxy = ((View) super.getModel()).getElement();
            if ((semanticProxy==null)||semanticProxy.eIsProxy()) {
                semanticElement = null;
            } else {
                semanticElement = semanticProxy;
            }
        } else {
            semanticProxy = (EObject) basicGetModel();
            if ((semanticProxy!=null) && semanticProxy.eIsProxy()) {
                semanticElement = EMFCoreUtil.resolve(getEditingDomain(),
                    semanticProxy);
            } else {
                semanticElement = semanticProxy;
            }
        }

        if (semanticElement != null)
            addSemanticListeners();
        else if (semanticProxy != null) {
            addListenerFilter("SemanticProxy", this, semanticProxy); //$NON-NLS-1$
        }
        GraphicalEditPart.super.activate();

    }

    /**
     * Adds a listener filter by adding the given listener to a passed notifier
     * 
     * @param filterId
     *            A unique filter id (within the same editpart instance)
     * @param listener
     *            A listener instance
     * @param element
     *            An element to add the listener to
     */
    protected void addListenerFilter(String filterId,
            NotificationListener listener,
            EObject element) {
        if (element == null)
            return;

        DiagramEventBroker diagramEventBroker = getDiagramEventBroker();
        if (diagramEventBroker != null) {
        	assert filterId != null;
        	assert listener != null;
            if (listenerFilters == null)
                listenerFilters = new HashMap();
            diagramEventBroker.addNotificationListener(element,listener);
            listenerFilters.put(filterId.intern(), new Object[] {element, listener});
        }
    }
    
    /**
     * Adds a listener filter by adding the given listener to a passed notifier
     * 
     * @param filterId
     *            A unique filter id (within the same editpart instance)
     * @param listener
     *            A listener instance
     * @param element
     *            An element to add the listener to
     */
    protected void addListenerFilter(String filterId,
            NotificationListener listener,
            EObject element,
            EStructuralFeature feature) {
        if (element == null)
            return;
                
        DiagramEventBroker diagramEventBroker = getDiagramEventBroker();
        if (diagramEventBroker != null) {
        	assert filterId != null;
        	assert listener != null;
            if (listenerFilters == null)
                listenerFilters = new HashMap();

            diagramEventBroker.addNotificationListener(element,feature,listener);

            listenerFilters.put(filterId.intern(), new Object[] {element,feature, listener});
        }
    }

    /** Creates a connection editpart. */
    final protected org.eclipse.gef.ConnectionEditPart createConnection(
            Object connectionView) {
        return (org.eclipse.gef.ConnectionEditPart) createChild(connectionView);
    }

    /**
     * Overridden to support editpolicies installed programmatically and via the
     * <code>EditPolicyService</code>. Subclasses should override
     * <code>createDefaultEditPolicies()</code>.
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    final protected void createEditPolicies() {
        createDefaultEditPolicies();
        EditPolicyService.getInstance().createEditPolicies(this);
    }

    /**
     * Should be overridden to install editpolicies programmatically.
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    protected void createDefaultEditPolicies() {
        installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE,
            new SemanticEditPolicy());
        installEditPolicy(EditPolicyRoles.PROPERTY_HANDLER_ROLE,
            new PropertyHandlerEditPolicy());
        installEditPolicy(EditPolicyRoles.DECORATION_ROLE,
            new DecorationEditPolicy());
    }

    public void deactivate() {
        if ( !isActive() ) {
            return;
        }
        removeNotationalListeners();
        removeSemanticListeners();
        if (listenerFilters != null ) {
            for (Iterator i = listenerFilters.keySet().iterator(); i.hasNext();) {
                Object[] obj = (Object[]) listenerFilters.get(i.next());
                if (obj.length>2){
                    getDiagramEventBroker().
                        removeNotificationListener((EObject)obj[0],(EStructuralFeature) obj[1],(NotificationListener) obj[2]);
                }else {
                    getDiagramEventBroker().removeNotificationListener((EObject) obj[0],(NotificationListener) obj[1]);
                }
            }
        }
        super.deactivate();
    }

    public void removeNotify() {
        
        View view = getNotationView();
        if (view != null && !view.isVisible() && getFlag(FLAG__AUTO_CONNECTIONS_VISIBILITY)){
            setConnectionsVisibility(false);
        }
        
        super.removeNotify();
        
        if (cachedFontData != null) {
            getResourceManager().destroyFont(
                FontDescriptor.createFrom(cachedFontData));
            cachedFontData = null;
        }

    }

    /**
     * executes the passed <code>Command</code>
     * @param command the command to exectue
     */
    protected void executeCommand(Command command) {
        getViewer().getEditDomain().getCommandStack().execute(command);
    }
    
    /**
     * Access the model member variable
     * @return
     */
    final protected Object basicGetModel(){
        return super.getModel();
    }

    /** Adds the ability to adapt to the edit part's view class. */
    public Object getAdapter(Class key) {       

        // Adapt to IActionFilter
        if (key == IActionFilter.class) {
            return ActionFilterService.getInstance();
        }

        if (View.class.isAssignableFrom(key)) {
            Object _model = getModel();
            if (key.isInstance(_model))
                return _model;
            else
                return null;
        }

        if (key == SnapToHelper.class) {
            return SnapToHelperUtil.getSnapHelper(this);
        }

        Object model = basicGetModel();
        if (model != null &&
            EObject.class.isAssignableFrom(key)) {
            // Adapt to semantic element
            EObject semanticObject = null;
            if (hasNotationView()){
                semanticObject = ViewUtil.resolveSemanticElement((View)model);
            }
            else{
                EObject element = (EObject)model;
                if (element.eIsProxy()){
                    semanticObject = EMFCoreUtil.resolve(getEditingDomain(), element);
                }
            }
            if ((semanticObject!=null) && key.isInstance(semanticObject)) {
                return semanticObject;
            }
            else if (key.isInstance(model)){
                return model;
            }
            
        }

        // Delegate
        return super.getAdapter(key);
    }

    /**
     * Utility method to get the primary view and then query for a child based on the
     * semantic hint
     * 
     * @param semanticHint <code>String</code> that is the hint that can retrieved on the getType
     * method of <code>View</code>
     * @return <code>IGraphicalEditPart</code> that matches with the given semanticHint
     */
    public IGraphicalEditPart getChildBySemanticHintOnPrimaryView(String semanticHint) {
        View primaryView = getPrimaryView();
        View childView = ViewUtil.getChildBySemanticHint(primaryView, semanticHint);
        if (childView != null)
            return  (IGraphicalEditPart)getViewer().getEditPartRegistry().get(childView);

        return null;
    }
    
    /**
     * Method getChildBySemanticHint.
     * 
     * @param semanticHint
     * @return IGraphicalEditPart
     */
    public IGraphicalEditPart getChildBySemanticHint(String semanticHint) {
        View view;
        if (hasNotationView() && (view = (View) super.getModel()) != null) {
            view = ViewUtil.getChildBySemanticHint(view,semanticHint);
            if (view != null){
                IGraphicalEditPart ep =   (IGraphicalEditPart)getViewer().getEditPartRegistry().get(view);
                // TODO remove this code after the clients change there code to 
                // be aware of the on demand editpart creation
                if (ep == null){
                    // the ep had not been created yet, create a dummy one 
                    ep  =  new DummyEditPart(view);
                }
                return ep;
            }
        }
        return null;
    }
    
    /**
     * Method getChildViewBySemanticHint.
     * 
     * @param semanticHint
     * @return IGraphicalEditPart
     */
    public View getChildViewBySemanticHint(String semanticHint) {
        View view;
        if (hasNotationView() && (view = (View) super.getModel()) != null) {
            return ViewUtil.getChildBySemanticHint(view, semanticHint);
        }
        return null;
    }

    
    /** counter that tracs the recursive depth of the getCommand() method. */
    private static volatile int GETCOMMAND_RECURSIVE_COUNT = 0;
    
    /** A list of editparts who's canonical editpolicies are to be temporarily disabled. */
    private static Set _disableCanonicalEditPolicyList = new HashSet();
    

    /** Return a command for the supplied request. */
    public Command getCommand(Request _request) {
        if ( !isEditModeEnabled() ) {
            if (RequestConstants.REQ_OPEN.equals(_request.getType())) {
                //allowed, continue
            }
            else {
                return UnexecutableCommand.INSTANCE;
            }           
        }
        
        Command cmd = null;
        try {
            GETCOMMAND_RECURSIVE_COUNT++;
            final Request request = _request;
            try {
                cmd = (Command) getEditingDomain().runExclusive(
                    new RunnableWithResult.Impl() {

                        public void run() {
                            setResult(GraphicalEditPart.super
                                .getCommand(request));
                        }
                    });
            } catch (InterruptedException e) {
                Trace.catching(DiagramUIPlugin.getInstance(),
                    DiagramUIDebugOptions.EXCEPTIONS_CATCHING, getClass(),
                    "getCommand", e); //$NON-NLS-1$
                Log.error(DiagramUIPlugin.getInstance(),
                    DiagramUIStatusCodes.IGNORED_EXCEPTION_WARNING,
                    "getCommand", e); //$NON-NLS-1$
            }
            

            if ( cmd != null ) {
                _disableCanonicalEditPolicyList.addAll(disableCanonicalFor(_request));
            }
            
            GETCOMMAND_RECURSIVE_COUNT--;
            
            if ( GETCOMMAND_RECURSIVE_COUNT == 0 ) {
                if ( cmd != null 
                        && !_disableCanonicalEditPolicyList.isEmpty() ) {
                    CompoundCommand cc = new CompoundCommand();
                    cc.setLabel( cmd.getLabel() );
                    ToggleCanonicalModeCommand tcmd = 
                        ToggleCanonicalModeCommand.getToggleCanonicalModeCommand(_disableCanonicalEditPolicyList, false);
                    cc.add( tcmd );
                    cc.add( cmd );
                    ToggleCanonicalModeCommand tcmd2 = ToggleCanonicalModeCommand.getToggleCanonicalModeCommand(tcmd, true);
                    if (tcmd2 != null) {
                        tcmd2.setDomain(getEditingDomain());
                    }
                    cc.add( tcmd2 );
                    _disableCanonicalEditPolicyList.clear();
                    return cc.unwrap();
                }
            }
        }
        catch( RuntimeException t ) {
            GETCOMMAND_RECURSIVE_COUNT = 0;
            throw t;
        }
        return cmd;
        
    }
    
    /**
     * Return a list of editparts who's canonical editpolicies should be disabled
     * prior to executing the commands associated to the supplied request.
     * This implementation will return the editpart honoring a <code>SemanticWrapperRequest</code>
     * and a <code>CreateConnectionViewRequest</code>'s source and target editparts.
     *
     * @param request a request that has returned a command.
     * @return list of editparts.
     */
    protected Collection disableCanonicalFor( final Request request ) {
        //
        // not the most generic of solution; however, it will have to do for now...
        //
        // Alternate solutions
        // 1. common request interface on all the requests
        //  IRequest#getAffectedEditParts
        //
        // 2. Traverse down the command and collect of the ICommand#getAffectedObjects()
        //  -- this requires that all our commands properly set this value.
        
        Set hosts = new HashSet();
        if ( (request instanceof EditCommandRequestWrapper)  
                || request instanceof TargetRequest
                || request instanceof DropRequest ) {
            hosts.add(this);
            hosts.add(getParent());
        }
        if((request instanceof ReconnectRequest)) {
            ReconnectRequest reconnect = (ReconnectRequest)request;
            hosts.add(this);
            hosts.add(getParent());
            if(reconnect.getTarget() != null) {
                EditPart target  = reconnect.getTarget();
                addEditPartAndParent(hosts, target);
            }
            if(reconnect.getConnectionEditPart() != null) {
                org.eclipse.gef.ConnectionEditPart connectionEditPart = reconnect.getConnectionEditPart();
                if(connectionEditPart.getSource() != null) {
                    EditPart srcEP = connectionEditPart.getSource();
                    addEditPartAndParent(hosts, srcEP);
                }
                if(connectionEditPart.getTarget() != null) {
                    EditPart trgEP = connectionEditPart.getTarget();
                    addEditPartAndParent(hosts, trgEP);
                }
            }
        }
        if ((request instanceof CreateConnectionRequest) ) {
            CreateConnectionRequest ccvr = (CreateConnectionRequest)request;
            hosts.add(this);
            hosts.add(getParent());
            if ( ccvr.getSourceEditPart() != null ) {
                hosts.add( ccvr.getSourceEditPart());
                hosts.add( ccvr.getSourceEditPart().getParent());
            }
            if ( ccvr.getTargetEditPart() != null ) {
                hosts.add( ccvr.getTargetEditPart());
                hosts.add( ccvr.getTargetEditPart().getParent());
            }
        }
        if ((request instanceof GroupRequest)) {
            List parts = ((GroupRequest)request).getEditParts();
            hosts.add(this);
            hosts.add(getParent());
        
            Iterator editparts = parts == null ? Collections.EMPTY_LIST.iterator() : parts.iterator();  
            while ( editparts.hasNext() ) {
                EditPart ep = (EditPart)editparts.next();
                addEditPartAndParent(hosts, ep);
            }
        }
        
        /////////////////////////////////////////////////////////////
        // This following behavior is specific to BorderItemEditPart and
        // AbstractBorderItemEditPart, but we do not want to allow clients to
        // override this method so we do not want to make it protected.
        
        if (this instanceof IBorderItemEditPart) {
            if ((request instanceof CreateConnectionViewRequest)) {
                CreateConnectionViewRequest ccvr = (CreateConnectionViewRequest) request;
                if (ccvr.getSourceEditPart() instanceof IBorderItemEditPart) {
                    hosts.add(ccvr.getSourceEditPart().getParent().getParent());
                }
                if (ccvr.getTargetEditPart() instanceof IBorderItemEditPart) {
                    hosts.add(ccvr.getTargetEditPart().getParent().getParent());
                }
            }
        }
        /////////////////////////////////////////////////////////////

        return hosts;
    }

    private void addEditPartAndParent(Set hosts, EditPart editPart) {
        hosts.add(editPart);
        hosts.add(editPart.getParent());
    }

    /**
     * gets the content pane for the supplied editpart.
     * @param editPart the edit part to use to get the contents pane
     * @return <code>IFigure</code>
     */
    protected IFigure getContentPaneFor(IGraphicalEditPart editPart) {
        return getContentPane();
    }

    /**
     * Convenience method returning the editpart's parimary view. 
     * @return the diagram
     */
    protected Diagram getDiagramView() {
        return ((View) getRoot().getContents().getModel()).getDiagram();
    }

    /**
     * Convenience method returning the editpart's parimary view. Same as
     * calling <code>getView().getPrimaryView()</code>
     */
    public View getPrimaryView() {
    	if (this instanceof IPrimaryEditPart && getModel() instanceof View) {
    		return (View) getModel();
    	} else {
    		for (EditPart ep = getParent(); ep != null; ep = ep.getParent()) {
    			if (ep instanceof IGraphicalEditPart && ep.getModel() instanceof View) {
    				return ((IGraphicalEditPart) ep).getPrimaryView();
    			}
    		}
    	}
    	return null;
    }

    /**
     * gets this editpart's edit domain.
     * @return the edit domain
     */
    protected EditDomain getEditDomain() {
        EditDomain result = null;

        try {
            result = getRoot().getViewer().getEditDomain();
        } catch (NullPointerException nullPointerException) {
            /*
             * The reason why we would the code in try block throws a NPE is
             * partly because when the diagram is saved as another diagram, the
             * an event is generated which forces the refreshing of the
             * properties and if the selection is this editpart, then in order
             * to open a read action, properties provider will grab its
             * editDomain. Since this editPart would be in the state of flux and
             * may not have the root or the viewer set yet, therefore, a null
             * pointer exception can be thrown.
             *  
             */
            return null;
        }
        return result;
    }

    /** Return the editpart's diagram edit domain. */
    public IDiagramEditDomain getDiagramEditDomain() {
        return (IDiagramEditDomain) getEditDomain();
    }

    /**
     * Return this editpart's view (model) children.
     * 
     * @return list of views.
     */
    protected List getModelChildren() {
        Object model = getModel();
        if(model!=null && model instanceof View){
            return new ArrayList(((View)model).getVisibleChildren());
        }
        return Collections.EMPTY_LIST;
    }
    
    /**
     * Convenience method to retreive the value for the supplied value from the
     * editpart's associated view element. Same as calling
     * <code> ViewUtil.getStructuralFeatureValue(getNotationView(),feature)</code>.
     */
    public Object getStructuralFeatureValue(EStructuralFeature feature) {
        if (hasNotationView())
            return ViewUtil.getPropertyValue((View) super.getModel(), feature,
                feature.getEContainingClass());
        else
            return null;
    }

    
    /**
     * gets the semantic element associated to this editpart.
     * @return the semantic element or <code>null</code> if the semantic element was
     * <code>null</code> or unresolvable 
     */
    public EObject resolveSemanticElement() {
        EObject semanticElement = null;
        Object basicModel = basicGetModel();
        if (hasNotationView()) {
            semanticElement = ((View) basicModel).getElement();
        } else if (basicModel instanceof EObject) {
            semanticElement = (EObject) basicModel;
        }
        if (semanticElement == null) {
            return null;
        }

        if (!semanticElement.eIsProxy()) {
            return semanticElement;
        }

        try {
            return (EObject) getEditingDomain().runExclusive(
                new RunnableWithResult.Impl() {

                    public void run() {
                        Object model = getModel();
                        if (model instanceof View) {
                            setResult(ViewUtil
                                .resolveSemanticElement((View) getModel()));
                        } else if (model instanceof EObject) {
                            EObject element = (EObject) model;
                            if (element.eIsProxy())
                                setResult(EMFCoreUtil.resolve(
                                    getEditingDomain(), element));
                            else
                                setResult(element);
                        }
                    }
                });
        } catch (InterruptedException e) {
            Trace.catching(DiagramUIPlugin.getInstance(),
                DiagramUIDebugOptions.EXCEPTIONS_CATCHING, getClass(),
                "resolveSemanticElement", e); //$NON-NLS-1$
            Log.error(DiagramUIPlugin.getInstance(),
                DiagramUIStatusCodes.IGNORED_EXCEPTION_WARNING,
                "resolveSemanticElement", e); //$NON-NLS-1$
            return null;
        }

    }

    /**
     * Walks up the editpart hierarchy to find and return the
     * <code>TopGraphicEditPart</code> instance.
     */
    public TopGraphicEditPart getTopGraphicEditPart() {
        EditPart editPart = this;
        while (editPart instanceof IGraphicalEditPart) {
            if (editPart instanceof TopGraphicEditPart)
                return (TopGraphicEditPart) editPart;
            editPart = editPart.getParent();
        }
        return null;
    }

    /**
     * Return the editpart's associated Notation View.
     * @return <code>View</code>, the associated view or null if there is no associated Notation View
     */
    public View getNotationView() {
        Object model = getModel();
        if (model instanceof View)
            return (View)model;
        return null;
    }

    /**
     * Method reactivateSemanticModel. This method reactivates the edit part's
     * emantic model by: 1- removing semantic listeners 2- adding semantic
     * listeners if the semantic reference is resolvable 3- Refreshing it
     * 
     * This method is called in response to IView's Properties.ID_SEMANTICREF
     * property change event However, it will only work under the following
     * assumptions: 1- The old and new semantic models are compatible in their
     * kind 2- The deltas between old and new semantic models do not affect
     * notation 3- Connections are not refereshed since they are maintained by
     * the diagram
     */
    protected void reactivateSemanticModel() {
        removeSemanticListeners();
        if (resolveSemanticElement() != null)
            addSemanticListeners();
    }

    /** Finds an editpart given a starting editpart and an EObject */
    public EditPart findEditPart(EditPart epBegin, EObject theElement) {
        if (theElement == null) {
            return null;
        }
        EditPart epStart = null;
        if (epBegin == null) {
            epStart = this;
        } else {
            epStart = epBegin;
        }

        final View view = (View) ((IAdaptable) epStart)
            .getAdapter(View.class);

        if (view != null) {
            EObject el = ViewUtil.resolveSemanticElement(view);

            if ((el != null) && el.equals(theElement)) {
                return epStart;
            }
        }

        ListIterator childLI = epStart.getChildren().listIterator();
        while (childLI.hasNext()) {
            EditPart epChild = (EditPart) childLI.next();

            EditPart elementEP = findEditPart(epChild, theElement);
            if (elementEP != null) {
                return elementEP;
            }
        }
        return null;
    }

    /** Invoke the editpart's refresh mechanism. */
    public void refresh() {
        try {
            getEditingDomain().runExclusive(new Runnable() {
    
                public void run() {
                    EditPolicyIterator i = getEditPolicyIterator();
                    while (i.hasNext()) {
                        EditPolicy policy = i.next();
                        if (policy instanceof GraphicalEditPolicyEx) {
                            ((GraphicalEditPolicyEx) policy).refresh();
                        }
                    }
                    GraphicalEditPart.super.refresh();
                }
            });
        } catch (InterruptedException e) {
            Trace.catching(DiagramUIPlugin.getInstance(),
                DiagramUIDebugOptions.EXCEPTIONS_CATCHING, getClass(),
                "refresh", e); //$NON-NLS-1$
            Log.error(DiagramUIPlugin.getInstance(),
                DiagramUIStatusCodes.IGNORED_EXCEPTION_WARNING,
                "refresh", e); //$NON-NLS-1$
        }

    }

    /** Refresh the editpart's figure background colour. */
    protected void refreshBackgroundColor() {
        FillStyle style = (FillStyle)getPrimaryView().getStyle(NotationPackage.Literals.FILL_STYLE);
        if ( style != null ) {
            setBackgroundColor(DiagramColorRegistry.getInstance().getColor(Integer.valueOf(style.getFillColor())));
        }
    }

    /** Refresh the editpart's figure font. */
    protected void refreshFont() {
        FontStyle style = (FontStyle) getPrimaryView().getStyle(NotationPackage.Literals.FONT_STYLE);
        if (style != null) {
            setFont(new FontData(
                style.getFontName(), 
                style.getFontHeight(), 
                (style.isBold() ? SWT.BOLD : SWT.NORMAL) | 
                (style.isItalic() ? SWT.ITALIC : SWT.NORMAL)));
        }
    }

    /** Refresh the editpart's figure font colour. */
    protected void refreshFontColor() {
        FontStyle style = (FontStyle)  getPrimaryView().getStyle(NotationPackage.Literals.FONT_STYLE);
        if ( style != null ) {
            setFontColor(DiagramColorRegistry.getInstance().getColor(Integer.valueOf(style.getFontColor())));
        }
    }

    /** Refresh the editpart's figure foreground colour. */
    protected void refreshForegroundColor() {
        LineStyle style = (LineStyle)  getPrimaryView().getStyle(NotationPackage.Literals.LINE_STYLE);
        if ( style != null ) {
            setForegroundColor(DiagramColorRegistry.getInstance().getColor(Integer.valueOf(style.getLineColor())));
        }
    }

    /** Refresh the editpart's figure visibility. */
    protected void refreshVisibility() {
        Object model = null;
        EditPart ep = this;
        while (!(model instanceof View) && ep!=null){
            model = ep.getModel();
            ep = ep.getParent();
        }
        if (model instanceof View)
            setVisibility(((View)model).isVisible());
    }

    /** Refresh the editpart's figure visual properties. */
    protected void refreshVisuals() {
        super.refreshVisuals();
        refreshVisibility();
    }

    /**
     * Removes a listener previously added with the given id
     * 
     * @param filterId the filter ID
     */
    protected void removeListenerFilter(String filterId) {
        if (listenerFilters == null)
            return;
        Object[] objects = (Object[]) listenerFilters.remove(filterId);
        if (objects == null)
            return;
        if (objects.length>2){
            getDiagramEventBroker().
                removeNotificationListener((EObject) objects[0],
                                             (EStructuralFeature) objects[1],
                                             (NotificationListener) objects[2]);
        }else{
            getDiagramEventBroker().removeNotificationListener((EObject) objects[0],(NotificationListener) objects[1]);
        }
        
    }

    /**
     * sets the back ground color of this edit part
     * @param color the new value of the back ground color
     */
    protected void setBackgroundColor(Color color) {
        getFigure().setBackgroundColor(color);
    }

    /**
     * Sets the font to the label.
     * This method could be overriden to change the font data of the font
     * overrides typically look like this:
     *      super.setFont(
     *      new FontData(
     *          fontData.getName(),
     *          fontData.getHeight(),
     *          fontData.getStyle() <| &> SWT.????));
     * @param fontData the font data
     */
    protected void setFont(FontData fontData) {
         if (cachedFontData != null && cachedFontData.equals(fontData)) {
            // the font was previously set and has not changed; do nothing.
            return;
        }

        try {
            Font newFont = getResourceManager().createFont(
                FontDescriptor.createFrom(fontData));
            getFigure().setFont(newFont);
            getFigure().repaint();

            if (cachedFontData != null) {
                getResourceManager().destroyFont(
                    FontDescriptor.createFrom(cachedFontData));
            }
            cachedFontData = fontData;
        } catch (DeviceResourceException e) {
            Trace.catching(DiagramUIPlugin.getInstance(),
                DiagramUIDebugOptions.EXCEPTIONS_CATCHING, getClass(),
                "setFont", e); //$NON-NLS-1$
            Log.error(DiagramUIPlugin.getInstance(),
                DiagramUIStatusCodes.IGNORED_EXCEPTION_WARNING, "setFont", e); //$NON-NLS-1$
        }
    }

    /**
     * sets the font color
     * @param color the new value of the font color
     */
    protected void setFontColor(Color color) {
        // NULL implementation
    }

    /**
     * sets the fore ground color of this edit part's figure
     * @param color the new value of the foregroundcolor
     */
    protected void setForegroundColor(Color color) {
        getFigure().setForegroundColor(color);
    }
    
    /**
     * Sets the passed feature if possible on this editpart's view
     * to the passed value.
     * @param feature the feature to use
     * @param value  the value of the property being set
     */
    public void setStructuralFeatureValue(EStructuralFeature feature, Object value) {
        if (hasNotationView() && (feature != null)) {
            ViewUtil.setPropertyValue((View) super.getModel(), feature, feature
                .getEContainingClass(), value);
        }
    }

    /**
     * sets the visibility of this edit part
     * @param vis the new value of the visibility
     */
    protected void setVisibility(boolean vis) {
        if (!vis && getSelected() != SELECTED_NONE)
            getViewer().deselect(this);
        
        IFigure _figure = getFigure();
        if (_figure.isVisible()==vis){
            return;
        }
        
        // if we are going to hide the node then connections coming to the
        // node or outside it should be hidden as well
        if (getFlag(FLAG__AUTO_CONNECTIONS_VISIBILITY)) {
        	setConnectionsVisibility(vis);
        }
        _figure.setVisible(vis);
        _figure.revalidate();
    }
    
    protected void setConnectionsVisibility(boolean visibility) {
        List _srcConnections = getSourceConnections();
        for (Iterator iter = _srcConnections.iterator(); iter.hasNext();) {
            ConnectionEditPart connection = (ConnectionEditPart) iter.next();
            if (connection.getFigure().isVisible()!=visibility)
                connection.setVisibility(visibility);
        }
        
        List _targetConnections = getTargetConnections();
        for (Iterator iter = _targetConnections.iterator(); iter.hasNext();) {
            ConnectionEditPart connection = (ConnectionEditPart) iter.next();
            if (connection.getFigure().isVisible()!=visibility)
                connection.setVisibility(visibility);
        }
        
    }

    /**
     * This method adds all listeners to the notational world (views, figures,
     * editpart...etc) Override this method to add more notational listeners
     * down the hierarchy
     */
    protected void addNotationalListeners() {
        if (hasNotationView()){
            addListenerFilter("View", this,(View)getModel()); //$NON-NLS-1$
        }
    }
    
    /**
     * This method adds all listeners to the semantic element behind this EditPart
     * Override this method to add more semantic listeners down the hierarchy
     * This method is called only if the semantic element is resolvable
     */
    protected void addSemanticListeners() {
        addListenerFilter("SemanticElement", this,resolveSemanticElement());//$NON-NLS-1$
    }

    /**
     * This method removes all listeners to the notational world (views,
     * figures, editpart...etc) Override this method to remove notational
     * listeners down the hierarchy
     */
    protected void removeNotationalListeners() {
        removeListenerFilter("View"); //$NON-NLS-1$
    }

    /**
     * This method removes all listeners to the semantic element behind this EditPart
     * Override this method to remove semantic listeners
     * down the hierarchy
     */
    protected void removeSemanticListeners() {
        removeListenerFilter("SemanticElement"); //$NON-NLS-1$
    }

    /**
     * Perform a request by executing a command from the target editpart of the
     * request For the Direct_Edit request, we need to show up an editor first
     * 
     * @see org.eclipse.gef.EditPart#performRequest(org.eclipse.gef.Request)
     */
    public void performRequest(Request request) {
        if ( !isEditModeEnabled() ) {
            if (RequestConstants.REQ_OPEN.equals(request.getType())) {
                //allowed, continue
            }
            else {
                return;
            }
        }
        
        if (RequestConstants.REQ_DIRECT_EDIT == request.getType()) {
            performDirectEditRequest(request);
        } else {
            EditPart targetEditPart = getTargetEditPart(request);
            if (targetEditPart != null) {
                Command command = targetEditPart.getCommand(request);
                if (command != null) {
                    getDiagramEditDomain().getDiagramCommandStack().execute(
                        command);
                    return;
                }
            }
        }
    }

    /**
     * Performs a direct edit request (usually by showing some type of editor)
     * 
     * @param request
     *            the direct edit request
     */
    protected void performDirectEditRequest(Request request) {
        // NULL implementation
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#getAccessibleEditPart()
     */
    protected AccessibleEditPart getAccessibleEditPart() {
        if (accessibleEP == null)
            accessibleEP = new AccessibleGraphicalEditPart() {

            private String getSemanticName() {
                EObject semanticElement = resolveSemanticElement();
            
                if( semanticElement != null ) {
                    String name = semanticElement.getClass().getName();
                    int startIndex = name.lastIndexOf('.') + 1;
                    int endIndex = name.lastIndexOf( "Impl" ); //$NON-NLS-1$
                    return name.substring(startIndex, endIndex);
                }
            
                return null;
            }

            public void getName(AccessibleEvent e) {
                IGraphicalEditPart childEP = getChildBySemanticHint(CommonParserHint.NAME);
                if (childEP == null) {
                    childEP = getChildBySemanticHint(CommonParserHint.DESCRIPTION);
                }
                if (childEP != null) {
                    ILabelDelegate label = (ILabelDelegate) childEP.getAdapter(ILabelDelegate.class);
                    if (label != null) {
                        e.result = label.getText();
                    }
                } else {
                    e.result = getSemanticName();
                }
            }
            };

        return accessibleEP;
    }

    /** Adds a [ref, editpart] mapping to the EditPartForElement map. */
    protected void registerModel() {
        EditPartViewer viewer = getViewer();
        if (hasNotationView()) {
            super.registerModel();
        } else {
            viewer.getEditPartRegistry().put(basicGetModel(), this);
        }

        // Save the elements Guid to use during unregister.
        // If the reference is null, do not register.
        EObject ref = null;
        if (hasNotationView())
            ref = getNotationView().getElement();
        else
            ref = (EObject) basicGetModel();
        if (ref == null) {
            return;
        }
        elementGuid = EMFCoreUtil.getProxyID(ref);
        ((IDiagramGraphicalViewer) viewer).registerEditPartForElement(
            elementGuid, this);
    }


    /** Remove this editpart from the EditPartForElement map. */
    protected void unregisterModel() {
        EditPartViewer viewer = getViewer();
        if (hasNotationView())
            super.unregisterModel();
        else {
            Map registry = viewer.getEditPartRegistry();
            if (registry.get(basicGetModel()) == this)
                registry.remove(basicGetModel());
        }

        //Do not need to unregister if the guid is null.
        if (elementGuid == null) {
            return;
        }
        ((IDiagramGraphicalViewer) viewer).unregisterEditPartForElement(
            elementGuid, this);
    }

    /**
     * Refreshes a child editpart by removing it and refreshing children
     * @param child the child to refresh
     */
    protected final void refreshChild(GraphicalEditPart child) {
        removeChild(child);
        refreshChildren();
    }
    
    /**
     * Refreshes a source connection editpart by removing it and refreshing source connections
     * @param conn the connection to refresh
     */
    protected final void refreshSourceConnection(ConnectionEditPart conn) {
        removeSourceConnection(conn);
        refreshSourceConnections();
    }

    /**
     * Refreshes a target connection editpart by removing it and refreshing target connections
     * @param conn the connection to refresh
     */
    protected final void refreshTargetConnection(ConnectionEditPart conn) {
        removeTargetConnection(conn);
        refreshTargetConnections();
    }

    /**
     * Handles the case where the semantic reference has changed.
     */
    protected final void handleMajorSemanticChange() {
        if (getParent() instanceof GraphicalEditPart)
            ((GraphicalEditPart) getParent()).refreshChild(this);
        else if (getParent() instanceof ConnectionEditPart)
            ((ConnectionEditPart) getParent()).refreshChild(this);
    }
    
    /**
     * @see org.eclipse.gef.EditPart#getDragTracker(org.eclipse.gef.Request)
     */
    public DragTracker getDragTracker(Request request) {
        return new DragEditPartsTrackerEx(this);
    }

    /**
     * @return <tt>true</tt> a canonical editpolicy has been installed on this editpart
     * and it is active; otherwise <tt>false</tt>.
     */
    public boolean isCanonical() {
        CanonicalEditPolicy policy = (CanonicalEditPolicy)getEditPolicy(EditPolicyRoles.CANONICAL_ROLE);
        return policy == null ? false : policy.isActive();
    }

    /**
     * Return <tt>true</tt> if the editpart's figure is visible;
     * <tt>false</tt> otherwise.
     */
    public boolean isSelectable() {
        return getFigure().isShowing();
    }

    /* 
     * @see org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart#disableEditMode()
     */
    public void disableEditMode() {
        if (!isEditModeEnabled()) {
            return;
        }

        List l = getSourceConnections();
        int size = l.size();
        for (int i = 0; i < size; i++) {
            Object obj = l.get(i);
            if (obj instanceof IEditableEditPart) {
                ((IEditableEditPart) obj).disableEditMode();
            }
        }

        List c = getChildren();
        size = c.size();
        for (int i = 0; i < size; i++) {
            Object obj = c.get(i);
            if (obj instanceof IEditableEditPart) {
                ((IEditableEditPart) obj).disableEditMode();
            }
        }

        setFlag(FLAG__IS_EDITABLE, false);
    }
    
    
    
    protected void addChild(EditPart child, int index) {
        super.addChild(child, index);
        if (child instanceof GraphicalEditPart){
            GraphicalEditPart gEP = (GraphicalEditPart)child;
            boolean editMode = isEditModeEnabled(); 
            if (editMode != gEP.isEditModeEnabled()){
                if (editMode)
                    gEP.enableEditMode();
                else
                    gEP.disableEditMode();
            }
        }
    }

    /* 
     * @see org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart#enableEditMode()
     */
    public void enableEditMode() {
        if (isEditModeEnabled()) {
            return;
        }

        setFlag(FLAG__IS_EDITABLE, true);

        List c = getChildren();
        int size = c.size();
        for (int i = 0; i < size; i++) {
            Object obj = c.get(i);
            if (obj instanceof IEditableEditPart) {
                ((IEditableEditPart) obj).enableEditMode();
            }
        }

        List l = getSourceConnections();
        size = l.size();
        for (int i = 0; i < size; i++) {
            Object obj = l.get(i);
            if (obj instanceof IEditableEditPart) {
                ((IEditableEditPart) obj).enableEditMode();
            }
        }
    }
    
    /* 
     * @see org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart#isEditModeEnabled()
     */
    public boolean isEditModeEnabled() {
        // protect against deadlock - don't allow any action while write transaction
        // is active on another thread
        if (EditPartUtil.isWriteTransactionInProgress(this, true, true))
            return false;
        return getFlag(FLAG__IS_EDITABLE);
    }
    
    /* 
     * @see org.eclipse.gef.EditPart#showSourceFeedback(org.eclipse.gef.Request)
     */
    public void showSourceFeedback(Request request) {
        if ( !isEditModeEnabled()) {
            return;
        }
        
        super.showSourceFeedback(request);
    }
    
    /* 
     * @see org.eclipse.gef.EditPart#showTargetFeedback(org.eclipse.gef.Request)
     */
    public void showTargetFeedback(Request request) {
        if ( !isEditModeEnabled()) {
            return;
        }
        
        super.showTargetFeedback(request);
    }

    /* 
     * @see org.eclipse.gef.EditPart#eraseSourceFeedback(org.eclipse.gef.Request)
     */
    public void eraseSourceFeedback(Request request) {
        if ( !isEditModeEnabled()) {
            return;
        }
        
        super.eraseSourceFeedback(request);
    }
    /* 
     * @see org.eclipse.gef.EditPart#eraseTargetFeedback(org.eclipse.gef.Request)
     */
    public void eraseTargetFeedback(Request request) {
        if ( !isEditModeEnabled()) {
            return;
        }

        super.eraseTargetFeedback(request);
    }
    
    /**
     * this method will return the primary child EditPart  inside this edit part
     * @return the primary child view inside this edit part
     */
    public EditPart getPrimaryChildEditPart(){
        if (getChildren().size() > 0)
            return (EditPart) getChildren().get(0);
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart#getDiagramPreferencesHint()
     */
    public PreferencesHint getDiagramPreferencesHint() {
        RootEditPart root = getRoot();
        if (root instanceof IDiagramPreferenceSupport) {
            return ((IDiagramPreferenceSupport) root).getPreferencesHint();
        }
        return PreferencesHint.USE_DEFAULTS;
    }

    public void notifyChanged(Notification notification) {
        if (isActive()){
            handleNotificationEvent(notification);
        }
    }
    
    public Command transactionAboutToCommit(Notification notification) {
        return null;
    }

    
    /**
     * Handles the property changed event.  Clients should override to
     * respond to the specific notification events they are interested.
     * 
     * Note: This method may get called on a non-UI thread.  Clients should
     * either ensure that their code is thread safe and/or doesn't make
     * unsupported calls (i.e. Display.getCurrent() ) assuming they are on
     * the main thread.  Alternatively if this is not possible, then the
     * client can wrap their handler within the Display.synchExec runnable
     * to ensure synchronization and subsequent execution on the main thread.
     * 
     * @param event
     *            the <code>Notification</code> object that is the property changed event
     */
    protected void handleNotificationEvent(Notification event) {        
        if (NotationPackage.Literals.VIEW__PERSISTED_CHILDREN.equals(event.getFeature())||
                NotationPackage.Literals.VIEW__TRANSIENT_CHILDREN.equals(event.getFeature())) {
            refreshChildren();
        }else if (NotationPackage.Literals.VIEW__VISIBLE.equals(event.getFeature())) {
            Object notifier = event.getNotifier();
            if (notifier== getModel())
                refreshVisibility();
            else{
                // refresh the children only if the notificatino is not comming for and edge
                // sincethe children list had no edges
                if (!(event.getNotifier() instanceof Edge))
                    refreshChildren();
            }
        }
        else if (NotationPackage.Literals.VIEW__ELEMENT.equals(event.getFeature())) {
            handleMajorSemanticChange();
        } 
    }
    
    /**
     * @return <code>IMapMode</code> that allows for the coordinate mapping from device to
     * logical units. 
     */
     protected IMapMode getMapMode() {
        RootEditPart root = getRoot();
        if (root instanceof DiagramRootEditPart) {
            DiagramRootEditPart dgrmRoot = (DiagramRootEditPart)root;
            return dgrmRoot.getMapMode();
        }
        
        return MapModeUtil.getMapMode();
    }
    
    /**
     * indicates if this edit part's model is a view or not 
     * @return <code>true</code> or <code>false</code>
     */
    public boolean hasNotationView(){
        return true;
    }
    
    /**
     * Returns tis edit part's model; the returned values is not granteed to be 
     * <code>View</code>, the return value could be null or any Object depending
     * on the edit part implementation 
     * 
     */
    public Object getModel() {
        if (hasNotationView()){
            return super.getModel();
        } else {
            Object _model = basicGetModel();
            Node node = NotationFactory.eINSTANCE.createNode();;
            node.setElement((EObject)_model);
            return node;
        }
    }
    
    /**
     * Derives my editing domain from my model or my diagram element. Subclasses
     * may override.
     */
    public TransactionalEditingDomain getEditingDomain() {
        if (editingDomain == null) {
            // try to get the editing domain for the model
            editingDomain = TransactionUtil.getEditingDomain(getModel());
            
            if (editingDomain == null) {
                // try to get the editing domain from the diagram view
                editingDomain = TransactionUtil.getEditingDomain(getDiagramView());
            }
        }
        return editingDomain;
    }
    
    protected IFigure createFigure() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setModel(Object model) {
        // reset the editing domain cache
        editingDomain = null;
        super.setModel(model);
    }

    /**
     * Gets the diagram event broker from the editing domain.
     * 
     * @return the diagram event broker
     */
    private DiagramEventBroker getDiagramEventBroker() {
        TransactionalEditingDomain theEditingDomain = getEditingDomain();
        if (theEditingDomain != null) {
            return DiagramEventBroker.getInstance(theEditingDomain);
        }
        return null;
    }

    // documentation copied from superclass
    public RootEditPart getRoot() {
        if (getParent() != null) {
            return super.getRoot();
        }
        return null;
    }
    
   public Object getPreferredValue(EStructuralFeature feature) {
        Object preferenceStore = getDiagramPreferencesHint()
            .getPreferenceStore();
        if (preferenceStore instanceof IPreferenceStore) {
            if (feature == NotationPackage.eINSTANCE.getLineStyle_LineColor()) {
                
                return FigureUtilities.RGBToInteger(PreferenceConverter
                    .getColor((IPreferenceStore) preferenceStore,
                        IPreferenceConstants.PREF_LINE_COLOR));
                
            } else if (feature == NotationPackage.eINSTANCE
                .getFontStyle_FontColor()) {
                
                return FigureUtilities.RGBToInteger(PreferenceConverter
                    .getColor((IPreferenceStore) preferenceStore,
                        IPreferenceConstants.PREF_FONT_COLOR));
                
            } else if (feature == NotationPackage.eINSTANCE
                .getFillStyle_FillColor()) {
                
                return FigureUtilities.RGBToInteger(PreferenceConverter
                    .getColor((IPreferenceStore) preferenceStore,
                        IPreferenceConstants.PREF_FILL_COLOR));
                
            }
        }

        return getStructuralFeatureValue(feature);
    }    
    /**
     * Gets the resource manager to remember the resources allocated for this
     * graphical viewer. All resources will be disposed when the graphical
     * viewer is closed if they have not already been disposed.
     * 
     * @return the resource manager
     */
    protected ResourceManager getResourceManager() {
        EditPartViewer viewer = getViewer();
        if (viewer instanceof DiagramGraphicalViewer) {
            return ((DiagramGraphicalViewer) viewer).getResourceManager();
        }
        return JFaceResources.getResources();
    }    
    
	/**
	 * Set the line width of the shape. Clients need to override if they support
	 * line width.
	 * 
	 * @param width
	 *            the line width.
	 */
	protected void setLineWidth(int width) {
		/* not implemented */
	}

	/**
	 * Get the line width of the shape.
	 * 
	 * @return width the line width.
	 */
	protected int getLineWidth() {
		/* a default of -1 means the diagram does not implement line width */
		int lineWidth = -1;

		LineStyle style = (LineStyle) getPrimaryView().getStyle(
				NotationPackage.eINSTANCE.getLineStyle());
		if (style != null) {
			lineWidth = style.getLineWidth();
		}

		return lineWidth;
	}

	/**
	 * Refresh the line width of the shape.
	 */
	protected void refreshLineWidth() {
		setLineWidth(getLineWidth());
	}

	/**
	 * Set the line type of the shape. Clients need to override if they support
	 * line type.
	 * 
	 * @param lineType
	 *            the line type.
	 */
	protected void setLineType(int lineType) {
		/* not implemented */
	}

	/**
	 * Get the line type of the shape.
	 * 
	 * @return the line type.
	 */
	protected int getLineType() {
		// default to a solid line.
		int lineType = Graphics.LINE_SOLID;

		LineTypeStyle style = (LineTypeStyle) getPrimaryView().getStyle(
				NotationPackage.eINSTANCE.getLineTypeStyle());
		if (style != null) {
			if (style.getLineType() == LineType.SOLID_LITERAL) {
				lineType = Graphics.LINE_SOLID;
			} else if (style.getLineType() == LineType.DASH_LITERAL) {
				lineType = Graphics.LINE_DASH;
			} else if (style.getLineType() == LineType.DOT_LITERAL) {
				lineType = Graphics.LINE_DOT;
			} else if (style.getLineType() == LineType.DASH_DOT_LITERAL) {
				lineType = Graphics.LINE_DASHDOT;
			} else if (style.getLineType() == LineType.DASH_DOT_DOT_LITERAL) {
				lineType = Graphics.LINE_DASHDOTDOT;
			}
		}

		return lineType;
	}

	/**
	 * Refresh the line type of the shape.
	 */
	protected void refreshLineType() {
		setLineType(getLineType());
	}
	
	/**
	 * Sets the flag for automatically refreshing source/target connections visibility based on the editpart's
	 * visibility (i.e. {@link #setConnectionsVisibility(boolean) will be called}
	 * 
	 * @param autoRefreshConnectionsVisibility <code>true</code> to set automatic refresh to on
	 * @since 1.2
	 */
	public void setAutomaticalRefreshConnectionsVisibility(boolean autoRefreshConnectionsVisibility) {
		setFlag(FLAG__AUTO_CONNECTIONS_VISIBILITY, autoRefreshConnectionsVisibility);
	}
	
	/**
	 * Returns <code>true</code if automatic update of source/target connections visibility is triggered by editpart's visibility (i.e. {@link #setConnectionsVisibility(boolean) is being called}
	 * 
	 * @return <code>true</code> if automatic visibility refresh for connections is on
	 * @since 1.2
	 */
	public boolean isAutomaticalRefreshConnectionsVisibility() {
		return getFlag(FLAG__AUTO_CONNECTIONS_VISIBILITY);
	}

}