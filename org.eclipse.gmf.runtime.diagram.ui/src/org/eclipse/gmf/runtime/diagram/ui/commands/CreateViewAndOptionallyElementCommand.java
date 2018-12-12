/******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.commands;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.common.core.command.AbstractCommand;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.util.ObjectAdapter;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramCommandStack;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequestFactory;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

/**
 * A command used to optionally create a new view and new element. This command
 * is used when it is not known at command creation time whether or not an
 * element should be created as well. For example, when creating a connection to
 * an unspecified target, did the user want to
 * <li>create a new element for the target (view and element)?</li>
 * <li>use an existing element and its view already on the diagram (nothing
 * created)?</li>
 * <li>use an existing element and add a new view to the diagram (view only)?
 * 
 * <p>
 * Note: This command will pop up a dialog box if the element exists already and
 * there is a view for it on the diagram to ask the user what they want to do.
 * </p>
 * 
 * @author cmahoney
 */
public class CreateViewAndOptionallyElementCommand
	extends AbstractCommand {

	/**
	 * Adapts to the element, if null at command execution time, an element is
	 * to be created.
	 */
	private IAdaptable elementAdapter;

	/** The location to create the new view. */
	private Point location;

	/** The container editpart to send the view request to. */
	private IGraphicalEditPart containerEP;

	/** The command executed, saved for undo/redo. */
	private Command command = null;

	/** The result to be returned from which the new view can be retrieved. */
	private ObjectAdapter resultAdapter = new ObjectAdapter();
	
	/**
	 * The hint used to find the appropriate preference store from which general
	 * diagramming preference values for properties of shapes, connections, and
	 * diagrams can be retrieved. This hint is mapped to a preference store in
	 * the {@link DiagramPreferencesRegistry}.
	 */
	private PreferencesHint preferencesHint;

	/**
	 * Creates a new <code>CreateViewAndOptionallyElementCommand</code>.
	 * 
	 * @param elementAdapter
	 *            Adapts to the element, if null at command execution time, an
	 *            element is to be created.
	 * @param containerEP
	 *            The container edit part, where the view request is sent.
	 * @param location
	 *            The location to create the new view. If null, a default
	 *            location is used
	 * @param preferencesHint
	 *            The preference hint that is to be used to find the appropriate
	 *            preference store from which to retrieve diagram preference
	 *            values. The preference hint is mapped to a preference store in
	 *            the preference registry <@link DiagramPreferencesRegistry>.
	 */
	public CreateViewAndOptionallyElementCommand(IAdaptable elementAdapter,
		IGraphicalEditPart containerEP, Point location, PreferencesHint preferencesHint) {
		super(DiagramUIMessages.CreateCommand_Label, null);

		setElementAdapter(elementAdapter);
		setContainerEP(containerEP);
		if (location != null) {
			setLocation(location);
		} else {
			setLocation(getContainerEP().getFigure().getBounds().getTopRight()
				.translate(100, 100));
		}
		setPreferencesHint(preferencesHint);
	}

    public List getAffectedFiles() {
		if (containerEP != null) {
			View view = (View)containerEP.getModel();
			if (view != null) {
				IFile f = WorkspaceSynchronizer.getFile(view.eResource());
				return f != null ? Collections.singletonList(f) : Collections.EMPTY_LIST;
			}
		}

        return super.getAffectedFiles();
	}
	
	/**
	 * Searches the container editpart to see if the element passed in already
	 * has a view.
	 * 
	 * @param element
	 * @return IView the view if found; or null
	 */
    protected View getExistingView(EObject element) {
        IGraphicalEditPart theTarget = (IGraphicalEditPart) findChildEditPart(
            getContainerEP(), element);
        if (theTarget != null)
            return (View) theTarget.getModel();
        return null;
    }

    /**
     * Returns an immediate child editpart of the editpart passed in whose
     * element is the same as the element passed in if it exists; returns null
     * if such an editpart does not exist.
     * 
     * @param editpart
     *            the parent editpart
     * @param theElement
     *            the element to match
     * @return an immediate child editpart of the editpart passed in whose
     *         element is the same as the element passed in if it exists; null
     *         otherwise
     */
    private EditPart findChildEditPart(EditPart editpart, EObject theElement) {
        if (theElement == null) {
            return null;
        }

        ListIterator childLI = editpart.getChildren().listIterator();
        while (childLI.hasNext()) {
            EditPart epChild = (EditPart) childLI.next();
            Object model = epChild.getModel();

            if (model instanceof View) {
                EObject el = ((View) model).getElement();

                if ((el != null) && el.equals(theElement)) {
                    return epChild;
                }
            }
        }
        return null;
    }

	/**
     * Prompts the user to see if they would like to use an existing view on the
     * diagram. Clients may subclass this method to customize the message
     * dialog.
     * 
     * @param view
     *            the existing view
     * @return true if this view should be used; false otherwise
     */
    protected boolean useExistingView(View view) {
        MessageBox messageBox = new MessageBox(Display.getCurrent()
            .getActiveShell(), SWT.YES | SWT.NO);
        messageBox
            .setText(DiagramUIMessages.CreateViewAndOptionallyElementCommand_ViewExists_Title);
        messageBox
            .setMessage(NLS
                .bind(
                    DiagramUIMessages.CreateViewAndOptionallyElementCommand_ViewExists_Message,
                    EMFCoreUtil.getName(view.getElement())));
        return messageBox.open() == SWT.YES;
    }
	
	/**
	 * <li>If the element adapter is empty, this command creates a new element
	 * and view for it.</li>
	 * <li>If the element adapter is not empty, and a view for this element
	 * exists in the container, this command will prompt the user to see if they
	 * want to use the existing view or create a new view for the element and
	 * then execute accordingly.</li>
	 * <li>If the element adapter is not empty, and a view for this element
	 * does not exist in the container, this command will create a new element
	 * and view.</li>
	 * 
	 */
    protected CommandResult doExecuteWithResult(
            IProgressMonitor progressMonitor, IAdaptable info)
        throws ExecutionException {
        
		CreateViewRequest createRequest;

		// Create the element first, if one does not exist.
		EObject element = (EObject) getElementAdapter().getAdapter(
			EObject.class);
		if (element == null) {
			IElementType type = (IElementType) getElementAdapter()
				.getAdapter(IElementType.class);
			if (type == null) {
				return CommandResult.newErrorCommandResult(getLabel());
			}
			createRequest = CreateViewRequestFactory
				.getCreateShapeRequest(type, getPreferencesHint());
		} else {
			createRequest = new CreateViewRequest(
				new CreateViewRequest.ViewDescriptor(
					new EObjectAdapter(element), getPreferencesHint()));
		}
		createRequest.setLocation(getLocation());

		if (createRequest != null) {
			IGraphicalEditPart target = (IGraphicalEditPart) getContainerEP().getTargetEditPart(createRequest);
			if ( target != null ) {
				Command theCmd = target.getCommand(createRequest);
				setCommand(theCmd);

				View theExistingView = getExistingView(element);            
                
                if (theExistingView != null && useExistingView(theExistingView)) {
                    setResult(new EObjectAdapter(theExistingView));
                    return CommandResult.newOKCommandResult(getResult());
                }
				// Fall-thru and create a new view
				if (getCommand().canExecute()) {
					ICommand cmd = DiagramCommandStack.getICommand(getCommand());
					cmd.execute(progressMonitor, info);					
					if (progressMonitor.isCanceled()) {
						return CommandResult.newCancelledCommandResult();
					}else if (!(cmd.getCommandResult().getStatus().isOK())){
						return cmd.getCommandResult();
					}				
					Object obj = ((List) createRequest.getNewObject()).get(0);										
					setResult((IAdaptable) obj);
					return CommandResult.newOKCommandResult(getResult());
				}
			}
		}
		containerEP = null;// to allow garbage collection
		return CommandResult.newErrorCommandResult(getLabel());
	}

    public boolean canUndo() {
		return getCommand() != null && getCommand().canUndo();
	}

    public boolean canRedo() {
		return CommandUtilities.canRedo(command);
	}

    
    protected CommandResult doRedoWithResult(IProgressMonitor progressMonitor, IAdaptable info)
        throws ExecutionException {
        
		if (getCommand() != null) {
			getCommand().redo();
		}
        return CommandResult.newOKCommandResult();
	}

    protected CommandResult doUndoWithResult(IProgressMonitor progressMonitor, IAdaptable info)
        throws ExecutionException {

		if (getCommand() != null) {
			getCommand().undo();
		}
        return CommandResult.newOKCommandResult();
	}

	/**
	 * @return the adapter from which the view can be retrieved.
	 */
	public IAdaptable getResult() {
		return resultAdapter;
	}

	/**
	 * Sets the result to adapt to the view passed in.
	 * @param viewAdapter
	 */
	protected void setResult(IAdaptable viewAdapter) {
		View view = (View) viewAdapter.getAdapter(View.class);
		resultAdapter.setObject(view);
	}

	/**
	 * Gets the elementAdapter.
	 * @return Returns the elementAdapter.
	 */
	protected IAdaptable getElementAdapter() {
		return elementAdapter;
	}

	/**
	 * Sets the elementAdapter.
	 * @param elementAdapter The elementAdapter to set.
	 */
	protected void setElementAdapter(IAdaptable elementAdapter) {
		this.elementAdapter = elementAdapter;
	}

	/**
	 * Gets the location.
	 * @return Returns the location.
	 */
	protected Point getLocation() {
		return location;
	}

	/**
	 * Sets the location.
	 * @param location The location to set.
	 */
	protected void setLocation(Point location) {
		this.location = location;
	}

	/**
	 * Gets the containerEP.
	 * @return Returns the containerEP.
	 */
	protected IGraphicalEditPart getContainerEP() {
		return containerEP;
	}

	/**
	 * Sets the containerEP.
	 * @param containerEP The containerEP to set.
	 */
	protected void setContainerEP(IGraphicalEditPart containerEP) {
		this.containerEP = containerEP;
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
	
	/**
	 * Sets the preferences hint that is to be used to find the appropriate
	 * preference store from which to retrieve diagram preference values. The
	 * preference hint is mapped to a preference store in the preference
	 * registry <@link DiagramPreferencesRegistry>.
	 * 
	 * @param preferencesHint the preferences hint
	 */
	protected void setPreferencesHint(PreferencesHint preferencesHint) {
		this.preferencesHint = preferencesHint;
	}

	/**
	 * Gets the command.
	 * @return Returns the command.
	 */
	protected Command getCommand() {
		return command;
	}

	/**
	 * Sets the command.
	 * @param command The command to set.
	 */
	protected void setCommand(Command command) {
		this.command = command;
	}
	
	
}
