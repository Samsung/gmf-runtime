/******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/


package org.eclipse.gmf.runtime.diagram.ui.resources.editor.util;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.gmf.runtime.common.core.util.Log;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.EditorPlugin;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.EditorStatusCodes;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.l10n.EditorMessages;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

/**
 * @author qili
 *
 * Class used for the creation of diagram files.
 * 
 */
public abstract class DiagramFileCreator {

	public abstract String getExtension();

	protected Plugin getPlugin() {
		return EditorPlugin.getInstance();
	}
	
	/**
	 * Creates a file resource handle for the file with the given workspace path.
	 * This method does not create the file resource; this is the responsibility
	 * of <code>createFile</code>.
	 *
	 * @param filePath the path of the file resource to create a handle for
	 * @return the new file resource handle
	 * @see #createFile
	 */
	private IFile createFileHandle(IPath filePath) {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
	}

	/**
	 * Given a string representing the possible file name, this function will ensure the
	 * proper extension is attached to it.
	 * @param szFileName String representing the file name.
	 * @return Corrected file name containing the file extension.
	 */
	public String appendExtensionToFileName(String szFileName) {
		if (!szFileName.endsWith(getExtension())) {
			return szFileName + getExtension();
		} else
			return szFileName;
	}

	/**
	 * Returns a unique file name for a given path and file name
	 * @param containerPath the container's full path
	 * @param szFileName original file name
	 * @return the unique file name with number attached in the event of multiple matches
	 */
	public String getUniqueFileName(
		final IPath containerPath,
		String szFileName) {
		int nFileNumber = 1;

		String szNewFileName = szFileName;
		
		IPath filePath = containerPath.append(appendExtensionToFileName(szNewFileName));
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		while (workspaceRoot.exists(filePath)) {
			nFileNumber++;
			szNewFileName = szFileName + nFileNumber;
			filePath = containerPath.append(appendExtensionToFileName(szNewFileName));
		}
		return szNewFileName;
	}


	/**
	* Creates a file resource given the file handle and contents.
	*
	* @param fileHandle the file handle to create a file resource with
	* @param contents the initial contents of the new file resource, or
	*   <code>null</code> if none (equivalent to an empty stream)
	* @param monitor the progress monitor to show visual progress with
	* @exception CoreException if the operation fails
	* @exception OperationCanceledException if the operation is canceled
	*/
	protected void createFile(
		IFile fileHandle,
		InputStream contents,
		IProgressMonitor monitor)
		throws CoreException {
		if (contents == null)
			contents = new ByteArrayInputStream(new byte[0]);

		try {
			// Create a new file resource in the workspace
			fileHandle.create(contents, false, monitor);
		} catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
				fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
			else {
				Log.error(getPlugin(), IStatus.ERROR, e.getMessage(), e);
				throw e;
			}
		}

		if (monitor.isCanceled()) {
			OperationCanceledException oce = new OperationCanceledException();
			Log.error(getPlugin(), IStatus.ERROR, oce.getMessage(), oce);
			throw oce;
		}
	}
	
	/**
	 * This implementation creates the file in a NullProgressMonitor.
	 * Superclasses may override and create the file in a runnable context.
	 *  
	 * @param fileHandle the file handle to create a file resource with
	 * @param contents the initial contents of the new file resource, or
	 * <code>null</code> if none (equivalent to an empty stream)
	 * @param monitor the progress monitor to show visual progress with
	 * @throws InterruptedException subclasses may throw this exception if running in a context which allows cancelling
	 * @throws InvocationTargetException when an error occured while creating the file
	 */
	private void createFile(IFile fileHandle,
			InputStream contents, IRunnableContext runContext) throws InterruptedException, InvocationTargetException {
		try {
		createFile(
			fileHandle,
			contents,
			new NullProgressMonitor());
		}
		catch (CoreException e) {
			Log.error(getPlugin(), IStatus.ERROR, e.getMessage(), e);
			throw new InvocationTargetException(e);
		}
	}

	/**
	 * Creates a new file cache given the name and containment path
	 * @param containerPath - IPath Directory path where the file will be stored
	 * @param fileName - IPath Name of the file to be created.
	 * @param initialContents InputStream of the initial contents of the file if desired.
	 * @param shell SWT Shell object as a context so that error messages / progress can be displayed.
	 * @param runContext IRunnableContext object which will run the file creation operation.
	 * @return IFile Resulting file that is created from the method logic.
	 */
	public IFile createNewFile(
		final IPath containerPath,
		final String fileName,
		final InputStream initialContents,
		Shell shell,
		IRunnableContext runContext) {

		IFile newFile;

		// create the new file and cache it if successful
		IPath newFilePath =
			containerPath.append(appendExtensionToFileName(fileName));
		final IFile newFileHandle = createFileHandle(newFilePath);

		//this could modify the workspace
		try {
			createFile(newFileHandle, initialContents, runContext);

		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof CoreException) {
				ErrorDialog.openError(shell,
				// Was Utilities.getFocusShell()
					EditorMessages.Editor_error_create_file_title,
				null, // no special message
				 ((CoreException) e.getTargetException()).getStatus());
			} else {
				// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
				Log.error(EditorPlugin.getInstance(), EditorStatusCodes.ERROR, NLS.bind("Exception in {0}.getNewFile(): {1}", new Object[] { getClass().getName(), e.getTargetException()}), e);//$NON-NLS-1$				
				MessageDialog.openError(shell,
					EditorMessages.Editor_error_create_file_title, NLS.bind(
						EditorMessages.Editor_error_create_file_message, e
							.getTargetException().getMessage())); 
			}
			return null;
		}

		newFile = newFileHandle;

		return newFile;
	}

}
