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

package org.apache.geronimo.connector.outbound;

import javax.naming.NamingException;
import javax.resource.spi.ManagedConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.connector.deployment.ManagedConnectionFactoryHelper;
import org.apache.geronimo.connector.outbound.security.ManagedConnectionFactoryListener;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GEndpointInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.naming.ger.GerContextManager;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/22 02:46:27 $
 *
 * */
public class ManagedConnectionFactoryWrapper implements GBean, DynamicGBean {

    private static final GBeanInfo GBEAN_INFO;
    private static final Log log = LogFactory.getLog(ManagedConnectionFactoryHelper.class);

    private final Class managedConnectionFactoryClass;
    private final Class connectionFactoryInterface;
    private final Class connectionFactoryImplClass;
    private final Class connectionInterface;
    private final Class connectionImplClass;


    private String globalJNDIName;

    private ResourceAdapterWrapper resourceAdapterWrapper;
    private ConnectionManagerFactory connectionManagerFactory;
    private ManagedConnectionFactoryListener managedConnectionFactoryListener;

    private ManagedConnectionFactory managedConnectionFactory;

    private Object connectionFactory;

    private DynamicGBeanDelegate delegate;


    private boolean registered = false;

    //default constructor for enhancement proxy endpoint
    public ManagedConnectionFactoryWrapper() {
        managedConnectionFactoryClass = null;
        connectionFactoryInterface = null;
        connectionFactoryImplClass = null;
        connectionInterface = null;
        connectionImplClass = null;
    }

    public ManagedConnectionFactoryWrapper(
            Class managedConnectionFactoryClass,
            Class connectionFactoryInterface,
            Class connectionFactoryImplClass,
            Class connectionInterface,
            Class connectionImplClass,
            String globalJNDIName,
            ResourceAdapterWrapper resourceAdapterWrapper,
            ConnectionManagerFactory connectionManagerFactory,
            ManagedConnectionFactoryListener managedConnectionFactoryListener) throws InstantiationException, IllegalAccessException {
        this.managedConnectionFactoryClass = managedConnectionFactoryClass;
        this.connectionFactoryInterface = connectionFactoryInterface;
        this.connectionFactoryImplClass = connectionFactoryImplClass;
        this.connectionInterface = connectionInterface;
        this.connectionImplClass = connectionImplClass;

        this.globalJNDIName = globalJNDIName;
        this.resourceAdapterWrapper = resourceAdapterWrapper;
        this.connectionManagerFactory = connectionManagerFactory;

        //set up that must be done before start
        managedConnectionFactory = (ManagedConnectionFactory) managedConnectionFactoryClass.newInstance();
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(managedConnectionFactory);
        this.managedConnectionFactoryListener = managedConnectionFactoryListener;
    }

    public Class getManagedConnectionFactoryClass() {
        return managedConnectionFactoryClass;
    }

    public Class getConnectionFactoryInterface() {
        return connectionFactoryInterface;
    }

    public Class getConnectionFactoryImplClass() {
        return connectionFactoryImplClass;
    }

    public Class getConnectionInterface() {
        return connectionInterface;
    }

    public Class getConnectionImplClass() {
        return connectionImplClass;
    }

    public String getGlobalJNDIName() {
        return globalJNDIName;
    }


    public Object getConnectionFactory() {
        return connectionFactory;
    }


    public ResourceAdapterWrapper getResourceAdapterWrapper() {
        return resourceAdapterWrapper;
    }

    public void setResourceAdapterWrapper(ResourceAdapterWrapper resourceAdapterWrapper) {
        this.resourceAdapterWrapper = resourceAdapterWrapper;
    }

    public ConnectionManagerFactory getConnectionManagerFactory() {
        return connectionManagerFactory;
    }

    public void setConnectionManagerFactory(ConnectionManagerFactory connectionManagerFactory) {
        this.connectionManagerFactory = connectionManagerFactory;
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        //register with resource adapter if not yet done
        if (!registered && resourceAdapterWrapper != null) {
            resourceAdapterWrapper.registerManagedConnectionFactory(managedConnectionFactory);
            registered = true;
            log.debug("Registered managedConnectionFactory with ResourceAdapter " + resourceAdapterWrapper.toString());
        }
        //set up login if present
        if (managedConnectionFactoryListener != null) {
            managedConnectionFactoryListener.setManagedConnectionFactory(managedConnectionFactory);
        }

        //create a new ConnectionFactory
        connectionFactory = connectionManagerFactory.createConnectionFactory(managedConnectionFactory);
        //If a globalJNDIName is supplied, bind it.
        if (globalJNDIName != null) {
            GerContextManager.bind(globalJNDIName, connectionFactory);
            log.debug("Bound connection factory into global 'ger:' context at " + globalJNDIName);
        }

    }

    public void doStop() throws WaitingException {
        //tear down login if present
        if (managedConnectionFactoryListener != null) {
            managedConnectionFactoryListener.setManagedConnectionFactory(null);
        }
        connectionFactory = null;
        if (globalJNDIName != null) {
            try {
                GerContextManager.unbind(globalJNDIName);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void doFail() {
    }

    //DynamicGBean implementation
    public Object getAttribute(String name) throws Exception {
        return delegate.getAttribute(name);
    }

    public void setAttribute(String name, Object value) throws Exception {
        delegate.setAttribute(name, value);
    }

    public Object invoke(String name, Object[] arguments, String[] types) throws Exception {
        //we have no dynamic operations.
        return null;
    }


    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ManagedConnectionFactoryWrapper.class.getName());

        infoFactory.addAttribute(new GAttributeInfo("ManagedConnectionFactoryClass", true));
        infoFactory.addAttribute(new GAttributeInfo("ConnectionFactoryInterface", true));
        infoFactory.addAttribute(new GAttributeInfo("ConnectionFactoryImplClass", true));
        infoFactory.addAttribute(new GAttributeInfo("ConnectionInterface", true));
        infoFactory.addAttribute(new GAttributeInfo("ConnectionImplClass", true));

        infoFactory.addAttribute(new GAttributeInfo("GlobalJNDIName", true));

        infoFactory.addOperation(new GOperationInfo("getConnectionFactory"));

        infoFactory.addEndpoint(new GEndpointInfo("ResourceAdapterWrapper", ResourceAdapterWrapper.class.getName()));
        infoFactory.addEndpoint(new GEndpointInfo("ConnectionManagerFactory", ConnectionManagerFactory.class.getName()));
        infoFactory.addEndpoint(new GEndpointInfo("ManagedConnectionFactoryListener", ManagedConnectionFactoryListener.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"ManagedConnectionFactoryClass", "ConnectionFactoryInterface", "ConnectionFactoryImplClass", "ConnectionInterface", "ConnectionImplClass",
                "GlobalJNDIName", "ResourceAdapterWrapper", "ConnectionManagerFactory", "ManagedConnectionFactoryListener"},
                new Class[]{Class.class, Class.class, Class.class, Class.class, Class.class,
                String.class, ResourceAdapterWrapper.class, ConnectionManagerFactory.class, ManagedConnectionFactoryListener.class}));

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
