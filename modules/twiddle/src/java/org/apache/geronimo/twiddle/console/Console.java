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

package org.apache.geronimo.twiddle.console;

import java.io.IOException;

/**
 * Abstraction of a console.
 *
 * <p>Console impl must provide a no-argument constructor for pluggablity.
 *
 * <p>Modeled after Java-Readline.
 *
 * @version <code>$Id: Console.java,v 1.1 2003/08/13 10:54:37 jdillon Exp $</code>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface Console
{
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
