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

import java.io.IOException;

/**
 * Abstraction of a console.
 *
 * <p>Console impl must provide a no-argument constructor for pluggablity.
 *
 * <p>Modeled after Java-Readline.
 *
 * @version <code>$Revision: 1.3 $ $Date: 2004/03/10 10:00:38 $</code>
 */
public interface Console
{
    //
    // TODO: Refatcor line reading into subpackage/classes
    //
    
    /**
     * Set the input/output context.
     *
     * @param ioContext     The input/output context.
     */
    void setIOContext(IOContext ioContext);
    
    /**
     * Get the input/output context.
     *
     * @return The input/output context.
     */
    IOContext getIOContext();
    
    /**
     * Set the history backing for the console.
     *
     * @param history   The history backing.
     */
    void setHistory(History history);
    
    /**
     * Get the history backing for the console.
     *
     * @return The history backing.
     */
    History getHistory();
    
    /**
     * Set the command-line completer for the console.
     *
     * @param completer     The command-line completer.
     */
    void setCompleter(Completer completer);
    
    /**
     * Get the command-line completer for the console.
     *
     * @return The command-line completer.
     */
    Completer getCompleter();
    
    /**
     * Get a line of input.
     *
     * @param prompt            The command-line prompt to display.
     * @param updateHistory     True to update history, false to disable.
     *
     * @throws IOException      Failed to read input.
     */
    String getLine(String prompt, boolean updateHistory) throws IOException;
    
    /**
     * Get a line of input.
     *
     * @param prompt    The command-line prompt to display.
     *
     * @throws IOException      Failed to read input.
     */
    String getLine(String prompt) throws IOException;
}
