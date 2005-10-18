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

import javax.management.ObjectName;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

/**
 * Able to retrieve the values of certain "manageable" attributes from a
 * repository that is more accessible to an end user (compared to the
 * serialized data in the ConfigStore).
 *
 * @version $Rev: 169154 $ $Date: 2005-05-08 15:35:23 -0400 (Sun, 08 May 2005) $
 */
public interface ManageableAttributeStore {

    public static final String ATTRIBUTE_STORE = "AttributeStore";

    public Collection setAttributes(URI configurationName, Collection datas) throws InvalidConfigException;
    /**
     * Return the object name of this store
     * @return the object name of this store
     */
//    public String getObjectName();

    /**
     * Gets a stored value (if any) for a particular attribute.  The attribute
     * is identified by the configuration name, GBean ObjectName, and attribute
     * information.  Note: it is not possible to store a meaningful value of
     * "null"; that would be treated the same as if no value was stored.
     *
     * @param configurationName The name of the configuration holding the GBean
     *                          in question
     * @param gbean The ObjectName of the GBean in question
     * @param attribute The attribute in question
     *
     * @return An object of the correct type to populate into the attribute in
     *         question, or null if this store has no value saved for the
     *         specified attribute.
     */
//    public Object getValue(String configurationName, ObjectName gbean, GAttributeInfo attribute);

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
     * Saves the current values to persistent storage.  This should be called
     * when the server is shut down or more often, to make sure that any
     * changes will be reflected the next time the server starts and the
     * store is consulted.
     */
    public void save() throws IOException;
}
