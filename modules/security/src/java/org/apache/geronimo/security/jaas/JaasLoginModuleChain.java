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
package org.apache.geronimo.security.jaas;

/**
 * @version $Rev$ $Date$
 */
public interface JaasLoginModuleChain {
    /**
     * Gets the ObjectName of the login module that this node in the
     * chain corresponds to (a LoginModuleGBean).
     *
     * @return The ObjectName of the login module GBean, in String form.
     */
    String getLoginModuleName();

    /**
     * Gets the ObjectName of the next node in the chain after this one
     * (another JaasLoginModuleChain).
     *
     * @return The ObjectName of the next node, in String form, or null
     *         if this is the last.
     */
    public String getNextName();

    /**
     * The String form of the control flag for the login module at this
     * position in the chain.
     */
    public String getControlFlag();

    /**
     * The String form of the control flag for the login module at this
     * position in the chain.
     */
    public void setControlFlag(String controlFlag);

}
