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
package org.apache.geronimo.security;

import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.core.service.AbstractManagedComponent;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.login.Configuration;
import javax.security.jacc.PolicyContextException;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.Query;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.MBeanServerNotification;
import javax.management.InstanceNotFoundException;
import java.util.Set;
import java.util.Hashtable;
import java.security.AccessController;


/**
 * An MBean that maintains a list of security realms.
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/18 05:17:17 $
 * @jmx:mbean
 */
public class SecurityService extends AbstractManagedComponent implements SecurityServiceMBean, GeronimoMBeanTarget {

    private static final ObjectName DEFAULT_NAME = JMXUtil.getObjectName("geronimo.security:type=SecurityService");
    private GeronimoMBeanContext context;
    private static Hashtable services = new Hashtable();
    private static long lastIdUsed = 0;
    private static long myId = 0;

    private final Log log = LogFactory.getLog(SecurityService.class);

    static {
        AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        Configuration.setConfiguration(new GeronimoLoginConfiguration());
                        return null;
                    }
                });
    }

    /**
     * Permissions that protect access to sensitive security information
     */
    public static final GeronimoSecurityPermission CONFIGURE = new GeronimoSecurityPermission("configure");

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(SecurityService.class.getName());
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ServiceId",
                true, false,
                "Id of this security service"));
        return mbeanInfo;
    }

    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        if (objectName == null) objectName = DEFAULT_NAME;
        return super.preRegister(mBeanServer, objectName);
    }

    /**
     * @param aBoolean a <code>Boolean</code> value
     */
    public void postRegister(Boolean aBoolean) {
        super.postRegister(aBoolean);

        synchronized (services) {
            myId = ++lastIdUsed;
            services.put(new Long(myId), this);
        }
    }

    public void preDeregister() throws Exception {
        services.remove(new Long(myId));
    }

    public void postDeregister() {
    }

    public void setMBeanContext(GeronimoMBeanContext context) {
        this.context = context;
    }

    public boolean canStart() {
        return true;
    }

    public void doStart() {
        /**
         *  @see "JSR 115 4.6.1" Container Subject Policy Contact Handler
         */
        try {
            ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerContainerSubject(), true);
            ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerSOAPMessage(), true);
            ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerHttpServletRequest(), true);
            ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerEnterpriseBean(), true);
            ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerEJBArguments(), true);
        } catch (PolicyContextException pce) {
            log.error("Exception in doStart()", pce);

            IllegalStateException ise = new IllegalStateException();
            ise.initCause(pce);
            throw ise;
        }
        log.debug("Security Server started");
    }

    public boolean canStop() {
        return true;
    }

    public void doStop() {
        log.debug("Security Server stopped");
    }

    public void doFail() {
    }

    /**
     *
     * @return
     * @throws GeronimoSecurityException
     * @jmx:managed-operation
     */
    public String getServiceId() throws GeronimoSecurityException {
        return new Long(myId).toString();
    }

    /**
     *
     * @return
     * @throws GeronimoSecurityException
     * @jmx:managed-operation
     */
    public Set getRealms() throws GeronimoSecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(CONFIGURE);

        try {
            return server.queryMBeans(JMXUtil.getObjectName("geronimo.security:type=SecurityRealm"), null);
        } catch (Exception e) {
            throw new GeronimoSecurityException(e);
        }
    }

    /**
     * <p>This method is used to obtain a web module configuration that corresponds to the identified policy context.
     * The methods of the <code>WebModuleConfigurationMBean</code> class are used to map deployment descriptor
     * information into policy statements needed by the identified policy context as well as the principal to roll
     * mapping.</p>
     *
     * <p>If at the time of the call, the identified web module configuration does not exist, then the web module
     * configuration will be created and the JMX MBean that implements the context's
     * <code>WebModuleConfigurationMBean</code> interface will be returned. If the state of the identified context is
     * "deleted" or "inService" it will be transitioned to the "open" state as a result of the call. The states in the
     * lifecycle of a policy context are defined by the <code>WebModuleConfigurationMBean</code> interface.</p>
     *
     * <p>For a given value of policy context identifier, this method must always return the same instance of
     * <code>WebModuleConfigurationMBean</code> and there must be at most one actual instance of a
     * <code>WebModuleConfigurationMBean</code> with a given policy context identifier (during a process context).</p>
     *
     * <p>To preserve the invariant that there be at most one <code>WebModuleConfigurationMBean</code> object for a
     * given policy context, it may be necessary for this method to be thread safe.</p>
     *
     * @param contextID A String identifying the web module configuration to be returned. The value passed to this
     *                  parameter must not be null.
     * @param remove A boolean value that establishes whether or not the security configuration of an existing web
     *                  module is to be removed before its <code>WebModuleConfigurationMBean</code> object is returned.
     *                  If the value passed to this parameter is <code>true</code> the security configuration of an
     *                  existing web module will be removed. If the value is <code>false</code>, it will not be removed.
     * @return an MBean that implements the <code>WebModuleConfigurationMBean</code> Interface matched to the
     *                  identified policy context.
     * @throws GeronimoSecurityException if the implementation throws a checked exception that has not been accounted for by
     *                  the <code>getWebModuleConfiguration</code> method signature.
     * @jmx:managed-operation
     */
    public ObjectName getWebModuleConfiguration(String contextID, boolean remove) throws GeronimoSecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(CONFIGURE);

        Set configBeans = server.queryMBeans(JMXUtil.getObjectName("geronimo.security:type=WebModuleConfigurationMBean,*"),
                                             Query.eq(Query.attr("ContextID"),
                                                      Query.value(contextID)));
        assert configBeans.size() <= 1;

        WebModuleConfigurationMBean configBean = null;
        ObjectName objName;
        if (configBeans.isEmpty()) {
            configBean = new WebModuleConfiguration(contextID);

            objName = configBean.getObjectName();
            try {
                server.registerMBean(configBean, configBean.getObjectName());
            } catch (InstanceAlreadyExistsException e) {
                throw new GeronimoSecurityException(e);
            } catch (MBeanRegistrationException e) {
                throw new GeronimoSecurityException(e);
            } catch (NotCompliantMBeanException e) {
                throw new GeronimoSecurityException(e);
            } catch (IllegalArgumentException e) {
                throw new GeronimoSecurityException(e);
            }
        } else {
            objName = (ObjectName)configBeans.iterator().next();
        }

        if (remove) {
            configBean = (WebModuleConfigurationMBean) MBeanProxyFactory.getProxy(WebModuleConfigurationMBean.class,
                                                                                  server,
                                                                                  objName);
            configBean.delete();
        }

        return objName;
    }

    /**
     * <p>This method is used to obtain a EJB module configuration that corresponds to the identified policy context.
     * The methods of the <code>EjbModuleConfigurationMBean</code> class are used to map deployment descriptor
     * information into policy statements needed by the identified policy context as well as the principal to roll
     * mapping.</p>
     *
     * <p>If at the time of the call, the identified EJB module configuration does not exist, then the EJB module
     * configuration will be created and the JMX MBean that implements the context's
     * <code>EjbModuleConfigurationMBean</code> interface will be returned. If the state of the identified context is
     * "deleted" or "inService" it will be transitioned to the "open" state as a result of the call. The states in the
     * lifecycle of a policy context are defined by the <code>EjbModuleConfigurationMBean</code> interface.</p>
     *
     * <p>For a given value of policy context identifier, this method must always return the same instance of
     * <code>EjbModuleConfigurationMBean</code> and there must be at most one actual instance of a
     * <code>EjbModuleConfigurationMBean</code> with a given policy context identifier (during a process context).</p>
     *
     * <p>To preserve the invariant that there be at most one <code>EjbModuleConfigurationMBean</code> object for a
     * given policy context, it may be necessary for this method to be thread safe.</p>
     *
     * @param contextID A String identifying the EJB module configuration to be returned. The value passed to this
     *                  parameter must not be null.
     * @param remove A boolean value that establishes whether or not the security configuration of an existing EJB
     *                  module is to be removed before its <code>EjbModuleConfigurationMBean</code> object is returned.
     *                  If the value passed to this parameter is <code>true</code> the security configuration of an
     *                  existing EJB module will be removed. If the value is <code>false</code>, it will not be removed.
     * @return an MBean that implements the <code>EjbModuleConfigurationMBean</code> Interface matched to the
     *                  identified policy context.
     * @throws GeronimoSecurityException if the implementation throws a checked exception that has not been accounted for by
     *                  the <code>getEjbModuleConfiguration</code> method signature.
     * @jmx:managed-operation
     */
    public ObjectName getEjbModuleConfiguration(String contextID, boolean remove) throws GeronimoSecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(CONFIGURE);

        Set configBeans = server.queryMBeans(JMXUtil.getObjectName("geronimo.security:type=EjbModuleConfigurationMBean,*"),
                                             Query.eq(Query.attr("ContextID"),
                                                      Query.value(contextID)));
        assert configBeans.size() <= 1;

        EJBModuleConfigurationMBean configBean = null;
        ObjectName objName;
        if (configBeans.isEmpty()) {
            configBean = new EJBModuleConfiguration(contextID);

            objName = configBean.getObjectName();
            try {
                server.registerMBean(configBean, configBean.getObjectName());
            } catch (InstanceAlreadyExistsException e) {
                throw new GeronimoSecurityException(e);
            } catch (MBeanRegistrationException e) {
                throw new GeronimoSecurityException(e);
            } catch (NotCompliantMBeanException e) {
                throw new GeronimoSecurityException(e);
            } catch (IllegalArgumentException e) {
                throw new GeronimoSecurityException(e);
            }
        } else {
            objName = (ObjectName)configBeans.iterator().next();
        }

        if (remove) {
            configBean = (EJBModuleConfiguration) MBeanProxyFactory.getProxy(EJBModuleConfiguration.class,
                                                                                  server,
                                                                                  objName);
            configBean.delete();
        }

        return objName;
    }

    /**
     * Monitor JMX notifications<p>
     *
     * When a security realm is registered in JMX, then set up the containment relationship with it so that it
     * becomes one of our components.
     * @param n a <code>Notification</code> value
     * @param o an <code>Object</code> value
     */
    public void handleNotification(Notification n, Object o) {
        ObjectName source = null;

        try {
            // Respond to registrations of SecurityRealm
            if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(n.getType())) {
                MBeanServerNotification notification = (MBeanServerNotification) n;
                source = notification.getMBeanName();
                if (server.isInstanceOf(source, SecurityRealm.class.getName())) {
                    log.debug("Received registration notification for SecurityRealm=" + source);
                    dependencyService.addStartDependency(source, objectName);
                } else if (server.isInstanceOf(source, AbstractModuleConfiguration.class.getName())) {
                    log.debug("Received registration notification for ModuleConfiguration=" + source);
                    dependencyService.addStartDependency(source, objectName);
                } else {
                    log.debug("Ignoring registration of mbean=" + source);
                }
            }
        } catch (InstanceNotFoundException e) {
            log.debug("Registration notification received for non-existant object: " + source);
        } catch (Exception e) {
            throw new IllegalStateException(e.toString());
        }

        super.handleNotification(n, o);
    }
}
