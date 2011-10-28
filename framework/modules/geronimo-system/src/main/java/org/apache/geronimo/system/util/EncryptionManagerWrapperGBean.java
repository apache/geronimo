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
 
package org.apache.geronimo.system.util;

import java.io.Serializable;

import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;


public class EncryptionManagerWrapperGBean {

    public String encrypt(Serializable source) {
        return EncryptionManager.encrypt(source);
    }

    public Serializable decrypt(String source) {
        return EncryptionManager.decrypt(source);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EncryptionManagerWrapperGBean.class, "GBean");
        infoBuilder.addOperation("encrypt", new Class[] {Serializable.class}, "java.io.Serializable");
        infoBuilder.addOperation("decrypt", new Class[] {String.class}, "java.lang.String");
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
