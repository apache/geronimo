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

package org.apache.geronimo.common.mutable;

/**
 * Mutable object interface.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:26 $
 */
public interface Mutable
{
    /**
     * Set the value of a mutable object.
     *
     * @param value   Target value for object.
     */
    void setValue(Object value);
    
    /**
     * Get the value of a mutable object.
     *
     * @return Object value.
     */
    Object getValue();
}
