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
 * Thrown to indicate that a method argument was <code>null</code> and 
 * should <b>not</b> have been.
 *
 * @version $Revision: 1.11 $ $Date: 2004/03/10 09:58:25 $
 */
public class NullArgumentException
    extends InvalidArgumentException
{
    /**
     * A simple helper method to check that the given argument value
     * is not null. If it is <code>null</code> then a 
     * <code>NullArgumentException</code> is thrown.
     * 
     * @param name name of the argument
     * @param value the value of the argument
     *
     * @throws NullArgumentException if the argument is <code>null</code>
     */
    public static void checkForNull(String name, Object value)
        throws NullArgumentException
    {
        if (value == null) {
            throw new NullArgumentException(name);
        }
    }
    
    /**
     * Construct a <code>NullArgumentException</code>.
     *
     * @param name    Argument name.
     */
    public NullArgumentException(final String name) {
        this(name,null);
    }
    
    /**
     * Construct a <code>NullArgumentException</code>.
     *
     * @param name    Argument name.
     * @param index   Argument index.
     */
    public NullArgumentException(final String name, final long index) {
        this(name, new Long(index));
    }
    
    /**
     * Construct a <code>NullArgumentException</code>.
     *
     * @param name    Argument name.
     * @param index   Argument index.
     */
    public NullArgumentException(final String name, final Object index) {
        super(name, null, index, "cannot be null");
    }

}
