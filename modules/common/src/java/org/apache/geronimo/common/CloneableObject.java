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

package org.apache.geronimo.common;

/**
 * A simple base-class for classes which need to be cloneable.
 *
 * @version $Revision: 1.7 $ $Date: 2004/03/10 09:58:25 $
 */
public class CloneableObject
   implements java.lang.Cloneable
{
    /**
     * Clone the object via {@link Object#clone}.  This will return
     * and object of the correct type, with all fields shallowly
     * cloned.
     */
    public Object clone()
    {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    
    /**
     * An interface which exposes a <em>public</em> clone method, 
     * unlike {@link Object#clone} which is protected and throws
     * exceptions... how useless is that?
     */
    public static interface Cloneable
        extends java.lang.Cloneable
    {
        Object clone();
    }
}
