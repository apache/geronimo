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

package org.apache.geronimo.twiddle.config;

import org.apache.geronimo.twiddle.command.CommandException;

/**
 * A configuration exception.
 *
 * @version <code>$Revision: 1.3 $ $Date: 2004/03/10 10:00:38 $</code>
 */
public class ConfigurationException
    extends CommandException
{
    /**
     * Construct a <code>ConfigurationException</code> with the specified detail 
     * message.
     *
     * @param msg  Detail message.
     */
    public ConfigurationException(String msg) {
        super(msg);
    }
    
    /**
     * Construct a <code>ConfigurationException</code> with the specified detail 
     * message and nested <code>Throwable</code>.
     *
     * @param msg     Detail message.
     * @param nested  Nested <code>Throwable</code>.
     */
    public ConfigurationException(String msg, Throwable nested) {
        super(msg, nested);
    }
    
    /**
     * Construct a <code>ConfigurationException</code> with the specified
     * nested <code>Throwable</code>.
     *
     * @param nested  Nested <code>Throwable</code>.
     */
    public ConfigurationException(Throwable nested) {
        super(nested);
    }
    
    /**
     * Construct a <code>ConfigurationException</code> with no detail.
     */
    public ConfigurationException() {
        super();
    }
}
