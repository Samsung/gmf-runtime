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
package org.eclipse.gmf.tests.runtime.common.ui.services.provider;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * The org.eclipse.gmf.tests.runtime.common.ui.services.provider tests plug-in
 * is not a JUnit test plug-in. This plug-in is used by
 * org.eclipse.gmf.tests.runtime.common.ui for ProviderPolicyExceptionsTest and
 * ProviderPolicyTest.
 * <p>
 * This AllTestsTest simply is a place holder if someone does try to run the
 * plug-in as a tests plug-in.
 */
public class AllTestsTest
	extends TestCase {

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	public static Test suite() {
		return new TestSuite(AllTestsTest.class);
	}

	public void test_testNothing() {
		// the test is successful
	}
}
