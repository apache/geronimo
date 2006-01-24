/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.management.geronimo;

/**
 * Geronimo override of the JCAResource management interface.
 *
 * @version $Rev: 368994 $ $Date: 2006-01-14 02:07:18 -0500 (Sat, 14 Jan 2006) $
 */
public interface JCAResource extends org.apache.geronimo.management.JCAResource {
    /**
     * A list of instances of this resource adapter (e.g. the RA may have
     * several instances configured each with different config settings).
     *
     * @return the ObjectNames of the resource adapter instances provided by this
     *         resource
     */
    public String[] getResourceAdapterInstances();
}
