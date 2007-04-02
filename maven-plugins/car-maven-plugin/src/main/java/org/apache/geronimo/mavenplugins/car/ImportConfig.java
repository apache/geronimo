/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.car;

import org.codehaus.mojo.pluginsupport.util.ArtifactItem;

/**
 * ???
 *
 * @version $Rev:385659 $ $Date$
 */
public class ImportConfig
    extends ArtifactItem
{
    /**
     * The type of import this artifact is.  One of CLASSES, ALL or SERVICES.
     *
     * @parameter
     */
    private String importType;

    /**
     * Get the import type.
     *
     * @return  The import type; or null if not set.
     */
    public String getImportType() {
        return importType;
    }

    /**
     * Set the import type.
     *
     * @param importType    The import type; or null to unset.
     */
    public void setImportType(final String importType) {
        this.importType = importType;
    }
}
