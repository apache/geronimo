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

/**
 * Thrown to indicate that a method argument was <tt>null</tt> and 
 * should <b>not</b> have been.
 *
 * @version <tt>$Revision: 1.4 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class NullArgumentException 
   extends IllegalArgumentException
{
    /**
     * A simple helper method to check that the given argument value
     * is not null. If it is null then a <tt>NullArgumentException</tt> is thrown.
     * 
     * @param name name of the argument
     * @param argument the value of the argument
     * @throws NullArgumentException if the argument is null
     */
    public static void checkForNull(String name, Object argument) throws NullArgumentException {
        if (argument == null) {
            throw new NullArgumentException(name);
        }
    }
    
    /** The name of the argument that was <tt>null</tt>. */
    protected final String name;
    
    /** The index of the argument or null if no index. */
    protected final Object index;
    
    /**
     * Construct a <tt>NullArgumentException</tt>.
     *
     * @param name    Argument name.
     */
    public NullArgumentException(final String name) {
        super(makeMessage(name));
        
        this.name = name;
        this.index = null;
    }
    
    /**
     * Construct a <tt>NullArgumentException</tt>.
     *
     * @param name    Argument name.
     * @param index   Argument index.
     */
    public NullArgumentException(final String name, final long index) {
        super(makeMessage(name, new Long(index)));
        
        this.name = name;
        this.index = new Long(index);
    }
    
    /**
     * Construct a <tt>NullArgumentException</tt>.
     *
     * @param name    Argument name.
     * @param index   Argument index.
     */
    public NullArgumentException(final String name, final Object index) {
        super(makeMessage(name, index));
        
        this.name = name;
        this.index = index;
    }
    
    /**
     * Construct a <tt>NullArgumentException</tt>.
     */
    public NullArgumentException() {
        this.name = null;
        this.index = null;
    }
    
    /**
     * Get the argument name that was <tt>null</tt>.
     *
     * @return  The argument name that was <tt>null</tt>.
     */
    public final String getArgumentName() {
        return name;
    }
    
    /**
     * Get the argument index.
     *
     * @return  The argument index.
     */
    public final Object getArgumentIndex() {
        return index;
    }
    
    /**
     * Make a execption message for the argument name.
     */
    private static String makeMessage(final String name) {
        return "'" + name + "' is null";
    }
    
    /**
     * Make a execption message for the argument name and index
     */
    private static String makeMessage(final String name, final Object index) {
        return "'" + name + "[" + index + "]' is null";
    }
}
