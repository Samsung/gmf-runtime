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


package org.eclipse.gmf.tests.runtime.diagram.ui.commands;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.core.internal.commands.SendBackwardCommand;
import org.eclipse.gmf.runtime.notation.View;


/**
 * Test the SendBackward ZOrder command
 *
 * @author jschofie
 */
public class SendBackwardCommandTest
	extends CommandTestFixture {

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.tests.runtime.diagram.ui.commands.CommandTestFixture#createCommand()
	 */
	protected ICommand createCommand() {
		return new SendBackwardCommand(getEditingDomain(), noteView);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.tests.runtime.diagram.ui.commands.CommandTestFixture#testDoExecute()
	 */
	public void testDoExecute() {
		assertEquals(getDiagram().getChildren().size(), 1);
		View firstNote  = (View)getDiagram().getChildren().get(0);

		ICommand zorderCommand = new SendBackwardCommand(getEditingDomain(), firstNote);
		
        try {
            zorderCommand.execute(new NullProgressMonitor(), null);
        } catch (ExecutionException e) {
            fail("command execution failure: " + e.getLocalizedMessage()); //$NON-NLS-1$
        }

		assertEquals(getDiagram().getChildren().get(0), firstNote);
	}

	public void testLastEntry() {
		createView();
		assertEquals(getDiagram().getChildren().size(), 2);
		View firstNote  = (View)getDiagram().getChildren().get(0);
		View secondNote = (View)getDiagram().getChildren().get(1);

		ICommand zorderCommand = new SendBackwardCommand(getEditingDomain(), secondNote);
		
        try {
            zorderCommand.execute(new NullProgressMonitor(), null);
        } catch (ExecutionException e) {
            fail("command execution failure: " + e.getLocalizedMessage()); //$NON-NLS-1$
        }

		assertEquals(getDiagram().getChildren().get(0), secondNote);
		assertEquals(getDiagram().getChildren().get(1), firstNote);
	}
	
	public void testMiddleEntry() {
		createView();
		createView();
		assertEquals(getDiagram().getChildren().size(), 3);
		View firstNote  = (View)getDiagram().getChildren().get(0);
		View secondNote = (View)getDiagram().getChildren().get(1);
		View thirdNote  = (View)getDiagram().getChildren().get(2);

		ICommand zorderCommand = new SendBackwardCommand(getEditingDomain(), secondNote);
		
        try {
            zorderCommand.execute(new NullProgressMonitor(), null);
        } catch (ExecutionException e) {
            fail("command execution failure: " + e.getLocalizedMessage()); //$NON-NLS-1$
        }

		assertEquals(getDiagram().getChildren().get(0), secondNote);
		assertEquals(getDiagram().getChildren().get(1), firstNote);
		assertEquals(getDiagram().getChildren().get(2), thirdNote);
	}
	
	public void testSendToBack() {
		createView();
		createView();
		assertEquals(getDiagram().getChildren().size(), 3);
		View firstNote  = (View)getDiagram().getChildren().get(0);
		View secondNote = (View)getDiagram().getChildren().get(1);
		View thirdNote  = (View)getDiagram().getChildren().get(2);

		ICommand zorderCommand = new SendBackwardCommand(getEditingDomain(), thirdNote);
		
        try {
            zorderCommand.execute(new NullProgressMonitor(), null);
        } catch (ExecutionException e) {
            fail("command execution failure: " + e.getLocalizedMessage()); //$NON-NLS-1$
        }
		assertEquals(getDiagram().getChildren().get(0), firstNote);
		assertEquals(getDiagram().getChildren().get(1), thirdNote);
		assertEquals(getDiagram().getChildren().get(2), secondNote);
		
		zorderCommand = new SendBackwardCommand(getEditingDomain(), thirdNote);
		
        try {
            zorderCommand.execute(new NullProgressMonitor(), null);
        } catch (ExecutionException e) {
            fail("command execution failure: " + e.getLocalizedMessage()); //$NON-NLS-1$
        }
		
		assertEquals(getDiagram().getChildren().get(0), thirdNote);
		assertEquals(getDiagram().getChildren().get(1), firstNote);
		assertEquals(getDiagram().getChildren().get(2), secondNote);
	}
}
