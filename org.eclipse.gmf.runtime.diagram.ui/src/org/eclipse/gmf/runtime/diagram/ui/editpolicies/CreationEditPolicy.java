/******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.editpolicies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.FileModificationValidator;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.core.commands.AddCommand;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.commands.CommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.CommandUtilities;
import org.eclipse.gmf.runtime.diagram.ui.commands.CreateCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.CreateOrSelectElementCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.SemanticCreateCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GroupEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.EditCommandRequestWrapper;
import org.eclipse.gmf.runtime.diagram.ui.requests.RefreshConnectionsRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.MoveRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.swt.widgets.Display;

/**
 * This edit policy returns the command that will create a new notational view
 * for a the given request.
 * 
 * @author choang
 */
public class CreationEditPolicy extends AbstractEditPolicy {
	
	/**
	 * @return Command for the REQ_CREATE request. If the request is of type
	 *         CreateViewAndElementRequest then a command to create the
	 *         semantic, and the view will
	 * @see org.eclipse.gef.EditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	public Command getCommand(Request request) {
		if (understandsRequest(request)) {
			if (request instanceof CreateUnspecifiedTypeRequest) {
				return getUnspecifiedTypeCreateCommand((CreateUnspecifiedTypeRequest) request);
			} else if (request instanceof CreateViewAndElementRequest) {
				return getCreateElementAndViewCommand(
					(CreateViewAndElementRequest)request);
			}
			else if (request instanceof CreateViewRequest) {
				return getCreateCommand((CreateViewRequest)request);
			}
			else if (request instanceof ChangeBoundsRequest) {
				return getReparentCommand((ChangeBoundsRequest)request);
			}
			return super.getCommand(request);
		}
		return null;
	}

	/** Understands <tt>REQ_CREATE</tt> and <tt>REQ_ADD</tt> request types. */
	public boolean understandsRequest(Request request) {
		return RequestConstants.REQ_CREATE.equals(request.getType())
		    || RequestConstants.REQ_ADD.equals(request.getType())
			|| super.understandsRequest(request);
	}
	
	/**
	 * Determines if a semantic reparent is being performed.
	 * @param element element being reparented
	 * @param newContext the new container element
	 * @return <tt>true</tt> if the supplied elemnet's container is
	 * not equals to the supplied <code>newContext</code>.\; otherwise <tt>false</tt>.
	 */
	protected boolean shouldReparent( EObject element, EObject newContext ) {
		return !(element == null ||
				element == newContext ||
				element.eContainer() == newContext ||
				isContainedIn( element, newContext )
				);
	}
	
	/*
	 * This method checks to see element's containment tree already includes
	 * itself.  This is necessary to prevent cyclic graphs in the model that
	 * cause StackOverflowExceptions.
	 */
	private boolean isContainedIn( EObject element, EObject newContext )
	{
		EObject container = newContext.eContainer();
		while( container != null ) {
			if( container.equals(element) )
				return true;
			container = container.eContainer();
		}
		return false;
	}

	/** Return a command to reparent both the semantic and view elements. */
	/**
	 * return a command to reparent both the semantic and view elements. 
	 * @param request the request
	 * @return command
	 */
	protected Command getReparentCommand(ChangeBoundsRequest request) {
		Iterator editParts = request.getEditParts().iterator();
		View container = (View)getHost().getAdapter(View.class);
		EObject context = container == null ? null : ViewUtil.resolveSemanticElement(container);
        CompositeCommand cc = new CompositeCommand(DiagramUIMessages.AddCommand_Label);
		while ( editParts.hasNext() ) {
			EditPart ep = (EditPart)editParts.next();
			if ( ep instanceof LabelEditPart ) {
				continue;
			}
			if (ep instanceof GroupEditPart) {
                cc.compose(getReparentGroupCommand((GroupEditPart) ep));
            }		
			View view = (View)ep.getAdapter(View.class);
			if ( view == null ) {
				continue;
			}
			
			EObject semantic = ViewUtil.resolveSemanticElement(view);
			if ( semantic == null ) {
				cc.compose(getReparentViewCommand((IGraphicalEditPart)ep));
			}
			else if ( context != null && shouldReparent(semantic, context)) {
				cc.compose(getReparentCommand((IGraphicalEditPart)ep));
			}
		}
		return cc.isEmpty() ? null : new ICommandProxy(cc.reduce());
	}
	
    /**
     * Return the command to reparent the supplied group editpart's semantic and
     * notation elements.
     * 
     * @param gep
     *            the groupEP editpart being reparented
     * @return A composite command that will reparent both the semantic and
     *         notation elements of the group.
     */
    protected ICommand getReparentGroupCommand(GroupEditPart groupEP) {
        CompositeCommand cc = new CompositeCommand(
            DiagramUIMessages.AddCommand_Label);
        View container = (View) getHost().getModel();
        EObject context = ViewUtil.resolveSemanticElement(container);

        // semantic
        TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost())
            .getEditingDomain();
        for (Iterator iter = groupEP.getShapeChildren().iterator(); iter
            .hasNext();) {
            IGraphicalEditPart childEP = (IGraphicalEditPart) iter.next();
            EObject element = ViewUtil.resolveSemanticElement((View) childEP
                .getModel());
            if (element != null) {
                Command moveSemanticCmd = getHost().getCommand(
                    new EditCommandRequestWrapper(new MoveRequest(
                        editingDomain, context, element)));

                if (moveSemanticCmd == null) {
                    return org.eclipse.gmf.runtime.common.core.command.UnexecutableCommand.INSTANCE;
                }

                cc.compose(new CommandProxy(moveSemanticCmd));
            }
        }

        // notation
        cc.compose(getReparentViewCommand(groupEP));
        return cc;
    }
	
	/** 
	 * Return the command to reparent the supplied editpart's semantic and notation
	 * elements.
	 * @param gep the editpart being reparented
	 * @return A CompositeCommand2 that will reparent both the semantic and notation elements.
	 */
	protected ICommand getReparentCommand( IGraphicalEditPart gep ) {
        CompositeCommand cc = new CompositeCommand(DiagramUIMessages.AddCommand_Label); 
		View container = (View)getHost().getModel();
		EObject context = ViewUtil.resolveSemanticElement(container);
		View view = (View)gep.getModel();
		EObject element = ViewUtil.resolveSemanticElement(view);

        TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost())
            .getEditingDomain();
        
        //
		// semantic
		if ( element != null ) {
			Command moveSemanticCmd =
				getHost().getCommand(
					new EditCommandRequestWrapper(
						new MoveRequest(editingDomain, context, element)));
            
              if (moveSemanticCmd == null) {
                  return org.eclipse.gmf.runtime.common.core.command.UnexecutableCommand.INSTANCE;
              }
			
			cc.compose ( new CommandProxy(moveSemanticCmd) );
		}
		//
		// notation
		cc.compose(getReparentViewCommand(gep));
		return cc;
	}
	
	/** 
	 * Return the command to reparent the supplied editpart's view only.
	 * @param gep the editpart being reparented
	 * @return A command to reparent the notation element.
	 */
	protected ICommand getReparentViewCommand( IGraphicalEditPart gep ) {
		View container = (View)getHost().getModel();
		View view = (View)gep.getModel();
		return new AddCommand(gep.getEditingDomain(), new EObjectAdapter(container),
							  new EObjectAdapter(view));
	}
	
	/**
	 * Called in response to a <tt>REQ_CREATE</tt> request.
	 * 
	 * Creates a compound command and populated with the following commands for
	 * each element to be created: <BR>
	 * <OL>
	 * <LI>a {@link CreateCommand}for each of the request's view descriptor.
	 * </OL>
	 * 
	 * @param request
	 *            a create request (understands instances of
	 *            {@link CreateViewRequest}).
	 * @return a command to satify the request; <tt>null</tt> if the request
	 *         is not understood.
	 */
	protected Command getCreateCommand(CreateViewRequest request) {

        TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost())
            .getEditingDomain();
        CompositeTransactionalCommand cc = new CompositeTransactionalCommand(
            editingDomain, DiagramUIMessages.AddCommand_Label);
        
        Iterator descriptors = request.getViewDescriptors().iterator();

		while (descriptors.hasNext()) {
			CreateViewRequest.ViewDescriptor descriptor =
				(CreateViewRequest.ViewDescriptor)descriptors.next();

			CreateCommand createCommand =
				new CreateCommand(editingDomain,
					descriptor, 
					(View)(getHost().getModel()));

			cc.compose(createCommand);
		}
		return new ICommandProxy(cc.reduce());

	}

	/**
	 * Method getCreateElementAndViewCommand.
	 * 
	 * @param request
	 * @return Command Which creates the sematnic and the view command for the
	 *         given CreateViewAndElementRequest
	 */
	protected Command getCreateElementAndViewCommand(CreateViewAndElementRequest request) {
		// get the element descriptor
		CreateElementRequestAdapter requestAdapter =
			request.getViewAndElementDescriptor().getCreateElementRequestAdapter();

		// get the semantic request
		CreateElementRequest createElementRequest =
			(CreateElementRequest) requestAdapter.getAdapter(
				CreateElementRequest.class);

		if (createElementRequest.getContainer() == null) {
			// complete the semantic request by filling in the host's semantic
			// element as the context
			View view = (View)getHost().getModel();
			EObject hostElement = ViewUtil.resolveSemanticElement(view);
			
			if (hostElement == null && view.getElement() == null) {
				hostElement = view;
			}			

			// Returns null if host is unresolvable so that trying to create a
			// new element in an unresolved shape will not be allowed.
			if (hostElement == null) {
				return null;
			}
			createElementRequest.setContainer(hostElement);
		}

		// get the create element command based on the elementdescriptor's
		// request
		Command createElementCommand =
			getHost().getCommand(
				new EditCommandRequestWrapper(
					(CreateElementRequest)requestAdapter.getAdapter(
						CreateElementRequest.class), request.getExtendedData()));

		if (createElementCommand == null) { 
			return UnexecutableCommand.INSTANCE;
		}		
		if(!createElementCommand.canExecute()){
			return createElementCommand;
		}
		// create the semantic create wrapper command
		SemanticCreateCommand semanticCommand =
			new SemanticCreateCommand(requestAdapter, createElementCommand);
		Command viewCommand = getCreateCommand(request);

		Command refreshConnectionCommand =
			getHost().getCommand(
				new RefreshConnectionsRequest(((List)request.getNewObject())));


		// form the compound command and return
        CompositeCommand cc = new CompositeCommand(semanticCommand.getLabel());
		cc.compose(semanticCommand);
		cc.compose(new CommandProxy(viewCommand));
		if ( refreshConnectionCommand != null ) {
			cc.compose(new CommandProxy(refreshConnectionCommand));
		}

		return new ICommandProxy(cc);
	}

	/**
	 * Gets the command to create a new view (and optionally element) for an
	 * unspecified type request. This command includes a command to popup a menu
	 * to prompt the user for the type to be created.
	 * 
	 * @param request
	 *            the unspecified type request
	 * @return the command
	 */
	private Command getUnspecifiedTypeCreateCommand(
			final CreateUnspecifiedTypeRequest request) {

		final Map createCmds = new HashMap();
		List validTypes = new ArrayList();
		for (Iterator iter = request.getElementTypes().iterator(); iter
			.hasNext();) {
			IElementType elementType = (IElementType) iter.next();
			Request createRequest = request.getRequestForType(elementType);
			if (createRequest != null) {
				EditPart target = getHost().getTargetEditPart(createRequest);
				if ( target == null )  {
					continue;
				}
				Command individualCmd = target.getCommand(createRequest);

				if (individualCmd != null && individualCmd.canExecute()) {
					createCmds.put(elementType, individualCmd);
					validTypes.add(elementType);
				}
			}
		}

		if (createCmds.isEmpty()) {
			return null;
		} else if (createCmds.size() == 1) {
			return (Command) createCmds.values().toArray()[0];
		} else {
			CreateOrSelectElementCommand selectAndCreateViewCmd = new CreateOrSelectElementCommand(
                DiagramUIMessages.CreateCommand_Label, Display.getCurrent()
                    .getActiveShell(), validTypes) {
				
				private Command _createCmd;
				/**
				 * Execute the command that prompts the user with the popup
				 * menu, then executes the command prepared for the element
				 * type that the user selected.
				 */
				protected CommandResult doExecuteWithResult(
                        IProgressMonitor progressMonitor, IAdaptable info)
                    throws ExecutionException {
                    
					CommandResult cmdResult = super.doExecuteWithResult(progressMonitor, info);
					if (!cmdResult.getStatus().isOK()) {
						return cmdResult;
					}

					IElementType type = (IElementType) cmdResult
						.getReturnValue();

					_createCmd = (Command) createCmds.get(type);
					Assert.isTrue(_createCmd != null && _createCmd.canExecute());
					
					// validate the affected files
					IStatus status = validateAffectedFiles(_createCmd);
					if (!status.isOK()) {
						return new CommandResult(status);
					}
					
					_createCmd.execute();

					// Set the result in the unspecified type request.
					CreateRequest createRequest = request
						.getRequestForType(type);
                    
                    Collection newObject = ((Collection) createRequest
                            .getNewObject());
					request.setNewObject(newObject);

					return CommandResult.newOKCommandResult(newObject);
				}
				
				protected CommandResult doUndoWithResult(
                        IProgressMonitor progressMonitor, IAdaptable info)
                    throws ExecutionException {
                    
					if (_createCmd != null && _createCmd.canUndo() ) {
						// validate the affected files
						IStatus status = validateAffectedFiles(_createCmd);
						if (!status.isOK()) {
							return new CommandResult(status);
						}
						_createCmd.undo();
					}
					return super.doUndoWithResult(progressMonitor, info);
				}
				
				protected CommandResult doRedoWithResult(
                        IProgressMonitor progressMonitor, IAdaptable info)
                    throws ExecutionException {
                    
					if (_createCmd != null && CommandUtilities.canRedo(_createCmd)) {
						// validate the affected files
						IStatus status = validateAffectedFiles(_createCmd);
						if (!status.isOK()) {
							return new CommandResult(status);
						}
						_createCmd.redo();
					}
					return super.doRedoWithResult(progressMonitor, info);
				}
				
				private IStatus validateAffectedFiles(Command command) {
					Collection affectedFiles = CommandUtilities
							.getAffectedFiles(command);
					int fileCount = affectedFiles.size();
					if (fileCount > 0) {
						return FileModificationValidator
								.approveFileModification((IFile[]) affectedFiles
										.toArray(new IFile[fileCount]));
					}
					return Status.OK_STATUS;
				}
			};

			return new ICommandProxy(selectAndCreateViewCmd);
		}
	}
	
	/** Return the host if this editpolicy understands the supplied request. */
	public EditPart getTargetEditPart(Request request) {
		return understandsRequest(request) ? getHost() : null;
	}
}
