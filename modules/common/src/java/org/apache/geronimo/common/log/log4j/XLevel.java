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
package org.apache.geronimo.common.log.log4j;

import org.apache.log4j.Level;

/**
 * Extention levels for Log4j
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/27 10:08:45 $
 */
public final class XLevel
    extends Level
{
    public static final int TRACE_INT = Level.DEBUG_INT - 1;
    private static String TRACE_NAME = "TRACE";

    /**
     * The Log4j Level Object to use for trace level messages
     */
    public static final XLevel TRACE = new XLevel(TRACE_INT, TRACE_NAME, 7);

    protected XLevel(int level, String name, int syslogEquiv) {
        super(level, name, syslogEquiv);
    }

    /**
     * Convert the String argument to a level. If the conversion
     * fails then this method returns {@link #TRACE}.
     */
    public static Level toLevel(String name) {
        return toLevel(name, XLevel.TRACE);
    }

    /**
     * Convert the String argument to a level. If the conversion
     * fails, return the level specified by the second argument,
     * i.e. defaultValue.
     */
    public static Level toLevel(String name, Level defaultValue) {
        if (name == null) {
            return defaultValue;
        }
        if (name.toUpperCase().equals(TRACE_NAME)) {
            return XLevel.TRACE;
        }
        return Level.toLevel(name, defaultValue);
    }

    /**
     * Convert an integer passed as argument to a level. If the
     * conversion fails, then this method returns {@link #DEBUG}.
     */
    public static Level toLevel(int level) throws IllegalArgumentException {
        if (level == TRACE_INT) {
            return XLevel.TRACE;
        } else {
            return Level.toLevel(level);
        }
    }
}
