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
 * This exception is thrown to indicate that a problem has occured while
 * trying to coerce an object.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:25 $
 */
public class CoercionException
    extends RuntimeException
{
    /**
     * Construct a <tt>CoercionException</tt> with the specified detail 
     * message.
     *
     * @param msg  Detail message.
     */
    public CoercionException(String msg) {
        super(msg);
    }
    
    /**
     * Construct a <tt>CoercionException</tt> with the specified detail 
     * message and nested <tt>Throwable</tt>.
     *
     * @param msg     Detail message.
     * @param nested  Nested <tt>Throwable</tt>.
     */
    public CoercionException(String msg, Throwable nested) {
        super(msg, nested);
    }
    
    /**
     * Construct a <tt>CoercionException</tt> with the specified
     * nested <tt>Throwable</tt>.
     *
     * @param nested  Nested <tt>Throwable</tt>.
     */
    public CoercionException(Throwable nested) {
        super(nested);
    }
    
    /**
     * Construct a <tt>CoercionException</tt> with no detail.
     */
    public CoercionException() {
        super();
    }
}
