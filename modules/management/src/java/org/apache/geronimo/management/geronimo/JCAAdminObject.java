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
package org.apache.geronimo.management.geronimo;

import java.util.Map;

/**
 * Management interface for admin objects
 * todo: make it implement J2EEManagedObject
 *
 * @version $Rev: 368994 $ $Date: 2006-01-14 02:07:18 -0500 (Sat, 14 Jan 2006) $
 */
public interface JCAAdminObject {
    public String getAdminObjectClass();
    public String getAdminObjectInterface();

    /**
     * Gets the config properties in the form of a map where the key is the
     * property name and the value is property type (as a String not a Class).
     */
    public Map getConfigProperties();

    public void setConfigProperty(String property, Object value) throws Exception;

    public Object getConfigProperty(String property) throws Exception;
}
