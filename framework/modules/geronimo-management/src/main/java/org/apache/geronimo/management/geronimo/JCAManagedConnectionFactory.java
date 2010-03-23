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
package org.apache.geronimo.management.geronimo;

import java.util.Map;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public interface JCAManagedConnectionFactory extends org.apache.geronimo.management.JCAManagedConnectionFactory {
    public String getManagedConnectionFactoryClass();

    public String getConnectionFactoryInterface() ;

    public String[] getImplementedInterfaces();

    public String getConnectionFactoryImplClass();

    public String getConnectionInterface();

    public String getConnectionImplClass();

    /**
     * Gets the config properties in the form of a map where the key is the
     * property name and the value is property type (as a Class).
     *
     * @return map of config property name to config property type name
     */
    public Map<String, Class> getConfigProperties();

    public void setConfigProperty(String property, Object value) throws Exception;

    public Object getConfigProperty(String property) throws Exception;

}
