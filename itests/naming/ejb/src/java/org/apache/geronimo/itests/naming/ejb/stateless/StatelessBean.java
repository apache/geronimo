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
package org.apache.geronimo.itests.naming.ejb.stateless;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.notgeronimo.itests.naming.common.Test;

/**
 * @version $Rev:  $ $Date:  $
 */
public class StatelessBean implements SessionBean {

    public void testWebService() {
        Test test = new Test();
        try {
            test.testWebServiceLookup();
        } catch (Exception e) {
            throw new EJBException(e);
        }

    }

    public void ejbCreate() {

    }

    public void ejbActivate() {

    }

    public void ejbPassivate() {

    }

    public void ejbRemove() {

    }

    public void setSessionContext(SessionContext ctx) {

    }
}
