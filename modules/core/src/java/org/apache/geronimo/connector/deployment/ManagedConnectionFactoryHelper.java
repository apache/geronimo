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

package org.apache.geronimo.connector.deployment;

import javax.management.ObjectName;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;
import org.apache.geronimo.naming.ger.GerContextManager;
import org.apache.geronimo.connector.outbound.ConnectionManagerFactory;

/**
 * ManagedConnectionFactoryHelper
 *
 * @version $Revision: 1.5 $ $Date: 2004/01/21 22:21:25 $
 */
public class ManagedConnectionFactoryHelper implements GeronimoMBeanTarget {

    private static final Log log = LogFactory.getLog(ManagedConnectionFactoryHelper.class);

    static final String TARGET_NAME = "mcfHelper";

    private String connectionFactoryImplClass;
    private String connectionFactoryInterface;
    private String connectionImplClass;
    private String connectionInterface;

    private String ConnectionManagerFactoryClass;

    private String managedConnectionFactoryClass;

    private String globalJNDIName;

    private ResourceAdapterHelper resourceAdapterHelper;
    private ConnectionManagerFactory connectionManagerFactory;

    private Object connectionFactory;


    private boolean registered = false;
    private GeronimoMBeanContext mbeanContext;

    /* (non-Javadoc)
         * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#setMBeanContext(org.apache.geronimo.kernel.service.GeronimoMBeanContext)
         */
    public void setMBeanContext(GeronimoMBeanContext mbeanContext) {
        this.mbeanContext = mbeanContext;

    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#canStart()
     */
    public boolean canStart() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#canStop()
     */
    public boolean canStop() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#doFail()
     */
    public void doFail() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#doStart()
     */
    public void doStart() {
        //Set resource adapter on mcf instance if provided
        ManagedConnectionFactory mcf = (ManagedConnectionFactory) mbeanContext.getTarget();
        boolean doRegister = false;
        synchronized (this) {
            if (!registered) {
                registered = true;
                doRegister = true;
            }
        }
        if (doRegister) {
            if (resourceAdapterHelper != null) {
                try {
                    resourceAdapterHelper.registerManagedConnectionFactory(mcf);
                } catch (ResourceException re) {
                    throw new RuntimeException(re);
                }
            }

        }
        if (connectionManagerFactory == null) {
            log.info("connectionManagerFactory is not set!!");
            return;
        }
        try {
            connectionFactory = connectionManagerFactory.createConnectionFactory(mcf);
        } catch (ResourceException re) {
            throw new RuntimeException(re);
        }
        if (globalJNDIName != null) {
            try {
                GerContextManager.bind(globalJNDIName, connectionFactory);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#doStop()
     */
    public void doStop() {
        if (globalJNDIName != null) {
            try {
                GerContextManager.unbind(globalJNDIName);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public String getConnectionFactoryImplClass() {
        return connectionFactoryImplClass;
    }

    public String getConnectionFactoryInterface() {
        return connectionFactoryInterface;
    }

    public String getConnectionImplClass() {
        return connectionImplClass;
    }

    public String getConnectionInterface() {
        return connectionInterface;
    }

    /**
     * @return Returns the connectionManagerFactoryClass.
     */
    public String getConnectionManagerFactoryClass() {
        return ConnectionManagerFactoryClass;
    }

    /**
     */
    public String getManagedConnectionFactoryClass() {
        return managedConnectionFactoryClass;
    }

    /**
     * @param clazz
     */
    public void setConnectionFactoryImplClass(String clazz) {
        connectionFactoryImplClass = clazz;

    }

    /**
     * @param clazz
     */
    public void setConnectionFactoryInterface(String clazz) {
        connectionFactoryInterface = clazz;

    }

    /**
     * @param clazz
     */
    public void setConnectionImplClass(String clazz) {
        connectionImplClass = clazz;

    }

    /**
     * @param clazz
     */
    public void setConnectionInterface(String clazz) {
        connectionInterface = clazz;

    }

    /**
     * @param connectionManagerFactoryClass The connectionManagerFactoryClass to set.
     */
    public void setConnectionManagerFactoryClass(String connectionManagerFactoryClass) {
        ConnectionManagerFactoryClass = connectionManagerFactoryClass;
    }

    /**
     * @param clazz
     */
    public void setManagedConnectionFactoryClass(String clazz) {
        managedConnectionFactoryClass = clazz;

    }

    public String getGlobalJNDIName() {
        return globalJNDIName;
    }

    public void setGlobalJNDIName(String globalJNDIName) {
        this.globalJNDIName = globalJNDIName;
    }


    /**
     * @return Returns the connectionFactory.
     */
    public Object getConnectionFactory() {
        return connectionFactory;
    }


    /**
     * @return Returns the resourceAdapter.
     */
    public ResourceAdapterHelper getResourceAdapterHelper() {
        return resourceAdapterHelper;
    }

    /**
     * @param resourceAdapterHelper The resourceAdapterHelper to set.
     */
    public void setResourceAdapterHelper(ResourceAdapterHelper resourceAdapterHelper) {
        this.resourceAdapterHelper = resourceAdapterHelper;
    }

    /**
     * @return Returns the connectionManagerFactory.
     */
    public ConnectionManagerFactory getConnectionManagerFactory() {
        return connectionManagerFactory;
    }

    /**
     * @param connectionManagerFactory The connectionManagerFactory to set.
     */
    public void setConnectionManagerFactory(ConnectionManagerFactory connectionManagerFactory) {
        this.connectionManagerFactory = connectionManagerFactory;
    }


    public static void addMBeanInfo(GeronimoMBeanInfo mbeanInfo,
                                    ObjectName resourceAdapterName,
                                    ObjectName connectionManagerFactoryName) {

        mbeanInfo.setTargetClass(TARGET_NAME, ManagedConnectionFactoryHelper.class.getName());

        //These should all be read only, but this would need values set from attribute info on startup
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ConnectionFactoryInterface", true, true, "Interface implemented by the ConnectionFactory", TARGET_NAME));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ConnectionFactoryImplClass", true, true, "Class of the the ConnectionFactory", TARGET_NAME));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ConnectionInterface", true, true, "Interface implemented by the Connection", TARGET_NAME));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ConnectionImplClass", true, true, "Class of the Connection", TARGET_NAME));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ManagedConnectionFactoryClass", true, true, "Class of the ManagedConnectionFactory", TARGET_NAME));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("GlobalJNDIName", true, true, "Optional name to bind ConnectionFactory under in ger: context", TARGET_NAME));

        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getConnectionFactory", new GeronimoParameterInfo[]{}, GeronimoOperationInfo.INFO, "Retrieve the ConnectionFactory we deployed", TARGET_NAME));

        if (resourceAdapterName != null) {
            mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("ResourceAdapterHelper", ResourceAdapterHelper.class.getName(), resourceAdapterName, true, TARGET_NAME));
        }
        mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("ConnectionManagerFactory", ConnectionManagerFactory.class.getName(), connectionManagerFactoryName, true, TARGET_NAME));


    }


}
