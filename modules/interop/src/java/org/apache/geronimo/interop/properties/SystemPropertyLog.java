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

public class SystemPropertyLog {
    public static SystemPropertyLog getInstance(String instanceName) {
        SystemPropertyLog log = new SystemPropertyLog();
        log.init(instanceName);
        return log;
    }

    private String  instanceName;

    public String getInstanceName() {
        return instanceName;
    }

    protected void init(String instanceName) {
        instanceName = instanceName;
    }

    public void debugUsingValue(String value) {
        System.out.println("SystemPropertyLog.debugUsingValue(): value: " + value);
    }

    public void debugUsingDefaultValue(String defaultValue) {
        System.out.println("SystemPropertyLog.debugUsingValue(): defaultValue: " + defaultValue);
    }
}
