/******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.properties.filters;

import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart;
import org.eclipse.jface.viewers.IFilter;

/**
 * Filter to display a property section if the selection is a connection edit part.
 * 
 * @author Anthony Hunter
 */
public class ConnectionEditPartPropertySectionFilter
	implements IFilter {

    public boolean select(Object object) {
        if (object instanceof ConnectionEditPart)
            return true;

        return false;
    }
}