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

package org.apache.geronimo.twiddle.command;

import java.io.PrintWriter;
import java.io.Reader;

/**
 * Provides a command with details on its environment.
 *
 * @version <code>$Id: CommandContext.java,v 1.1 2003/08/13 08:32:09 jdillon Exp $</code>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface CommandContext
{
    /////////////////////////////////////////////////////////////////////////
    //                          Environemnt Access                         //
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Get an enviornment attribute.
     *
     * @param name  The name of the attribute.
     * @return      The attribute value, or null if the attribute was not set.
     */
    Object get(String name);
    
    /**
     * Set an environment attribute.
     *
     * @param name      The name of the attribute.
     * @param value     The value of the attribute.
     */
    Object set(String name, Object value);
    
    /**
     * Unset an enviornment attribute.
     *
     * @param name  The name of the attribute.
     * @return      The previous value of the attribute, or null if there was none.
     */
    Object unset(String name);
    
    /**
     * Check if an attribute is set.
     *
     * @param name  The name of the attribute.
     * @return      True if the attribute is set, else false.
     */
    boolean isSet(String name);
    
    
    /////////////////////////////////////////////////////////////////////////
    //                          Container Access                           //
    /////////////////////////////////////////////////////////////////////////
    
    CommandContainer getContainer();
    
    
    /////////////////////////////////////////////////////////////////////////
    //                         Input/Output Access                         //
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Get the output writer for the command.
     *
     * @return  The output writer.
     */
    PrintWriter getWriter();
    
    /**
     * Get the error output writer for the command.
     *
     * @return  The error output writer.
     */
    PrintWriter getErrorWriter();
    
    /**
     * Get the input reader for the command.
     *
     * @return  The input reader.
     */
    Reader getReader();
}
