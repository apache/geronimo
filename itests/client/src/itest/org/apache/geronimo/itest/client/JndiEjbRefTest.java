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

package org.apache.geronimo.itest.client;

import javax.naming.InitialContext;

import junit.framework.TestCase;
import org.openejb.test.entity.bmp.BasicBmpHome;
import org.openejb.test.entity.bmp.BasicBmpObject;
import org.openejb.test.stateful.BasicStatefulHome;
import org.openejb.test.stateful.BasicStatefulObject;
import org.openejb.test.stateless.BasicStatelessHome;
import org.openejb.test.stateless.BasicStatelessObject;

public class JndiEjbRefTest extends TestCase {


    public void testLookupEntityBean() throws Exception {
        InitialContext ctx = new InitialContext();
        assertNotNull("The InitialContext is null", ctx);

        BasicBmpHome home = (BasicBmpHome) ctx.lookup("java:comp/env/ejb/bmp_entity");
        assertNotNull("The EJBHome looked up is null", home);

        BasicBmpObject object = home.create("Enc Bean");
        assertNotNull("The EJBObject is null", object);
    }

    public void testLookupStatefulBean() throws Exception {

        InitialContext ctx = new InitialContext();
        assertNotNull("The InitialContext is null", ctx);

        BasicStatefulHome home = (BasicStatefulHome) ctx.lookup("java:comp/env/ejb/stateful");
        assertNotNull("The EJBHome looked up is null", home);

        BasicStatefulObject object = home.create("Enc Bean");
        assertNotNull("The EJBObject is null", object);

    }

    public void testLookupStatelessBean() throws Exception {

        InitialContext ctx = new InitialContext();
        assertNotNull("The InitialContext is null", ctx);

        BasicStatelessHome home = (BasicStatelessHome) ctx.lookup("java:comp/env/ejb/stateless");
        assertNotNull("The EJBHome looked up is null", home);

        BasicStatelessObject object = home.create();
        assertNotNull("The EJBObject is null", object);

    }

}
