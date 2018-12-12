/******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.resources.editor.document;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.EditorPlugin;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.l10n.EditorMessages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.ibm.icu.util.StringTokenizer;


/**
 * This registry manages shared document providers. Document
 * providers are specified in <code>plugin.xml</code> either
 * per name extension or per editor input type. A name extension
 * rule always overrules an editor input type rule. Editor input
 * type rules follow the same rules <code>IAdapterManager</code>
 * used to find object adapters.
 *
 * @see org.eclipse.core.runtime.IAdapterManager
 */
public class DocumentProviderRegistry {
	
	public interface IDocumentProviderSelector {
		public boolean select(String documentType);
	}

	/** The registry singleton. */
	private static DocumentProviderRegistry fgRegistry;

	/**
	 * Returns the standard document provider registry.
	 *
	 * @return the default document provider registry
	 */
	public static DocumentProviderRegistry getDefault() {
		if (fgRegistry == null)
			fgRegistry= new DocumentProviderRegistry();
		return fgRegistry;
	}

	/** The mapping between name extensions and configuration elements. */
	public Map fExtensionMapping= new HashMap();
	/** The mapping between editor input type names and configuration elements. */
	public Map fInputTypeMapping= new HashMap();
	/** The mapping between configuration elements and instantiated document providers. */
	private Map fInstances= new HashMap();


	/**
	 * Creates a new document provider registry and initializes it with the information
	 * found in the plug-in registry.
	 */
	private DocumentProviderRegistry() {
		initialize();
	}

	/**
	 * Reads the comma-separated value of the given configuration element
	 * for the given attribute name and remembers the configuration element
	 * in the given map under the individual tokens of the attribute value.
	 *
	 * @param map the map
	 * @param element the configuration element
	 * @param attributeName the attribute name
	 */
	private void read(Map map, IConfigurationElement element, String attributeName) {
		String value= element.getAttribute(attributeName);

		if (value != null) {
			StringTokenizer tokenizer= new StringTokenizer(value, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				String token= tokenizer.nextToken().trim();

				Set s= (Set) map.get(token);
				if (s == null) {
					s= new HashSet();
					map.put(token, s);
				}
				s.add(element);
			}
		}
	}

	/**
	 * Initializes the document provider registry. It retrieves all implementers of the <code>documentProviders</code>
	 * extension point and remembers those implementers based on the name extensions and the editor input
	 * types they are for.
	 */
	private void initialize() {

		IExtensionPoint extensionPoint;
		extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(EditorPlugin.getPluginId(), "documentProviders"); //$NON-NLS-1$

		if (extensionPoint == null) {
			String msg= NLS.bind(EditorMessages.DocumentProviderRegistry_error_extension_point_not_found, PlatformUI.PLUGIN_ID);
			Bundle bundle = Platform.getBundle(EditorPlugin.getPluginId());
			ILog log= Platform.getLog(bundle);
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, null));
			return;
		}

		IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
		for (int i= 0; i < elements.length; i++) {
			read(fExtensionMapping, elements[i], "extensions"); //$NON-NLS-1$
			read(fInputTypeMapping, elements[i], "inputTypes"); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the document provider for the given configuration element.
	 * If there is no instantiated document provider remembered for this
	 * element, a new document provider is created and put into the cache.
	 *
	 * @param entry the configuration element
	 * @return the document provider for the given entry
	 */
	private IDocumentProvider getDocumentProvider(IConfigurationElement entry) {
		IDocumentProvider provider= (IDocumentProvider) fInstances.get(entry);
		if (provider == null) {
			try {
				provider = (IDocumentProvider) entry.createExecutableExtension("class"); //$NON-NLS-1$
				fInstances.put(entry, provider);
			} catch (CoreException x) {
				// ignore
			}
		}
		return provider;
	}
	
	/**
	 * Returns the first enumerated element of the given set.
	 *
	 * @param set the set
	 * @return the first configuration element in the set or <code>null</code> if none
	 */
	private IConfigurationElement selectConfigurationElement(Collection set, IDocumentProviderSelector selector) {
		if (set != null && !set.isEmpty()) {
			Iterator e= set.iterator();
			while(e.hasNext()) {
				IConfigurationElement element = (IConfigurationElement) e.next();
				String docTypeClass = element.getAttribute("documentType"); //$NON-NLS-1$
				if(selector.select(docTypeClass))
					return element;
			}
		}
		return null;
	}

	/**
	 * Returns a shared document provider for the given name extension.
	 *
	 * @param extension the name extension to be used for lookup
	 * @return the shared document provider or <code>null</code>
	 */
	public IDocumentProvider getDocumentProvider(String extension, IDocumentProviderSelector selector) {

		Set set= (Set) fExtensionMapping.get(extension);
		if (set != null) {
			IConfigurationElement entry = selectConfigurationElement(set, selector);
			if(entry != null)
				return getDocumentProvider(entry);
		}
		return null;
	}

	/**
	 * Computes the class hierarchy of the given type. The type is
	 * part of the computed hierarchy.
	 *
	 * @param type the type
	 * @return a list containing the super class hierarchy
	 */
	private List computeClassList(Class type) {

		List result= new ArrayList();

		Class c= type;
		while (c != null) {
			result.add(c);
			c= c.getSuperclass();
		}

		return result;
	}

	/**
	 * Computes the list of all interfaces for the given list of
	 * classes. The interface lists of the given classes are
	 * concatenated.
	 *
	 * @param classes a list of {@link java.lang.Class} objects
	 * @return a list with elements of type <code>Class</code>
	 */
	private List computeInterfaceList(List classes) {

		List result= new ArrayList(4);
		Hashtable visited= new Hashtable(4);

		Iterator e= classes.iterator();
		while (e.hasNext()) {
			Class c= (Class) e.next();
			computeInterfaceList(c.getInterfaces(), result, visited);
		}

		return result;
	}

	/**
	 * Computes the list of all interfaces of the given list of interfaces,
	 * taking a depth-first approach.
	 *
	 * @param interfaces an array of {@link java.lang.Class} objects denoting interfaces
	 * @param result the result list
	 * @param visited map of visited interfaces
	 */
	private void computeInterfaceList(Class[] interfaces, List result, Hashtable visited) {

		List toBeVisited= new ArrayList(interfaces.length);

		for (int i= 0; i < interfaces.length; i++) {
			Class iface= interfaces[i];
			if (visited.get(iface) == null) {
				visited.put(iface, iface);
				result.add(iface);
				toBeVisited.add(iface);
			}
		}

		Iterator e= toBeVisited.iterator();
		while(e.hasNext()) {
			Class iface= (Class) e.next();
			computeInterfaceList(iface.getInterfaces(), result, visited);
		}
	}

	/**
	 * Returns the configuration elements for the first class in the list
	 * of given classes for which configuration elements have been remembered.
	 *
	 * @param classes a list of {@link java.lang.Class} objects
	 * @return an input type mapping or <code>null</code>
	 */
	private Set getFirstInputTypeMapping(List classes) {
		Iterator e= classes.iterator();
		while (e.hasNext()) {
			Class c= (Class) e.next();
			Set mapping= (Set)fInputTypeMapping.get(c.getName());
			if (mapping != null)
				return mapping;
		}
		return null;
	}

	/**
	 * Returns the configuration elements for the first class in the list
	 * of given classes for which configuration elements have been remembered.
	 *
	 * @param classes a list of {@link java.lang.Class} objects
	 * @return an input type mapping or <code>null</code>
	 */
	private List getInputTypeMappings(List classes) {
		List mappings = new ArrayList();
		Iterator e= classes.iterator();
		while (e.hasNext()) {
			Class c= (Class) e.next();
			Set mapping= (Set)fInputTypeMapping.get(c.getName());
			if (mapping != null)
				mappings.addAll(mapping);
		}
		return mappings;
	}

	/**
	 * Returns the appropriate configuration element for the given type. If
	 * there is no configuration element for the type's name, first the list of
	 * super classes is searched, and if not successful the list of all interfaces.
	 *
	 * @param type a {@link java.lang.Class} object
	 * @return an input type mapping or <code>null</code>
	 */
	protected Set findInputTypeMapping(Class type) {

		if (type == null)
			return null;

		Set mapping= (Set)fInputTypeMapping.get(type.getName());
		if (mapping != null)
			return mapping;

		List classList= computeClassList(type);
		mapping= getFirstInputTypeMapping(classList);
		if (mapping != null)
			return mapping;

		return getFirstInputTypeMapping(computeInterfaceList(classList));
	}

	/**
	 * Returns the appropriate configuration element for the given type. If
	 * there is no configuration element for the type's name, first the list of
	 * super classes is searched, and if not successful the list of all interfaces.
	 *
	 * @param type a {@link java.lang.Class} object
	 * @return an input type mapping or <code>null</code>
	 */
	private List findInputTypeMappings(Class type) {

		List inputTypeMappings = new ArrayList();
		if (type == null)
			return null;

		Set mapping= (Set)fInputTypeMapping.get(type.getName());
		if (mapping != null)
			inputTypeMappings.addAll(mapping);

		List classList= computeClassList(type);
		List clsMapping= getInputTypeMappings(classList);
		if (clsMapping != null)
			inputTypeMappings.addAll(clsMapping);

		inputTypeMappings.addAll(getInputTypeMappings(computeInterfaceList(classList)));
		return inputTypeMappings;
	}

	/**
	 * Returns the shared document for the type of the given editor input.
	 *
	 * @param editorInput the input for whose type the provider is looked up
	 * @return the shared document provider
	 */
	public IDocumentProvider getDocumentProvider(IEditorInput editorInput, IDocumentProviderSelector selector) {

		IDocumentProvider provider= null;

		IFile file= (IFile) editorInput.getAdapter(IFile.class);
		if (file != null)
			provider= getDocumentProvider(file.getFileExtension(), selector);

		if (provider == null) {
			List inputTypeMappings= findInputTypeMappings(editorInput.getClass());
			if (inputTypeMappings != null) {
				IConfigurationElement entry = selectConfigurationElement(inputTypeMappings, selector);
				if(entry != null)
					provider= getDocumentProvider(entry);
			}
		}

		return provider;
	}
}
