/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version <code>$Id: Console.java,v 1.3 2003/08/14 20:24:35 bsnyder Exp $</code>
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
