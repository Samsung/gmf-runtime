/******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.common.ui.action;

import java.util.EventObject;

/**
 * Represent an event that is fired when an action manager changes. Instances of
 * this class have an associated action manager (the source of the event) and
 * action (the action that was run).
 * 
 * @author khussey
 * 
 * @see org.eclipse.gmf.runtime.common.ui.action.IActionManagerChangeListener
 */
public class ActionManagerChangeEvent extends EventObject {

	private static final long serialVersionUID = 1L;

    /**
     * The action that was run.
     */
    private final IActionWithProgress action;

    /**
     * Constructs a new action manager change event for the specified action
     * manager.
     * 
     * @param source The action manager that changed.
     */
    public ActionManagerChangeEvent(ActionManager source) {
        this(source, null);
    }

    /**
     * Constructs a new action manager change event for the specified action
     * manager and action.
     * 
     * @param source The action manager that changed.
     * @param action The action that has been run.
     */
    public ActionManagerChangeEvent(
        ActionManager source,
        IActionWithProgress action) {

        super(source);

        this.action = action;
    }

    /**
     * Retrieves the value of the <code>action</code> instance variable.
     * 
     * @return The value of the <code>action</code> instance variable.
     */
    public IActionWithProgress getAction() {
        return action;
    }

    /**
     * Sets the <code>source</code> instance variable to the specified value.
     * 
     * @param source The new value for the <code>source</code> instance
     *                variable.
     */
    protected void setSource(ActionManager source) {
        assert null != source;

        this.source = source;
    }

}
