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
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.common.platform.Java;

/**
 * Interface which is implemented by all the nested throwable flavors.
 *
 * @version <tt>$Revision: 1.3 $</tt>
 */
public interface NestedThrowable
   extends Serializable
{
    /**
     * A system wide flag to enable or disable printing of the
     * parent throwable traces.
     *
     * <p>
     * This value is set from the system property
     * <tt>org.apache.geronimo.common.NestedThrowable.parentTraceEnabled</tt>
     * or if that is not set defaults to <tt>true</tt>.
     */
    boolean PARENT_TRACE_ENABLED = Util.getBoolean("parentTraceEnabled", true);
    
    /**
     * A system wide flag to enable or disable printing of the
     * nested detail throwable traces.
     *
     * <p>
     * This value is set from the system property
     * <tt>org.apache.geronimo.common.NestedThrowable.nestedTraceEnabled</tt>
     * or if that is not set defaults to <tt>true</tt> unless
     * using JDK 1.4 with {@link #PARENT_TRACE_ENABLED} set to false,
     * then <tt>false</tt> since there is a native mechansim for this there.
     *
     * <p>
     * Note then when running under 1.4 is is not possible to disable
     * the nested trace output, since that is handled by java.lang.Throwable
     * which we delegate the parent printing to.
     */
    boolean NESTED_TRACE_ENABLED = Util.getBoolean("nestedTraceEnabled",
                                                   (Java.isCompatible(Java.VERSION_1_4) &&
                                                    !PARENT_TRACE_ENABLED) ||
                                                    !Java.isCompatible(Java.VERSION_1_4));

    /**
     * A system wide flag to enable or disable checking of parent and child
     * types to detect uneeded nesting
     *
     * <p>
     * This value is set from the system property
     * <tt>org.apache.geronimo.common.NestedThrowable.detectDuplicateNesting</tt>
     * or if that is not set defaults to <tt>true</tt>.
     */
    boolean DETECT_DUPLICATE_NESTING = Util.getBoolean("detectDuplicateNesting", true);
    
    /**
     * Return the nested throwable.
     *
     * @return  Nested throwable.
     */
    Throwable getNested();
    
    /**
     * Return the nested <tt>Throwable</tt>.
     *
     * <p>For JDK 1.4 compatibility.
     *
     * @return  Nested <tt>Throwable</tt>.
     */
    Throwable getCause();


    /////////////////////////////////////////////////////////////////////////
    //                      Nested Throwable Utilities                     //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Utilitiy methods for the various flavors of
     * <code>NestedThrowable</code>.
     */
    final class Util 
    {
        // Can not be final due to init bug, see getLogger() for details
        private static Log log = LogFactory.getLog(NestedThrowable.class);
        
        /**
         * Something is very broken with class nesting, which can sometimes
         * leave log uninitialized durring one of the following method calls.
         *
         * <p>
         * This is a HACK to keep those methods from NPE until this problem
         * can be resolved.
         */
        private static Log getLog()
        {
            if (log == null)
                log = LogFactory.getLog(NestedThrowable.class);
            
            return log;
        }
        
        /** A helper to get a boolean property. */
        protected static boolean getBoolean(String name, boolean defaultValue)
        {
            name = NestedThrowable.class.getName() + "." + name;
            String value = System.getProperty(name, String.valueOf(defaultValue));
            
            // HACK see getLog() for details
            log = getLog();
            log.debug(name + "=" + value);
            
            return new Boolean(value).booleanValue();
        }
        
        /**
         * Check and possibly warn if the nested exception type is the same
         * as the parent type (duplicate nesting).
         */
        public static void checkNested(final NestedThrowable parent,
                                       final Throwable child)
        {
            if (!DETECT_DUPLICATE_NESTING || parent == null || child == null) return;
            
            Class parentType = parent.getClass();
            Class childType = child.getClass();
            
            //
            // This might be backwards... I always get this confused
            //
            
            if (parentType.isAssignableFrom(childType)) {
                // HACK see getLog() for details
                log = getLog();
                log.warn("Duplicate throwable nesting of same base type: " +
                         parentType + " is assignable from: " + childType);
            }
        }
        
        /**
         * Returns a formated message for the given detail message
         * and nested <code>Throwable</code>.
         *
         * @param msg     Detail message.
         * @param nested  Nested <code>Throwable</code>.
         * @return        Formatted message.
         */
        public static String getMessage(final String msg,
                                        final Throwable nested)
        {
            StringBuffer buff = new StringBuffer(msg == null ? "" : msg);
            
            if (nested != null) {
                buff.append(msg == null ? "- " : "; - ")
                    .append("nested throwable: (")
                    .append(nested)
                    .append(")");
            }
            
            return buff.toString();
        }

        /**
         * Prints the nested <code>Throwable</code> to the given stream.
         *
         * @param nested  Nested <code>Throwable</code>.
         * @param stream  Stream to print to.
         */
        public static void print(final Throwable nested,
                                 final PrintStream stream)
        {
            if (stream == null)
                throw new NullArgumentException("stream");
            
            if (NestedThrowable.NESTED_TRACE_ENABLED && nested != null) {
                synchronized (stream) {
                    if (NestedThrowable.PARENT_TRACE_ENABLED) {
                        stream.print(" + nested throwable: ");
                    }
                    else {
                        stream.print("[ parent trace omitted ]: ");
                    }
                    
                    nested.printStackTrace(stream);
                }
            }
        }
        
        /**
         * Prints the nested <code>Throwable</code> to the given writer.
         *
         * @param nested  Nested <code>Throwable</code>.
         * @param writer  Writer to print to.
         */
        public static void print(final Throwable nested,
                                 final PrintWriter writer)
        {
            if (writer == null)
                throw new NullArgumentException("writer");
            
            if (NestedThrowable.NESTED_TRACE_ENABLED && nested != null) {
                synchronized (writer) {
                    if (NestedThrowable.PARENT_TRACE_ENABLED) {
                        writer.print(" + nested throwable: ");
                    }
                    else {
                        writer.print("[ parent trace omitted ]: ");
                    }
                    
                    nested.printStackTrace(writer);
                }
            }
        }
    }
}
