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

import java.text.MessageFormat;

/**
 * Allows for an exception to be formatted using a given list of parameters.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/08/30 08:36:16 $
 */
public class ParameterizedException
    extends Exception
{
    /**
     * Default constructor.
     */
    public ParameterizedException() {
        super();
    }

    /**
     * Exception with a given message.
     *
     * @param message unformatted message.
     */
    public ParameterizedException(String message) {
        super(message);
    }

    /**
     * Exception with a given message and root cause.
     *
     * @param message unformatted message.
     * @param cause root cause of the exception.
     */
    public ParameterizedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception with a root cause.
     *
     * @param cause root cause of the exception.
     */
    public ParameterizedException(Throwable cause) {
        super(cause);
    }

    /**
     * Exception with a formatted message
     *
     * @param msg message format used by <tt>MessageFormat</tt>
     * @param params message parameters
     *
     * @see java.text.MessageFormat
     */
    public ParameterizedException(String message, Object[] params) {
        this(message, params, null);
    }

    /**
     * Exception with a formatted message and root cause.
     *
     * @param msg message format used by <tt>MessageFormat</tt>
     * @param params message parameters
     * @param ex root cause
     *
     * @see java.text.MessageFormat
     */
    public ParameterizedException(String message, Object[] params, Throwable ex)
    {
        super(getFormattedMessage(message, params), ex);
    }

    /**
     * Formats the given message
     *
     * @param msg message format used by <tt>MessageFormat</tt>
     * @param params message parameters
     * @return the formatted message
     */
    public static String getFormattedMessage(String message, Object[] params)
    {
        if (message != null) {
            if (params != null) {
                int n = params.length;
                Object[] localParams = new Object[n];
                for (int i = 0; i < n; ++i) {
                    if (params[i] == null) {
                        localParams[i] = "???";
                    } else {
                        localParams[i] = params[i];
                    }
                }
                try {
                    message = MessageFormat.format(message, localParams);
                } catch (Exception ex) {
                    // ignore, return passed in message
                }
            }
        }
        return message;
    }
}
