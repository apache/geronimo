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

package org.apache.geronimo.twiddle.console;

/**
* A factory for creating {@link Console} instances.
 *
 * @version <code>$Revision: 1.3 $ $Date: 2004/03/10 10:00:38 $</code>
 */
public class ConsoleFactory
{
    /**
     * Create a new console.
     *
     * @return A new console instance
     *
     * @throws Exception    Failed to create console instance.
     */
    public static Console create() throws Exception
    {
        //
        // TODO: Determine which Console impl to use, create and return it.
        //       For now just use the native Java impl.
        //
        
        return new org.apache.geronimo.twiddle.console.java.Console();
    }
}
