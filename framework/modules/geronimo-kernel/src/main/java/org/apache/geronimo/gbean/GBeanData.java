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
package org.apache.geronimo.gbean;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.annotation.EncryptionSetting;

/**
 * @version $Rev$ $Date$
 */
public class GBeanData implements Externalizable {
    private static final long serialVersionUID = -1012491431781444074L;

    private Externalizable backwardExternalizables[] = new Externalizable[]{
            new V0Externalizable(),
            new V1Externalizable()
    };

    private GBeanInfo gbeanInfo;
    private final GBeanInfoFactory infoFactory;
    private final Map<String, Object> attributes;
    private final Map<String, ReferencePatterns> references;
    private final Set<ReferencePatterns> dependencies;
    private AbstractName abstractName;
    private int priority;
    private String[] serviceInterfaces;
    private Dictionary serviceProperties;


    public GBeanData() {
        attributes = new HashMap<String, Object>();
        references = new HashMap<String, ReferencePatterns>();
        dependencies = new HashSet<ReferencePatterns>();
        infoFactory = newGBeanInfoFactory();
    }

    public GBeanData(Class gbeanClass) {
        this();

        GBeanInfo gbeanInfo = infoFactory.getGBeanInfo(gbeanClass);
        setGBeanInfo(gbeanInfo);
    }

    public GBeanData(GBeanInfo gbeanInfo) {
        this();
        
        setGBeanInfo(gbeanInfo);
    }
    
    public GBeanData(AbstractName abstractName, GBeanInfo gbeanInfo) {
        this();
        this.abstractName = abstractName;
        
        setGBeanInfo(gbeanInfo);
    }

    public GBeanData(AbstractName abstractName, Class gbeanClass) {
        this();
        this.abstractName = abstractName;

        GBeanInfo gbeanInfo = infoFactory.getGBeanInfo(gbeanClass);
        setGBeanInfo(gbeanInfo);
    }
    
    public GBeanData(GBeanData gbeanData) {
        setGBeanInfo(gbeanData.gbeanInfo);
        infoFactory = gbeanData.infoFactory;
        attributes = new HashMap<String, Object>(gbeanData.attributes);
        references = new HashMap<String, ReferencePatterns>(gbeanData.references);
        dependencies = new HashSet<ReferencePatterns>(gbeanData.dependencies);
        abstractName = gbeanData.abstractName;
        if (gbeanData.serviceInterfaces != null) {
            serviceInterfaces = Arrays.copyOf(gbeanData.serviceInterfaces, gbeanData.serviceInterfaces.length);
        }
        if (gbeanData.serviceProperties != null) {
            serviceProperties = new Hashtable();
            for (Enumeration e = gbeanData.serviceProperties.keys(); e.hasMoreElements();) {
                Object key = e.nextElement();
                serviceProperties.put(key, gbeanData.serviceProperties.get(key));
            }

        }
    }

    public AbstractName getAbstractName() {
        return abstractName;
    }

    public void setAbstractName(AbstractName abstractName) {
        this.abstractName = abstractName;
    }

    public GBeanInfo getGBeanInfo() {
        return gbeanInfo;
    }

    public void clearAttribute(String name) {
        attributes.remove(name);
    }

    public void clearReference(String name) {
        references.remove(name);
    }

    public void setGBeanInfo(GBeanInfo gbeanInfo) {
        this.gbeanInfo = gbeanInfo;
        if (gbeanInfo == null) {
            priority = GBeanInfo.PRIORITY_NORMAL;
        } else {
            priority = gbeanInfo.getPriority();
        }
    }

    public Map<String, Object> getAttributes() {
        return new HashMap<String, Object>(attributes);
    }

    public Set<String> getAttributeNames() {
        return new HashSet<String>(attributes.keySet());
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    private EncryptionSetting isEncrypted(String attrName) {
        if (gbeanInfo != null) {
            GAttributeInfo attr = gbeanInfo.getAttribute(attrName);
            if (attr != null) {
                return attr.getEncryptedSetting();
            }
        }
        return EncryptionSetting.PLAINTEXT;
    }

    public void setAttribute(String name, Object value) {
        value = isEncrypted(name). decrypt(value);
        attributes.put(name, value);
    }

    public Map<String, ReferencePatterns> getReferences() {
        return new HashMap<String, ReferencePatterns>(references);
    }

    public Set<String> getReferencesNames() {
        return new HashSet<String>(references.keySet());
    }

    public ReferencePatterns getReferencePatterns(String name) {
        return references.get(name);
    }

    public void setReferencePattern(String name, AbstractNameQuery pattern) {
        setReferencePatterns(name, Collections.singleton(pattern));
    }

    public void setReferencePattern(String name, AbstractName abstractName) {
        setReferencePatterns(name, new ReferencePatterns(abstractName));
    }

    public void setReferencePatterns(String name, Set patterns) {
        setReferencePatterns(name, new ReferencePatterns(patterns));
    }

    public void setReferencePatterns(String name, ReferencePatterns patterns) {
        references.put(name, patterns);
    }

    public Set<ReferencePatterns> getDependencies() {
        return new HashSet<ReferencePatterns>(dependencies);
    }

    public void setDependencies(Set<ReferencePatterns> dependencies) {
        this.dependencies.clear();
        addDependencies(dependencies);
    }

    public void addDependencies(Set<? extends Object> dependencies) {
        for (Object dependency : dependencies) {
            if (dependency instanceof AbstractName) {
                AbstractName name = (AbstractName) dependency;
                addDependency(name);
            } else if (dependency instanceof AbstractNameQuery) {
                AbstractNameQuery nameQuery = (AbstractNameQuery) dependency;
                addDependency(nameQuery);
            } else if (dependency instanceof ReferencePatterns) {
                ReferencePatterns referencePatterns = (ReferencePatterns) dependency;
                addDependency(referencePatterns);
            } else {
                throw new IllegalArgumentException("Unknown dependency type: " + dependency);
            }
        }
    }

    public void addDependency(ReferencePatterns dependency) {
        this.dependencies.add(dependency);
    }

    public void addDependency(AbstractNameQuery refInfo) {
        this.dependencies.add(new ReferencePatterns(refInfo));
    }

    public void addDependency(AbstractName dependency) {
        this.dependencies.add(new ReferencePatterns(dependency));
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String[] getServiceInterfaces() {
        return serviceInterfaces;
    }

    public void setServiceInterfaces(String[] serviceInterfaces) {
        this.serviceInterfaces = serviceInterfaces;
    }

    public Dictionary getServiceProperties() {
        if (serviceProperties == null) {
            serviceProperties = new Hashtable();
        }
        return serviceProperties;
    }

    public void setServiceProperties(Dictionary serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // write version index
        out.writeObject(backwardExternalizables.length - 1);

        // write the gbean info
        out.writeObject(gbeanInfo);

        // write the abstract name
        out.writeObject(abstractName);

        // write the priority
        out.writeInt(priority);

        // write the attributes
        out.writeInt(attributes.size());
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            value = isEncrypted(name).encrypt(value);
            try {
                out.writeObject(name);
                out.writeObject(value);
            } catch (IOException e) {
                throw (IOException) new IOException("Unable to write attribute: " + name + " in gbean: " + abstractName).initCause(e);
            } catch (NoClassDefFoundError e) {
                throw (IOException) new IOException("Unable to write attribute: " + name + " in gbean: " + abstractName).initCause(e);
            }
        }

        // write the references
        out.writeInt(references.size());
        for (Map.Entry<String, ReferencePatterns> entry : references.entrySet()) {
            String name = entry.getKey();
            ReferencePatterns value = entry.getValue();
            try {
                out.writeObject(name);
                out.writeObject(value);
            } catch (IOException e) {
                throw (IOException) new IOException("Unable to write reference pattern: " + name + " in gbean: " + abstractName).initCause(e);
            }
        }
        //write the dependencies
        out.writeInt(dependencies.size());
        for (ReferencePatterns referencePatterns : dependencies) {
            try {
                out.writeObject(referencePatterns);
            } catch (IOException e) {
                throw (IOException) new IOException("Unable to write dependency pattern in gbean: " + abstractName).initCause(e);
            }
        }
        out.writeObject(serviceInterfaces);
        out.writeObject(serviceProperties);
    }


    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Object opaque = in.readObject();
        if (opaque instanceof Integer) {
            backwardExternalizables[((Integer) opaque)].readExternal(in);
        } else {
            gbeanInfo = (GBeanInfo) opaque;
            backwardExternalizables[0].readExternal(in);
        }
    }

    /**
     * Note: this comparator
     * imposes orderings that are inconsistent with equals.
     */
    public static class PriorityComparator implements Comparator<GBeanData> {

        public int compare(GBeanData o1, GBeanData o2) {
            return o1.priority - o2.priority;
        }
    }

    protected GBeanInfoFactory newGBeanInfoFactory() {
        return new MultiGBeanInfoFactory();
    }

    private class V0Externalizable implements Externalizable {

        public void writeExternal(ObjectOutput out) throws IOException {
            throw new UnsupportedOperationException();
        }

        public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            // read the gbean info
            readGBeanInfo(in);

            // read the abstract name
            try {
                abstractName = (AbstractName) in.readObject();
            } catch (IOException e) {
                throw (IOException) new IOException("Unable to deserialize AbstractName for GBeanData of type " + gbeanInfo.getClassName()).initCause(e);
            }

            readPriority(in);

            try {
                // read the attributes
                int attributeCount = in.readInt();
                for (int i = 0; i < attributeCount; i++) {
                    String attributeName = (String) in.readObject();
                    Object attributeValue;
                    try {
                        attributeValue = in.readObject();
                    } catch (ClassNotFoundException e) {
                        throw new ClassNotFoundException("Unable to find class used in GBeanData " + abstractName + ", attribute: " + attributeName, e);
                    } catch (IOException e) {
                        throw (IOException) new IOException("Unable to deserialize GBeanData " + abstractName + ", attribute: " + attributeName).initCause(e);
                    }
                    setAttribute(attributeName, attributeValue);
                }

                // read the references
                int endpointCount = in.readInt();
                for (int i = 0; i < endpointCount; i++) {
                    String referenceName = (String) in.readObject();
                    ReferencePatterns referencePattern;
                    try {
                        referencePattern = (ReferencePatterns) in.readObject();
                    } catch (ClassNotFoundException e) {
                        throw new ClassNotFoundException("Unable to find class used in GBeanData " + abstractName + ", reference: " + referenceName, e);
                    } catch (IOException e) {
                        throw (IOException) new IOException("Unable to deserialize GBeanData " + abstractName + ", reference: " + referenceName).initCause(e);
                    }
                    setReferencePatterns(referenceName, referencePattern);
                }

                //read the dependencies
                int dependencyCount = in.readInt();
                for (int i = 0; i < dependencyCount; i++) {
                    ReferencePatterns depdendencyPattern = (ReferencePatterns) in.readObject();
                    dependencies.add(depdendencyPattern);
                }

                serviceInterfaces = (String[]) in.readObject();
                serviceProperties = (Dictionary) in.readObject();
            } catch (IOException e) {
                throw (IOException) new IOException("Unable to deserialize GBeanData " + abstractName).initCause(e);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Unable to find class used in GBeanData " + abstractName, e);
            }
        }

        protected void readGBeanInfo(ObjectInput in) throws IOException, ClassNotFoundException {
        }

        protected void readPriority(ObjectInput in) throws IOException, ClassNotFoundException {
            priority = GBeanInfo.PRIORITY_NORMAL;
        }

    }

    private class V1Externalizable extends V0Externalizable {

        public void writeExternal(ObjectOutput out) throws IOException {
            throw new UnsupportedOperationException();
        }

        protected void readGBeanInfo(ObjectInput in) throws IOException, ClassNotFoundException {
            gbeanInfo = (GBeanInfo) in.readObject();
        }

        protected void readPriority(ObjectInput in) throws IOException, ClassNotFoundException {
            priority = in.readInt();
        }

    }

}


