/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.gbean;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.ObjectName;

/**
 * @version $Rev$ $Date$
 */
public class GBeanData implements Externalizable {
    private GBeanInfo gbeanInfo;
    private final Map attributes;
    private final Map references;

    public GBeanData() {
        attributes = new HashMap();
        references = new HashMap();
    }

    public GBeanData(GBeanInfo gbeanInfo) {
        this.gbeanInfo = gbeanInfo;
        attributes = new HashMap();
        references = new HashMap();
    }

    public GBeanData(GBeanData gbeanData) {
        gbeanInfo = gbeanData.gbeanInfo;
        attributes = new HashMap(gbeanData.attributes);
        references = new HashMap(gbeanData.references);
    }

    public GBeanInfo getGBeanInfo() {
        return gbeanInfo;
    }

    public void setGBeanInfo(GBeanInfo gbeanInfo) {
        this.gbeanInfo = gbeanInfo;
    }

    public Map getAttributes() {
        return new HashMap(attributes);
    }

    public Set getAttributeNames() {
        return new HashSet(attributes.keySet());
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Map getReferences() {
        return new HashMap(references);
    }

    public Set getReferencesNames() {
        return new HashSet(references.keySet());
    }

    public Set getReferencePatterns(String name) {
        return (Set) references.get(name);
    }

    public void setReferencePattern(String name, ObjectName pattern) {
        setReferencePatterns(name, Collections.singleton(pattern));
    }

    public void setReferencePatterns(String name, Set patterns) {
        references.put(name, patterns);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // write the gbean info
        out.writeObject(gbeanInfo);

        // write the attributes
        out.writeInt(attributes.size());
        for (Iterator iterator = attributes.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            try {
                out.writeObject(name);
                out.writeObject(value);
            } catch (IOException e) {
                throw (IOException) new IOException("Unable to write attribute: " + name).initCause(e);
            }
        }

        // write the references
        out.writeInt(references.size());
        for (Iterator iterator = references.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Set value = (Set) entry.getValue();
            try {
                out.writeObject(name);
                out.writeObject(value);
            } catch (IOException e) {
                throw (IOException) new IOException("Unable to write reference pattern: " + name).initCause(e);
            }
        }
    }


    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // read the gbean info
        gbeanInfo = (GBeanInfo) in.readObject();

        // read the attributes
        int attributeCount = in.readInt();
        for (int i = 0; i < attributeCount; i++) {
            setAttribute((String) in.readObject(), in.readObject());
        }

        // read the references
        int endpointCount = in.readInt();
        for (int i = 0; i < endpointCount; i++) {
            setReferencePatterns((String) in.readObject(), (Set) in.readObject());
        }
    }
}

