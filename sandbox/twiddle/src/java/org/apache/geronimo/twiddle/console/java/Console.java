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

package org.apache.geronimo.twiddle.console.java;

import java.io.IOException;
import java.io.EOFException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;

import org.apache.geronimo.twiddle.console.AbstractConsole;
import org.apache.geronimo.twiddle.console.Completer;
import org.apache.geronimo.twiddle.console.History;
import org.apache.geronimo.twiddle.console.TransientHistory;

/**
 * An implementation of <code>Console</code> in pure Java.
 *
 * <p>Limitations:
 * <ul>
 *    <li>Minimal support for history (collection).
 *    <li>Does not support completion.
 * </ul>
 *
 * @version <code>$Revision: 1.3 $ $Date: 2004/03/10 10:00:38 $</code>
 */
public class Console
    extends AbstractConsole
{
    /** The reader to get input from. */
    protected BufferedReader reader;
    
    /** The writer to render the prompt to. */
    protected PrintWriter writer;
    
    /**
     * Construct a native Java console, using a transient history map.
     */
    public Console()
    {
        setHistory(new TransientHistory());
    }
    
    /**
     * The native Java console impl can not perform completion (at this time).
     *
     * @throws UnsupportedOperationException
     */
    public void setCompleter(Completer completer)
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Helper to lazy initialize the reader.
     */
    protected BufferedReader getReader()
    {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(getIOContext().getInputStream()));
        }
        
        return reader;
    }
    
    /**
     * Helper to lazy initialize the writer.
     */
    protected PrintWriter getWriter()
    {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(getIOContext().getOutputStream()));
        }
        
        return writer;
    }
    
    /**
     * Get a line of input.
     *
     * @param prompt            The prompt to render.
     * @param updateHistory     True to update history with non-null input.
     * @return                  A line of input, or null if the line was empty.
     *
     * @throws EOFException
     * @throws IOException
     */
    public String getLine(final String prompt, final boolean updateHistory) 
        throws IOException
    {
        // Render the prompt
        PrintWriter writer = getWriter();
        writer.print(prompt);
        writer.flush();
        
        // Get some input
        BufferedReader reader = getReader();
        String line = reader.readLine();
        
        // Sanity check the results
        if (line == null) {
            throw new EOFException();
        }
        if (line.length() == 0) {
            line = null;
        }
        
        // Update history
        if (line != null && updateHistory && isHistoryEnabled()) {
            History h = getHistory();
            h.add(line);
        }
        
        return line;
    }
}
