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
 * An exception throw to indicate a problem with some type of data conversion.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:02 $
 */
public class DataConversionException
    extends RuntimeException
{
    /**
     * Construct a <tt>DataConversionException</tt> with a specified detail message.
     *
     * @param msg     Detail message
     */
    public DataConversionException(final String msg) {
        super(msg);
    }
    
    /**
     * Construct a <tt>DataConversionException</tt> with a specified detail Throwable
     * and message.
     *
     * @param msg     Detail message
     * @param detail  Detail Throwable
     */
    public DataConversionException(final String msg, final Throwable detail) {
        super(msg, detail);
    }
    
    /**
     * Construct a <tt>DataConversionException</tt> with a specified detail Throwable.
     *
     * @param detail  Detail Throwable
     */
    public DataConversionException(final Throwable detail) {
        super(detail);
    }
    
    /**
     * Construct a <tt>DataConversionException</tt> with no specified detail message.
     */
    public DataConversionException() {
        super();
    }
}
