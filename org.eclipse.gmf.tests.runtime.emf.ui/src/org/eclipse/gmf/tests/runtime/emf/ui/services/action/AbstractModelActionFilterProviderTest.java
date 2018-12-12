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

package org.eclipse.gmf.tests.runtime.emf.ui.services.action;

import junit.framework.TestCase;

import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.core.util.StringStatics;
import org.eclipse.gmf.runtime.emf.ui.services.action.AbstractModelActionFilterProvider;

/**
 * Tests the AbstractModelActionFilterProvider.
 * 
 * @author ldamus
 */
public class AbstractModelActionFilterProviderTest
    extends TestCase {

    protected static class MyModelActionFilterProvider
        extends AbstractModelActionFilterProvider {

        private boolean testedAttribute = false;

        private TransactionalEditingDomain editingDomain = TransactionalEditingDomain.Factory.INSTANCE
            .createEditingDomain();

        protected boolean doTestAttribute(Object target, String name,
                String value) {
            
            testedAttribute = true;
            return true;
        }

        protected boolean doProvides(IOperation operation) {
            return true;
        }

        protected TransactionalEditingDomain getEditingDomain(Object target) {
            return editingDomain;
        }

        public boolean didTestAttribute() {
            return testedAttribute;
        }
    }

    /**
     * Tests that subclasses of AbstractModelActionFilterProviderTest can
     * override the editing domain getter method.
     */
    public void test_editingDomainOverride_130758() {

        MyModelActionFilterProvider provider = new MyModelActionFilterProvider();

        // pass in an object that will return null in the superclass
        // getEditingDomain implementation
        boolean result = provider.testAttribute(Boolean.FALSE,
            StringStatics.BLANK, null);

        assertTrue(result);
        assertTrue(provider.didTestAttribute());
    }
}
