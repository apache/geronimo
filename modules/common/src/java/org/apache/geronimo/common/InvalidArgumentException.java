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
 * Thrown to inidcate an invalid value used for a method argument.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:25 $
 */
public class InvalidArgumentException
    extends IllegalArgumentException
{
    /**
     * Make a execption message for the given reasons
     */
    private static String makeMessage(String name, Object value, Object index, String reason)
    {
        assert name != null;
        
        return "'" + name + "'"
            + (index == null ? "" : "[" + index + "]")
            + "='" + value + "'"
            + (reason == null ? " (invalid value)" : " (" + reason + ")");
        
        // TODO I18N internationalise reason
    }
    
    /** The index of the argument or null if no index. */
    private final Object index;
    
    /** The name of the argument that was invalid. */
    private final String name;
    
    /** The value of the argument or null if no value. */
    private final Object value;
    
    /**
     * Construct a <code>InvalidArgumentException</code>.
     *
     * @param name    Argument name.
     */
    public InvalidArgumentException(String name, Object value) {
        this(name, value, null, null);
    }
    
    /**
     * Construct a <code>InvalidArgumentException</code>.
     *
     * @param name    Argument name.
     */
    public InvalidArgumentException(String name, Object value, String reason) {
        this(name, value, null, reason);
    }

    /**
     * Construct a <code>InvalidArgumentException</code>.
     *
     * @param name      The name of the argument
     * @param value     The value of the argument, or <code>null</code>
     * @param index     The index of the argument, or <code>null</code> if none.
     * @param reason    The reason (short description) of why it is invalid, 
     *                  or <code>null</code> for the default
     */
    public InvalidArgumentException(String name, Object value, Object index, String reason)
    {
        super(makeMessage(name, value, index, reason));
        
        this.name = name;
        this.value = value;
        this.index = index;
    }
    
    /**
     * Returns the index associated with this argument.
     * May be <code>null</code> if there is no such index.
     *
     * @return the index associated with this argument
     */
    public Object getIndex() {
        return index;
    }

    /**
     * Returns the name of the argument.
     *
     * @return the name of the argument
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the argument.
     *
     * @return The value of the argument or <code>null</code> if not specified.
     */
    public Object getValue() {
        return value;
    }

}
