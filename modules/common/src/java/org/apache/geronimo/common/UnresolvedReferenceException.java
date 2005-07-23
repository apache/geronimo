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
package org.apache.geronimo.common;

/**
 * A problem with a reference of some kind (most often a resource reference).
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class UnresolvedReferenceException extends DeploymentException {
    private String resourceType;
    private boolean multiple;
    private String nameQuery;

    public UnresolvedReferenceException(String resourceType, boolean multiple, String nameQuery) {
        this.resourceType = resourceType;
        this.multiple = multiple;
        this.nameQuery = nameQuery;
    }

    public String getResourceType() {
        return resourceType;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public String getNameQuery() {
        return nameQuery;
    }

    public String getMessage() {
        return (multiple ? "Ambiguous " : "Unknown ") + resourceType + " reference (query=" + nameQuery + ")";
    }
}
