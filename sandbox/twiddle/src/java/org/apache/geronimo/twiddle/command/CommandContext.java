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

package org.apache.geronimo.twiddle.command;

import org.apache.geronimo.twiddle.console.IOContext;

/**
 * Provides a command with details on its environment.
 *
 * @version $Rev$ $Date$
 */
public interface CommandContext
{
    /**
     * Get the command environemnt.
     *
     * @return The commands environment.
     */
    Environment getEnvironment();
    
    /**
     * Get the input/output context for the command.
     *
     * @return The input/output context the command is running in.
     */
    IOContext getIOContext();
}
