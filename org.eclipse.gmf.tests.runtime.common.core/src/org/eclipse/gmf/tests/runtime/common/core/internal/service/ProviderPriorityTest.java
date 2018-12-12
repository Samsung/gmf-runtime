/******************************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.tests.runtime.common.core.internal.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.eclipse.gmf.runtime.common.core.service.ProviderPriority;

/**
 * @author khussey
 */
public class ProviderPriorityTest extends TestCase {

    protected static class Fixture extends ProviderPriority {

    	private static final long serialVersionUID = 1L;

        protected Fixture() {
            super("Fixture", 0); //$NON-NLS-1$
        }

        protected List getValues() {
            return super.getValues();
        }

    }

    private Fixture fixture = null;

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(ProviderPriorityTest.class);
    }

    public ProviderPriorityTest(String name) {
        super(name);
    }

    protected Fixture getFixture() {
        return fixture;
    }

    private void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }

    protected void setUp() {
        setFixture(new Fixture());
    }

    public void test_readResolve() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        ObjectOutput output = null;
        ObjectInput input = null;
        try {
            output = new ObjectOutputStream(stream);
            for (Iterator i = getFixture().getValues().iterator();
                i.hasNext();
                ) {
                output.writeObject(i.next());
            }
            output.flush();

            input =
                new ObjectInputStream(
                    new ByteArrayInputStream(stream.toByteArray()));
            for (Iterator i = getFixture().getValues().iterator();
                i.hasNext();
                ) {
                assertSame(i.next(), input.readObject());
            }
        } catch (Exception e) {
            fail();
        } finally {
            try {
                output.close();
                input.close();
            } catch (Exception e) {
            	// Nothing to do
            }
        }
    }

}
