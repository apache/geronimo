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
package org.apache.geronimo.gbean.jmx;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import net.sf.cglib.reflect.FastClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GEndpointInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.jmx.MBeanOperationSignature;
import org.apache.geronimo.kernel.management.NotificationType;

/**
 * A GeronimoMBean is a J2EE Management Managed Object, and is standard base for Geronimo services.
 * This wraps one or more target POJOs and exposes the attributes and opperation according to a supplied
 * GeronimoMBeanInfo instance.  The GeronimoMBean also support caching of attribute values and invocation results
 * which can reduce the number of calls to a target.
 *
 * @version $Revision: 1.5 $ $Date: 2004/01/22 02:46:27 $
 */
public class GBeanMBean extends AbstractManagedObject implements DynamicMBean {
    public static final FastClass fastClass = FastClass.create(GBeanMBean.class);
    private static final Log log = LogFactory.getLog(GBeanMBean.class);

    /**
     * Gets the context class loader from the thread or the system class loader if there is no context class loader.
     * @return the context class loader or the system classloader
     */
    private static ClassLoader getContextClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }

    /**
     * Attributes supported by this GBeanMBean by (String) name.
     */
    private final Map attributeMap = new HashMap();

    /**
     * Endpoints supported by this GBeanMBean by (String) name.
     */
    private final Map endpointMap = new HashMap();

    /**
     * Opperations supported by this GBeanMBean by (MBeanOperationSignature) name.
     */
    private final Map operationMap = new HashMap();

    /**
     * Notifications (MBeanNotificationInfo) fired by this mbean.
     */
    private final Set notifications = new HashSet();

    /**
     * The classloader used for all invocations and creating targets.
     */
    private final ClassLoader classLoader;

    private final GBeanInfo gbeanInfo;
    private final MBeanInfo mbeanInfo;
    private final String name;
    private final Class type;

    private boolean offline = true;
    private Object target;

    public GBeanMBean(GBeanInfo beanInfo, ClassLoader classLoader) throws InvalidConfigurationException {
        this.gbeanInfo = beanInfo;
        this.classLoader = classLoader;
        try {
            type = classLoader.loadClass(beanInfo.getClassName());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load GBean class from classloader: " +
                    " className=" + beanInfo.getClassName());
        }

        name = beanInfo.getName();

        // attributes
        Map constructorTypes = gbeanInfo.getConstructor().getAttributeTypeMap();
        for (Iterator iterator = beanInfo.getAttributeSet().iterator(); iterator.hasNext();) {
            GAttributeInfo attributeInfo = (GAttributeInfo) iterator.next();
            addAttribute(new GBeanMBeanAttribute(this, attributeInfo, (Class) constructorTypes.get(attributeInfo.getName())));
        }

        // endpoints
        for (Iterator iterator = beanInfo.getEndpointsSet().iterator(); iterator.hasNext();) {
            GEndpointInfo endpointInfo = (GEndpointInfo) iterator.next();
            addEndpoint(new GBeanMBeanEndpoint(this, endpointInfo, (Class) constructorTypes.get(endpointInfo.getName())));
        }

        // operations
        for (Iterator iterator = beanInfo.getOperationsSet().iterator(); iterator.hasNext();) {
            GOperationInfo operationInfo = (GOperationInfo) iterator.next();
            addOperation(new GBeanMBeanOperation(this, operationInfo));
        }

        // add all attributes and operations from the ManagedObject interface
        addManagedObjectInterface();

        int idx;
        idx = 0;
        MBeanAttributeInfo[] mbeanAttrs = new MBeanAttributeInfo[attributeMap.size()];
        for (Iterator i = attributeMap.values().iterator(); i.hasNext();) {
            GBeanMBeanAttribute attr = (GBeanMBeanAttribute) i.next();
            mbeanAttrs[idx++] = attr.getMBeanAttributeInfo();
        }

        idx = 0;
        MBeanOperationInfo[] mbeanOps = new MBeanOperationInfo[operationMap.size()];
        for (Iterator i = operationMap.values().iterator(); i.hasNext();) {
            GBeanMBeanOperation op = (GBeanMBeanOperation) i.next();
            mbeanOps[idx++] = op.getMbeanOperationInfo();
        }

        mbeanInfo = new MBeanInfo(
                beanInfo.getClassName(),
                null,
                mbeanAttrs,
                new MBeanConstructorInfo[0],
                mbeanOps,
                (MBeanNotificationInfo[]) notifications.toArray(new MBeanNotificationInfo[notifications.size()]));
    }

    public GBeanMBean(GBeanInfo beanInfo) throws InvalidConfigurationException {
        this(beanInfo, getContextClassLoader());
    }

    /**
     * "Bootstrapping" constructor.  The class specified is loaded and the static method
     * "getGBeanInfo" is called to get the gbean info.  Usually one will include
     * this static method in the class to be wrapped in the GBeanMBean instance.
     * @param className name of the class to call getGBeanInfo on
     * @param classLoader the class loader for this GBean
     * @throws java.lang.Exception if an exception occurs while getting the GeronimoMBeanInfo from the class
     */
    public GBeanMBean(String className, ClassLoader classLoader) throws Exception {
        this(GBeanInfo.getGBeanInfo(className, classLoader), classLoader);
    }

    /**
     * "Bootstrapping" constructor.  The class specified is loaded and the static method
     * "getGBeanInfo" is called to get the gbean info.  Usually one will include
     * this static method in the class to be wrapped in the GBeanMBean instance.
     * @param className name of the class to call getGBeanInfo on
     * @throws java.lang.Exception if an exception occurs while getting the GeronimoMBeanInfo from the class
     */
    public GBeanMBean(String className) throws Exception {
        this(className, ClassLoader.getSystemClassLoader());
    }

    public String getName() {
        return name;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public boolean isOffline() {
        return offline;
    }

    public Class getType() {
        return type;
    }

    public Object getTarget() {
        return target;
    }

    public synchronized ObjectName preRegister(MBeanServer server, ObjectName objectName) throws Exception {
        ObjectName returnValue = super.preRegister(server, objectName);


        // get the constructor
        GConstructorInfo constructorInfo = gbeanInfo.getConstructor();
        Class[] parameterTypes = (Class[]) constructorInfo.getTypes().toArray(new Class[constructorInfo.getTypes().size()]);
        Constructor constructor = type.getConstructor(parameterTypes);

        // create the instance
        Object[] parameters = new Object[parameterTypes.length];
        Iterator names = constructorInfo.getAttributeNames().iterator();
        Iterator assertedTypes = constructorInfo.getTypes().iterator();
        for (int i = 0; i < parameters.length; i++) {
            String name = (String) names.next();
            if (attributeMap.containsKey(name)) {
                parameters[i] = getAttribute(name);
            } else if (endpointMap.containsKey(name)) {
                GBeanMBeanEndpoint endpoint = (GBeanMBeanEndpoint) endpointMap.get(name);
                endpoint.online();
                parameters[i] = endpoint.getProxy();
            } else {
                throw new InvalidConfigurationException("Unknown attribute or endpoint name in constructor: name=" + name);
            }
            Class assertedType = (Class) assertedTypes.next();
            assert parameters[i] == null || assertedType.isPrimitive() || assertedType.isAssignableFrom(parameters[i].getClass()):
                    "Attempting to construct " + objectName + " of type " + gbeanInfo.getClassName()
                    + ". Constructor parameter " + i + " should be " + assertedType.getName()
                    + " but is " + parameters[i].getClass().getName();
        }
        target = constructor.newInstance(parameters);

        // bring all of the attributes online
        for (Iterator iterator = attributeMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanAttribute attribute = (GBeanMBeanAttribute) iterator.next();
            attribute.online();
        }

        // bring any endpoint not used in the constructor online
        // @todo this code sucks, but works
        for (Iterator iterator = endpointMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanEndpoint endpoint = (GBeanMBeanEndpoint) iterator.next();
            if (!constructorInfo.getAttributeNames().contains(endpoint.getName())) {
                endpoint.online();
            }
        }

        return returnValue;
    }

    public void postRegister(Boolean registrationDone) {
        super.postRegister(registrationDone);

        if (registrationDone.booleanValue()) {
            // we're now offically on line
            if (target instanceof GBean) {
                GBean gbean = (GBean) target;
                gbean.setGBeanContext(new GBeanMBeanContext(server, this, objectName));
            }
            offline = false;
        } else {
            // we need to bring the endpoints back off line
            for (Iterator iterator = endpointMap.values().iterator(); iterator.hasNext();) {
                GBeanMBeanEndpoint endpoint = (GBeanMBeanEndpoint) iterator.next();
                endpoint.offline();
            }

            // well that didn't work, ditch the instance
            target = null;
        }
    }

    public void postDeregister() {
        // take all of the attributes offline
        for (Iterator iterator = attributeMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanAttribute attribute = (GBeanMBeanAttribute) iterator.next();
            attribute.offline();
        }

        // take all of the endpoints offline
        for (Iterator iterator = endpointMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanEndpoint endpoint = (GBeanMBeanEndpoint) iterator.next();
            endpoint.offline();
        }

        if (target instanceof GBean) {
            GBean gbean = (GBean) target;
            gbean.setGBeanContext(null);
        }

        offline = true;
        target = null;

        super.postDeregister();
    }

    public GBeanInfo getGBeanInfo() {
        return gbeanInfo;
    }

    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    protected void doStart() throws Exception {
        // start all of the endpoints
        for (Iterator iterator = endpointMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanEndpoint endpoint = (GBeanMBeanEndpoint) iterator.next();
            endpoint.start();
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            if (target instanceof GBean) {
                ((GBean) target).doStart();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    protected void doStop() throws Exception {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            if (target instanceof GBean) {
                ((GBean) target).doStop();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        // stop all of the endpoints
        for (Iterator iterator = endpointMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanEndpoint endpoint = (GBeanMBeanEndpoint) iterator.next();
            endpoint.stop();
        }
    }

    protected void doFail() {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            if (target instanceof GBean) {
                ((GBean) target).doFail();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        // stop all of the endpoints
        for (Iterator iterator = endpointMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanEndpoint endpoint = (GBeanMBeanEndpoint) iterator.next();
            endpoint.stop();
        }
    }

    public Object getAttribute(String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        GBeanMBeanAttribute attribute = (GBeanMBeanAttribute) attributeMap.get(attributeName);
        if (attribute == null) {
            throw new AttributeNotFoundException("Unknown attribute " + attributeName);
        }

        return attribute.getValue();
    }

    public void setAttribute(Attribute attributeValue) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        GBeanMBeanAttribute attribute = (GBeanMBeanAttribute) attributeMap.get(attributeValue.getName());
        if (attribute == null) {
            throw new AttributeNotFoundException("Unknown attribute " + attributeValue.getName());
        }

        attribute.setValue(attributeValue.getValue());
    }

    public void setAttribute(String name, Object value) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        GBeanMBeanAttribute attribute = (GBeanMBeanAttribute) attributeMap.get(name);
        if (attribute == null) {
            throw new AttributeNotFoundException("Unknown attribute " + name);
        }

        attribute.setValue(value);
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList results = new AttributeList(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            String name = attributes[i];
            try {
                Object value = getAttribute(name);
                results.add(new Attribute(name, value));
            } catch (JMException e) {
                log.warn("Exception while getting attribute " + name, e);
            }
        }
        return results;
    }

    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList results = new AttributeList(attributes.size());
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            Attribute attribute = (Attribute) iterator.next();
            try {
                setAttribute(attribute);
                results.add(attribute);
            } catch (JMException e) {
                log.warn("Exception while setting attribute " + attribute.getName(), e);
            }
        }
        return results;
    }

    public Object invoke(String methodName, Object[] arguments, String[] types) throws MBeanException, ReflectionException {
        MBeanOperationSignature key = new MBeanOperationSignature(methodName, types);
        Object operation = operationMap.get(key);
        if (operation == null) {
            throw new ReflectionException(new NoSuchMethodException("Unknown operation " + key));
        }


        // If this is an attribute accessor get call the getAttibute or setAttribute method
        if (operation instanceof GBeanMBeanAttribute) {
            if (arguments == null || arguments.length == 0) {
                return ((GBeanMBeanAttribute) operation).getValue();
            } else {
                ((GBeanMBeanAttribute) operation).setValue(arguments[0]);
                return null;
            }
        }

        return ((GBeanMBeanOperation) operation).invoke(arguments);
    }

    public Set getEndpointPatterns(String name) {
        GBeanMBeanEndpoint endpoint = (GBeanMBeanEndpoint) endpointMap.get(name);
        if (endpoint == null) {
            throw new IllegalArgumentException("Unknown endpoint " + name);
        }
        return endpoint.getPatterns();
    }

    public void setEndpointPatterns(String name, Set patterns) {
        GBeanMBeanEndpoint endpoint = (GBeanMBeanEndpoint) endpointMap.get(name);
        if (endpoint == null) {
            throw new IllegalArgumentException("Unknown endpoint " + name);
        }
        endpoint.setPatterns(patterns);
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return mbeanInfo.getNotifications();
    }

    private void addAttribute(GBeanMBeanAttribute mbeanAttribute) {
        String attributeName = mbeanAttribute.getName();

        // add to attribute map
        attributeMap.put(attributeName, mbeanAttribute);
    }

    private void addEndpoint(GBeanMBeanEndpoint mbeanEndpoint) {
        String endpointName = mbeanEndpoint.getName();

        // add to endpoint map
        endpointMap.put(endpointName, mbeanEndpoint);
    }

    private void addOperation(GBeanMBeanOperation mbeanOperation) {
        MBeanOperationSignature signature = new MBeanOperationSignature(mbeanOperation.getName(), mbeanOperation.getParameterTypes());
        operationMap.put(signature, mbeanOperation);
    }

    private void addManagedObjectInterface() {
        addAttribute(new GBeanMBeanAttribute(
                this,
                "state",
                Integer.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Integer(getState());
                    }
                },
                null));

        addAttribute(new GBeanMBeanAttribute(
                this,
                "objectName",
                String.class,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return getObjectName();
                    }
                },
                null));

        addAttribute(new GBeanMBeanAttribute(
                this,
                "startTime",
                Long.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Long(getStartTime());
                    }
                },
                null));

        addAttribute(new GBeanMBeanAttribute(
                this,
                "stateManageable",
                Boolean.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Boolean(isStateManageable());
                    }
                },
                null));

        addAttribute(new GBeanMBeanAttribute(
                this,
                "statisticsProvider",
                Boolean.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Boolean(isStatisticsProvider());
                    }
                },
                null));


        addAttribute(new GBeanMBeanAttribute(
                this,
                "eventProvider",
                Boolean.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Boolean(isEventProvider());
                    }
                },
                null));

        addOperation(new GBeanMBeanOperation(
                this,
                "start",
                Collections.EMPTY_LIST,
                Void.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        start();
                        return null;
                    }
                }));

        addOperation(new GBeanMBeanOperation(
                this,
                "startRecursive",
                Collections.EMPTY_LIST,
                Void.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        startRecursive();
                        return null;
                    }
                }));

        addOperation(new GBeanMBeanOperation(
                this,
                "stop",
                Collections.EMPTY_LIST,
                Void.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        stop();
                        return null;
                    }
                }));

        notifications.add(new MBeanNotificationInfo(
                NotificationType.TYPES,
                "javax.management.Notification",
                "J2EE Notifications"));
    }
}
