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
package org.apache.geronimo.kernel.config;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GReferenceInfo;

import javax.management.ObjectName;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

/**
 * Able to retrieve the values of certain "manageable" attributes from a
 * repository that is more accessible to an end user (compared to the
 * serialized data in the ConfigStore).
 *
 * @version $Rev: 169154 $ $Date: 2005-05-08 15:35:23 -0400 (Sun, 08 May 2005) $
 */
public interface ManageableAttributeStore {

    public static final String ATTRIBUTE_STORE = "AttributeStore";

    /**
     * Given a conifguration name and a set of GBeanDatas, apply all the saved
     * attribute values to those GBeans before the caller attempts to load
     * them.
     *
     * @param configurationName The configuration in question
     * @param datas             The initial GBeanData's for all the GBeans in
     *                          the configuration
     * @param classLoader
     * @return                  The modified GBeanData's
     * @throws InvalidConfigException If something bad happens
     */
    public Collection setAttributes(URI configurationName, Collection datas, ClassLoader classLoader) throws InvalidConfigException;

    /**
     * Sets the stored value for a particular attribute.  The attribute is
     * identified by the configuration name, GBean ObjectName, and attribute
     * information.  Note: it is not possible to store a meaningful value of
     * "null"; that would be treated the same as if no value was stored.
     *
     * Generally, whenever the value for a manageable attribute is changed,
     * this method should be called so that value isn't reversed the next time
     * the GBean is started.
     *
     * @param configurationName The name of the configuration holding the GBean
     *                          in question
     * @param gbean The ObjectName of the GBean in question
     * @param attribute The attribute in question
     * @param value The value to save, or null if no value should be saved
     */
    public void setValue(String configurationName, ObjectName gbean, GAttributeInfo attribute, Object value);

    /**
     * Sets the pattern for a GBean reference. The reference is
     * identified by the configuration name, GBean ObjectName, and reference
     * information.
     *
     * To "null-out" the reference use setReferencePatterns(configurationName, gbean, reference, Collections.EMPTY_SET).
     *
     * @param configurationName the name of the configuration holding the GBean in question
     * @param gbean the ObjectName of the GBean
     * @param reference the attribute information
     * @param pattern new object name pattern for this reference
     */
    public void setReferencePattern(String configurationName, ObjectName gbean, GReferenceInfo reference, ObjectName pattern);

    /**
     * Sets the patterns for a GBean reference. The reference is
     * identified by the configuration name, GBean ObjectName, and reference
     * information.
     *
     * @param configurationName the name of the configuration holding the GBean in question
     * @param gbean the ObjectName of the GBean
     * @param reference the attribute information
     * @param patterns new object name patterns for this reference; must not be null
     */
    public void setReferencePatterns(String configurationName, ObjectName gbean, GReferenceInfo reference, Set patterns);

    /**
     * Sets whether a particular GBean should be loaded for this configuration.
     * The GBean must already exist in the configuration, this just toggles the
     * flag for whether to stop it from loading when the configuration is
     * loaded.
     *
     * @param configurationName The configuration that the GBean belongs to
     * @param gbean             The GBean in question
     * @param load              True if the GBean should load with the configuration
     */
    public void setShouldLoad(String configurationName, ObjectName gbean, boolean load);


    /**
     * Adds a GBean to the configuration.
     * @param configurationName the configuration that the GBean belongs to
     * @param gbeanData the GBean to add
     */
    public void addGBean(String configurationName, GBeanData gbeanData);

    /**
     * Saves the current values to persistent storage.  This should be called
     * when the server is shut down or more often, to make sure that any
     * changes will be reflected the next time the server starts and the
     * store is consulted.
     */
    public void save() throws IOException;
}
