/******************************************************************************
 * Copyright (c) 2000, 2005  IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.resources.editor.document;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.EditorPlugin;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.l10n.EditorMessages;


/**
 * An abstract implementation of a sharable document provider.
 * <p>
 * Subclasses must implement 
 * <code>createDocument</code>, 
 * <code>createEmptyDocument</code>,
 * <code>getOperationRunner</code>, and <code>doSaveDocument</code>.
 * </p>
 */
public abstract class AbstractDocumentProvider implements IDocumentProvider {

		/**
		 * Operation created by the document provider and to be executed by the providers runnable context.
		 *
		 */
		protected static abstract class DocumentProviderOperation implements IRunnableWithProgress {

			/**
			 * The actual functionality of this operation.
			 *
			 * @param monitor a progress monitor to track execution
			 * @throws CoreException
			 */
			protected abstract void execute(IProgressMonitor monitor) throws CoreException;

			/**
			 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					execute(monitor);
				} catch (CoreException x) {
					throw new InvocationTargetException(x);
				}
			}
		}

		/**
		 * Collection of all information managed for a connected element.
		 */
		protected class ElementInfo implements IDocumentListener {

			/** The element for which the info is stored */
			public Object fElement;
			/** How often the element has been connected */
			public int fCount;
			/** Can the element be saved */
			public boolean fCanBeSaved;
			/** The element's document */
			public IDocument fDocument;
			/**
			 * Has element state been validated
			 */
			public boolean fIsStateValidated;
			/**
			 * The status of this element
			 */
			public IStatus fStatus;


			/**
			 * Creates a new element info, initialized with the given
			 * document and annotation model.
			 *
			 * @param document the document
			 * @param model the annotation model
			 */
			public ElementInfo(IDocument document) {
				fDocument= document;
				fCount= 0;
				fCanBeSaved= false;
				fIsStateValidated= false;
			}

			/**
			 * An element info equals another object if this object is an element info
			 * and if the documents of the two element infos are equal.
			 * @see Object#equals(java.lang.Object)
			 */
			public boolean equals(Object o) {
				if (o instanceof ElementInfo) {
					ElementInfo e= (ElementInfo) o;
					return fDocument.equals(e.fDocument);
				}
				return false;
			}

			/*
			 * @see Object#hashCode()
			 */
			public int hashCode() {
				return fDocument.hashCode();
			}

			/**
			 * @see IDocumentListener#documentChanged(DocumentEvent)
			 */
			public void documentChanged(DocumentEvent event) {
				fCanBeSaved= true;
				removeUnchangedElementListeners(fElement, this);
				fireElementDirtyStateChanged(fElement, fCanBeSaved);
			}

			/**
			 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
			 */
			public void documentAboutToBeChanged(DocumentEvent event) {
				// nothing to do.
			}
		}


	/**
	 * Enables a certain behavior.
	 * Indicates whether this provider should behave as described in
	 * use case 5 of http://bugs.eclipse.org/bugs/show_bug.cgi?id=10806.
	 * Current value: <code>true</code> since 3.0
	 * 
	 */
	static final protected boolean PR10806_UC5_ENABLED= true;

	/**
	 * Enables a certain behavior.
	 * Indicates whether this provider should behave as described in
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=14469
	 * Notes: This contradicts <code>PR10806_UC5_ENABLED</code>.
	 * Current value: <code>false</code> since 3.0
	 * 
	 */
	static final protected boolean PR14469_ENABLED= false;

	/**
	 * Constant for representing the OK status. This is considered a value object.
	 * 
	 */
	static final public IStatus STATUS_OK= new Status(IStatus.OK, EditorPlugin.getPluginId(), IStatus.OK, EditorMessages.AbstractDocumentProvider_ok, null);

	/**
	 * Constant for representing the error status. This is considered a value object.
	 * 
	 */
	static final public IStatus STATUS_ERROR= new Status(IStatus.ERROR, EditorPlugin.getPluginId(), IStatus.INFO, EditorMessages.AbstractDocumentProvider_error, null);


	/** Element information of all connected elements */
	private Map fElementInfoMap= new HashMap();
	/** The element state listeners */
	private List fElementStateListeners= new ArrayList();
	/**
	 * The current progress monitor
	 */
	private IProgressMonitor fProgressMonitor;


	/**
	 * Creates a new document provider.
	 */
	protected AbstractDocumentProvider() {
		// Empty
	}

	/**
	 * Creates the document for the given element.
	 * <p>
	 * Subclasses must implement this method.</p>
	 *
	 * @param element the element
	 * @return the document
	 * @exception CoreException if the document could not be created
	 */
	protected abstract IDocument createDocument(Object element) throws CoreException;

	/**
	 * Creates the document for the given element.
	 * <p>
	 * Subclasses must implement this method.</p>
	 *
	 * @param element the element
	 * @return the document
	 * @exception CoreException if the document could not be created
	 */
	protected abstract IDocument createEmptyDocument();

	/**
	 * Performs the actual work of saving the given document provided for the
	 * given element.
	 * <p>
	 * Subclasses must implement this method.</p>
	 *
	 * @param monitor a progress monitor to report progress and request cancellation
	 * @param element the element
	 * @param document the document
	 * @param overwrite indicates whether an overwrite should happen if necessary
	 * @exception CoreException if document could not be stored to the given element
	 */
	protected abstract void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException;

	/**
	 * Returns the runnable context for this document provider.
	 *
	 * @param monitor a progress monitor to track the operation
	 * @return the runnable context for this document provider
	 * 
	 */
	protected abstract IRunnableContext getOperationRunner(IProgressMonitor monitor);

	/**
	 * Returns the scheduling rule required for executing
	 * <code>synchronize</code> on the given element. This default
	 * implementation returns <code>null</code>.
	 *
	 * @param element the element
	 * @return the scheduling rule for <code>synchronize</code>
	 * 
	 */
	protected ISchedulingRule getSynchronizeRule(Object element) {
		return null;
	}

	/**
	 * Returns the scheduling rule required for executing
	 * <code>validateState</code> on the given element. This default
	 * implementation returns <code>null</code>.
	 *
	 * @param element the element
	 * @return the scheduling rule for <code>validateState</code>
	 * 
	 */
	protected ISchedulingRule getValidateStateRule(Object element) {
		return null;
	}

	/**
	 * Returns the scheduling rule required for executing
	 * <code>save</code> on the given element. This default
	 * implementation returns <code>null</code>.
	 *
	 * @param element the element
	 * @return the scheduling rule for <code>save</code>
	 * 
	 */
	protected ISchedulingRule getSaveRule(Object element) {
		return null;
	}

	/**
	 * Returns the scheduling rule required for executing
	 * <code>reset</code> on the given element. This default
	 * implementation returns <code>null</code>.
	 *
	 * @param element the element
	 * @return the scheduling rule for <code>reset</code>
	 * 
	 */
	protected ISchedulingRule getResetRule(Object element) {
		return null;
	}

	/**
	 * Returns the element info object for the given element.
	 *
	 * @param element the element
	 * @return the element info object, or <code>null</code> if none
	 */
	protected ElementInfo getElementInfo(Object element) {
		return (ElementInfo) fElementInfoMap.get(element);
	}

	/**
	 * Creates a new element info object for the given element.
	 * <p>
	 * This method is called from <code>connect</code> when an element info needs
	 * to be created. The <code>AbstractDocumentProvider</code> implementation
	 * of this method returns a new element info object whose document and
	 * annotation model are the values of <code>createDocument(element)</code>
	 * and  <code>createAnnotationModel(element)</code>, respectively. Subclasses
	 * may override.</p>
	 *
	 * @param element the element
	 * @return a new element info object
	 * @exception CoreException if the document or annotation model could not be created
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		return new ElementInfo(createDocument(element));
	}

	/**
	 * Disposes of the given element info object.
	 * <p>
	 * This method is called when an element info is disposed. The
	 * <code>AbstractDocumentProvider</code> implementation of this
	 * method does nothing. Subclasses may reimplement.</p>
	 *
	 * @param element the element
	 * @param info the element info object
	 */
	protected void disposeElementInfo(Object element, ElementInfo info) {
		// for subclasses
	}

	/**
	 * Called on initial creation and when the dirty state of the element
	 * changes to <code>false</code>. Adds all listeners which must be
	 * active as long as the element is not dirty. This method is called
	 * before <code>fireElementDirtyStateChanged</code> or <code>
	 * fireElementContentReplaced</code> is called.
	 * Subclasses may extend.
	 *
	 * @param element the element
	 * @param info the element info object
	 */
	protected void addUnchangedElementListeners(Object element, ElementInfo info) {
		if (info.fDocument != null)
			info.fDocument.addDocumentListener(info);
	}

	/**
	 * Called when the given element gets dirty. Removes all listeners
	 * which must be active only when the element is not dirty. This
	 * method is called before <code>fireElementDirtyStateChanged</code>
	 * or <code>fireElementContentReplaced</code> is called.
	 * Subclasses may extend.
	 *
	 * @param element the element
	 * @param info the element info object
	 */
	protected void removeUnchangedElementListeners(Object element, ElementInfo info) {
		if (info.fDocument != null)
			info.fDocument.removeDocumentListener(info);
	}

	/**
	 * Enumerates the elements connected via this document provider.
	 *
	 * @return the list of elements (element type: <code>Object</code>)
	 */
	protected Iterator getConnectedElements() {
		Set s= new HashSet();
		Set keys= fElementInfoMap.keySet();
		if (keys != null)
			s.addAll(keys);
		return s.iterator();
	}

	/*
	 * @see IDocumentProvider#connect(Object)
	 */
	public final void connect(Object element) throws CoreException {
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		if (info == null) {

			info= createElementInfo(element);
			if (info == null)
				info= new ElementInfo(null);

			info.fElement= element;

			addUnchangedElementListeners(element, info);

			fElementInfoMap.put(element, info);
			if (fElementInfoMap.size() == 1)
				connected();
		}
		++ info.fCount;
	}

	/**
	 * This hook method is called when this provider starts managing documents for
	 * elements. I.e. it is called when the first element gets connected to this provider.
	 * Subclasses may extend.
	 * 
	 */
	protected void connected() {
		// for subclasses
	}

	/*
	 * @see IDocumentProvider#disconnect
	 */
	public final void disconnect(Object element) {
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);

		if (info == null)
			return;

		if (info.fCount == 1) {

			fElementInfoMap.remove(element);
			removeUnchangedElementListeners(element, info);
			disposeElementInfo(element, info);

			if (fElementInfoMap.size() == 0)
				disconnected();

		} else
		 	-- info.fCount;
	}

	/**
	 * This hook method is called when this provider stops managing documents for
	 * element. I.e. it is called when the last element gets disconnected from this provider.
	 * Subclasses may extend.
	 * 
	 */
	protected void disconnected() {
		// for subclasses
	}

	/*
	 * @see IDocumentProvider#getDocument(Object)
	 */
	public IDocument getDocument(Object element) {

		if (element == null)
			return null;

		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		return (info != null ? info.fDocument : null);
	}

	/*
	 * @see IDocumentProvider#mustSaveDocument(Object)
	 */
	public boolean mustSaveDocument(Object element) {

		if (element == null)
			return false;

		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		return (info != null ? info.fCount == 1 && info.fCanBeSaved : false);
	}

	/*
	 * @see IDocumentProvider#canSaveDocument(Object)
	 */
	public boolean canSaveDocument(Object element) {

		if (element == null)
			return false;

		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		return (info != null ? info.fCanBeSaved : false);
	}

	/**
	 * Executes the actual work of reseting the given elements document.
	 *
	 * @param element the element
	 * @param monitor the progress monitor
	 * @throws CoreException
	 * 
	 */
	protected void doResetDocument(Object element, IProgressMonitor monitor) throws CoreException {
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		if (info != null) {

			IDocument original= null;
			IStatus status= null;

			try {
				original= createDocument(element);
			} catch (CoreException x) {
				status= x.getStatus();
			}

			info.fStatus= status;

			if (original != null) {
				fireElementContentAboutToBeReplaced(element);
				info.fDocument.setContent(original.getContent());
				if (info.fCanBeSaved) {
					info.fCanBeSaved= false;
					addUnchangedElementListeners(element, info);
				}
				fireElementContentReplaced(element);
				fireElementDirtyStateChanged(element, false);
			}
		}
	}

	/**
	 * Executes the given operation in the providers runnable context.
	 *
	 * @param operation the operation to be executes
	 * @param monitor the progress monitor
	 * @exception CoreException the operation's core exception
	 * 
	 */
	protected void executeOperation(DocumentProviderOperation operation, IProgressMonitor monitor) throws CoreException {
		try {
			IRunnableContext runner= getOperationRunner(monitor);
			if (runner != null)
				runner.run(false, false, operation);
			else
				operation.run(monitor);
		} catch (InvocationTargetException x) {
			Throwable e= x.getTargetException();
			if (e instanceof CoreException)
				throw (CoreException) e;
			String message= (e.getMessage() != null ? e.getMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, EditorPlugin.getPluginId(), IStatus.ERROR, message, e));
		} catch (InterruptedException x) {
			String message= (x.getMessage() != null ? x.getMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.CANCEL, EditorPlugin.getPluginId(), IStatus.OK, message, x));
		}
	}

	/*
	 * @see IDocumentProvider#resetDocument(Object)
	 */
	public final void resetDocument(final Object element) throws CoreException {

		if (element == null)
			return;

		executeOperation(getResetOperation(element), getProgressMonitor());
	}
	
	public DocumentProviderOperation getResetOperation(final Object element) {
		class ResetOperation extends DocumentProviderOperation implements ISchedulingRuleProvider {

			protected void execute(IProgressMonitor monitor) throws CoreException {
				doResetDocument(element, monitor);
			}

			public ISchedulingRule getSchedulingRule() {
				return getResetRule(element);
			}
		}

		return new ResetOperation();
	}


	/*
	 * @see IDocumentProvider#saveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
	public final void saveDocument(IProgressMonitor monitor, final Object element, final IDocument document, final boolean overwrite) throws CoreException {

		if (element == null)
			return;


		executeOperation(getSaveOperation(element, document, overwrite), monitor);
	}
	
	public DocumentProviderOperation getSaveOperation(final Object element, final IDocument document, final boolean overwrite) {
		class SaveOperation extends DocumentProviderOperation implements ISchedulingRuleProvider {

			/* (non-Javadoc)
			 * @see org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.document.AbstractDocumentProvider.DocumentProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected void execute(IProgressMonitor pm) throws CoreException {
				ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
				if (info != null) {
					if (info.fDocument != document) {
						Status status= new Status(IStatus.WARNING, EditorPlugin.getPluginId(), IStatus.ERROR, EditorMessages.AbstractDocumentProvider_error_save_inuse, null);
						throw new CoreException(status);
					}

					doSaveDocument(pm, element, document, overwrite);

					if (pm != null && pm.isCanceled())
						return;

					info.fCanBeSaved= false;
					addUnchangedElementListeners(element, info);
					fireElementDirtyStateChanged(element, false);

				} else {
					doSaveDocument(pm, element, document, overwrite);
				}
			}

			public ISchedulingRule getSchedulingRule() {
				return getSaveRule(element);
			}
		}
		return new SaveOperation();
	}

	/**
	 * The <code>AbstractDocumentProvider</code> implementation of this
	 * <code>IDocumentProvider</code> method does nothing. Subclasses may
	 * reimplement.
	 *
	 * @param element the element
	 */
	public void aboutToChange(Object element) {
		// for subclasses
	}

	/**
	 * The <code>AbstractDocumentProvider</code> implementation of this
	 * <code>IDocumentProvider</code> method does nothing. Subclasses may
	 * reimplement.
	 *
	 * @param element the element
	 */
	public void changed(Object element) {
		// for subclasses
	}

	/*
	 * @see IDocumentProvider#addElementStateListener(IElementStateListener)
	 */
	public void addElementStateListener(IElementStateListener listener) {
		assert listener != null;
		if (!fElementStateListeners.contains(listener))
			fElementStateListeners.add(listener);
	}

	/*
	 * @see IDocumentProvider#removeElementStateListener(IElementStateListener)
	 */
	public void removeElementStateListener(IElementStateListener listener) {
		assert listener != null;
		fElementStateListeners.remove(listener);
	}

	/**
	 * Informs all registered element state listeners about a change in the
	 * dirty state of the given element.
	 *
	 * @param element the element
	 * @param isDirty the new dirty state
	 * @see IElementStateListener#elementDirtyStateChanged(Object, boolean)
	 */
	protected void fireElementDirtyStateChanged(Object element, boolean isDirty) {
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener l= (IElementStateListener) e.next();
			l.elementDirtyStateChanged(element, isDirty);
		}
	}

	/**
	 * Informs all registered element state listeners about an impending
	 * replace of the given element's content.
	 *
	 * @param element the element
	 * @see IElementStateListener#elementContentAboutToBeReplaced(Object)
	 */
	protected void fireElementContentAboutToBeReplaced(Object element) {
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener l= (IElementStateListener) e.next();
			l.elementContentAboutToBeReplaced(element);
		}
	}

	/**
	 * Informs all registered element state listeners about the just-completed
	 * replace of the given element's content.
	 *
	 * @param element the element
	 * @see IElementStateListener#elementContentReplaced(Object)
	 */
	protected void fireElementContentReplaced(Object element) {
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener l= (IElementStateListener) e.next();
			l.elementContentReplaced(element);
		}
	}

	/**
	 * Informs all registered element state listeners about the deletion
	 * of the given element.
	 *
	 * @param element the element
	 * @see IElementStateListener#elementDeleted(Object)
	 */
	protected void fireElementDeleted(Object element) {
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener l= (IElementStateListener) e.next();
			l.elementDeleted(element);
		}
	}

	/**
	 * Informs all registered element state listeners about a move.
	 *
	 * @param originalElement the element before the move
	 * @param movedElement the element after the move
	 * @see IElementStateListener#elementMoved(Object, Object)
	 */
	protected void fireElementMoved(Object originalElement, Object movedElement) {
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener l= (IElementStateListener) e.next();
			l.elementMoved(originalElement, movedElement);
		}
	}

	/*
	 * @see IDocumentProvider#getModificationStamp(Object)
	 * 
	 */
	public long getModificationStamp(Object element) {
		return 0;
	}

	/*
	 * @see IDocumentProvider#getSynchronizationStamp(Object)
	 * 
	 */
	public long getSynchronizationStamp(Object element) {
		return 0;
	}

	/*
	 * @see IDocumentProvider#isDeleted(Object)
	 * 
	 */
	public boolean isDeleted(Object element) {
		return false;
	}

	/*
	 * @see IDocumentProviderExtension#isReadOnly(Object)
	 * 
	 */
	public boolean isReadOnly(Object element) {
		return true;
	}

	/*
	 * @see IDocumentProviderExtension#isModifiable(Object)
	 * 
	 */
	public boolean isModifiable(Object element) {
		return false;
	}

	/**
	 * Returns whether <code>validateState</code> has been called for the given element
	 * since the element's state has potentially been invalidated.
	 *
	 * @param element the element
	 * @return whether <code>validateState</code> has been called for the given element
	 * 
	 */
	public boolean isStateValidated(Object element) {
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		if (info != null)
			return info.fIsStateValidated;
		return false;
	}

	/**
	 * Hook method for validating the state of the given element. Must not take care of cache updating etc.
	 * Default implementation is empty.
	 *
	 * @param element the element
	 * @param computationContext the context in which validation happens
	 * @exception CoreException in case validation fails
	 * 
	 */
	protected void doValidateState(Object  element, Object computationContext) throws CoreException {
		// empty block
	}

	/*
	 * @see IDocumentProviderExtension#validateState(Object, Object)
	 * 
	 */
	public void validateState(final Object element, final Object computationContext) throws CoreException {
		if (element == null)
			return;

		class ValidateStateOperation extends DocumentProviderOperation implements ISchedulingRuleProvider {

			protected void execute(IProgressMonitor monitor) throws CoreException {
				ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
				if (info == null)
					return;

				doValidateState(element, computationContext);

				doUpdateStateCache(element);
				info.fIsStateValidated= true;
				fireElementStateValidationChanged(element, true);
			}

			public ISchedulingRule getSchedulingRule() {
				return getValidateStateRule(element);
			}
		}

		executeOperation(new ValidateStateOperation(), getProgressMonitor());
	}

	/**
	 * Hook method for updating the state of the given element.
	 * Default implementation is empty.
	 *
	 * @param element the element
	 * @exception CoreException in case state cache updating fails
	 * 
	 */
	protected void doUpdateStateCache(Object element) throws CoreException {
		// for subclasses
	}

	/**
	 * Returns whether the state of the element must be invalidated given its
	 * previous read-only state.
	 *
	 * @param element the element
	 * @param wasReadOnly the previous read-only state
	 * @return <code>true</code> if the state of the given element must be invalidated
	 * 
	 */
	protected boolean invalidatesState(Object element, boolean wasReadOnly) {
		assert PR10806_UC5_ENABLED != PR14469_ENABLED;
		boolean readOnlyChanged= (isReadOnly(element) != wasReadOnly && !wasReadOnly);
		if (PR14469_ENABLED)
			return readOnlyChanged && !canSaveDocument(element);
		return readOnlyChanged;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocumentProvider#updateStateCache(java.lang.Object)
	 */
	final public void updateStateCache(Object element) throws CoreException {
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		if (info != null) {
			boolean wasReadOnly= isReadOnly(element);
			doUpdateStateCache(element);
			if (invalidatesState(element, wasReadOnly)) {
				info.fIsStateValidated= false;
				fireElementStateValidationChanged(element, false);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocumentProvider#setCanSaveDocument(java.lang.Object)
	 */
	public void setCanSaveDocument(Object element) {
		if (element != null) {
			ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
			if (info != null) {
				info.fCanBeSaved= true;
				removeUnchangedElementListeners(element, info);
				fireElementDirtyStateChanged(element, info.fCanBeSaved);
			}
		}
	}

	/**
	 * Informs all registered element state listeners about a change in the
	 * state validation of the given element.
	 *
	 * @param element the element
	 * @param isStateValidated
	 * @see IElementStateListenerExtension#elementStateValidationChanged(Object, boolean)
	 * 
	 */
	protected void fireElementStateValidationChanged(Object element, boolean isStateValidated) {
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			Object o= e.next();
			if (o instanceof IElementStateListener) {
				IElementStateListener l= (IElementStateListener) o;
				l.elementStateValidationChanged(element, isStateValidated);
			}
		}
	}

	/**
	 * Informs all registered element state listeners about the current state
	 * change of the element
	 *
	 * @param element the element
	 * @see IElementStateListenerExtension#elementStateChanging(Object)
	 * 
	 */
	protected void fireElementStateChanging(Object element) {
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			Object o= e.next();
			if (o instanceof IElementStateListener) {
				IElementStateListener l= (IElementStateListener) o;
				l.elementStateChanging(element);
			}
		}
	}

	/**
	 * Informs all registered element state listeners about the failed
	 * state change of the element
	 *
	 * @param element the element
	 * @see IElementStateListenerExtension#elementStateChangeFailed(Object)
	 * 
	 */
	protected void fireElementStateChangeFailed(Object element) {
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			Object o= e.next();
			if (o instanceof IElementStateListener) {
				IElementStateListener l= (IElementStateListener) o;
				l.elementStateChangeFailed(element);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocumentProvider#getStatus(java.lang.Object)
	 */
	public IStatus getStatus(Object element) {
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		if (info != null) {
			if (info.fStatus != null)
				return info.fStatus;
			return (info.fDocument == null ? STATUS_ERROR : STATUS_OK);
		}

		return STATUS_ERROR;
	}

	/**
	 * Performs the actual work of synchronizing the given element.
	 *
	 * @param element the element
	 * @param monitor the progress monitor
	 * @exception CoreException in the case that synchronization fails
	 * 
	 */
	protected void doSynchronize(Object element, IProgressMonitor monitor) throws CoreException {
		// for subclasses
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editor.IDocumentProvider#synchronize(java.lang.Object)
	 */
	public final void synchronize(final Object element) throws CoreException {

		if (element == null)
			return;

		class SynchronizeOperation extends DocumentProviderOperation implements ISchedulingRuleProvider {

			protected void execute(IProgressMonitor monitor) throws CoreException {
				doSynchronize(element, monitor);
			}

			public ISchedulingRule getSchedulingRule() {
				return getSynchronizeRule(element);
			}
		}

		executeOperation(new SynchronizeOperation(), getProgressMonitor());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocumentProvider#getProgressMonitor()
	 */
	public IProgressMonitor getProgressMonitor() {
		return fProgressMonitor == null ? new NullProgressMonitor() : fProgressMonitor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocumentProvider#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		fProgressMonitor= progressMonitor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocumentProvider#isSynchronized(java.lang.Object)
	 */
	public boolean isSynchronized(Object element) {
		return true;
	}
}
