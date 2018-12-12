/******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/
package org.eclipse.gmf.runtime.diagram.ui.printing.render.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The Diagram UI Printing Render plug-in.
 * 
 * @author cmahoney
 */
public class DiagramUIPrintingRenderPlugin
	extends AbstractUIPlugin {

	/**
	 * The shared instance.
	 */
	private static DiagramUIPrintingRenderPlugin plugin;

	/**
	 * The constructor.
	 * 
	 * @see org.eclipse.core.runtime.Plugin#Plugin()
	 */
	public DiagramUIPrintingRenderPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the plugin instance
	 */
	public static DiagramUIPrintingRenderPlugin getInstance() {
		return plugin;
	}

	/**
	 * Retrieves the unique identifier of this plug-in.
	 * 
	 * @return A non-empty string which is unique within the plug-in registry.
	 */
	public static String getPluginId() {
		return getInstance().getBundle().getSymbolicName();
	}

}