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
package org.apache.geronimo.ejb.container;

import javax.transaction.TransactionManager;

import org.apache.geronimo.cache.InstanceCache;
import org.apache.geronimo.cache.InstanceFactory;
import org.apache.geronimo.cache.InstancePool;
import org.apache.geronimo.ejb.EJBProxyFactoryManager;
import org.apache.geronimo.lock.LockDomain;
import org.apache.geronimo.ejb.metadata.EJBMetadata;
import org.apache.geronimo.common.Container;

/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:51:54 $
 */
public final class EJBPlugins {
    // only static methods are allowed for this class
    private EJBPlugins() {}

    public static String EJB_PROXY_FACTORY_MANAGER = "EJB Proxy Factory Manager";
    public static String EJB_METADATA = "EJB Metadata";
    public static String TRANSACTION_MANAGER = "Transaction Manager";
    public static String EJB_CONTEXT_INSTANCE_FACTORY = "EJB Context Instance Factory";
    public static String EJB_CONTEXT_INSTANCE_POOL = "EJB Context Instance Pool";
    public static String EJB_CONTEXT_INSTANCE_CACHE = "EJB Context Instance Cache";
    public static String PERSISTENCE_MANAGER = "Persustence Manager";
    public static String LOCK_DOMAIN = "Lock Domain";

    public static EJBProxyFactoryManager getEJBProxyFactoryManager(Container container) {
        return (EJBProxyFactoryManager) container.getPluginObject(EJB_PROXY_FACTORY_MANAGER);
    }

    public static void putEJBProxyFactoryManager(Container container, EJBProxyFactoryManager ejbProxyFactoryManager) {
        container.putPluginObject(EJB_PROXY_FACTORY_MANAGER, ejbProxyFactoryManager);
    }

    public static EJBMetadata getEJBMetadata(Container container) {
        return (EJBMetadata) container.getPluginObject(EJB_METADATA);
    }

    public static void putEJBMetadata(Container container, EJBMetadata ejbMetadata) {
        container.putPluginObject(EJB_METADATA, ejbMetadata);
    }

    public static TransactionManager getTransactionManager(Container container) {
        return (TransactionManager) container.getPluginObject(TRANSACTION_MANAGER);
    }

    public static void putTransactionManager(Container container, TransactionManager transactionManager) {
        container.putPluginObject(TRANSACTION_MANAGER, transactionManager);
    }

    public static InstanceFactory getInstanceFactory(Container container) {
        return (InstanceFactory) container.getPluginObject(EJB_CONTEXT_INSTANCE_FACTORY);
    }

    public static void putInstanceFactory(Container container, InstanceFactory instanceFactory) {
        container.putPluginObject(EJB_CONTEXT_INSTANCE_FACTORY, instanceFactory);
    }

    public static InstancePool getInstancePool(Container container) {
        return (InstancePool) container.getPluginObject(EJB_CONTEXT_INSTANCE_POOL);
    }

    public static void putInstancePool(Container container, InstancePool instancePool) {
        container.putPluginObject(EJB_CONTEXT_INSTANCE_POOL, instancePool);
    }

    public static InstanceCache getInstanceCache(Container container) {
        return (InstanceCache) container.getPluginObject(EJB_CONTEXT_INSTANCE_CACHE);
    }

    public static void putInstanceCache(Container container, InstanceCache instanceCache) {
        container.putPluginObject(EJB_CONTEXT_INSTANCE_CACHE, instanceCache);
    }

    public static PersistenceManager getPersistenceManager(Container container) {
        return (PersistenceManager) container.getPluginObject(EJB_CONTEXT_INSTANCE_CACHE);
    }

    public static void putPersistenceManager(Container container, PersistenceManager persistenceManager) {
        container.putPluginObject(EJB_CONTEXT_INSTANCE_CACHE, persistenceManager);
    }

    public static LockDomain getLockDomain(Container container) {
        return (LockDomain) container.getPluginObject(LOCK_DOMAIN);
    }

    public static void putLockDomain(Container container, LockDomain lockDomain) {
        container.putPluginObject(LOCK_DOMAIN, lockDomain);
    }
}
