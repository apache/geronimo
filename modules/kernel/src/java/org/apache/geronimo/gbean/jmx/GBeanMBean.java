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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.apache.geronimo.gbean.GOperationSignature;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.management.NotificationType;

/**
 * A GBeanMBean is a J2EE Management Managed Object, and is standard base for Geronimo services.
 * This wraps one or more target POJOs and exposes the attributes and operations according to a supplied
 * {@link GBeanInfo} instance.  The GBeanMBean also supports caching of attribute values and invocation results
 * which can reduce the number of calls to a target.
 *
 * @version $Revision: 1.18 $ $Date: 2004/06/02 05:33:03 $
 */
public class GBeanMBean extends AbstractManagedObject implements DynamicMBean {
    /**
     * Method name used to retrieve the RawInvoker for the GBean
     */
    static final String RAW_INVOKER = "$$RAW_INVOKER$$";

    private static final Log log = LogFactory.getLog(GBeanMBean.class);
    private final Constructor constructor;

    /**
     * Gets the context class loader from the thread or the system class loader if there is no context class loader.
     *
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

    /**
     * Constructa a GBeanMBean using the supplied gbeanInfo and class loader
     * @param gbeanInfo the metadata describing the attributes, operations, constructor and references of the gbean
     * @param classLoader the class loader used to load the gbean instance and attribute/reference types
     * @throws InvalidConfigurationException if the gbeanInfo is inconsistent with the actual java classes, such as
     *  mismatched attribute types
     */
    public GBeanMBean(GBeanInfo gbeanInfo, ClassLoader classLoader) throws InvalidConfigurationException {
        this.gbeanInfo = gbeanInfo;
        this.classLoader = classLoader;
        try {
            type = classLoader.loadClass(gbeanInfo.getClassName());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load GBeanInfo class from classloader: " +
                    " className=" + gbeanInfo.getClassName());
        }

        name = gbeanInfo.getName();

        // get the constructor
        constructor = searchForConstructor(gbeanInfo, type);

        // build a map from constructor argument names to type
        Class[] constructorParameterTypes = constructor.getParameterTypes();
        Map constructorTypes = new HashMap(constructorParameterTypes.length);
        List constructorAttributeNames = this.gbeanInfo.getConstructor().getAttributeNames();
        for (int i = 0; i < constructorParameterTypes.length; i++) {
            Class type = constructorParameterTypes[i];
            constructorTypes.put(constructorAttributeNames.get(i), type);
        }

        // attributes
        Set attributesSet = new HashSet();
        for (Iterator iterator = gbeanInfo.getAttributes().iterator(); iterator.hasNext();) {
            GAttributeInfo attributeInfo = (GAttributeInfo) iterator.next();
            attributesSet.add(new GBeanMBeanAttribute(this, attributeInfo, constructorTypes.containsKey(attributeInfo.getName())));
        }
        addManagedObjectAttributes(attributesSet);
        attributes = (GBeanMBeanAttribute[]) attributesSet.toArray(new GBeanMBeanAttribute[attributesSet.size()]);
        for (int i = 0; i < attributes.length; i++) {
            attributeIndex.put(attributes[i].getName(), new Integer(i));
        }

        // references
        Set referencesSet = new HashSet();
        for (Iterator iterator = gbeanInfo.getReferences().iterator(); iterator.hasNext();) {
            GReferenceInfo referenceInfo = (GReferenceInfo) iterator.next();
            referencesSet.add(new GBeanMBeanReference(this, referenceInfo, (Class) constructorTypes.get(referenceInfo.getName())));
        }
        references = (GBeanMBeanReference[]) referencesSet.toArray(new GBeanMBeanReference[gbeanInfo.getReferences().size()]);
        for (int i = 0; i < references.length; i++) {
            referenceIndex.put(references[i].getName(), new Integer(i));
        }

        // operations
        Set operationsSet = new HashSet();
        for (Iterator iterator = gbeanInfo.getOperations().iterator(); iterator.hasNext();) {
            GOperationInfo operationInfo = (GOperationInfo) iterator.next();
            operationsSet.add(new GBeanMBeanOperation(this, operationInfo));
        }
        addManagedObjectOperations(operationsSet);
        operations = (GBeanMBeanOperation[]) operationsSet.toArray(new GBeanMBeanOperation[gbeanInfo.getOperations().size()]);
        for (int i = 0; i < operations.length; i++) {
            GBeanMBeanOperation operation = operations[i];
            GOperationSignature signature = new GOperationSignature(operation.getName(), operation.getParameterTypes());
            operationIndex.put(signature, new Integer(i));
        }

        // add notification type from the ManagedObject interface
        notifications.add(new MBeanNotificationInfo(NotificationType.TYPES,
                "javax.management.Notification",
                "J2EE Notifications"));

        // Build the MBeanInfo
        ArrayList mbeanAttributesList = new ArrayList(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            // only add the attributes that are readable or writable
            MBeanAttributeInfo mbeanAttributeInfo = attributes[i].getMBeanAttributeInfo();
            if (mbeanAttributeInfo != null) {
                mbeanAttributesList.add(mbeanAttributeInfo);
            }
        }
        MBeanAttributeInfo[] mbeanAttributes = (MBeanAttributeInfo[]) mbeanAttributesList.toArray(new MBeanAttributeInfo[attributes.length]);

        MBeanOperationInfo[] mbeanOperations = new MBeanOperationInfo[operations.length];
        for (int i = 0; i < operations.length; i++) {
            mbeanOperations[i] = operations[i].getMbeanOperationInfo();
        }

        mbeanInfo = new MBeanInfo(gbeanInfo.getClassName(),
                null,
                mbeanAttributes,
                new MBeanConstructorInfo[0],
                mbeanOperations,
                // Is there any way to add notifications before an instance of the class is created?
                (MBeanNotificationInfo[]) notifications.toArray(new MBeanNotificationInfo[notifications.size()]));
    }

    /**
     * Search for a single valid constructor in the class.  A valid constructor is determined by the
     * attributes and references declared in the GBeanInfo.  For each, constructor gbean attribute
     * the parameter must have the exact same type.  For a constructor gbean reference parameter, the
     * parameter type must either match the reference proxy type, be java.util.Collection, or be
     * java.util.Set.
     *
     * @param beanInfo the metadata describing the constructor, attrbutes and references
     * @param type the target type in which we search for a constructor
     * @return the sole matching constructor
     * @throws InvalidConfigurationException if there are no valid constructors or more then one valid
     * constructors; multiple constructors can match in the case of a gbean reference parameter
     */
    private static Constructor searchForConstructor(GBeanInfo beanInfo, Class type) throws InvalidConfigurationException {
        Set attributes = beanInfo.getAttributes();
        Map attributeTypes = new HashMap(attributes.size());
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            GAttributeInfo attribute = (GAttributeInfo) iterator.next();
            attributeTypes.put(attribute.getName(), attribute.getType());
        }

        Set references = beanInfo.getReferences();
        Map referenceTypes = new HashMap(references.size());
        for (Iterator iterator = references.iterator(); iterator.hasNext();) {
            GReferenceInfo reference = (GReferenceInfo) iterator.next();
            referenceTypes.put(reference.getName(), reference.getType());
        }

        List arguments = beanInfo.getConstructor().getAttributeNames();
        String[] argumentTypes = new String[arguments.size()];
        boolean[] isReference = new boolean[arguments.size()];
        for (int i = 0; i < argumentTypes.length; i++) {
            String argumentName = (String) arguments.get(i);
            if (attributeTypes.containsKey(argumentName)) {
                argumentTypes[i] = (String) attributeTypes.get(argumentName);
                isReference[i] = false;
            } else if (referenceTypes.containsKey(argumentName)) {
                argumentTypes[i] = (String) referenceTypes.get(argumentName);
                isReference[i] = true;
            }
        }

        Constructor[] constructors = type.getConstructors();
        Set validConstructors = new HashSet();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            if (isValidConstructor(constructor, argumentTypes, isReference)) {
                validConstructors.add(constructor);
            }
        }

        if (validConstructors.isEmpty()) {
            throw new InvalidConfigurationException("Could not find a valid constructor for GBean: " + beanInfo.getName());
        }
        if (validConstructors.size() > 1) {
            throw new InvalidConfigurationException("More then one valid constructors found for GBean: " + beanInfo.getName());
        }
        return (Constructor) validConstructors.iterator().next();
    }

    /**
     * Is this a valid constructor for the GBean.  This is determined based on the argument types and
     * if an argument is a reference, as determined by the boolean array, the argument may also be
     * java.util.Collection or java.util.Set.
     * @param constructor the class constructor
     * @param argumentTypes types of the attributes and references
     * @param isReference if the argument is a gbean reference
     * @return true if this is a valid constructor for gbean; false otherwise
     */
    private static boolean isValidConstructor(Constructor constructor, String[] argumentTypes, boolean[] isReference) {
        Class[] parameterTypes = constructor.getParameterTypes();

        // same number of parameters?
        if (parameterTypes.length != argumentTypes.length) {
            return false;
        }

        // is each parameter the correct type?
        for (int i = 0; i < parameterTypes.length; i++) {
            String parameterType = parameterTypes[i].getName();
            if (isReference[i]) {
                // reference: does type match
                // OR is it a java.util.Collection
                // OR is it a java.util.Set?
                if (!parameterType.equals(argumentTypes[i]) &&
                        !parameterType.equals(Collection.class.getName()) &&
                        !parameterType.equals(Set.class.getName())) {
                     return false;
                }
            } else {
                // attribute: does type match?
                if (!parameterType.equals(argumentTypes[i])) {
                     return false;
                }
            }
        }
        return true;
    }

    public GBeanMBean(GBeanInfo beanInfo) throws InvalidConfigurationException {
        this(beanInfo, getContextClassLoader());
    }

    /**
     * "Bootstrapping" constructor.  The class specified is loaded and the static method
     * "getGBeanInfo" is called to get the gbean info.  Usually one will include
     * this static method in the class to be wrapped in the GBeanMBean instance.
     *
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
     *
     * @param className name of the class to call getGBeanInfo on
     * @throws java.lang.Exception if an exception occurs while getting the GBeanInfo from the class
     */
    public GBeanMBean(String className) throws Exception {
        this(className, ClassLoader.getSystemClassLoader());
    }

    /**
     * Gets the name of the GBean as defined in the gbean info.
     * @return the gbean name
     */
    public String getName() {
        return name;
    }

    /**
     * The class loader used to build this gbean.  This class loader is set into the thread context
     * class loader before callint the target instace.
     * @return the class loader used to build this gbean
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Is this gbean offline. An offline gbean is not registered with jmx and effectivly invisible
     * to external users.
     * @return true if the gbean is offline
     */
    public boolean isOffline() {
        return offline;
    }

    /**
     * The java type of the wrapped gbean instance
     * @return the java type of the gbean
     */
    public Class getType() {
        return type;
    }

    public Object getTarget() {
        // todo this seems like a really realy bad idea
        return target;
    }

    /**
     * Gets an unmodifiable map from attribute names to index number (Integer).  This index number
     * can be used to efficiently set or retrieve an attribute value.
     * @return an unmodifiable map of attribute indexes by name
     */
    public Map getAttributeIndex() {
        return Collections.unmodifiableMap(new HashMap(attributeIndex));
    }

    /**
     * Gets an unmodifiable map from operation signature (GOperationSignature) to index number (Integer).
     * This index number can be used to efficciently invoke the operation.
     * @return an unmodifiable map of operation indexec by signature
     */
    public Map getOperationIndex() {
        return Collections.unmodifiableMap(new HashMap(operationIndex));
    }

    /**
     * Gets the GBeanInfo used to build this gbean.
     * @return the GBeanInfo used to build this gbean
     */
    public GBeanInfo getGBeanInfo() {
        return gbeanInfo;
    }

    /**
     * Gets the MBeanInfo equivilent of the GBeanInfo used to construct this gbean.
     * @return the MBeanInfo for this gbean
     */
    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    public synchronized ObjectName preRegister(MBeanServer server, ObjectName objectName) throws Exception {
        ObjectName returnValue = super.preRegister(server, objectName);

        GConstructorInfo constructorInfo = gbeanInfo.getConstructor();
        Class[] parameterTypes = constructor.getParameterTypes();

        // create parameter array
        Object[] parameters = new Object[parameterTypes.length];
        Iterator names = constructorInfo.getAttributeNames().iterator();
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
            assert parameters[i] == null || parameterTypes[i].isPrimitive() || parameterTypes[i].isAssignableFrom(parameters[i].getClass()):
                    "Attempting to construct " + objectName + " of type " + gbeanInfo.getClassName()
                    + ". Constructor parameter " + i + " should be " + parameterTypes[i].getName()
                    + " but is " + parameters[i].getClass().getName();
        }

        // create instance
        try {
            target = constructor.newInstance(parameters);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof Exception) {
                throw (Exception) targetException;
            } else if (targetException instanceof Error) {
                throw (Error) targetException;
            }
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("Constructor mismatch for " + returnValue, e);
            throw e;
        }

        // bring all of the attributes online; this causes the persistent
        // values to be set into the instance if it is not a constructor arg
        for (int i = 0; i < attributes.length; i++) {
            attributes[i].online();
        }

        // bring any reference not used in the constructor online; this causes
        // the proxy to be set into the intstance
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

    /**
     * Gets the attribute value using the attribute index.  This is the most efficient way to get
     * an attribute as it avoids a HashMap lookup.
     * @param index the index of the attribute
     * @return the attribute value
     * @throws ReflectionException if a problem occurs while getting the value
     * @thorws IndexOutOfBoundsException if the index is invalid
     */
    public Object getAttribute(int index) throws ReflectionException {
        GBeanMBeanAttribute attribute = attributes[index];
        return attribute.getValue();
    }

    /**
     * Gets an attirubte's value by name.  This get style is less efficient becuse the attribute must
     * first be looked up in a HashMap.
     * @param attributeName the name of the attribute to retrieve
     * @return the attribute value
     * @throws ReflectionException if a problem occurs while getting the value
     * @throws AttributeNotFoundException if the attribute name is not found in the map
     */
    public Object getAttribute(String attributeName) throws ReflectionException, AttributeNotFoundException {
        GBeanMBeanAttribute attribute = getAttributeByName(attributeName);
        if (attribute == null && attributeName.equals(RAW_INVOKER)) {
            return rawInvoker;
        }
        return attribute.getValue();
    }

    /**
     * Sets the attribute value using the attribute index.  This is the most efficient way to set
     * an attribute as it avoids a HashMap lookup.
     * @param index the index of the attribute
     * @param value the new value of attribute value
     * @throws ReflectionException if a problem occurs while setting the value
     * @thorws IndexOutOfBoundsException if the index is invalid
     */
    public void setAttribute(int index, Object value) throws ReflectionException, IndexOutOfBoundsException {
        GBeanMBeanAttribute attribute = attributes[index];
        attribute.setValue(value);
    }

    /**
     * Sets an attirubte's value by name.  This set style is less efficient becuse the attribute must
     * first be looked up in a HashMap.
     * @param attributeName the name of the attribute to retrieve
     * @param value the new attribute value
     * @throws ReflectionException if a problem occurs while getting the value
     * @throws AttributeNotFoundException if the attribute name is not found in the map
     */
    public void setAttribute(String attributeName, Object value) throws ReflectionException, AttributeNotFoundException {
        GBeanMBeanAttribute attribute = getAttributeByName(attributeName);
        attribute.setValue(value);
    }

    /**
     * Sets an attirubte's value by name.  This set style is generally very inefficient becuse the attribute object
     * is usually constructed first and the target attribute must be looked up in a HashMap.
     * @param attributeValue the attribute object, which contains a name and value
     * @throws ReflectionException if a problem occurs while getting the value
     * @throws AttributeNotFoundException if the attribute name is not found in the map
     */
    public void setAttribute(Attribute attributeValue) throws ReflectionException, AttributeNotFoundException {
        GBeanMBeanAttribute attribute = getAttributeByName(attributeValue.getName());
        attribute.setValue(attributeValue.getValue());
    }

    /**
     * Gets several attirubte values by name.  This set style is very inefficient becuse each attribute implementation
     * must be looked up in a HashMap by name and each value must be wrapped in an Attribute object and that requires
     * lots of object creation.  Further, any exceptions are not seen by the caller.
     * @param attributes the attribute objects, which contains a name and value
     */
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

    /**
     * Sets several attirubte values by name.  This set style is generally very inefficient becuse each attribute object
     * is usually constructed first and the target attribute must be looked up in a HashMap.  Further
     * any exception are not seen by the caller.
     * @param attributes the attribute objects, which contains a name and value
     */
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
            throw new AttributeNotFoundException("Unknown attribute " + name);
        }
        GBeanMBeanAttribute attribute = attributes[index.intValue()];
        return attribute;
    }

    public Object invoke(int index, Object[] arguments) throws ReflectionException {
        GBeanMBeanOperation operation = operations[index];
        return operation.invoke(arguments);
    }

    /**
     * Invokes an operation on the target gbean by method signature.  This style if invocation is
     * inefficient, because the target method must be looked up in a hashmap using a freshly constructed
     * GOperationSignature object.
     * @param operationName the name of the operation to invoke
     * @param arguments arguments to the operation
     * @param types types of the operation arguemtns
     * @return the result of the operation
     * @throws ReflectionException if a problem occurs while invokeing the operation
     */
    public Object invoke(String operationName, Object[] arguments, String[] types) throws ReflectionException {
        GOperationSignature signature = new GOperationSignature(operationName, types);
        Integer index = (Integer) operationIndex.get(signature);
        if (index == null) {
            throw new ReflectionException(new NoSuchMethodException("Unknown operation " + signature));
        }
        GBeanMBeanOperation operation = operations[index.intValue()];
        return operation.invoke(arguments);
    }

    /**
     * Gets the object name patters for a reference.
     * @param name the reference name
     * @return the object name patterns for the reference
     */
    public Set getReferencePatterns(String name) {
        return getReferenceByName(name).getPatterns();
    }

    /**
     * Sets the object name patterns for a reference.
     * @param name the reference name
     * @param patterns the new object name patterns for the reference
     */
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
        attributesSet.add(new GBeanMBeanAttribute(this,
                RAW_INVOKER,
                RawInvoker.class,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return rawInvoker;
                    }
                },
                null));

        attributesSet.add(new GBeanMBeanAttribute(this,
                "state",
                Integer.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Integer(getState());
                    }
                },
                null));

        attributesSet.add(new GBeanMBeanAttribute(this,
                "objectName",
                String.class,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return getObjectName();
                    }
                },
                null));

        attributesSet.add(new GBeanMBeanAttribute(this,
                "startTime",
                Long.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Long(getStartTime());
                    }
                },
                null));

        attributesSet.add(new GBeanMBeanAttribute(this,
                "stateManageable",
                Boolean.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Boolean(isStateManageable());
                    }
                },
                null));

        attributesSet.add(new GBeanMBeanAttribute(this,
                "statisticsProvider",
                Boolean.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        return new Boolean(isStatisticsProvider());
                    }
                },
                null));


        attributesSet.add(new GBeanMBeanAttribute(this,
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
        operationsSet.add(new GBeanMBeanOperation(this,
                "start",
                Collections.EMPTY_LIST,
                Void.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        start();
                        return null;
                    }
                }));

        operationsSet.add(new GBeanMBeanOperation(this,
                "startRecursive",
                Collections.EMPTY_LIST,
                Void.TYPE,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        startRecursive();
                        return null;
                    }
                }));

        operationsSet.add(new GBeanMBeanOperation(this,
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
