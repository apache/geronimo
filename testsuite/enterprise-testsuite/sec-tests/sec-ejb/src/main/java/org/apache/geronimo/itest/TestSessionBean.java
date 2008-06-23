/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.itest;

import javax.annotation.security.DeclareRoles;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * @version $Rev$ $Date$
 */
@DeclareRoles({"foo", "bar", "baz"})
public class TestSessionBean implements SessionBean {

    SessionContext sessionContext;

    public String testAccessBar() {
        return testAccess();
    }

    public String testAccessFoo() {
        return testAccess();
    }

    public String testAccessBaz() {
        return testAccess();
    }

    private String testAccess() {
        StringBuilder r = new StringBuilder("Test EJB principal: ").append(sessionContext.getCallerPrincipal().getName()).append("\n");
        r.append("TestSession isCallerInRole foo: ").append(sessionContext.isCallerInRole("foo")).append("\n");
        r.append("TestSession isCallerInRole bar: ").append(sessionContext.isCallerInRole("bar")).append("\n");
        r.append("TestSession isCallerInRole baz: ").append(sessionContext.isCallerInRole("baz")).append("\n");
        return r.toString();
    }

    public void ejbCreate() {
        System.out.println("TestSessionBean");
    }

    public void ejbActivate() throws EJBException {
    }

    public void ejbPassivate() throws EJBException {
    }

    public void ejbRemove() throws EJBException {
    }

    public void setSessionContext(SessionContext sessionContext) throws EJBException {
        this.sessionContext = sessionContext;
    }
}
