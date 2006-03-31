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

package org.apache.geronimo.kernel.config;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Iterator;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.ObjectInputStreamExt;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * @version $Rev: 382645 $ $Date$
 */
public class ConfigurationData implements Serializable {
    private static final long serialVersionUID = 4324193220056650732L;

    /**
     * Identifies the type of configuration (WAR, RAR et cetera)
     */
    private final ConfigurationModuleType moduleType;

    /**
     * Defines the configuration id, parent configurations, and classpath
     */
    private final Environment environment;

    /**
     * List of URIs in this configuration's classpath.  These are for the classes directly included in the configuration
     */
    private final LinkedHashSet classPath = new LinkedHashSet();

    /**
     * GBeans contained in this configuration.
     */
    private final List gbeans = new ArrayList();

    /**
     * The serialized form of the gbeans.  Once this is set on more gbeans can be added.
     */
    private byte[] gbeanState;

    /**
     * Child configurations of this configuration
     */
    private final List childConfigurations = new ArrayList();

    /**
     * The base file of the configuation
     */
    private transient File configurationDir;

    /**
     * The naming system
     */
    private transient Naming naming;

    /**
     * The configuration store from which this configuration was loaded, or null if it was not loaded from a configuration store.
     */
    private transient ConfigurationStore configurationStore;

    public ConfigurationData(Artifact configId, Naming naming) {
        this(new Environment(configId), naming);
    }

    public ConfigurationData(Environment environment, Naming naming) {
        this(null, null, null, null, environment, null, naming);
    }

    public ConfigurationData(ConfigurationModuleType moduleType, LinkedHashSet classPath, List gbeans, List childConfigurations, Environment environment, File configurationDir, Naming naming) {
        if (naming == null) throw new NullPointerException("naming is null");
        this.naming = naming;
        if (moduleType != null) {
            this.moduleType = moduleType;
        } else {
            this.moduleType = ConfigurationModuleType.CAR;
        }
        if (classPath != null) {
            this.classPath.addAll(classPath);
        }
        if (gbeans != null){
            this.gbeans.addAll(gbeans);
        }
        if (childConfigurations != null) {
            this.childConfigurations.addAll(childConfigurations);
        }

        if (environment == null) throw new NullPointerException("environment is null");
        if (environment.getConfigId() == null) throw new NullPointerException("environment.configId is null");
        this.environment = environment;
        this.configurationDir = configurationDir;
    }

    public Artifact getId() {
        return environment.getConfigId();
    }

    public ConfigurationModuleType getModuleType() {
        return moduleType;
    }

    public List getClassPath() {
        return Collections.unmodifiableList(new ArrayList(classPath));
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

    public GBeanData addGBean(String name, GBeanInfo gbeanInfo) {
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

    public List getChildConfigurations() {
        return Collections.unmodifiableList(childConfigurations);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public File getConfigurationDir() {
        return configurationDir;
    }

    public void setConfigurationDir(File configurationDir) {
        this.configurationDir = configurationDir;
    }

    public Naming getNaming() {
        return naming;
    }

    public void setNaming(Naming naming) {
        this.naming = naming;
    }

    public ConfigurationStore getConfigurationStore() {
        return configurationStore;
    }

    public void setConfigurationStore(ConfigurationStore configurationStore) {
        this.configurationStore = configurationStore;
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
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to deserialize GBeanState", e);
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
