/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.naming.java;

import javax.naming.InitialContext;
import javax.naming.LinkRef;

import junit.framework.TestCase;

/**
 * Test component context can be inherited by Threads spawned by
 * a component. This is required for Application Client and Servlets;
 * it is not applicable to EJBs as they are not allowed to create Threads.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:09 $
 */
public class ThreadContextTest extends TestCase {

    private Throwable failure = null;
    public void testThreadInheritence() throws Throwable {
        Thread worker = new Thread() {
            public void run() {
                try {
                    assertEquals("Hello", new InitialContext().lookup("java:comp/env/hello"));
                } catch (Throwable e) {
                    failure = e;
                }
            }
        };
        worker.start();
        worker.join();
        if (failure != null) {
            throw failure;
        }
    }

    protected void setUp() throws Exception {
        ReadOnlyContext readOnlyContext = new ReadOnlyContext();
        readOnlyContext.internalBind("env/hello", "Hello");
        readOnlyContext.internalBind("env/world", "Hello World");
        readOnlyContext.internalBind("env/link", new LinkRef("java:comp/env/hello"));
        RootContext.setComponentContext(readOnlyContext);
    }
}
