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

import javax.ejb.SessionBean;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;

/**
 * @version $Rev$ $Date$
 */
@javax.annotation.security.DeclareRoles({"foo", "bar"})
public class TestSessionBean implements SessionBean {

    SessionContext sessionContext;

    public String testAccess() {
        return sessionContext.getCallerPrincipal().getName();
    }

    public String testNoAccess() {
        return sessionContext.getCallerPrincipal().getName();
    }

    public boolean isCallerInRole(String role) {
        return sessionContext.isCallerInRole(role);
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
