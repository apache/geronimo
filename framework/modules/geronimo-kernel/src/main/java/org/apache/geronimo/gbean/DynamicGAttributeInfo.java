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

package org.apache.geronimo.gbean;

import org.apache.geronimo.gbean.annotation.EncryptionSetting;

/**
 * Describes an attibute of a GBean.
 * 
 * @version $Rev$ $Date$
 */
public class DynamicGAttributeInfo extends GAttributeInfo {
    public DynamicGAttributeInfo(String name, String type, boolean persistent, boolean manageable, EncryptionSetting encrypted, boolean readable, boolean writable) {
        super(name, type, persistent, manageable, encrypted, readable, writable, null, null);
    }
    
    public DynamicGAttributeInfo(String name, String type, boolean persistent, boolean manageable, boolean readable, boolean writable) {
        super(name, type, persistent, manageable, readable, writable, null, null);
    }
    
}
