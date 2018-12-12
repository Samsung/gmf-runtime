/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.gmf.runtime.gef.ui.internal.editparts;

import org.eclipse.gef.editparts.ZoomListener;

/**
 * Listens to animated zoom changes.
 * @author Steve Shaw
 */
public interface AnimatedZoomListener extends ZoomListener {

	/**
	 * Called whenever the ZoomManager's starts an animated
	 * zoom.
	 */
	void animatedZoomStarted();

	/**
	 * Called whenever the ZoomManager's ends an animated
	 * zoom.
	 */
	void animatedZoomEnded();
}
