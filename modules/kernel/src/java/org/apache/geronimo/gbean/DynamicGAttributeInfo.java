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
 * @version $Revision: 1.7 $ $Date: 2004/06/02 05:33:03 $
 */
public class DynamicGAttributeInfo extends GAttributeInfo {
    public DynamicGAttributeInfo(String name) {
        this(name, false, true, true);
    }

    public DynamicGAttributeInfo(String name, boolean persistent) {
        this(name, persistent, true, true);
    }

    public DynamicGAttributeInfo(String name, boolean persistent, boolean readable, boolean writable) {
        super(name, Object.class.getName(), persistent, Boolean.valueOf(readable), Boolean.valueOf(writable), null, null);
    }
}
