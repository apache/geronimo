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
package org.apache.geronimo.security.realm;

/**
 * A helper class that lists principals available in a security realm in order
 * to help populate deployment descriptors.  This may or may not be provided
 * for a specific security realm.  A LoginModule may implement this interface,
 * in which case the GenericSecurityRealm can take advantage of that [and the
 * LoginModule should accept an initialize(null, null, null, options) call].
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public interface DeploymentSupport {
    /**
     * Gets the names of all principal classes that may be populated into
     * a Subject.
     */
    String[] getPrincipalClassNames();

    /**
     * Gets the names of all principal classes that should correspond to
     * roles when automapping.  This is a default, and may be overridden
     * by specific values configured for the realm.
     */
    String[] getAutoMapPrincipalClassNames();

    /**
     * Gets a list of all the principals of a particular type (identified by
     * the principal class).  These are available for manual role mapping.
     */
    String[] getPrincipalsOfClass(String className);
}
