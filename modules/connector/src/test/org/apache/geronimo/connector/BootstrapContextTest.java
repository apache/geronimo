/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.connector;

import java.util.Timer;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;

import junit.framework.TestCase;

/**
 * Unit tests for {@link BootstrapContext}
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:11 $
 */
public class BootstrapContextTest extends TestCase {

    /**
     * Creates a new instance of BootstrapContextTest
     * @param name the name of the test
     */
    public BootstrapContextTest(String name) {
        super(name);
    }

    /**
     * Tests get and set work manager
     */
    public void testGetSetWorkManager() {
        BootstrapContext context = new BootstrapContext();
        MockWorkManager manager = new MockWorkManager("testGetSetWorkManager");
        context.setWorkManager(manager);
        WorkManager wm = context.getWorkManager();

        assertTrue("Make sure it is the same object", manager.equals(wm));
    }

    /**
     * Tests get and set XATerminator
     */
    public void testGetSetXATerminator() {
        BootstrapContext context = new BootstrapContext();
        MockXATerminator t = new MockXATerminator("testGetSetXATerminator");
        context.setXATerminator(t);
        XATerminator xat = context.getXATerminator();

        assertTrue("Make sure it is the same object", t.equals(xat));
    }

    /**
     * Tests getTimer
     */
    public void testGetTimer() throws Exception {
        BootstrapContext context = new BootstrapContext();
        Timer t = context.createTimer();
        assertNotNull("Object is not null", t);
    }

}
