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
package org.apache.geronimo.core.service;

/**
 * The result of an Invocation.
 * There are two types of result:
 * <ul>
 * <li>normal - indicating the operation completed normally (e.g. the method returned)</li>
 * <li>exception - indicating the operation completed abnormally (e.g. the method threw a checked exception)</li>
 * </ul>
 * <p>Note that these should both be considered a normal completion of the operation by the container. Abnormal
 * completions, such as a RuntimeException or Error from the invocation, or any problem in the interceptor
 * chain itself, should result in a Throwable being thrown up the chain rather than being contained in this
 * result.</p>
 * <p>This distinction mirrors the semantics for EJB invocations, where a business method is considered to have
 * completed successfuly even if it throws declared Exception - the Exception there is indicating a business level
 * issue and not a system problem.</p>
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/08 04:25:19 $
 */
public interface InvocationResult {
    /**
     * Was this a normal completion (return)?
     * @return true if the invocation returned; false if a declared exception was thrown
     */
    boolean isNormal();

    /**
     * Get the return value from the invocation.
     * It is an error to call this method if the invocation is not complete normally.
     * @return the return value from the invocation; null if the operation was void
     */
    Object getResult();

    /**
     * Was an application exception raised by the invocation?
     * Note, this indicates a checked application exception was thrown; this will never contain
     * a system exception
     * @return true if a declared exception was thrown; false if the invocation returned
     */
    boolean isException();

    /**
     * Get the application exception raised by the invocation.
     * It is an error to call this method if the invocation did not raise an exception
     * @return the checked Exception raised by the application
     */
    Exception getException();
}
