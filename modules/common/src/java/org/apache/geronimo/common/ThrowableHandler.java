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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A static singleton that handles processing throwables that otherwise would
 * be ignored or dumped to System.err.
 *
 * @version <tt>$Revision: 1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public final class ThrowableHandler
{
    /**
     * Container for throwable types.
     */
    public static interface Type
    {
        /** Unknown throwable. */
        int UNKNOWN = 0;
        
        /** Error throwable. */
        int ERROR = 1;
        
        /** Warning throwable. */
        int WARNING = 2;
    }
   

    /////////////////////////////////////////////////////////////////////////
    //                            Listener Methods                         //
    /////////////////////////////////////////////////////////////////////////

    /** The list of listeners */
    protected static List listeners = Collections.synchronizedList(new ArrayList());

    /**
     * Add a ThrowableListener to the listener list.  Listener is added only
     * if if it is not already in the list.
     *
     * @param listener   ThrowableListener to add to the list.
     */
    public static void addThrowableListener(ThrowableListener listener) {
        // only add the listener if it isn't already in the list
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a ThrowableListener from the listener list.
     *
     * @param listener   ThrowableListener to remove from the list.
     */
    public static void removeThrowableListener(ThrowableListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Fire onThrowable to all registered listeners.
     * 
     * @param type    The type off the throwable.
     * @param t       Throwable
     */
    protected static void fireOnThrowable(int type, Throwable t) {
        Object[] list = listeners.toArray();
        
        for (int i=0; i<list.length; i++) {
            ((ThrowableListener)list[i]).onThrowable(type, t);
        }
    }


    /////////////////////////////////////////////////////////////////////////
    //                          Throwable Processing                       //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Add a throwable that is to be handled.
     *
     * @param type    The type off the throwable.
     * @param t       Throwable to be handled.
     */
    public static void add(int type, Throwable t) {
        // don't add null throwables
        if (t == null) return;
        
        try {
            fireOnThrowable(type, t);
        }
        catch (Throwable bad) {
            // don't let these propagate, that could introduce unwanted side-effects
            System.err.println("Unable to handle throwable: " + t + " because of:");
            bad.printStackTrace();
        }
    }
    
    /**
     * Add a throwable that is to be handled with unknown type.
     *
     * @param t    Throwable to be handled.
     */
    public static void add(Throwable t) {
        add(Type.UNKNOWN, t);
    }
    
    /**
     * Add a throwable that is to be handled with error type.
     *
     * @param t    Throwable to be handled.
     */
    public static void addError(Throwable t) {
        add(Type.ERROR, t);
    }
    
    /**
     * Add a throwable that is to be handled with warning type.
     *
     * @param t    Throwable to be handled.
     */
    public static void addWarning(Throwable t) {
        add(Type.ERROR, t);
    }
}
