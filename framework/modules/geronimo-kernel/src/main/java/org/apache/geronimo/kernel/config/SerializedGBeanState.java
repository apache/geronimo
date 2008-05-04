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
package org.apache.geronimo.kernel.config;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.ObjectInputStreamExt;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * @version $Rev$ $Date$
 */
public class SerializedGBeanState implements GBeanState, Serializable {
    private static final long serialVersionUID = 6015138334529564307L;

    /**
     * GBeans contained in this configuration.
     */
    private final List gbeans = new ArrayList();

    /**
     * The serialized form of the gbeans.  Once this is set on more gbeans can be added.
     */
    private byte[] gbeanState;

    public SerializedGBeanState(Collection gbeans) {
        if (gbeans != null){
            this.gbeans.addAll(gbeans);
        }
    }

    public List getGBeans(ClassLoader classLoader) throws InvalidConfigException {
        if (gbeanState == null) {
            return Collections.unmodifiableList(gbeans);
        }
        gbeans.addAll(loadGBeans(gbeanState, classLoader));
        return Collections.unmodifiableList(gbeans);
    }

    public void addGBean(GBeanData gbeanData) {
        if (gbeanState != null) {
            throw new IllegalStateException("GBeans have been serialized, so no more GBeans can be added");
        }

        gbeans.add(gbeanData);
    }

    public GBeanData addGBean(String name, GBeanInfo gbeanInfo, Naming naming, Environment environment) {
        if (gbeanState != null) {
            throw new IllegalStateException("GBeans have been serialized, so no more GBeans can be added");
        }

        String j2eeType = gbeanInfo.getJ2eeType();
        if (j2eeType == null) j2eeType = "GBean";
        AbstractName abstractName = naming.createRootName(environment.getConfigId(), name, j2eeType);
        GBeanData gBeanData = new GBeanData(abstractName, gbeanInfo);
        addGBean(gBeanData);
        return gBeanData;
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        if (gbeanState == null) {
            gbeanState = storeGBeans(gbeans);
            gbeans.clear();
        }

        stream.defaultWriteObject();
    }

    private static List loadGBeans(byte[] gbeanState, ClassLoader classLoader) throws InvalidConfigException {
        List gbeans = new ArrayList();
        if (gbeanState != null && gbeanState.length > 0) {
            // Set the thread context classloader so deserializing classes can grab the cl from the thread
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(classLoader);

                ObjectInputStream ois = new ObjectInputStreamExt(new ByteArrayInputStream(gbeanState), classLoader);
                try {
                    while (true) {
                        GBeanData gbeanData = new GBeanData();
                        gbeanData.readExternal(ois);
                        gbeans.add(gbeanData);
                    }
                } catch (EOFException e) {
                    // ok
                } finally {
                    ois.close();
                }
            } catch (ClassNotFoundException e) {
                throw new InvalidConfigException("Class not loadable in classloader: " + classLoader, e);
            } catch (NoClassDefFoundError e) {
                throw new InvalidConfigException("Class not loadable in classloader: " + classLoader, e);
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to deserialize GBeanState in classloader: " + classLoader, e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
        return gbeans;
    }

    private static byte[] storeGBeans(List gbeans) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
        } catch (IOException e) {
            throw (AssertionError) new AssertionError("Unable to initialize ObjectOutputStream").initCause(e);
        }
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();
            try {
                gbeanData.writeExternal(oos);
            } catch (Exception e) {
                //
                // HACK:
                //
                System.err.println("FAILED TO SERIALIZE: " + gbeanData.getGBeanInfo());
                
                throw (IOException) new IOException("Unable to serialize GBeanData for " + gbeanData.getAbstractName()).initCause(e);
            }
        }
        try {
            oos.flush();
        } catch (IOException e) {
            throw (AssertionError) new AssertionError("Unable to flush ObjectOutputStream").initCause(e);
        }
        return baos.toByteArray();
    }
}
