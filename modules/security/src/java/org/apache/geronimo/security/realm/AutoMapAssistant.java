/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.security.realm;

import org.apache.geronimo.security.deploy.MapOfSets;
import org.apache.geronimo.security.deploy.PrincipalInfo;


/**
 * Provides a way for security realms to provide reasonable defaults for
 * principal to role mapping.
 * <p/>
 * This interface is used by the deployment code to automatically map
 * principals to roles.
 *
 * @version $Rev$ $Date$
 */
public interface AutoMapAssistant {

    /**
     * Provides the realm name of the auto map assistant.
     * @return the realm name of the auto map assistant
     */
    public String getRealmName();

    /**
     * Provides the default principal to be used when an unauthenticated
     * subject uses a container.
     *
     * @return the default principal
     */
    public PrincipalInfo getDefaultPrincipal();

    /**
     * Provides a set of principal class names to be used when automatically
     * mapping principals to roles.
     *
     * @return a map of logindomain name to set of principal class names
     */
    public MapOfSets getAutoMapPrincipalClasses();
}
