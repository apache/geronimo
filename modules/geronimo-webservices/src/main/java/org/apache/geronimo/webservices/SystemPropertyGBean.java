/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.webservices;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

public class SystemPropertyGBean implements GBeanLifecycle {

    private String propertyName;
    private String propertyValue;
    
    public SystemPropertyGBean(String propertyName, String propertyValue) {
        if (propertyName == null || propertyValue == null) {
            throw new IllegalArgumentException("Property name or value is null");
        }
        
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public void doStart() throws Exception {
        setProperty(this.propertyName, this.propertyValue);          
    }

    private void setProperty(String propertyName, String value) {
        String propValue = System.getProperty(propertyName);
        // set only if the property is not set
        if (propValue == null) {
            System.setProperty(propertyName, value);
        }        
    }
    
    public void doStop() throws Exception {             
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(SystemPropertyGBean.class, SystemPropertyGBean.class, NameFactory.GERONIMO_SERVICE);
                
        infoFactory.addAttribute("propertyName", String.class, true, true);
        infoFactory.addAttribute("propertyValue", String.class, true, true);
        
        infoFactory.setConstructor(new String[] {"propertyName", "propertyValue"});
        
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
