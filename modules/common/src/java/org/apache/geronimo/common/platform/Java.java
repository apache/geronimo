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

package org.apache.geronimo.common.platform;

import org.apache.geronimo.common.ThrowableHandler;

/**
 * Provides common access to specifics about the version of <em>Java</em>
 * that a virtual machine supports.
 *
 * <p>Determines the version of the <em>Java Virtual Machine</em> by checking
 *    for the availablity of version specific classes.<p>
 *
 * <p>Classes are loaded in the following order:
 *    <ol>
 *    <li><tt>java.lang.StackTraceElement</tt> was introduced in JDK 1.4</li>
 *    <li><tt>java.lang.StrictMath</tt> was introduced in JDK 1.3</li>
 *    <li><tt>java.lang.ThreadLocal</tt> was introduced in JDK 1.2</li>
 *    <li><tt>java.lang.Void</tt> was introduced in JDK 1.1</li>
 *    </ol>
 * </p>
 *
 * @version <tt>$Revision: 1.3 $</tt>
 */
public final class Java
{
    /** Prevent instantiation */
    private Java() {}
    
    /** Java version 1.0 token */
    public static final int VERSION_1_0 = 0x01;
    
    /** Java version 1.1 token */
    public static final int VERSION_1_1 = 0x02;
    
    /** Java version 1.2 token */
    public static final int VERSION_1_2 = 0x03;
    
    /** Java version 1.3 token */
    public static final int VERSION_1_3 = 0x04;
    
    /** Java version 1.4 token */
    public static final int VERSION_1_4 = 0x05;
    
    /** 
     * Private to avoid over optimization by the compiler.
     *
     * @see #getVersion()   Use this method to access this final value.
     */
    private static final int VERSION;
    
    /** Initialize VERSION. */ 
    static {
        // default to 1.0
        int version = VERSION_1_0;
        
        try {
            // check for 1.1
            Class.forName("java.lang.Void");
            version = VERSION_1_1;
            
            // check for 1.2
            Class.forName("java.lang.ThreadLocal");
            version = VERSION_1_2;
            
            // check for 1.3
            Class.forName("java.lang.StrictMath");
            version = VERSION_1_3;
            
            // check for 1.4
            Class.forName("java.lang.StackTraceElement");
            version = VERSION_1_4;
        }
        catch (ClassNotFoundException e) {
            ThrowableHandler.add(e);
        }
        
        VERSION = version;
    }
    
    /**
     * Return the version of <em>Java</em> supported by the VM.
     *
     * @return  The version of <em>Java</em> supported by the VM.
     */
    public static int getVersion() {
        return VERSION;
    }
    
    /**
     * Retrurns true if the given version identifer is equal to the
     * version identifier of the current virtuial machine.
     *
     * @param version    The version identifier to check for.
     * @return           True if the current virtual machine is the same version.
     */
    public static boolean isVersion(final int version) {
        return VERSION == version;
    }
    
    /**
     * Retrurns true if the current virtual machine is compatible with
     * the given version identifer.
     *
     * @param version    The version identifier to check compatibility of.
     * @return           True if the current virtual machine is compatible.
     */
    public static boolean isCompatible(final int version) {
        // if our vm is the same or newer then we are compatible
        return VERSION >= version;
    }
}
