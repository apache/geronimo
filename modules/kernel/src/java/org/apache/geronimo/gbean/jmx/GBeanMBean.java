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
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
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
import org.apache.geronimo.gbean.GOperationSignature;
import org.apache.geronimo.gbean.GOperationSignature;
import org.apache.geronimo.kernel.management.NotificationType;

/**
 * A GBeanMBean is a J2EE Management Managed Object, and is standard base for Geronimo services.
 * This wraps one or more target POJOs and exposes the attributes and operations according to a supplied
 * {@link GBeanInfo} instance.  The GBeanMBean also supports caching of attribute values and invocation results
 * which can reduce the number of calls to a target.
 *
 * @version $Revision: 1.16 $ $Date: 2004/05/26 22:58:30 $
 */
public class GBeanMBean extends AbstractManagedObject implements DynamicMBean {
    /**
     * Method name used to retrieve the RawInvoker for the GBean
     */
    static final String RAW_INVOKER = "$$RAW_INVOKER$$";

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
     * Attributes lookup table
     */
    private final GBeanMBeanAttribute[] attributes;

    /**
     * Attributes supported by this GBeanMBean by (String) name.
     */
    private final Map attributeIndex = new HashMap();

    /**
     * References lookup table
     */
    private final GBeanMBeanReference[] references;

    /**
     * References supported by this GBeanMBean by (String) name.
     */
    private final Map referenceIndex = new HashMap();

    /**
     * Operations lookup table
     */
    private final GBeanMBeanOperation[] operations;

    /**
     * Operations supported by this GBeanMBean by (GOperationSignature) name.
     */
    private final Map operationIndex = new HashMap();

    /**
     * Notifications (MBeanNotificationInfo) fired by this mbean.
     */
    private final Set notifications = new HashSet();

    /**
     * The classloader used for all invocations and creating targets.
     */
    private final ClassLoader classLoader;

    /**
     * Metadata describing the attributes, operations and references of this GBean
     */
    private final GBeanInfo gbeanInfo;

    /**
     * JMX sped mbeanInfo for this gbean (translation of the above gbeanInfo
     */
    private final MBeanInfo mbeanInfo;

    /**
     * Our name
     */
    private final String name;

    /**
     * Java type of the wrapped GBean class
     */
    private final Class type;

    /**
     * Is this gbean off line?
     */
    private boolean offline = true;

    /**
     * Target instance of this GBean wrapper
     */
    private Object target;

    /**
     * A fast index based raw invoker for this GBean.
     */
    private RawInvoker rawInvoker;

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

        Map constructorTypes = gbeanInfo.getConstructor().getAttributeTypeMap();

        // attributes
        Set attributesSet = new HashSet();
        for (Iterator iterator = beanInfo.getAttributes().iterator(); iterator.hasNext();) {
            GAttributeInfo attributeInfo = (GAttributeInfo) iterator.next();
            attributesSet.add(new GBeanMBeanAttribute(this, attributeInfo, (Class) constructorTypes.get(attributeInfo.getName())));
        }
        addManagedObjectAttributes(attributesSet);
        attributes = (GBeanMBeanAttribute[]) attributesSet.toArray(new GBeanMBeanAttribute[attributesSet.size()]);
        for (int i = 0; i < attributes.length; i++) {
            attributeIndex.put(attributes[i].getName(), new Integer(i));
        }

        // references
        Set referencesSet = new HashSet();
        for (Iterator iterator = beanInfo.getReferences().iterator(); iterator.hasNext();) {
            GReferenceInfo referenceInfo = (GReferenceInfo) iterator.next();
            referencesSet.add(new GBeanMBeanReference(this, referenceInfo, (Class) constructorTypes.get(referenceInfo.getName())));
        }
        references = (GBeanMBeanReference[]) referencesSet.toArray(new GBeanMBeanReference[beanInfo.getReferences().size()]);
        for (int i = 0; i < references.length; i++) {
            referenceIndex.put(references[i].getName(), new Integer(i));
        }

        // operations
        Set operationsSet = new HashSet();
        for (Iterator iterator = beanInfo.getOperations().iterator(); iterator.hasNext();) {
            GOperationInfo operationInfo = (GOperationInfo) iterator.next();
            operationsSet.add(new GBeanMBeanOperation(this, operationInfo));
        }
        addManagedObjectOperations(operationsSet);
        operations = (GBeanMBeanOperation[]) operationsSet.toArray(new GBeanMBeanOperation[beanInfo.getOperations().size()]);
        for (int i = 0; i < operations.length; i++) {
            GBeanMBeanOperation operation = operations[i];
            GOperationSignature signature = new GOperationSignature(operation.getName(), operation.getParameterTypes());
            operationIndex.put(signature, new Integer(i));
        }

        // add notification type from the ManagedObject interface
        notifications.add(new MBeanNotificationInfo(
                NotificationType.TYPES,
                "javax.management.Notification",
                "J2EE Notifications"));

        MBeanAttributeInfo[] mbeanAttributes = new MBeanAttributeInfo[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            mbeanAttributes[i] = attributes[i].getMBeanAttributeInfo();
        }

        MBeanOperationInfo[] mbeanOperations = new MBeanOperationInfo[operations.length];
        for (int i = 0; i < operations.length; i++) {
            mbeanOperations[i] = operations[i].getMbeanOperationInfo();
        }

        mbeanInfo = new MBeanInfo(
                beanInfo.getClassName(),
                null,
                mbeanAttributes,
                new MBeanConstructorInfo[0],
                mbeanOperations,
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

    public Map getAttributeIndex() {
        return Collections.unmodifiableMap(new HashMap(attributeIndex));
    }

    public Map getOperationIndex() {
        return Collections.unmodifiableMap(new HashMap(operationIndex));
    }

    public GBeanInfo getGBeanInfo() {
        return gbeanInfo;
    }

    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
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
            if (attributeIndex.containsKey(name)) {
                parameters[i] = getAttribute(name);
            } else if (referenceIndex.containsKey(name)) {
                GBeanMBeanReference reference = getReferenceByName(name);
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
        for (int i = 0; i < attributes.length; i++) {
            attributes[i].online();
        }

        // bring any reference not used in the constructor online
        // @todo this code sucks, but works
        for (int i = 0; i < references.length; i++) {
            GBeanMBeanReference reference = references[i];
            if (!constructorInfo.getAttributeNames().contains(reference.getName())) {
                reference.online();
            }
        }

        // create the raw invoker for this gbean.... this MUST be closed
        // when the gbean goes offline or we will get a memory leak
        rawInvoker = new RawInvoker(this);

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
            for (int i = 0; i < references.length; i++) {
                references[i].offline();
            }

            // clean up the raw invoker... this holds a reference to this gbean (a possible memory leak)
            if (rawInvoker != null) {
                rawInvoker.close();
                rawInvoker = null;
            }

            // well that didn't work, ditch the instance
            target = null;
        }
    }

    public void postDeregister() {
        // take all of the attributes offline
        for (int i = 0; i < attributes.length; i++) {
            attributes[i].offline();
        }

        // take all of the reference offline
        for (int i = 0; i < references.length; i++) {
            references[i].offline();
        }

        if (target instanceof GBean) {
            GBean gbean = (GBean) target;
            gbean.setGBeanContext(null);
        }

        // clean up the raw invoker... this holds a reference to this gbean (a possible memory leak)
        if (rawInvoker != null) {
            rawInvoker.close();
            rawInvoker = null;
        }

        offline = true;
        target = null;

        super.postDeregister();
    }

    protected void doStart() throws Exception {
        // start all of the references
        for (int i = 0; i < references.length; i++) {
            references[i].start();
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
        for (int i = 0; i < references.length; i++) {
            references[i].stop();
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
        for (int i = 0; i < references.length; i++) {
            references[i].stop();
        }
    }

    public Object getAttribute(int index) throws ReflectionException {
        GBeanMBeanAttribute attribute = attributes[index];
        return attribute.getValue();
    }

    public Object getAttribute(String attributeName) throws ReflectionException, AttributeNotFoundException {
        GBeanMBeanAttribute attribute = getAttributeByName(attributeName);
        if (attribute == null && attributeName.equals(RAW_INVOKER)) {
            return rawInvoker;
        }
        return attribute.getValue();
    }

    public void setAttribute(int index, Object value) throws ReflectionException, AttributeNotFoundException {
        GBeanMBeanAttribute attribute = attributes[index];
        attribute.setValue(value);
    }

    public void setAttribute(String name, Object value) throws ReflectionException, AttributeNotFoundException {
        GBeanMBeanAttribute attribute = getAttributeByName(name);
        attribute.setValue(value);
    }

    public void setAttribute(Attribute attributeValue) throws ReflectionException, AttributeNotFoundException {
        GBeanMBeanAttribute attribute = getAttributeByName(attributeValue.getName());
        attribute.setValue(attributeValue.getValue());
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

    private GBeanMBeanAttribute getAttributeByName(String name) throws AttributeNotFoundException {
        Integer index = (Integer) attributeIndex.get(name);
        if (index == null) {
            // if this is a request for the raw invoker we need to return null
            // todo switch this to an attribute when fixing the ManagedObject interface attributes
            if (name.equals(RAW_INVOKER)) {
                return null;
            }
            throw new AttributeNotFoundException("Unknown attribute " + name);
        }
        GBeanMBeanAttribute attribute = attributes[index.intValue()];
        return attribute;
    }

    public Object invoke(int index, Object[] arguments) throws ReflectionException {
        GBeanMBeanOperation operation = operations[index];
        return operation.invoke(arguments);
    }

    public Object invoke(String methodName, Object[] arguments, String[] types) throws ReflectionException {
        GOperationSignature signature = new GOperationSignature(methodName, types);
        Integer index = (Integer) operationIndex.get(signature);
        if (index == null) {
            throw new ReflectionException(new NoSuchMethodException("Unknown operation " + signature));
        }
        GBeanMBeanOperation operation = operations[index.intValue()];
        return operation.invoke(arguments);
    }

    public Set getReferencePatterns(String name) {
        return getReferenceByName(name).getPatterns();
    }

    public void setReferencePatterns(String name, Set patterns) {
        getReferenceByName(name).setPatterns(patterns);
    }

    private GBeanMBeanReference getReferenceByName(String name) {
        Integer index = (Integer) referenceIndex.get(name);
        if (index == null) {
            throw new IllegalArgumentException("Unknown reference " + name);
        }
        GBeanMBeanReference reference = references[index.intValue()];
        return reference;
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return mbeanInfo.getNotifications();
    }

    private void addManagedObjectAttributes(Set attributesSet) {
        // todo none of these are going to be handled by the rawInvoker
        attributesSet.add(new GBeanMBeanAttribute(
                this,
                "state",
                Integer.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Integer(getState());
                    }
                },
                null));

        attributesSet.add(new GBeanMBeanAttribute(
                this,
                "objectName",
                String.class,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return getObjectName();
                    }
                },
                null));

        attributesSet.add(new GBeanMBeanAttribute(
                this,
                "startTime",
                Long.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Long(getStartTime());
                    }
                },
                null));

        attributesSet.add(new GBeanMBeanAttribute(
                this,
                "stateManageable",
                Boolean.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Boolean(isStateManageable());
                    }
                },
                null));

        attributesSet.add(new GBeanMBeanAttribute(
                this,
                "statisticsProvider",
                Boolean.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Boolean(isStatisticsProvider());
                    }
                },
                null));


        attributesSet.add(new GBeanMBeanAttribute(
                this,
                "eventProvider",
                Boolean.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Boolean(isEventProvider());
                    }
                },
                null));
    }

    private void addManagedObjectOperations(Set operationsSet) {
        // todo none of these are going to be handled by the rawInvoker
        operationsSet.add(new GBeanMBeanOperation(
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

        operationsSet.add(new GBeanMBeanOperation(
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

        operationsSet.add(new GBeanMBeanOperation(
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
    }
}
