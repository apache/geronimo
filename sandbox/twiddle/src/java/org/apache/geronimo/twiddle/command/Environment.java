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

package org.apache.geronimo.twiddle.command;

/**
 * Abstraction of a map-like environment.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:17 $
 */
public interface Environment
{
    /**
     * Get an enviornment attribute.
     *
     * @param name  The name of the attribute.
     * @return      The attribute value, or null if the attribute was not set.
     */
    Object get(String name);
    
    /**
     * Set an environment attribute.
     *
     * @param name      The name of the attribute.
     * @param value     The value of the attribute.
     */
    Object set(String name, Object value);
    
    /**
     * Unset an enviornment attribute.
     *
     * @param name  The name of the attribute.
     * @return      The previous value of the attribute, or null if there was none.
     */
    Object unset(String name);
    
    /**
     * Check if an attribute is set.
     *
     * @param name  The name of the attribute.
     * @return      True if the attribute is set, else false.
     */
    boolean isSet(String name);
}
