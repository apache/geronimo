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

package org.apache.geronimo.common;

import java.io.PrintWriter;
import java.io.PrintStream;

/**
 * A common superclass for <tt>Exception</tt> classes that can contain
 * a nested <tt>Throwable</tt> detail object.
 *
 * @version $Revision: 1.4 $ $Date: 2003/08/16 15:14:11 $
 */
public class NestedException
    extends Exception
    implements NestedThrowable
{
    /** The nested throwable */
    protected final Throwable nested;
    
    /**
     * Construct a <tt>NestedException</tt> with the specified detail 
     * message.
     *
     * @param msg  Detail message.
     */
    public NestedException(final String msg) {
        super(msg);
        this.nested = null;
    }
    
    /**
     * Construct a <tt>NestedException</tt> with the specified detail 
     * message and nested <tt>Throwable</tt>.
     *
     * @param msg     Detail message.
     * @param nested  Nested <tt>Throwable</tt>.
     */
    public NestedException(final String msg, final Throwable nested) {
        super(msg);
        this.nested = nested;
        NestedThrowable.Util.checkNested(this, nested);
    }
    
    /**
     * Construct a <tt>NestedException</tt> with the specified
     * nested <tt>Throwable</tt>.
     *
     * @param nested  Nested <tt>Throwable</tt>.
     */
    public NestedException(final Throwable nested) {
        this(nested.getMessage(), nested);
    }
    
    /**
     * Construct a <tt>NestedException</tt> with no detail.
     */
    public NestedException() {
        super();
        this.nested = null;
    }
    
    /**
     * Return the nested <tt>Throwable</tt>.
     *
     * @return  Nested <tt>Throwable</tt>.
     */
    public Throwable getNested() {
        return nested;
    }
    
    /**
     * Return the nested <tt>Throwable</tt>.
     *
     * <p>For JDK 1.4 compatibility.
     *
     * @return  Nested <tt>Throwable</tt>.
     */
    public Throwable getCause() {
        return nested;
    }
    
    /**
     * Returns the composite throwable message.
     *
     * @return  The composite throwable message.
     */
    public String getMessage() {
        return NestedThrowable.Util.getMessage(super.getMessage(), nested);
    }
    
    /**
     * Prints the composite message and the embedded stack trace to the
     * specified print stream.
     *
     * @param stream  Stream to print to.
     */
    public void printStackTrace(final PrintStream stream) {
        if (nested == null || NestedThrowable.PARENT_TRACE_ENABLED) {
            super.printStackTrace(stream);
        }
        NestedThrowable.Util.print(nested, stream);
    }
    
    /**
     * Prints the composite message and the embedded stack trace to the
     * specified print writer.
     *
     * @param writer  Writer to print to.
     */
    public void printStackTrace(final PrintWriter writer) {
        if (nested == null || NestedThrowable.PARENT_TRACE_ENABLED) {
            super.printStackTrace(writer);
        }
        NestedThrowable.Util.print(nested, writer);
    }
    
    /**
     * Prints the composite message and the embedded stack trace to 
     * <tt>System.err</tt>.
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }
    
    /**
     * Wraps the give throwable in a NestedException unless it is already
     * a NestedException.
     * 
     * @param t   The target throwable to wrap.
     * @return    A NestedException.
     */
    public static NestedException wrap(Throwable t) {
        if (t instanceof NestedException) {
            return (NestedException)t;
        }
        return new NestedException(t);
    }
}
