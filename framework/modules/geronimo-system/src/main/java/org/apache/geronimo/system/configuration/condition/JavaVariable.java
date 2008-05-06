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
package org.apache.geronimo.system.configuration.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to Java version details for use in condition expressions.
 *
 * @version $Rev$ $Date$
 */
public class JavaVariable
{
    private static final Logger log = LoggerFactory.getLogger(JavaVariable.class);
    
    public String getVendor() {
        return SystemUtils.JAVA_VENDOR;
    }

    public String getVersion() {
        return SystemUtils.JAVA_VERSION;
    }

    public String getVmVendor() {
        return SystemUtils.JAVA_VM_VENDOR;
    }
    
    public String getVmVersion() {
        return SystemUtils.JAVA_VM_VERSION;
    }

    public boolean getIs1_1() {
        return SystemUtils.IS_JAVA_1_1;
    }

    public boolean getIs1_2() {
        return SystemUtils.IS_JAVA_1_2;
    }

    public boolean getIs1_3() {
        return SystemUtils.IS_JAVA_1_3;
    }

    public boolean getIs1_4() {
        return SystemUtils.IS_JAVA_1_4;
    }

    public boolean getIs1_5() {
        return SystemUtils.IS_JAVA_1_5;
    }

    public boolean getIs1_6() {
        return SystemUtils.IS_JAVA_1_6;
    }

    public boolean getIsVersionAtLeast(final float requiredVersion) {
        return SystemUtils.isJavaVersionAtLeast(requiredVersion);
    }

    public boolean getIsVersionAtLeast(final int requiredVersion) {
        return SystemUtils.isJavaVersionAtLeast(requiredVersion);
    }
    
    public boolean getVersionMatches(String version) {
        version = version.trim();
        
        boolean result = false;
        
        if (version.endsWith("*")) {
            version = version.substring(0, version.length() - 1).trim();
            
            log.debug("Checking Java version is in the same group as: {}", version);
            
            String tmp = SystemUtils.JAVA_VERSION_TRIMMED;
            
            log.debug("Requested version: {}", tmp);
            log.debug("JVM version: {}", SystemUtils.JAVA_VERSION_FLOAT);
            
            result = tmp.startsWith(version);
        }
        else if (version.endsWith("+")) {
            version = version.substring(0, version.length() - 1).trim();
            
            log.debug("Checking Java version is greater than: {}", version);
            
            float tmp = Float.parseFloat(version);
            
            log.debug("Requested version: {}", tmp);
            log.debug("JVM version: {}", SystemUtils.JAVA_VERSION_FLOAT);
            
            result = tmp <= SystemUtils.JAVA_VERSION_FLOAT;
        }
        else {
            log.debug("Checking Java version is equal to: {}", version);
            
            float tmp = Float.parseFloat(version);
            
            log.debug("Requested version: {}", tmp);
            log.debug("JVM version: {}", SystemUtils.JAVA_VERSION_FLOAT);
            
            result = tmp == SystemUtils.JAVA_VERSION_FLOAT;
        }
        
        return result;
    }
}
