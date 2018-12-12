/******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation, Christian W. Damus, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 *    Christian W. Damus - bug 457888
 ****************************************************************************/

package org.eclipse.gmf.runtime.emf.type.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.util.Log;
import org.eclipse.gmf.runtime.common.core.util.Trace;
import org.eclipse.gmf.runtime.emf.type.core.internal.EMFTypeDebugOptions;
import org.eclipse.gmf.runtime.emf.type.core.internal.EMFTypePlugin;
import org.eclipse.gmf.runtime.emf.type.core.internal.EMFTypePluginStatusCodes;
import org.eclipse.gmf.runtime.emf.type.core.internal.descriptors.IEditHelperAdviceDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.internal.impl.DefaultClientContext;
import org.eclipse.gmf.runtime.emf.type.core.internal.impl.XMLClientContext;
import org.eclipse.gmf.runtime.emf.type.core.internal.l10n.EMFTypeCoreMessages;

/**
 * The context manager loads contexts from the <code>elementTypeBindings</code>
 * extension point and makes them available to the element type registry.
 * <P>
 * There is also a default client context, {@link #getDefaultClientContext()},
 * that is implicitly bound to all types and advice that otherwise have no
 * explicit binding.
 * 
 * @author ldamus
 */
public final class ClientContextManager {

	/**
	 * Extension point name for the element type bindings extension point.
	 */
	public static final String ELEMENT_TYPE_BINDINGS_EXT_P_NAME = "elementTypeBindings"; //$NON-NLS-1$

	private static final String E_CLIENT_CONTEXT = "clientContext"; //$NON-NLS-1$

	private static final String E_BINDING = "binding"; //$NON-NLS-1$

	private static final String A_CONTEXT = "context"; //$NON-NLS-1$

	private static final String E_ELEMENT_TYPE = "elementType"; //$NON-NLS-1$

	private static final String E_ADVICE = "advice"; //$NON-NLS-1$

	private static final String A_REF = "ref"; //$NON-NLS-1$

	private static final String A_PATTERN = "pattern"; //$NON-NLS-1$

	private static final ClientContextManager INSTANCE = new ClientContextManager();

	private final Set clientContexts = new java.util.HashSet();

	private final Map clientContextMap = new java.util.HashMap();
	
	private final CopyOnWriteArrayList<IClientContextManagerListener> listeners = new CopyOnWriteArrayList<IClientContextManagerListener>();
	
	private ExtensionTracker extensionTracker;
	
	private IExtensionChangeHandler extensionListener;

	/**
	 * Not instantiable by clients.
	 */
	private ClientContextManager() {
		super();

		IConfigurationElement[] configs = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EMFTypePlugin.getPluginId(),
						ELEMENT_TYPE_BINDINGS_EXT_P_NAME);

		if (EMFTypePlugin.isDynamicAware()) {
			startExtensionTracking();
		}
		
		configureElementTypeBindings(configs);
	}
	
	private void startExtensionTracking() {
		extensionListener = new IExtensionChangeHandler() {
			
			public void addExtension(IExtensionTracker tracker,
					IExtension extension) {
				configureElementTypeBindings(extension.getConfigurationElements());
			}

			public void removeExtension(IExtension extension, Object[] objects) {
				// Extension removal not supported
			}
		};
		
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(EMFTypePlugin.getPluginId(),
						ELEMENT_TYPE_BINDINGS_EXT_P_NAME);
		
		extensionTracker = new ExtensionTracker();
		extensionTracker.registerHandler(extensionListener, ExtensionTracker
				.createExtensionPointFilter(point));
	}

	/**
	 * Obtains the singleton instance of this class.
	 * 
	 * @return the singleton context manager
	 */
	public static final ClientContextManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Gets the default client context.
	 * 
	 * @return the default context
	 */
	public static final IClientContext getDefaultClientContext() {
		return getInstance().getClientContext(DefaultClientContext.ID);
	}

	/**
	 * Obtains a client context by its unique identifier.
	 * 
	 * @param contextId
	 *            the client context ID to look for
	 * @return the matching context, or <code>null</code> if none is
	 *         registered under this ID
	 */
	public IClientContext getClientContext(String contextId) {

		if (DefaultClientContext.ID.equals(contextId)) {
			return DefaultClientContext.getInstance();
		}

		return (IClientContext) clientContextMap.get(contextId);
	}

	/**
	 * Obtains all of the client contexts registered in the system.
	 * 
	 * @return the available {@link IClientContext}s
	 */
	public Set getClientContexts() {
		return clientContexts;
	}

	/**
	 * Obtains the client contexts to which the specified object belongs.
	 * 
	 * @param eObject
	 *            a model element
	 * @return the client context to which the <code>eObject</code> belongs.
	 *         This may be <code>null</code> if no context matches this
	 *         element. It will be a <code>MultiClientContest</code> if more
	 *         than one context matches the element.
	 */
	public IClientContext getClientContextFor(EObject eObject) {

		Collection contexts = new java.util.ArrayList();

		for (Iterator iter = getClientContexts().iterator(); iter.hasNext();) {
			IClientContext next = (IClientContext) iter.next();
			IElementMatcher matcher = next.getMatcher();

			try {
				if (matcher.matches(eObject)) {
					contexts.add(next);
				}
			} catch (RuntimeException e) {
				// client context matchers must not throw exceptions. This one
				// will not be trusted in future validation operations. This
				// is effected by removing it from the context manager
				iter.remove();
				clientContextMap.remove(next.getId());
				// context

				Trace.catching(EMFTypePlugin.getPlugin(),
						EMFTypeDebugOptions.EXCEPTIONS_CATCHING, getClass(),
						"getClientContextsFor", e); //$NON-NLS-1$

				Log
						.error(
								EMFTypePlugin.getPlugin(),
								EMFTypePluginStatusCodes.CLIENT_MATCHER_FAILURE,
								EMFTypeCoreMessages
										.bind(
												EMFTypeCoreMessages.client_matcherFailure_ERROR_,
												next.getId()), e);
			}
		}

		return getClientContext(contexts);
	}

	/**
	 * Constructs and returns an <code>IClientContext</code> that represents
	 * all of the <code>contexts</code>.
	 * 
	 * @param contexts
	 *            the contexts
	 * @return a multi-context that represents all of the <code>contexts</code>,
	 *         or the single context if there is only one in
	 *         <code>contexts</code>. If <code>contexts</code> is empty,
	 *         returns the {@link DefaultClientContext#getInstance()}.
	 */
	private IClientContext getClientContext(Collection contexts) {

		IClientContext result = DefaultClientContext.getInstance();

		if (contexts.size() > 1) {
			result = new MultiClientContext(contexts);

		} else if (!contexts.isEmpty()) {
			result = (IClientContext) contexts.iterator().next();
		}

		return result;
	}

	/**
	 * Finds the {@link IClientContext} that is bound to the
	 * <code>elementTypeDescriptor</code>. If more than one context is bound
	 * to the <code>elementTypeDescriptor</code>, the context that is
	 * returned will be a <code>MultiClientContext</code> representing them
	 * all.
	 * 
	 * @param elementTypeDescriptor
	 *            the element type descriptor
	 * @return the {@link IClientContext} that is bound to the
	 *         <code>elementTypeDescriptor</code>. May be a multi-context.
	 */
	public IClientContext getBinding(
			IElementTypeDescriptor elementTypeDescriptor) {
		Collection result = new ArrayList();

		for (Iterator i = getClientContexts().iterator(); i.hasNext();) {
			IClientContext next = (IClientContext) i.next();

			if (next.includes(elementTypeDescriptor)) {
				result.add(next);
			}
		}
		return getClientContext(result);
	}

	/**
	 * Finds the {@link IClientContext}s that are bound to the
	 * <code>elementType</code>. If there are more than one contexts bound,
	 * returns a <code>MultiClientContext</code> representing them all.
	 * 
	 * @param elementType
	 *            the element type
	 * @return the {@link IClientContext} that is bound to the
	 *         <code>elementType</code>. May be a multi-context.
	 */
	public IClientContext getBinding(IElementType elementType) {
		Collection result = new ArrayList();

		for (Iterator i = getClientContexts().iterator(); i.hasNext();) {
			IClientContext next = (IClientContext) i.next();

			if (next.includes(elementType)) {
				result.add(next);
			}
		}
		return getClientContext(result);
	}

	/**
	 * Finds the {@link IClientContext} that is bound to the <code>advice</code>.
	 * If more than one context is bound to the <code>advice</code>, the
	 * context that is returned will be a <code>MultiClientContext</code>
	 * representing them all.
	 * 
	 * @param elementType
	 *            the element type
	 * @return the {@link IClientContext} that is bound to the
	 *         <code>advice</code>. May be a multi-context.
	 */
	public IClientContext getBinding(IEditHelperAdviceDescriptor advice) {
		Collection result = new ArrayList();

		for (Iterator i = getClientContexts().iterator(); i.hasNext();) {
			IClientContext next = (IClientContext) i.next();

			if (next.includes(advice)) {
				result.add(next);
			}
		}
		return getClientContext(result);
	}

	/**
	 * <p>
	 * Configures my providers from the Eclipse configuration
	 * <code>elements</code> representing implementations of my extension
	 * point.
	 * </p>
	 * <p>
	 * <b>NOTE</b> that this method should only be called by the EMF Model
	 * Validation Plug-in, not by any client code!
	 * </p>
	 * 
	 * @param elements
	 *            the configuration elements representing constraint binding
	 *            extensions
	 */
	private synchronized void configureElementTypeBindings(IConfigurationElement[] elements) {
		// must create all of the contexts before we process the bindings.
		// Hence, this will loop over the elements twice
		configureClientContexts(elements);
		configureBindings(elements);
	}

	/**
	 * Registers the <code>clientContext</code>. Does nothing if the context
	 * has already been registered.
	 * 
	 * @param clientContext
	 *            the client context
	 */
	public void registerClientContext(IClientContext clientContext) {

		// prevent duplicates
		if (clientContexts.add(clientContext)) {
			clientContextMap.put(clientContext.getId(), clientContext);
			
			fireClientContextAdded(clientContext);
		}
	}

	/**
	 * Queries whether a {@code clientContext} was dynamically registered (that
	 * is, not statically registered on the extension point).
	 * 
	 * @param clientContext
	 *            a clientContext
	 * @return whether it was dynamically registered
	 * 
	 * @since 1.9
	 */
	public boolean isDynamic(IClientContext clientContext) {
		boolean result = true;

		// If it's some other kind of client-context, then it cannot be one that
		// we registered from the extension point, because all of those are of
		// some subtype of ClientContext
		if (clientContext instanceof ClientContext) {
			result = ((ClientContext) clientContext).isDynamic();
		}

		return result;
	}

	/**
	 * Removes a {@linkplain #isDynamic(IClientContext) dynamically-added} {@code clientContext} from the system.
	 * 
	 * @param clientContext
	 *            a client-context to remove
	 * @return {@code true} if it was successfully removed, {@code false}
	 *         otherwise, for example if it was not currently registered or a
	 *         different context instance is registered under the same ID, or
	 *         it was not {@linkplain #isDynamic(IClientContext) dynamically added}
	 * 
	 * @since 1.9
	 * 
	 * @see #isDynamic(IClientContext)
	 */
	public boolean deregisterClientContext(IClientContext clientContext) {
		boolean result = false;

		IClientContext existing = (IClientContext) clientContextMap.get(clientContext.getId());
		if (existing != null) {
			if (existing != clientContext) {
				// An impostor
				Log.warning(
						EMFTypePlugin.getPlugin(),
						EMFTypePluginStatusCodes.WRONG_CLIENT_CONTEXT,
						EMFTypeCoreMessages.bind(
								EMFTypeCoreMessages.wrong_type_WARN_,
								new Object[] { clientContext.getId(),
										EMFTypeCoreMessages.invalid_action_remove_context_WARN_,
										clientContext.toString(), existing.toString() }));
			} else if (!isDynamic(clientContext)) {
				Log.warning(
						EMFTypePlugin.getPlugin(),
						EMFTypePluginStatusCodes.DEPENDENCY_CONSTRAINT,
						EMFTypeCoreMessages.bind(EMFTypeCoreMessages.dependency_constraint_WARN_, new Object[] {
								clientContext.getId(), EMFTypeCoreMessages.invalid_action_remove_context_WARN_,
								EMFTypeCoreMessages.dependency_reason_static_WARN_ }));
			} else {
				clientContextMap.remove(clientContext.getId());
				clientContexts.remove(clientContext);

				result = true;
			}
		}

		if (result) {
			fireClientContextRemoved(clientContext);
		}
		
		return result;
	}
	
	/**
	 * Helper method to configure the <code>&lt;clientContext&gt;</code>
	 * occurrences amongst the <code>elements</code>.
	 * 
	 * @param elements
	 *            the top-level configuration elements on the
	 *            <code>elementTypeBindings</code> extension point
	 */
	private void configureClientContexts(IConfigurationElement[] elements) {
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement config = elements[i];

			if (E_CLIENT_CONTEXT.equals(config.getName())) {
				try {
					ClientContext context = new XMLClientContext(config);

					// prevent duplicates
					if (clientContexts.add(context)) {
						clientContextMap.put(context.getId(), context);
					}

				} catch (CoreException ce) {
					// this client context will not participate in extensible
					// type registry
					String sourcePluginId = config.getDeclaringExtension()
							.getContributor().getName();
					Log
							.error(
									EMFTypePlugin.getPlugin(),
									ce.getStatus().getCode(),
									EMFTypeCoreMessages
											.bind(
													EMFTypeCoreMessages.xml_parsing_elementTypeBindings_ERROR_,
													sourcePluginId, ce
															.getStatus()
															.getMessage()),
									ce);

				} catch (Exception e) {
					// this client context will not participate in extensible
					// type registry
					String sourcePluginId = config.getDeclaringExtension()
							.getContributor().getName();

					Log
							.error(
									EMFTypePlugin.getPlugin(),
									EMFTypePluginStatusCodes.ERROR_PARSING_XML,
									EMFTypeCoreMessages
											.bind(
													EMFTypeCoreMessages.xml_parsing_elementTypeBindings_ERROR_,
													sourcePluginId, e
															.getMessage()), e);
				}
			}
		}
	}

	/**
	 * Helper method to configure the <code>&lt;binding&gt;</code> occurrences
	 * amongst the <code>elements</code>.
	 * 
	 * @param elements
	 *            the top-level configuration elements on the
	 *            <code>elementTypeBindings</code> extension point
	 */
	private void configureBindings(IConfigurationElement[] elements) {
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement config = elements[i];

			if (E_BINDING.equals(config.getName())) {
				String contextId = config.getAttribute(A_CONTEXT);

				if (contextId == null) {
					Log
							.error(
									EMFTypePlugin.getPlugin(),
									EMFTypePluginStatusCodes.BINDING_NO_CONTEXT,
									EMFTypeCoreMessages
											.bind(
													EMFTypeCoreMessages.xml_parsing_elementTypeBindings_ERROR_,
													config
															.getDeclaringExtension()
															.getContributor()
															.getName(),
													EMFTypeCoreMessages.binding_noContextId_ERROR_));

				} else {
					ClientContext context = (ClientContext) getClientContext(contextId);

					if (context == null) {
						Log
								.error(
										EMFTypePlugin.getPlugin(),
										EMFTypePluginStatusCodes.BINDING_NO_SUCH_CONTEXT,
										EMFTypeCoreMessages
												.bind(
														EMFTypeCoreMessages.xml_parsing_elementTypeBindings_ERROR_,
														config
																.getDeclaringExtension()
																.getContributor()
																.getName(),
														EMFTypeCoreMessages.binding_noSuchContext_ERROR_));

					} else {
						configureBindings(context, config);
					}
				}
			}
		}
	}

	/**
	 * Helper method to process a particular binding element for its client
	 * <code>context</code>.
	 * 
	 * @param context
	 *            a client context referenced by a binding
	 * @param config
	 *            a particular <code>&lt;binding&gt;</config> element
	 */
	private void configureBindings(ClientContext context,
			IConfigurationElement config) {

		configureBindings(context, config, E_ELEMENT_TYPE);
		configureBindings(context, config, E_ADVICE);
	}

	/**
	 * Helper method to process a particular binding element for its client
	 * <code>context</code>.
	 * 
	 * @param context
	 *            a client context referenced by a binding
	 * @param config
	 *            a particular <code>&lt;binding&gt;</config> element
	 * @param elementName the name of the elements to process in the <code>&lt;binding&gt;</config> element
	 */
	private void configureBindings(ClientContext context,
			IConfigurationElement config, String elementName) {

		IConfigurationElement[] children = config.getChildren(elementName);

		for (int i = 0; i < children.length; i++) {
			String ref = children[i].getAttribute(A_REF);
			String patternString = children[i].getAttribute(A_PATTERN);

			if (ref == null && patternString == null) {
				// must be one of ref or pattern
				Log
						.error(
								EMFTypePlugin.getPlugin(),
								EMFTypePluginStatusCodes.BINDING_NO_REF_OR_PATTERN,
								EMFTypeCoreMessages
										.bind(
												EMFTypeCoreMessages.xml_parsing_elementTypeBindings_ERROR_,
												config.getDeclaringExtension()
														.getContributor()
														.getName(),
												EMFTypeCoreMessages
														.bind(
																EMFTypeCoreMessages.binding_no_ref_or_pattern_ERROR_,
																context.getId())));
				return;
			}

			if (ref != null && patternString != null) {
				// can't specify both ref and pattern
				Log
						.error(
								EMFTypePlugin.getPlugin(),
								EMFTypePluginStatusCodes.BINDING_BOTH_REF_AND_PATTERN,
								EMFTypeCoreMessages
										.bind(
												EMFTypeCoreMessages.xml_parsing_elementTypeBindings_ERROR_,
												config.getDeclaringExtension()
														.getContributor()
														.getName(),
												EMFTypeCoreMessages
														.bind(
																EMFTypeCoreMessages.binding_both_ref_and_pattern_ERROR_,
																context.getId())));
				return;
			}

			if (ref != null) {
				context.bindId(ref);

			} else {
				try {
					Pattern pattern = Pattern.compile(patternString);
					context.bindPattern(pattern);

				} catch (PatternSyntaxException pse) {
					Log
							.error(
									EMFTypePlugin.getPlugin(),
									EMFTypePluginStatusCodes.PATTERN_INVALID_SYNTAX,
									EMFTypeCoreMessages
											.bind(
													EMFTypeCoreMessages.xml_parsing_elementTypeBindings_ERROR_,
													config
															.getDeclaringExtension()
															.getContributor()
															.getName(),
													EMFTypeCoreMessages
															.bind(
																	EMFTypeCoreMessages.pattern_invalid_syntax_ERROR_,
																	patternString)), pse);
				}
			}
		}
	}

	/**
	 * Adds a {@code listener} for additions and removals of client contexts.
	 * Has no effect if the {@code listener} is already attached.
	 * 
	 * @param listener
	 *            the listener to add
	 * 
	 * @see #removeClientContextManagerListener(IClientContextManagerListener)
	 * 
	 * @since 1.9
	 */
	public void addClientContextManagerListener(IClientContextManagerListener listener) {
		listeners.addIfAbsent(listener);
	}

	/**
	 * Removes a {@code listener} from me. Has no effect if the {@code listener}
	 * is not currently attached.
	 * 
	 * @param listener
	 *            the listener to remove
	 * 
	 * @see #addClientContextManagerListener(IClientContextManagerListener)
	 * 
	 * @since 1.8
	 */
	public void removeClientContextManagerListener(IClientContextManagerListener listener) {
		listeners.remove(listener);
	}

	private void fireClientContextAdded(IClientContext context) {
		if (!listeners.isEmpty()) {
			ClientContextAddedEvent event = new ClientContextAddedEvent(context);
			for (IClientContextManagerListener next : listeners) {
				try {
					next.clientContextAdded(event);
				} catch (Exception e) {
					Log.warning(EMFTypePlugin.getPlugin(), EMFTypePluginStatusCodes.LISTENER_EXCEPTION,
							EMFTypeCoreMessages.contextManager_listener_exception_WARN_, e);
				}
			}
		}
	}

	private void fireClientContextRemoved(IClientContext context) {
		if (!listeners.isEmpty()) {
			ClientContextRemovedEvent event = new ClientContextRemovedEvent(context);
			for (IClientContextManagerListener next : listeners) {
				try {
					next.clientContextRemoved(event);
				} catch (Exception e) {
					Log.warning(EMFTypePlugin.getPlugin(), EMFTypePluginStatusCodes.LISTENER_EXCEPTION,
							EMFTypeCoreMessages.contextManager_listener_exception_WARN_, e);
				}
			}
		}
	}
}
