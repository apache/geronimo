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
 * This exception is thrown to indicate that an object was not coercible.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:02 $
 */
public class NotCoercibleException
    extends CoercionException
{
    /**
     * Construct a <tt>NotCoercibleException</tt> with the specified detail 
     * message.
     *
     * @param msg  Detail message.
     */
    public NotCoercibleException(String msg) {
        super(msg);
    }
    
    /**
     * Construct a <tt>NotCoercibleException</tt> with the specified detail 
     * message and nested <tt>Throwable</tt>.
     *
     * @param msg     Detail message.
     * @param nested  Nested <tt>Throwable</tt>.
     */
    public NotCoercibleException(String msg, Throwable nested) {
        super(msg, nested);
    }
    
    /**
     * Construct a <tt>NotCoercibleException</tt> with the specified
     * nested <tt>Throwable</tt>.
     *
     * @param nested  Nested <tt>Throwable</tt>.
     */
    public NotCoercibleException(Throwable nested) {
        super(nested);
    }
    
    /**
     * Construct a <tt>NotCoercibleException</tt> with no detail.
     */
    public NotCoercibleException() {
        super();
    }
    
    /**
     * Construct a <tt>NotCoercibleException</tt> with an object detail.
     *
     * @param obj     Object detail.
     */
    public NotCoercibleException(Object obj) {
        super(String.valueOf(obj));
    }
}
