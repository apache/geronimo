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
 * Thrown to inidcate an invalid value used for a method argument.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/01 15:09:26 $
 */
public class InvalidArgumentException
    extends IllegalArgumentException
{
    /**
     * Make a execption message for the given reasons
     */
    private static String makeMessage(String name, Object value, Object index, String reason)
    {
        assert name != null;
        
        return "'" + name + "'"
            + (index == null ? "" : "[" + index + "]")
            + "='" + value + "'"
            + (reason == null ? " (invalid value)" : " (" + reason + ")");
        
        // TODO I18N internationalise reason
    }
    
    /** The index of the argument or null if no index. */
    private final Object index;
    
    /** The name of the argument that was invalid. */
    private final String name;
    
    /** The value of the argument or null if no value. */
    private final Object value;
    
    /**
     * Construct a <code>InvalidArgumentException</code>.
     *
     * @param name    Argument name.
     */
    public InvalidArgumentException(String name, Object value) {
        this(name, value, null, null);
    }
    
    /**
     * Construct a <code>InvalidArgumentException</code>.
     *
     * @param name    Argument name.
     */
    public InvalidArgumentException(String name, Object value, String reason) {
        this(name, value, null, reason);
    }

    /**
     * Construct a <code>InvalidArgumentException</code>.
     *
     * @param name      The name of the argument
     * @param value     The value of the argument, or <code>null</code>
     * @param index     The index of the argument, or <code>null</code> if none.
     * @param reason    The reason (short description) of why it is invalid, 
     *                  or <code>null</code> for the default
     */
    public InvalidArgumentException(String name, Object value, Object index, String reason)
    {
        super(makeMessage(name, value, index, reason));
        
        this.name = name;
        this.value = value;
        this.index = index;
    }
    
    /**
     * Returns the index associated with this argument.
     * May be <code>null</code> if there is no such index.
     *
     * @return the index associated with this argument
     */
    public Object getIndex() {
        return index;
    }

    /**
     * Returns the name of the argument.
     *
     * @return the name of the argument
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the argument.
     *
     * @return The value of the argument or <code>null</code> if not specified.
     */
    public Object getValue() {
        return value;
    }

}
