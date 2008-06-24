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
package org.apache.geronimo.concurrent.impl;

import java.util.Hashtable;

import javax.management.ObjectName;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelException;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.kernel.management.State;

public class GBeanBuilder {
    
    protected Kernel kernel;
    protected ClassLoader classLoader;

    public GBeanBuilder(Kernel kernel, ClassLoader classLoader) {
        this.kernel = kernel;
        this.classLoader = classLoader;        
    }
    
    /* in memory */
    protected void addGBeanKernel(AbstractName gbeanName, GBeanData threadData) throws KernelException {
        kernel.loadGBean(threadData, this.classLoader);           
        kernel.startRecursiveGBean(gbeanName);
    }
    
    /* in memory */
    protected void removeGBeanKernel(AbstractName gbeanName) {
        try {
            if (kernel.getGBeanState(gbeanName) == State.RUNNING_INDEX) {
                kernel.stopGBean(gbeanName);
            }
            kernel.unloadGBean(gbeanName);
        } catch (GBeanNotFoundException e) {
            // Bean is no longer loaded
        }
    }
    
    /**
     * ObjectName must match this pattern: 
     * domain:j2eeType=&lt;j2eeType&gt;,name=MyName,J2EEServer=MyServer
     */
    public static void verifyObjectName(String objectNameStr, String j2eeType, String name) {
        ObjectName objectName = ObjectNameUtil.getObjectName(objectNameStr);
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException(
                    "ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!j2eeType.equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException(
                    name + " object name j2eeType property must be '" + j2eeType + "'",
                    objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_NAME)) {
            throw new InvalidObjectNameException(
                    name + " object must contain a name property", 
                    objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_SERVER)) {
            throw new InvalidObjectNameException(
                    name + " object name must contain a J2EEServer property",
                    objectName);
        }
        if (keyPropertyList.size() != 3) {
            throw new InvalidObjectNameException(
                    name + " object name can only have j2eeType, name, and J2EEServer properties",
                    objectName);
        }
    }
}
