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

package org.apache.geronimo.gbean.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GBeanLifecycleController;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GOperationSignature;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.management.EventProvider;
import org.apache.geronimo.kernel.management.ManagedObject;
import org.apache.geronimo.kernel.management.NotificationType;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.management.StateManageable;

/**
 * A GBeanInstance is a J2EE Management Managed Object, and is standard base for Geronimo services.
 *
 * @version $Rev: 106387 $ $Date: 2004-11-23 22:16:54 -0800 (Tue, 23 Nov 2004) $
 */
public final class GBeanInstance implements ManagedObject, StateManageable, EventProvider {
    private static final Log log = LogFactory.getLog(GBeanInstance.class);

    /**
     * Attribute name used to retrieve the RawInvoker for the GBean
     * @deprecated DO NOT USE THIS... THIS WILL BE REMOVED!!!!!!
     */
    public static final String RAW_INVOKER = "$$RAW_INVOKER$$";

    /**
     * The kernel in which this server is registered.
     */
    private final Kernel kernel;

    /**
     * The unique name of this service.
     */
    private final ObjectName objectName;

    /**
     * This handles all state transiitions for this instance.
     */
    private final GBeanInstanceState gbeanInstanceState;

    /**
     * The constructor used to create the instance
     */
    private final Constructor constructor;

    /**
     * A fast index based raw invoker for this GBean.
     */
    private final RawInvoker rawInvoker;

    /**
     * The single listener to which we broadcast lifecycle change events.
     */
    private final LifecycleBroadcaster lifecycleBroadcaster;

    /**
     * The lifecycle controller given to the instance
     */
    private final GBeanLifecycleController gbeanLifecycleController;

    /**
     * Attributes lookup table
     */
    private final GBeanAttribute[] attributes;

    /**
     * Attributes supported by this GBeanMBean by (String) name.
     */
    private final Map attributeIndex = new HashMap();

    /**
     * References lookup table
     */
    private final GBeanReference[] references;

    /**
     * References supported by this GBeanMBean by (String) name.
     */
    private final Map referenceIndex = new HashMap();

    /**
     * Operations lookup table
     */
    private final GBeanOperation[] operations;

    /**
     * Operations supported by this GBeanMBean by (GOperationSignature) name.
     */
    private final Map operationIndex = new HashMap();

    /**
     * The classloader used for all invocations and creating targets.
     */
    private final ClassLoader classLoader;

    /**
     * Metadata describing the attributes, operations and references of this GBean
     */
    private final GBeanInfo gbeanInfo;

    /**
     * Our name
     */
    private final String name;

    /**
     * Java type of the wrapped GBean class
     */
    private final Class type;

    /**
     * Has this instance been destroyed?
     */
    private boolean destroyed = true;

    /**
     * Is this gbean running?
     */
    private boolean running = false;

    /**
     * Target instance of this GBean wrapper
     */
    private Object target;

    /**
     * The time this application started.
     */
    private long startTime;

    /**
     * Is this gbean enabled?  A disabled gbean can not be started.
     */
    private boolean enabled = true;


    /**
     * Construct a GBeanMBean using the supplied GBeanData and class loader
     *
     * @param gbeanData the data for the new GBean including GBeanInfo, intial attribute values, and reference patterns
     * @param classLoader the class loader used to load the gbean instance and attribute/reference types
     * @throws org.apache.geronimo.gbean.InvalidConfigurationException if the gbeanInfo is inconsistent with the actual java classes, such as
     * mismatched attribute types or the intial data cannot be set
     */
    public GBeanInstance(GBeanData gbeanData, Kernel kernel, DependencyManager dependencyManager, LifecycleBroadcaster lifecycleBroadcaster, ClassLoader classLoader) throws InvalidConfigurationException {
        this.objectName = gbeanData.getName();
        this.kernel = kernel;
        this.lifecycleBroadcaster = lifecycleBroadcaster;
        this.gbeanInstanceState = new GBeanInstanceState(objectName, kernel, dependencyManager, new GBeanLifecycleCallback(), lifecycleBroadcaster);
        this.classLoader = classLoader;
        gbeanLifecycleController = new GBeanInstanceLifecycleController(this);

        GBeanInfo gbeanInfo = gbeanData.getGBeanInfo();
        try {
            type = classLoader.loadClass(gbeanInfo.getClassName());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load GBeanInfo class from classloader: " +
                    " className=" + gbeanInfo.getClassName());
        }

        name = gbeanInfo.getName();

        //
        Set constructorArgs = new HashSet(gbeanInfo.getConstructor().getAttributeNames());

        // attributes
        Map attributesMap = new HashMap();
        for (Iterator iterator = gbeanInfo.getAttributes().iterator(); iterator.hasNext();) {
            GAttributeInfo attributeInfo = (GAttributeInfo) iterator.next();
            attributesMap.put(attributeInfo.getName(), new GBeanAttribute(this, attributeInfo, constructorArgs.contains(attributeInfo.getName())));
        }
        addManagedObjectAttributes(attributesMap);
        attributes = (GBeanAttribute[]) attributesMap.values().toArray(new GBeanAttribute[attributesMap.size()]);
        for (int i = 0; i < attributes.length; i++) {
            attributeIndex.put(attributes[i].getName(), new Integer(i));
        }

        // references
        Set referencesSet = new HashSet();
        for (Iterator iterator = gbeanInfo.getReferences().iterator(); iterator.hasNext();) {
            GReferenceInfo referenceInfo = (GReferenceInfo) iterator.next();
            if (referenceInfo.getProxyType().equals(Collection.class.getName())) {
                referencesSet.add(new GBeanCollectionReference(this, referenceInfo, kernel, dependencyManager));
            } else {
                referencesSet.add(new GBeanSingleReference(this, referenceInfo, kernel, dependencyManager));
            }
        }
        references = (GBeanReference[]) referencesSet.toArray(new GBeanReference[gbeanInfo.getReferences().size()]);
        for (int i = 0; i < references.length; i++) {
            referenceIndex.put(references[i].getName(), new Integer(i));
        }

        // operations
        Map operationsMap = new HashMap();
        addManagedObjectOperations(operationsMap);
        for (Iterator iterator = gbeanInfo.getOperations().iterator(); iterator.hasNext();) {
            GOperationInfo operationInfo = (GOperationInfo) iterator.next();
            GOperationSignature signature = new GOperationSignature(operationInfo.getName(), operationInfo.getParameterList());
            // do not allow overriding of framework operations
            if (!operationsMap.containsKey(signature)) {
                GBeanOperation operation = new GBeanOperation(this, operationInfo);
                operationsMap.put(signature, operation);
            }
        }
        operations = new GBeanOperation[operationsMap.size()];
        int opCounter = 0;
        for (Iterator iterator = operationsMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry)iterator.next();
            operations[opCounter] = (GBeanOperation) entry.getValue();
            operationIndex.put(entry.getKey(), new Integer(opCounter));
            opCounter++;
        }

        // get the constructor
        List arguments = gbeanInfo.getConstructor().getAttributeNames();
        Class[] parameterTypes = new Class[arguments.size()];
        for (int i = 0; i < parameterTypes.length; i++) {
            String argumentName = (String) arguments.get(i);
            if (attributeIndex.containsKey(argumentName)) {
                Integer index = (Integer) attributeIndex.get(argumentName);
                GBeanAttribute attribute = attributes[index.intValue()];
                parameterTypes[i] = attribute.getType();
            } else if (referenceIndex.containsKey(argumentName)) {
                Integer index = (Integer) referenceIndex.get(argumentName);
                GBeanReference reference = references[index.intValue()];
                parameterTypes[i] = reference.getProxyType();
            }
        }
        try {
            constructor = type.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new InvalidConfigurationException("Could not find a valid constructor for GBean: " + gbeanInfo.getName());
        }

        // rebuild the gbean info based on the current attributes, operations, and references because
        // the above code add new attributes and operations
        this.gbeanInfo = rebuildGBeanInfo(gbeanInfo.getConstructor());

        // create the raw invokers
        rawInvoker = new RawInvoker(this);

        // set the initial attribute values
        try {
            setGBeanData(gbeanData);
        } catch (Exception e) {
            throw new InvalidConfigurationException("GBeanData could not be loaded into the GBeanMBean", e);
        }

        for (int i = 0; i < references.length; i++) {
            references[i].online();
        }
        lifecycleBroadcaster.fireLoadedEvent();
    }

    public void destroy() {
        synchronized (this) {
            if (destroyed) {
                return;
            }
            destroyed = true;
        }

        lifecycleBroadcaster.fireUnloadedEvent();

        // just to be sure, stop all the references again
        for (int i = 0; i < references.length; i++) {
            references[i].offline();
        }

        target = null;
    }

    /**
     * Gets the name of the GBean as defined in the gbean info.
     *
     * @return the gbean name
     */
    public String getName() {
        return name;
    }

    /**
     * The class loader used to build this gbean.  This class loader is set into the thread context
     * class loader before callint the target instace.
     *
     * @return the class loader used to build this gbean
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Has this gbean instance been destroyed. An destroyed gbean can no longer be used.
     *
     * @return true if the gbean has been destroyed
     */
    public synchronized boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Is this gbean instance running. Operations and non-persistenct attribtes can not be accessed while not running.
     *
     * @return true if the gbean is runing
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * The java type of the wrapped gbean instance
     *
     * @return the java type of the gbean
     */
    public Class getType() {
        return type;
    }

    public Object getTarget() {
        return target;
    }

    public final String getObjectName() {
        return objectName.getCanonicalName();
    }

    public final ObjectName getObjectNameObject() {
        return objectName;
    }

    /**
     * Is this gbean enabled.  A disabled gbean can not be started.
     *
     * @return true if the gbean is enabled and can be started
     */
    public synchronized final boolean isEnabled() {
        return enabled;
    }

    /**
     * Changes the enabled status.
     *
     * @param enabled the new enabled flag
     */
    public synchronized final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public final boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public final boolean isEventProvider() {
        return true;
    }

    public final String[] getEventTypes() {
        return NotificationType.TYPES;
    }

    public final long getStartTime() {
        return startTime;
    }

    public int getState() {
        return gbeanInstanceState.getState();
    }

    public final State getStateInstance() {
        return gbeanInstanceState.getStateInstance();
    }

    /**
     * Gets an unmodifiable map from attribute names to index number (Integer).  This index number
     * can be used to efficiently set or retrieve an attribute value.
     *
     * @return an unmodifiable map of attribute indexes by name
     */
    public Map getAttributeIndex() {
        return Collections.unmodifiableMap(new HashMap(attributeIndex));
    }

    /**
     * Gets an unmodifiable map from operation signature (GOperationSignature) to index number (Integer).
     * This index number can be used to efficciently invoke the operation.
     *
     * @return an unmodifiable map of operation indexec by signature
     */
    public Map getOperationIndex() {
        return Collections.unmodifiableMap(new HashMap(operationIndex));
    }

    /**
     * Gets the GBeanInfo used to build this gbean.
     *
     * @return the GBeanInfo used to build this gbean
     */
    public GBeanInfo getGBeanInfo() {
        return gbeanInfo;
    }

    /**
     * Moves this GBeanInstance to the starting state and then attempts to move this MBean immediately
     * to the running state.
     *
     * @throws IllegalStateException If the gbean is disabled
     */
    public final void start() {
        synchronized (this) {
            if (!enabled) {
                throw new IllegalStateException("A disabled GBean can not be started: objectName=" + objectName);
            }
        }
        gbeanInstanceState.start();
    }

    /**
     * Starts this GBeanInstance and then attempts to start all of its start dependent children.
     *
     * @throws IllegalStateException If the gbean is disabled
     */
    public final void startRecursive() {
        synchronized (this) {
            if (!enabled) {
                throw new IllegalStateException("A disabled GBean can not be started: objectName=" + objectName);
            }
        }
        gbeanInstanceState.startRecursive();
    }

    /**
     * Moves this GBeanInstance to the STOPPING state, calls stop on all start dependent children, and then attempt
     * to move this MBean to the STOPPED state.
     */
    public final void stop() {
        gbeanInstanceState.stop();
    }

    /**
     * Moves this GBeanInstance to the FAILED state.  There are no calls to dependent children, but they will be
     * notified using standard J2EE management notification.
     */
    final void fail() {
        gbeanInstanceState.fail();
    }

    /**
     * Gets the gbean data for the gbean held by this gbean mbean.
     * @return the gbean data
     */
    public GBeanData getGBeanData() {
        GBeanData gbeanData = new GBeanData(objectName, gbeanInfo);

        // add the attributes
        for (int i = 0; i < attributes.length; i++) {
            GBeanAttribute attribute = attributes[i];
            if (attribute.isPersistent()) {
                String name = attribute.getName();
                Object value;
                if ((running || attribute.isFramework()) && attribute.isReadable()) {
                    try {
                        value = attribute.getValue();
                    } catch (Throwable throwable) {
                        value = attribute.getPersistentValue();
                        log.debug("Could not get the current value of persistent attribute.  The persistent " +
                                "attribute will not reflect the current state attribute. " + attribute.getDescription(), throwable);
                    }
                } else {
                    value = attribute.getPersistentValue();
                }
                gbeanData.setAttribute(name, value);
            }
        }

        // add the references
        for (int i = 0; i < references.length; i++) {
            GBeanReference reference = references[i];
            String name = reference.getName();
            Set patterns = reference.getPatterns();
            gbeanData.setReferencePatterns(name, patterns);
        }
        return gbeanData;
    }

    public void setGBeanData(GBeanData gbeanData) throws Exception, NoSuchAttributeException {
        // set the attributes
        Map attributes = gbeanData.getAttributes();
        for (Iterator iterator = attributes.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            setAttribute(name, value);
        }

        // add the references
        Map references = gbeanData.getReferences();
        for (Iterator iterator = references.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Set patterns = (Set) entry.getValue();
            setReferencePatterns(name, patterns);
        }
    }

    /**
     * Gets the attribute value using the attribute index.  This is the most efficient way to get
     * an attribute as it avoids a HashMap lookup.
     *
     * @param index the index of the attribute
     * @return the attribute value
     * @throws Exception if a target instance throws and exception
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public Object getAttribute(int index) throws Exception {
        GBeanAttribute attribute = attributes[index];
        if (running || attribute.isFramework()) {
            return attribute.getValue();
        } else {
            return attribute.getPersistentValue();
        }
    }

    /**
     * Gets an attribute's value by name.  This get style is less efficient becuse the attribute must
     * first be looked up in a HashMap.
     *
     * @param attributeName the name of the attribute to retrieve
     * @return the attribute value
     * @throws Exception if a problem occurs while getting the value
     * @throws NoSuchAttributeException if the attribute name is not found in the map
     */
    public Object getAttribute(String attributeName) throws NoSuchAttributeException, Exception {
        GBeanAttribute attribute;
        try {
            attribute = getAttributeByName(attributeName);
        } catch (NoSuchAttributeException e) {
            if (attributeName.equals(RAW_INVOKER)) {
                return rawInvoker;
            }
            throw e;
        }

        if (running || attribute.isFramework()) {
            return attribute.getValue();
        } else {
            return attribute.getPersistentValue();
        }
    }

    /**
     * Sets the attribute value using the attribute index.  This is the most efficient way to set
     * an attribute as it avoids a HashMap lookup.
     *
     * @param index the index of the attribute
     * @param value the new value of attribute value
     * @throws Exception if a target instance throws and exception
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public void setAttribute(int index, Object value) throws Exception, IndexOutOfBoundsException {
        GBeanAttribute attribute = attributes[index];
        if (running || attribute.isFramework()) {
            attribute.setValue(value);
        } else {
            attribute.setPersistentValue(value);
        }
    }

    /**
     * Sets an attribute's value by name.  This set style is less efficient becuse the attribute must
     * first be looked up in a HashMap.
     *
     * @param attributeName the name of the attribute to retrieve
     * @param value the new attribute value
     * @throws Exception if a target instance throws and exception
     * @throws NoSuchAttributeException if the attribute name is not found in the map
     */
    public void setAttribute(String attributeName, Object value) throws Exception, NoSuchAttributeException {
        GBeanAttribute attribute = getAttributeByName(attributeName);
        if (running || attribute.isFramework()) {
            attribute.setValue(value);
        } else {
            attribute.setPersistentValue(value);
        }
    }

    private GBeanAttribute getAttributeByName(String name) throws NoSuchAttributeException {
        Integer index = (Integer) attributeIndex.get(name);
        if (index == null) {
            throw new NoSuchAttributeException("Unknown attribute " + name);
        }
        GBeanAttribute attribute = attributes[index.intValue()];
        return attribute;
    }

    /**
     * Invokes an opreation using the operation index.  This is the most efficient way to invoke
     * an operation as it avoids a HashMap lookup.
     *
     * @param index the index of the attribute
     * @param arguments the arguments to the operation
     * @return the result of the operation
     * @throws Exception if a target instance throws and exception
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws IllegalStateException if the gbean instance has been destroyed
     */
    public Object invoke(int index, Object[] arguments) throws Exception {
        GBeanOperation operation = operations[index];
        if (!running && !operation.isFramework()) {
            throw new IllegalStateException("Operations can only be invoke while the GBean instance is running: " + objectName);
        }
        return operation.invoke(arguments);
    }

    /**
     * Invokes an operation on the target gbean by method signature.  This style if invocation is
     * inefficient, because the target method must be looked up in a hashmap using a freshly constructed
     * GOperationSignature object.
     *
     * @param operationName the name of the operation to invoke
     * @param arguments arguments to the operation
     * @param types types of the operation arguemtns
     * @return the result of the operation
     * @throws Exception if a target instance throws and exception
     * @throws NoSuchOperationException if the operation signature is not found in the map
     * @throws IllegalStateException if the gbean instance has been destroyed
     */
    public Object invoke(String operationName, Object[] arguments, String[] types) throws Exception, NoSuchOperationException {
        GOperationSignature signature = new GOperationSignature(operationName, types);
        Integer index = (Integer) operationIndex.get(signature);
        if (index == null) {
            throw new NoSuchOperationException("Unknown operation " + signature);
        }
        GBeanOperation operation = operations[index.intValue()];
        if (!running && !operation.isFramework()) {
            throw new IllegalStateException("Operations can only be invoke while the GBean is running: " + objectName);
        }
        return operation.invoke(arguments);
    }

    /**
     * Gets the object name patters for a reference.
     *
     * @param name the reference name
     * @return the object name patterns for the reference
     */
    public Set getReferencePatterns(String name) {
        return getReferenceByName(name).getPatterns();
    }

    /**
     * Sets a single object name pattern for a reference.
     *
     * @param name the reference name
     * @param pattern the new single object name pattern for the reference
     */
    public void setReferencePattern(String name, ObjectName pattern) {
        getReferenceByName(name).setPatterns(Collections.singleton(pattern));
    }

    /**
     * Sets the object name patterns for a reference.
     *
     * @param name the reference name
     * @param patterns the new object name patterns for the reference
     */
    public void setReferencePatterns(String name, Set patterns) {
        getReferenceByName(name).setPatterns(patterns);
    }

    private GBeanReference getReferenceByName(String name) {
        Integer index = (Integer) referenceIndex.get(name);
        if (index == null) {
            throw new IllegalArgumentException("Unknown reference " + name);
        }
        GBeanReference reference = references[index.intValue()];
        return reference;
    }

    private class GBeanLifecycleCallback implements GBeanLifecycle {
        public void doStart() throws Exception {
            // start each of the references... if they need to wait remember the
            // waiting exception so we can throw it later. this way we the dependecies
            // are held until we can start
            WaitingException waitingException = null;
            for (int i = 0; i < references.length; i++) {
                try {
                    references[i].start();
                } catch (WaitingException e) {
                    waitingException = e;
                }
            }
            if (waitingException != null) {
                throw waitingException;
            }

            GConstructorInfo constructorInfo = gbeanInfo.getConstructor();
            Class[] parameterTypes = constructor.getParameterTypes();

            // create constructor parameter array
            Object[] parameters = new Object[parameterTypes.length];
            Iterator names = constructorInfo.getAttributeNames().iterator();
            for (int i = 0; i < parameters.length; i++) {
                String name = (String) names.next();
                if (attributeIndex.containsKey(name)) {
                    GBeanAttribute attribute = getAttributeByName(name);
                    parameters[i] = attribute.getPersistentValue();
                } else if (referenceIndex.containsKey(name)) {
                    parameters[i] = getReferenceByName(name).getProxy();
                } else {
                    throw new InvalidConfigurationException("Unknown attribute or reference name in constructor: name=" + name);
                }
            }

            // create instance
            try {
                target = constructor.newInstance(parameters);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                    throw (Exception) targetException;
                } else if (targetException instanceof Error) {
                    throw (Error) targetException;
                }
                throw e;
            } catch (IllegalArgumentException e) {
                log.warn("Constructor mismatch for " + objectName, e);
                throw e;
            }

            // inject the persistent attribute value into the new instance
            for (int i = 0; i < attributes.length; i++) {
                attributes[i].inject();
            }

            // inject the proxies into the new instance
            for (int i = 0; i < references.length; i++) {
                references[i].inject();
            }

            running = true;
            startTime = System.currentTimeMillis();
            if (target instanceof GBeanLifecycle) {
                ((GBeanLifecycle) target).doStart();
            }
        }

        public void doStop() throws Exception {
            if (target instanceof GBeanLifecycle) {
                ((GBeanLifecycle) target).doStop();
            }

            running = false;

            // stop all of the references
            for (int i = 0; i < references.length; i++) {
                references[i].stop();
            }

            if (target != null) {
                // stop all of the attributes
                for (int i = 0; i < attributes.length; i++) {
                    GBeanAttribute attribute = attributes[i];
                    if (attribute.isPersistent() && attribute.isReadable()) {
                        // copy the current attribute value to the persistent value
                        Object value = attribute.getValue();
                        attribute.setPersistentValue(value);
                    }
                }

                target = null;
            }
        }

        public void doFail() {
            running = false;

            if (target instanceof GBeanLifecycle) {
                ((GBeanLifecycle) target).doFail();
            }

            // stop all of the references
            for (int i = 0; i < references.length; i++) {
                references[i].stop();
            }

            target = null;

            // do not stop the attibutes in the case of a failure
            // failed gbeans may have corrupted attributes that would be persisted
        }
    }

    private void addManagedObjectAttributes(Map attributesMap) {
        //
        //  Special attributes
        //
        attributesMap.put("objectName",
                GBeanAttribute.createSpecialAttribute((GBeanAttribute) attributesMap.get("objectName"),
                        this,
                        "objectName",
                        String.class,
                        getObjectName()));

        attributesMap.put("gbeanInfo",
                GBeanAttribute.createSpecialAttribute((GBeanAttribute) attributesMap.get("gbeanInfo"),
                        this,
                        "gbeanInfo",
                        GBeanInfo.class,
                        gbeanInfo));

        attributesMap.put("classLoader",
                GBeanAttribute.createSpecialAttribute((GBeanAttribute) attributesMap.get("classLoader"),
                        this,
                        "classLoader",
                        ClassLoader.class,
                        classLoader));

        attributesMap.put("gbeanLifecycleController",
                GBeanAttribute.createSpecialAttribute((GBeanAttribute) attributesMap.get("gbeanLifecycleController"),
                        this,
                        "gbeanLifecycleController",
                        GBeanLifecycleController.class,
                        gbeanLifecycleController));

        attributesMap.put("kernel",
                GBeanAttribute.createSpecialAttribute((GBeanAttribute) attributesMap.get("kernel"),
                        this,
                        "kernel",
                        Kernel.class,
                        kernel));

        //
        // Framework attributes
        //
        attributesMap.put("state",
                GBeanAttribute.createFrameworkAttribute(this,
                        "state",
                        Integer.TYPE,
                        new MethodInvoker() {
                            public Object invoke(Object target, Object[] arguments) throws Exception {
                                return new Integer(getState());
                            }
                        }));

        attributesMap.put("startTime",
                GBeanAttribute.createFrameworkAttribute(this,
                        "startTime",
                        Long.TYPE,
                        new MethodInvoker() {
                            public Object invoke(Object target, Object[] arguments) throws Exception {
                                return new Long(getStartTime());
                            }
                        }));

        attributesMap.put("stateManageable",
                GBeanAttribute.createFrameworkAttribute(this,
                        "stateManageable",
                        Boolean.TYPE,
                        new MethodInvoker() {
                            public Object invoke(Object target, Object[] arguments) throws Exception {
                                return new Boolean(isStateManageable());
                            }
                        }));

        attributesMap.put("statisticsProvider",
                GBeanAttribute.createFrameworkAttribute(this,
                        "statisticsProvider",
                        Boolean.TYPE,
                        new MethodInvoker() {
                            public Object invoke(Object target, Object[] arguments) throws Exception {
                                return new Boolean(isStatisticsProvider());
                            }
                        }));


        attributesMap.put("eventProvider",
                GBeanAttribute.createFrameworkAttribute(this,
                        "eventProvider",
                        Boolean.TYPE,
                        new MethodInvoker() {
                            public Object invoke(Object target, Object[] arguments) throws Exception {
                                return new Boolean(isEventProvider());
                            }
                        }));

        attributesMap.put("eventTypes",
                GBeanAttribute.createFrameworkAttribute(this,
                        "eventTypes",
                        Boolean.TYPE,
                        new MethodInvoker() {
                            public Object invoke(Object target, Object[] arguments) throws Exception {
                                return getEventTypes();
                            }
                        }));

        attributesMap.put("gbeanEnabled",
                GBeanAttribute.createFrameworkAttribute(this,
                        "gbeanEnabled",
                        Boolean.TYPE,
                        new MethodInvoker() {
                            public Object invoke(Object target, Object[] arguments) throws Exception {
                                return new Boolean(isEnabled());
                            }
                        },
                        new MethodInvoker() {
                            public Object invoke(Object target, Object[] arguments) throws Exception {
                                Boolean enabled = (Boolean) arguments[0];
                                setEnabled(enabled.booleanValue());
                                return null;
                            }
                        },
                        true,
                        Boolean.TRUE));
    }

    private void addManagedObjectOperations(Map operationsMap) {
        operationsMap.put(new GOperationSignature("start", Collections.EMPTY_LIST),
                GBeanOperation.createFrameworkOperation(this,
                "start",
                Collections.EMPTY_LIST,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        start();
                        return null;
                    }
                }));

        operationsMap.put(new GOperationSignature("startRecursive", Collections.EMPTY_LIST),
                GBeanOperation.createFrameworkOperation(this,
                "startRecursive",
                Collections.EMPTY_LIST,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        startRecursive();
                        return null;
                    }
                }));

        operationsMap.put(new GOperationSignature("stop", Collections.EMPTY_LIST),
                GBeanOperation.createFrameworkOperation(this,
                "stop",
                Collections.EMPTY_LIST,
                new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        stop();
                        return null;
                    }
                }));
    }

    private GBeanInfo rebuildGBeanInfo(GConstructorInfo constructor) {
        Set attributeInfos = new HashSet();
        for (int i = 0; i < attributes.length; i++) {
            GBeanAttribute attribute = attributes[i];
            attributeInfos.add(attribute.getAttributeInfo());
        }
        Set operationInfos = new HashSet();
        for (int i = 0; i < operations.length; i++) {
            operationInfos.add(operations[i].getOperationInfo());
        }

        Set referenceInfos = new HashSet();
        for (int i = 0; i < references.length; i++) {
            referenceInfos.add(references[i].getReferenceInfo());
        }

        return new GBeanInfo(name,
                type.getName(),
                attributeInfos,
                constructor,
                operationInfos,
                referenceInfos);
    }

    public String toString() {
        if (objectName == null) {
            return super.toString();
        }
        return objectName.toString();
    }
}
