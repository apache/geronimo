/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.properties;

public class PropertyLog {
    public static PropertyLog getInstance(String instanceName) {
        PropertyLog log = new PropertyLog();
        log.init(instanceName);
        return log;
    }

    private String instanceName;

    public String getInstanceName() {
        return instanceName;
    }

    protected void init(String instanceName) {
        this.instanceName = instanceName;
    }

    public void debugUsingValue(String value) {
        System.out.println("PropertyLog.debugUsingValue(): NEEDS IMPLEMENTATION??");
    }

    public void debugUsingDefaultValue(String defaultValue) {
        System.out.println("PropertyLog.debugUsingValue(): NEEDS IMPLEMENTATION??");
    }

    public String errorMissingValueForRequiredProperty(String property, String context) {
        String msg = "PropertyLog.errorMissingValueForRequiredProperty(): property: " + property + ", context: " + context;
        System.out.println(msg);
        return msg;
    }

    public String errorMissingValueForRequiredSystemProperty(String property, String refProperty, String context) {
        String msg = "PropertyLog.errorMissingValueForRequiredSystemProperty(): property: " + property + ", refProperty: " + refProperty + ", context: " + context;
        System.out.println(msg);
        return msg;
    }
}
