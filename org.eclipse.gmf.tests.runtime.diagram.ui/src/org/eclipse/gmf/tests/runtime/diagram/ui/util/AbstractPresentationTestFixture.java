/******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.tests.runtime.diagram.ui.util;

import java.util.List;
import java.util.ListIterator;

import junit.framework.Assert;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.common.core.util.Log;
import org.eclipse.gmf.runtime.diagram.core.DiagramEditingDomainFactory;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.OffscreenEditPartFactory;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.util.IDEEditorUtil;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.tests.runtime.diagram.ui.AbstractTestBase;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorPart;


/**
 * @author choang
 *
 * Most fixture should extend fromthis Abstract class.  The only things that are left to do is to implement the following
 * abstract methods
 * 1.  createProject,createDiagram,craeteShapesAndConnectors
 * 2.  Implement you test class which should extend from org.junit.org or @see org.eclipse.gmf.tests.runtime.diagram.ui.AbstractTestBase
 * 
 */
public abstract class AbstractPresentationTestFixture
	extends Assert
	implements IPresentationTestFixture {
	
	private IProject project = null;
	private IDiagramWorkbenchPart diagramWorkbenchPart = null;
	private IFile diagramFile = null;
    private TransactionalEditingDomain editingDomain;
    private Resource resource;
	
	private Diagram diagram;
	private DiagramEditPart diagramEditPart;
	
	private Edge connectorView = null; 
	// connector view for the test fixture

    /**
     * Temporary shell to be used when creating the diagram editpart.
     */
    private Shell tempShell;
    
	/** verbose flag. */
	private static boolean _verbose = Boolean.getBoolean(AbstractTestBase.SYSPROP_VERBOSE);
	
	/**
	 * Returns the diagramFile.
	 * @return IFile
	 */
	public IFile getDiagramFile() {
		return diagramFile;
	}

	/**
	 * Enable verbose mode.  If enabled, {@link junit.framework.Assert#fail(java.lang.String)} 
	 * will print the supplied string; otherwise the string is ignored.
	 * 
	 * Verbose mode can also be enabled using the {@link AbstractTestBase#SYSPROP_VERBOSE} system property.
	 * @param enabled boolean flag
	 */
	protected final void enableVerbose( boolean enabled ) {
		_verbose = enabled;
	}
	
	/** Return the verbose mode. */
	public final boolean isVerbose() {
		return _verbose;
		
	}
	
	/** Calls <code>System.out.println(msg)</code> if in verbose mode. */
	public static final void println( Object msg ) {
		if ( _verbose ) {
			System.out.println(msg);
		}
	}

	
	/** Calls <code>System.out.print(msg)</code> if in verbose mode. */
	public static final void print( Object msg ) {
		if ( _verbose ) {
			System.out.print(msg);
		}
	}
	
	/**
	 * Returns the editor.
	 * @return IDiagramWorkbenchPart
	 */
	public IDiagramWorkbenchPart getDiagramWorkbenchPart() {
		if (diagramWorkbenchPart == null) {
			assertTrue(
				"It appears that the diagram needs to be opened for this test.  Call openDiagram().", false); //$NON-NLS-1$
		}
		return diagramWorkbenchPart;
	}

	/**
	 * Returns the project.
	 * @return IProject
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * Sets the diagramFile.
	 * @param diagramFile The diagramFile to set
	 */
	protected void setDiagramFile(IFile diagramFile) {
		this.diagramFile = diagramFile;
	}

	/**
	 * Method getCommandStack.
	 * @return CommandStack  Command stack for the diagram edit domain
	 */
	public CommandStack getCommandStack() {
		return getDiagramEditPart().getDiagramEditDomain().getDiagramCommandStack();
	}
	/**
	 * Sets the diagramWorkbenchPart.
	 * @param diagramWorkbenchPart The editorPart to set
	 */
	public void setDiagramWorkbenchPart(IDiagramWorkbenchPart diagramWorkbenchPart) {
		this.diagramWorkbenchPart = diagramWorkbenchPart;
	}

	/**
	 * Sets the project.
	 * @param project The project to set
	 */
	protected void setProject(IProject project) {
		this.project = project;
	}

	/**
	 * 
	 * Setup up the data for a test.  It will create the project,diagram and then opent the diagram
	 * and then create the test shapes and connectors on the diagram for the tests.
	 */
	public void setup() throws Exception {
		createProject();
		createDiagram();
        createResource();
		createDiagramEditPart();
		
		flushEventQueue(); // so that all editor related events are fired
		createShapesAndConnectors();
	}

	/**
	 * Will delete the project that was used for the test and removed all the resources in it.
	 */
	public final void tearDown() throws Exception { 
        if (tempShell != null) {
            tempShell.dispose();
            tempShell = null;
        }	
//		flushEventQueue();
		boolean diagramClosed = false;
		Throwable cde = null; // close diagram exception
		Throwable pde = null;  //project delete exception
		try {
			
			try {
				diagramClosed = closeDiagram();
			}
			catch( Throwable cdt ) {
				cde = cdt;
			}
			try { 
				if (project != null)
					project.delete(true, true, null);
			}
			catch( Throwable pdt ) {
				pde = pdt;
			}
		}
		finally {
            
			// erasing all the data
			setDiagramWorkbenchPart(null);
            // unload resource
            if (getResource() != null) {
                getResource().unload();
                setResource(null);
            }
			setDiagramFile(null);
			setProject(null);
			setConnectorView(null);
			flushEventQueue();
			if ( !diagramClosed ) {
				Log.error( TestsPlugin.getDefault(), IStatus.ERROR, "FAILED TO CLOSE DIAGRAM", cde);//$NON-NLS-1$
			}
			if ( pde != null ) {
				Log.error( TestsPlugin.getDefault(), IStatus.ERROR, "FAILED TO DELETE PROJECT", pde);//$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Returns the connectorView.
	 * Maybe null if there is no connector view for this test
	 * @return IConnectorView
	 */
	public Edge getConnectorView() {
		// maybe null if there is no connector view for this test
		return connectorView;
	}

	/**
	 * Sets the connectorView.
	 * @param connectorView The connectorView to set
	 */
	protected void setConnectorView(Edge connectorView) {
		this.connectorView = connectorView;
	}

	/** Clears the diaplay's event queue. */
	public void flushEventQueue() {
		Display display = Display.getCurrent();
		while (display.readAndDispatch()) {
			// empty block
		}
	}

	public void openDiagram() throws Exception {
		if (getDiagramFile() == null) {
			createDiagram();
		}

		assertTrue("creation of diagram failed", getDiagramFile() != null); //$NON-NLS-1$
		IWorkbenchPage page =
			PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow()
				.getActivePage();

		IDEEditorUtil.openDiagram(getDiagramFile(), page.getWorkbenchWindow(),
            false, new NullProgressMonitor());

		setDiagramWorkbenchPart((IDiagramWorkbenchPart)IDE.openEditor(page, getDiagramFile(), true));
		setDiagramEditPart(getDiagramWorkbenchPart().getDiagramEditPart());
        setDiagram(getDiagramEditPart().getDiagramView());
        setResource(getDiagram().eResource());
	}

	public boolean closeDiagram() {
		boolean closed = true;
		if (diagramWorkbenchPart != null // only close if it was opened
			&& diagramWorkbenchPart instanceof IEditorPart) {
			IWorkbenchPage page =
			getDiagramWorkbenchPart().getSite().getPage();
			closed = page.closeEditor((IEditorPart)getDiagramWorkbenchPart(), false);
			setDiagramWorkbenchPart(null);
		}
		return closed;
	}

	protected boolean isDirty() {
		return ((EditorPart) getDiagramWorkbenchPart()).isDirty();
	}
	
	/**
	 * @param diagram The diagram to set.
	 */
	public void setDiagram(Diagram diagram) {
		this.diagram = diagram;
	}
	/**
	 * @param diagramEditPart The diagramEditPart to set.
	 */
	public void setDiagramEditPart(DiagramEditPart diagramEditPart) {
		this.diagramEditPart = diagramEditPart;
	}

	public Diagram getDiagram() {
		return diagram;
	}

	public DiagramEditPart getDiagramEditPart() {
		return diagramEditPart;
	}

	/**
	 * Implement to setup the project for the tests.
	 */
	protected abstract void createProject() throws Exception;

	/**
	 * Implement to create the diagram and the diagram file for which
	 * the test should run under.  Please set the diagramFile variable.
	 */
	protected abstract void createDiagram() throws Exception;
    
	/**
     * Creates the editing domain and resource and adds the diagram to
     * that resource.
     */
    protected void createResource() {  
    	if (getResource() == null) {
	        IFile file = getDiagramFile();
	        
	        if (file != null) {
	            String filePath = file.getLocation().toOSString();
	            setResource(getEditingDomain().loadResource(filePath));
	
	        } else {
                setResource(getEditingDomain()
	                .createResource("null:/org.eclipse.gmf.tests.runtime.diagram.ui")); //$NON-NLS-1$
	        }
	
	        if (getDiagram() != null) {
	            
	            AbstractEMFOperation operation = new AbstractEMFOperation(
	            	getEditingDomain(), "AbstractPresentationTestFixture setup") { //$NON-NLS-1$
	
	                protected IStatus doExecute(IProgressMonitor monitor,
	                        IAdaptable info)
	                    throws ExecutionException {
	                    
	                    getResource().getContents().add(getDiagram());
	                    return Status.OK_STATUS;
	                };
	            };
	
	    
	            try {
	                operation.execute(new NullProgressMonitor(), null);
	            } catch (ExecutionException ie) {
	                fail("createResource failed: " + ie.getLocalizedMessage()); //$NON-NLS-1$
	            }
	        }
    	}
    }

	/**
	 * Creates and sets the diagram editpart using the offscreen rendering
	 * capabilities.
	 */
	protected void createDiagramEditPart()
		throws Exception
	{
		if (getDiagramEditPart() == null) {
			setDiagramEditPart(OffscreenEditPartFactory.getInstance()
				.createDiagramEditPart(getDiagram(), getTempShell()));
		}
	}
	
    public ShapeEditPart createShapeUsingTool(IElementType elementType,
            Point location, IGraphicalEditPart containerEP) {
        return createShapeUsingTool(elementType, location, null, containerEP);
    }
    
	/**
	 * Creates a new shape using the request created by the
	 * <code>CreationTool</code>.
	 * 
	 * @param elementType
	 *            the type of the shape/element to be created
	 * @param location
	 *            the location for the new shape
	 * @return the new shape's editpart
	 */
	public ShapeEditPart createShapeUsingTool(IElementType elementType,
			Point location, Dimension size, IGraphicalEditPart containerEP) {

		CreateRequest request = getCreationRequest(elementType);
		request.setLocation(location);
		if (size != null) {
		    request.setSize(size);
		}
		Command cmd = containerEP.getCommand(request);

		int previousNumChildren = containerEP.getChildren().size();

		getCommandStack().execute(cmd);
		assertEquals(previousNumChildren + 1, containerEP.getChildren().size());

		Object newView = ((IAdaptable) ((List) request.getNewObject()).get(0)).getAdapter(View.class);
		assertNotNull(newView);
		assertTrue(!ViewUtil.isTransient((View)newView));
		
		EObject element = ((View)newView).getElement();
		
		getCommandStack().undo();
		assertEquals(previousNumChildren, containerEP.getChildren().size());

		getCommandStack().redo();
		assertEquals(previousNumChildren + 1, containerEP.getChildren().size());

		IGraphicalEditPart newShape = null;
		if (element != null) {
			List children = containerEP.getChildren();
			ListIterator li = children.listIterator();
			while (li.hasNext()) {
				IGraphicalEditPart gep = (IGraphicalEditPart) li.next();
                if (gep.getNotationView() != null
                    && element.equals(gep.getNotationView().getElement())) {
                    newShape = gep;
                }
			}
		}
		else {
			newShape = (ShapeEditPart) getDiagramEditPart()
			.getViewer().getEditPartRegistry().get(newView);
			assertNotNull(newShape);
		}
		
		assertTrue(newShape != null && newShape instanceof ShapeEditPart);
		return (ShapeEditPart)newShape;
	}

	/**
	 * Given an <code>IElementType</code>, gets the creation request that can be used to 
	 * retrieve the command to creation the element for the type.
	 * 
	 * @param elementType
	 * @return
	 */
	public CreateRequest getCreationRequest(IElementType elementType) {
		class CreationTool
			extends org.eclipse.gmf.runtime.diagram.ui.tools.CreationTool {

			public CreationTool(IElementType theElementType) {
				super(theElementType);
			}

			/** Make public. */
			public Request createTargetRequest() {
				return super.createTargetRequest();
			}
			
			protected PreferencesHint getPreferencesHint() {
				return PreferencesHint.USE_DEFAULTS;
			}
		}

		CreationTool tool = new CreationTool(elementType);
		CreateRequest request = (CreateRequest) tool.createTargetRequest();
		return request;
	}

	/**
	 * Creates a new shape using the request created by the
	 * <code>CreationTool</code>.
	 * 
	 * @param elementType
	 *            the type of the shape/element to be created
	 * @param location
	 *            the location for the new shape
	 * @return the new shape's editpart
	 */
	public ShapeEditPart createShapeUsingTool(IElementType elementType,
			Point location) {

		return createShapeUsingTool(elementType, location, getDiagramEditPart());

	}
	
	   /**
     * Creates a new shape using the request created by the
     * <code>CreationTool</code>.
     * 
     * @param elementType
     *            the type of the shape/element to be created
     * @param location
     *            the location for the new shape
     * @return the new shape's editpart
     */
    public ShapeEditPart createShapeUsingTool(IElementType elementType,
            Point location, Dimension size) {

        return createShapeUsingTool(elementType, location, size, getDiagramEditPart());

    }
	
	/**
	 * Creates a new connector using the request created by the
	 * <code>ConnectionCreationTool</code>.
	 * 
	 * @param sourceEditPart
	 *            the new connector's source
	 * @param targetEditPart
	 *            the new connector's target
	 * @param elementType
	 *            the type of the connector/relationship to be created
	 * @return the new connector's editpart
	 */
	public ConnectionEditPart createConnectorUsingTool(
			final IGraphicalEditPart sourceEditPart,
			final IGraphicalEditPart targetEditPart, IElementType elementType) {

		class ConnectorCreationTool
			extends
			org.eclipse.gmf.runtime.diagram.ui.tools.ConnectionCreationTool {

			public ConnectorCreationTool(IElementType theElementType) {
				super(theElementType);
			}

			/** Make public. */
			public Request createTargetRequest() {
				return super.createTargetRequest();
			}
			
			protected PreferencesHint getPreferencesHint() {
				return PreferencesHint.USE_DEFAULTS;
			}
		}

		ConnectorCreationTool tool = new ConnectorCreationTool(elementType);
		CreateConnectionRequest request = (CreateConnectionRequest) tool
			.createTargetRequest();
		request.setTargetEditPart(sourceEditPart);
		request.setType(RequestConstants.REQ_CONNECTION_START);
		sourceEditPart.getCommand(request);
		request.setSourceEditPart(sourceEditPart);
		request.setTargetEditPart(targetEditPart);
		request.setType(RequestConstants.REQ_CONNECTION_END);
		Command cmd = targetEditPart.getCommand(request);

		int previousNumConnectors = getDiagramEditPart().getConnections().size();

		getCommandStack().execute(cmd);
		assertEquals(previousNumConnectors + 1, getDiagramEditPart()
			.getConnections().size());
		getCommandStack().undo();
		assertEquals(previousNumConnectors, getDiagramEditPart()
			.getConnections().size());
		getCommandStack().redo();
		assertEquals(previousNumConnectors + 1, getDiagramEditPart()
			.getConnections().size());

		Object newView = ((IAdaptable) request.getNewObject())
			.getAdapter(View.class);
		assertNotNull(newView);

		ConnectionEditPart newConnector = (ConnectionEditPart) getDiagramEditPart()
			.getViewer().getEditPartRegistry().get(newView);
		assertNotNull(newConnector);

		return newConnector;
	}

	/**
	 * Implement this to creates the shapes and the connectors for the tests.
	 * Will set the connect view if there is one needed for the test.
	 */
	protected abstract void createShapesAndConnectors() throws Exception;

    
    public TransactionalEditingDomain getEditingDomain() {
    	if (editingDomain == null) {
            if (getDiagram() != null) {
                editingDomain = TransactionUtil.getEditingDomain(getDiagram());
            } else {
                editingDomain = DiagramEditingDomainFactory.getInstance()
                    .createEditingDomain();
            }
        }
        return editingDomain;
    }
    
    protected Resource getResource() {
        return resource;
    }
    
    protected void setResource(Resource resource) {
        this.resource = resource;
    }
    
    /**
     * Lazily creates a new shell.
     * @return
     */
    private Shell getTempShell() {
        if (tempShell == null) {
            tempShell = new Shell();
        }
        return tempShell;
    }

}
