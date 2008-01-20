/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.geronimo.server;

import java.util.Properties;
import java.util.Arrays;

/**
 * Container for a set of options to be passed to a JVM.
 *
 * @version $Rev$ $Date$
 */
public class OptionSet
{
    /**
     * @parameter
     */
    private String id = null;

    /**
     * @parameter
     */
    private String[] options = null;

    /**
     * @parameter
     */
    private Properties properties = null;

    public String toString() {
        return "{ id=" + id +
               ", options=" + (options != null ? Arrays.asList(options) : null) +
               ", properties=" + properties +
               " }";
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setOptions(final String[] options) {
        this.options = options;
    }

    public String[] getOptions() {
        return options;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }
}
