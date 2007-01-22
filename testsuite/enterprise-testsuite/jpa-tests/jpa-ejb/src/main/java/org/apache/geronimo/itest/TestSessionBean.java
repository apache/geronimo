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
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.geronimo.itest.jpa.AllFieldTypes;

/**
 * @version $Rev$ $Date$
 */
public class TestSessionBean implements SessionBean {

    SessionContext sessionContext;

    public boolean testEntityManager() {
        try {
            EntityManager entityManager = (EntityManager) new InitialContext().lookup("java:comp/env/jpa/test");
            System.out.println("Accessed entity manager");
            AllFieldTypes allFieldTypes = new AllFieldTypes();
            entityManager.persist(allFieldTypes);
            System.out.println("saved object");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not access entity manager");
            return false;
        }
    }

    public boolean testEntityManagerFactory() {
        try {
            EntityManagerFactory entityManagerFactory = (EntityManagerFactory) new InitialContext().lookup("java:comp/env/jpa/testEMF");
            System.out.println("Accessed entity manager factory");
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            AllFieldTypes allFieldTypes = new AllFieldTypes();
            entityManager.persist(allFieldTypes);
            System.out.println("saved object");
            entityManager.close();
            System.out.println("Closed entity manager");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not access entity manager");
            return false;
        }
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
