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

package org.apache.geronimo.twiddle.console;

/**
 * Provides the interface for command-line history backing.
 *
 * @version <code>$Revision: 1.2 $ $Date: 2004/02/25 09:59:17 $</code>
 */
public interface History
{
    /**
     * Add a line to the command-line history.
     *
     * @param line  The line to add.
     * @return      The index of the added line.
     */
    int add(String line);
    
    /**
     * Get a line from the command-line history.
     *
     * @param i     The line index to retrieve.
     * @return      The command-line or null if index was not found.
     */
    String get(int i);
    
    /**
     * Get the number of lines in the command-line history.
     *
     * @return The number of lines.
     */
    int size();
    
    /**
     * Clear all command-lines.
     */
    void clear();
}
