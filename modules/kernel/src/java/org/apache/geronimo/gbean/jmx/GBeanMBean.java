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

package org.apache.geronimo.gbean.jmx;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.jmx.MBeanOperationSignature;
import org.apache.geronimo.kernel.management.NotificationType;

import net.sf.cglib.reflect.FastClass;

/**
 * A GBeanMBean is a J2EE Management Managed Object, and is standard base for Geronimo services.
 * This wraps one or more target POJOs and exposes the attributes and operations according to a supplied
 * {@link GBeanInfo} instance.  The GBeanMBean also supports caching of attribute values and invocation results
 * which can reduce the number of calls to a target.
 *
 * @version $Revision: 1.14 $ $Date: 2004/03/13 23:48:56 $
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
     * References supported by this GBeanMBean by (String) name.
     */
    private final Map referenceMap = new HashMap();

    /**
     * Operations supported by this GBeanMBean by (MBeanOperationSignature) name.
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
            throw new InvalidConfigurationException("Could not load GBeanInfo class from classloader: " +
                    " className=" + beanInfo.getClassName());
        }

        name = beanInfo.getName();

        // attributes
        Map constructorTypes = gbeanInfo.getConstructor().getAttributeTypeMap();
        for (Iterator iterator = beanInfo.getAttributes().iterator(); iterator.hasNext();) {
            GAttributeInfo attributeInfo = (GAttributeInfo) iterator.next();
            addAttribute(new GBeanMBeanAttribute(this, attributeInfo, (Class) constructorTypes.get(attributeInfo.getName())));
        }

        // references
        for (Iterator iterator = beanInfo.getReferences().iterator(); iterator.hasNext();) {
            GReferenceInfo referenceInfo = (GReferenceInfo) iterator.next();
            addReference(new GBeanMBeanReference(this, referenceInfo, (Class) constructorTypes.get(referenceInfo.getName())));
        }

        // operations
        for (Iterator iterator = beanInfo.getOperations().iterator(); iterator.hasNext();) {
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
                // Is there any way to add notifications before an instance of the class is created?
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
     * @throws java.lang.Exception if an exception occurs while getting the GBeanInfo from the class
     */
    public GBeanMBean(String className, ClassLoader classLoader) throws Exception {
        this(GBeanInfo.getGBeanInfo(className, classLoader), classLoader);
    }

    /**
     * "Bootstrapping" constructor.  The class specified is loaded and the static method
     * "getGBeanInfo" is called to get the gbean info.  Usually one will include
     * this static method in the class to be wrapped in the GBeanMBean instance.
     * @param className name of the class to call getGBeanInfo on
     * @throws java.lang.Exception if an exception occurs while getting the GBeanInfo from the class
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
            } else if (referenceMap.containsKey(name)) {
                GBeanMBeanReference reference = (GBeanMBeanReference) referenceMap.get(name);
                reference.online();
                parameters[i] = reference.getProxy();
            } else {
                throw new InvalidConfigurationException("Unknown attribute or reference name in constructor: name=" + name);
            }
            Class assertedType = (Class) assertedTypes.next();
            assert parameters[i] == null || assertedType.isPrimitive() || assertedType.isAssignableFrom(parameters[i].getClass()):
                    "Attempting to construct " + objectName + " of type " + gbeanInfo.getClassName()
                    + ". Constructor parameter " + i + " should be " + assertedType.getName()
                    + " but is " + parameters[i].getClass().getName();
        }
        try {
            target = constructor.newInstance(parameters);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if(targetException instanceof Exception) {
                throw (Exception)targetException;
            } else if(targetException instanceof Error) {
                throw (Error)targetException;
            }
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("Constructor mismatch for "  + returnValue, e);
            throw e;
        }

        // bring all of the attributes online
        for (Iterator iterator = attributeMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanAttribute attribute = (GBeanMBeanAttribute) iterator.next();
            attribute.online();
        }

        // bring any reference not used in the constructor online
        // @todo this code sucks, but works
        for (Iterator iterator = referenceMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanReference reference = (GBeanMBeanReference) iterator.next();
            if (!constructorInfo.getAttributeNames().contains(reference.getName())) {
                reference.online();
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
            // we need to bring the reference back off line
            for (Iterator iterator = referenceMap.values().iterator(); iterator.hasNext();) {
                GBeanMBeanReference reference = (GBeanMBeanReference) iterator.next();
                reference.offline();
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

        // take all of the reference offline
        for (Iterator iterator = referenceMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanReference reference = (GBeanMBeanReference) iterator.next();
            reference.offline();
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
        // start all of the references
        for (Iterator iterator = referenceMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanReference reference = (GBeanMBeanReference) iterator.next();
            reference.start();
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

        // stop all of the references
        for (Iterator iterator = referenceMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanReference reference = (GBeanMBeanReference) iterator.next();
            reference.stop();
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

        // stop all of the references
        for (Iterator iterator = referenceMap.values().iterator(); iterator.hasNext();) {
            GBeanMBeanReference reference = (GBeanMBeanReference) iterator.next();
            reference.stop();
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

    public Set getReferencePatterns(String name) {
        GBeanMBeanReference reference = (GBeanMBeanReference) referenceMap.get(name);
        if (reference == null) {
            throw new IllegalArgumentException("Unknown reference " + name);
        }
        return reference.getPatterns();
    }

    public void setReferencePatterns(String name, Set patterns) {
        GBeanMBeanReference reference = (GBeanMBeanReference) referenceMap.get(name);
        if (reference == null) {
            throw new IllegalArgumentException("Unknown reference " + name);
        }
        reference.setPatterns(patterns);
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return mbeanInfo.getNotifications();
    }

    private void addAttribute(GBeanMBeanAttribute mbeanAttribute) {
        String attributeName = mbeanAttribute.getName();

        // add to attribute map
        attributeMap.put(attributeName, mbeanAttribute);
    }

    private void addReference(GBeanMBeanReference mbeanReference) {
        String referenceName = mbeanReference.getName();

        // add to reference map
        referenceMap.put(referenceName, mbeanReference);
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
