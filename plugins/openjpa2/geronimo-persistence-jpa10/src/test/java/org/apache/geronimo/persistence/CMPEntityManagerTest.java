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

package org.apache.geronimo.persistence;

import java.util.Map;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;
import javax.ejb.EJBException;

import junit.framework.TestCase;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.geronimo.persistence.mockjpa.MockEntityManagerFactory;
import org.apache.geronimo.persistence.mockjpa.MockEntityManager;

/**
 * @version $Rev$ $Date$
 */
public class CMPEntityManagerTest extends TestCase {

    private GeronimoTransactionManager tm;
    private String persistenceUnit = "foo";
    private MockEntityManagerFactory entityManagerFactory;

    protected void setUp() throws Exception {
        tm = new GeronimoTransactionManager();
        tm.addTransactionAssociationListener(new TransactionListener());
        entityManagerFactory = new MockEntityManagerFactory();
    }

    /**
     * section 3.1.1
     * (not very clear).  getTransaction, joinTransaction throw IllegalStateException
     */
    public void testGetTransaction() throws Exception {
        CMPEntityManagerTxScoped entityManager1 = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, null);
        try {
            entityManager1.getTransaction();
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            //expected
        } catch (Exception e) {
            fail("Wrong exception " + e);
        }
        tm.begin();
        try {
            entityManager1.getTransaction();
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            //expected
        } catch (Exception e) {
            fail("Wrong exception " + e);
        }
        tm.commit();
    }

    public void testJoinTransaction() throws Exception {
        CMPEntityManagerTxScoped entityManager1 = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, null);
        try {
            entityManager1.joinTransaction();
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            //expected
        } catch (Exception e) {
            fail("Wrong exception " + e);
        }
        tm.begin();
        try {
            entityManager1.joinTransaction();
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            //expected
        } catch (Exception e) {
            fail("Wrong exception " + e);
        }
        tm.commit();
    }

    /**
     * section 3.1.1 ????
     * isOpen returns true
     */
    public void testIsOpen() throws Exception {
        CMPEntityManagerTxScoped entityManager1 = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, null);
        assertTrue(entityManager1.isOpen());
        tm.begin();
        assertTrue(entityManager1.isOpen());
        tm.commit();
        assertTrue(entityManager1.isOpen());
        tm.begin();
        assertTrue(entityManager1.isOpen());
        tm.rollback();
        assertTrue(entityManager1.isOpen());
    }

    /**
     * section 5.6.2
     * extended context is closed when the SFSB that caused it is removed
     */
//    public void testExtendedClosedOnBeanRemove() throws Exception {
//        CMPEntityManagerExtended entityManager1 = new CMPEntityManagerExtended(entityManagerRegistry, entityManagerFactory, null);
//        MockEntityManager pc1 = (MockEntityManager) entityManager1.find(EntityManager.class, "this");
//        assertTrue("base EntityManager should not be closed", !pc1.isClosed());
//        assertNotNull("InternalEntityManager should be registered", EntityManagerExtendedRegistry.getEntityManager(persistenceUnit));
//        entityManager1.beanRemoved();
//        assertTrue("base EntityManager should be closed", pc1.isClosed());
//        assertNull("InternalEntityManager should not be registered", EntityManagerExtendedRegistry.getEntityManager(persistenceUnit));
//    }

    /**
     * section 5.6.2.1
     * extended context is closed when the SFSB that caused it and all others that share it are removed
     */
//    public void testInheritedExtendedClosedOnBeanRemove() throws Exception {
//        CMPEntityManagerExtended entityManager1 = new CMPEntityManagerExtended(entityManagerRegistry, entityManagerFactory, null);
//        MockEntityManager pc1 = (MockEntityManager) entityManager1.find(EntityManager.class, "this");
//        assertTrue("base EntityManager should not be closed", !pc1.isClosed());
//        InternalCMPEntityManagerExtended internalEntityManager1 = EntityManagerExtendedRegistry.getEntityManager(persistenceUnit);
//        assertNotNull("InternalEntityManager should be registered", internalEntityManager1);
//        CMPEntityManagerExtended entityManager2 = new CMPEntityManagerExtended(entityManagerRegistry, entityManagerFactory, null);
//        InternalCMPEntityManagerExtended internalEntityManager2 = EntityManagerExtendedRegistry.getEntityManager(persistenceUnit);
//        //we should have got an exception if this isn't true
//        assertSame("2nd entity manager registering should use same internal entity manager", internalEntityManager1, internalEntityManager2);
//        MockEntityManager pc2 = (MockEntityManager) entityManager2.find(EntityManager.class, "this");
//        assertSame("2nd entity manager registering should use same mock entity manager", pc1, pc2);
//
//        //remove one bean, internal and mock entity managers should not change state
//        entityManager1.beanRemoved();
//        assertTrue("base EntityManager should not be closed", !pc1.isClosed());
//        assertNotNull("InternalEntityManager should be registered", EntityManagerExtendedRegistry.getEntityManager(persistenceUnit));
//
//        //close other bean, everything should close and unregister
//        entityManager2.beanRemoved();
//        assertTrue("base EntityManager should be closed", pc1.isClosed());
//        assertNull("InternalEntityManager should not be registered", EntityManagerExtendedRegistry.getEntityManager(persistenceUnit));
//    }

    /**
     * section 5.6.3.1
     * Trying to propagate a JTA tx with a persistence context bound into a SFSB with Extended persistence context
     * results in an EJBException
     */
//    public void testNoSimultaneousEntityManagers() throws Exception {
//        //set up the extended persistence context:
//        CMPEntityManagerExtended entityManager1 = new CMPEntityManagerExtended(entityManagerRegistry, entityManagerFactory, null);
//        //set up the caller
//        CMPEntityManagerTxScoped entityManager2 = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, null);
//        tm.begin();
//        //register the caller
//        MockEntityManager pc1 = (MockEntityManager) entityManager2.find(EntityManager.class, "this");
//        //caller calling SFSB means entityManager1 tries to join the trasaction:
//        InternalCMPEntityManagerExtended internalEntityManager = EntityManagerExtendedRegistry.getEntityManager(persistenceUnit);
//        try {
//            internalEntityManager.joinTransaction();
//            fail("Expected EJBException");
//        } catch (EJBException e) {
//            //expected
//        } catch (Exception e) {
//            fail("Unexpected exception " + e);
//        }
//        tm.commit();
//    }

    /**
     * section 5.8.2
     * use the same persistence context for all work in a tx
     */
    public void testSamePersistenceContext() throws Exception {
        tm.begin();
        CMPEntityManagerTxScoped entityManager1 = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, null);
        EntityManager pc1 = entityManager1.find(EntityManager.class, "this");
        CMPEntityManagerTxScoped entityManager2 = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, null);
        EntityManager pc2 = entityManager2.find(EntityManager.class, "this");
        assertSame("Should get same entity manager for all work in a tx", pc1, pc2);
        tm.commit();
    }

    /**
     * section 5.9.1
     * close or cleared is called when tx commits
     */
    public void testCloseOnCommit() throws Exception {
        tm.begin();
        CMPEntityManagerTxScoped entityManager1 = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, null);
        MockEntityManager pc1 = (MockEntityManager) entityManager1.find(EntityManager.class, "this");
        assertTrue("entityManager should not be closed or cleared", !pc1.isClosed() & !pc1.isCleared());
        tm.commit();
        assertTrue("entityManager should be closed or cleared", pc1.isClosed() || pc1.isCleared());
        tm.begin();
        CMPEntityManagerTxScoped entityManager2 = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, null);
        MockEntityManager pc2 = (MockEntityManager) entityManager2.find(EntityManager.class, "this");
        assertTrue("entityManager should not be closed or cleared", !pc2.isClosed() & !pc2.isCleared());
        tm.rollback();
        assertTrue("entityManager should be closed or cleared", pc2.isClosed() || pc2.isCleared());
    }

    /**
     * section 5.9.1
     * transaction required for persist, remove, merge, refresh
     */
    public void testTransactionRequired() throws Exception {
        CMPEntityManagerTxScoped entityManager1 = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, null);
        try {
            entityManager1.persist("foo");
            fail("expected TransactionRequiredException");
        } catch (TransactionRequiredException e) {
            //expected
        } catch (Exception e) {
            fail("Wrong exception" + e);
        }
        try {
            entityManager1.remove("foo");
            fail("expected TransactionRequiredException");
        } catch (TransactionRequiredException e) {
            //expected
        } catch (Exception e) {
            fail("Wrong exception" + e);
        }
        try {
            entityManager1.merge("foo");
            fail("expected TransactionRequiredException");
        } catch (TransactionRequiredException e) {
            //expected
        } catch (Exception e) {
            fail("Wrong exception" + e);
        }
        try {
            entityManager1.refresh("foo");
            fail("expected TransactionRequiredException");
        } catch (TransactionRequiredException e) {
            //expected
        } catch (Exception e) {
            fail("Wrong exception" + e);
        }
    }

    /**
     * section 5.9.1
     * when a SFSB/extended context starts a UserTransaction or a CMT tx starts the EM must join the transaction
     */
//    public void testExtendedEntityManagerJoinsNewTransactions() throws Exception {
//        CMPEntityManagerExtended entityManager1 = new CMPEntityManagerExtended(entityManagerRegistry, entityManagerFactory, null);
//        tm.begin();
//        MockEntityManager pc1 = (MockEntityManager) entityManager1.find(EntityManager.class, "this");
//
//        assertTrue("EntityManager was supposed to join the tx", pc1.isJoined());
//    }

    /**
     * section 5.9.1
     * application must not call close on its entityManager
     */
    public void testAppCallsCloseForbidden() throws Exception {
        CMPEntityManagerTxScoped entityManager1 = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, null);
        try {
            entityManager1.close();
            fail("Application should not be able to call close on its EntityManager");
        } catch (IllegalStateException e) {
            //expected
        }
        tm.begin();
        try {
            entityManager1.close();
            fail("Application should not be able to call close on its EntityManager");
        } catch (IllegalStateException e) {
            //expected
        }
        tm.commit();
    }


    /**
     * section 5.9.1
     *
     * @throws Exception
     */
    public void testNoPropertiesUsed() throws Exception {

        CMPEntityManagerTxScoped entityManager = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, null);
        tm.begin();
        entityManager.contains("bar");
        Map props = entityManager.find(Map.class, "properties");
        assertSame("Props are not null", props, null);
        tm.commit();
    }

    /**
     * section 5.9.1
     *
     * @throws Exception
     */
    public void testPropertiesUsed() throws Exception {
        Map properties = new HashMap();
        CMPEntityManagerTxScoped entityManager = new CMPEntityManagerTxScoped(tm, persistenceUnit, entityManagerFactory, properties);
        tm.begin();
        entityManager.contains("bar");
        Map props = entityManager.find(Map.class, "properties");
        assertSame("Props are not what was passed in", props, properties);
        tm.commit();
    }
}
