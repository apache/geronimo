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

package org.apache.geronimo.gbean;

/**
 * Describes an attibute of a GBean.
 * 
 * @version $Revision: 1.5 $ $Date: 2004/03/18 10:04:50 $
 */
public class DynamicGAttributeInfo extends GAttributeInfo {
    
    /**
     * Creates a non-persistent, reabable and writable GAttributeInfo
     * 
     * @param name
     */
    public DynamicGAttributeInfo(String name) {
        this(name, false, true, true);
    }

    /**
     * @param name
     * @param persistent
     */
    public DynamicGAttributeInfo(String name, boolean persistent) {
        this(name, persistent, true, true);
    }

    /**
     * @param name
     * @param persistent
     * @param readable
     * @param writable
     */
    public DynamicGAttributeInfo(String name, boolean persistent, boolean readable, boolean writable) {
        super(name, persistent, Boolean.valueOf(readable), Boolean.valueOf(writable), null, null);
    }
}
