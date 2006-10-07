/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.system.configuration.condition;

/**
 * Provides access to Java version details for use in condition expressions.
 *
 * @version $Rev$ $Date$
 */
public class JavaVariable
{
    public String getVendor() {
        return SystemUtils.JAVA_VENDOR;
    }

    public String getVresion() {
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

    public boolean isVersionAtLeast(final float requiredVersion) {
        return SystemUtils.isJavaVersionAtLeast(requiredVersion);
    }

    public boolean isVersionAtLeast(final int requiredVersion) {
        return SystemUtils.isJavaVersionAtLeast(requiredVersion);
    }
}
