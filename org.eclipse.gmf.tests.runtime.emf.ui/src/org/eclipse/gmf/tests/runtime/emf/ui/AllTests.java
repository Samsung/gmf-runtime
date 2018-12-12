/******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.tests.runtime.emf.ui;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.gmf.tests.runtime.emf.ui.action.AbstractModelActionDelegateTest;
import org.eclipse.gmf.tests.runtime.emf.ui.action.AbstractModelActionHandlerTest;
import org.eclipse.gmf.tests.runtime.emf.ui.services.action.AbstractModelActionFilterProviderTest;


/**
 * @author Anthony Hunter 
 * <a href="mailto:anthonyh@ca.ibm.com">mailto:anthonyh@ca.ibm.com</a>
 */
public class AllTests extends TestCase implements IPlatformRunnable {

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
        suite.addTestSuite(AbstractModelActionDelegateTest.class);
        suite.addTestSuite(AbstractModelActionHandlerTest.class);
        suite.addTestSuite(AbstractModelActionFilterProviderTest.class);
		suite.addTestSuite(ModelingAssistantServiceTests.class);
		return suite;
	}

	public AllTests() {
		super(""); //$NON-NLS-1$
	}

	public Object run(Object args) throws Exception {
		TestRunner.run(suite());
		return Arrays.asList(new String[] { "Please see raw test suite output for details." }); //$NON-NLS-1$
	}

}
