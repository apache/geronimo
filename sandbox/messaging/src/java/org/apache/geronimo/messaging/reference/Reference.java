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

package org.apache.geronimo.messaging.reference;

/**
 * (Remote) Reference to a Referenceable.
 * <BR> 
 * This interface is automatically implemented by Referenceable proxies.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:42 $
 */
public interface Reference
{
    
    /**
     * Gets the meta-data of the (remote) Referenceable.
     * 
     * @return Referenceable meta-data.
     */
    public ReferenceableInfo getReferenceableInfo();

    /**
     * Equals if obj is a Reference to the same Referenceable.
     * 
     * @param obj Object with which to be compared.
     * @return true if obj is a Reference to the same Referenceable.
     */
    public boolean equals(Object obj);
    
}
